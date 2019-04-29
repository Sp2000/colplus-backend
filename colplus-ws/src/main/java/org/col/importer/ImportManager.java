package org.col.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import io.dropwizard.lifecycle.Managed;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.col.api.exception.NotFoundException;
import org.col.api.model.ColUser;
import org.col.api.model.Dataset;
import org.col.api.model.DatasetImport;
import org.col.api.util.PagingUtil;
import org.col.api.vocab.DatasetOrigin;
import org.col.api.vocab.ImportState;
import org.col.api.vocab.Users;
import org.col.common.concurrent.ExecutorUtils;
import org.col.common.concurrent.StartNotifier;
import org.col.common.io.DownloadUtil;
import org.col.WsServerConfig;
import org.col.common.tax.AuthorshipNormalizer;
import org.col.dao.DatasetImportDao;
import org.col.db.mapper.DatasetMapper;
import org.col.db.mapper.DatasetPartitionMapper;
import org.col.es.NameUsageIndexService;
import org.col.img.ImageService;
import org.col.matching.NameIndex;
import org.gbif.nameparser.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.col.WsServer.MILLIS_TO_DIE;

/**
 * Manages import task scheduling, removing and listing
 */
public class ImportManager implements Managed {
  private static final Logger LOG = LoggerFactory.getLogger(ImportManager.class);
  public static final String THREAD_NAME = "dataset-importer";
  
  private ThreadPoolExecutor exec;
  private final PriorityBlockingQueue<ImportRequest> queue = new PriorityBlockingQueue<ImportRequest>();
  private final Map<Integer, Future> futures = new ConcurrentHashMap<Integer, Future>();
  private final WsServerConfig cfg;
  private final DownloadUtil downloader;
  private final SqlSessionFactory factory;
  private final NameIndex index;
  private final AuthorshipNormalizer aNormalizer;
  private final NameUsageIndexService indexService;
  private final ImageService imgService;
  private final Timer importTimer;
  private final Counter failed;
  
  public ImportManager(WsServerConfig cfg, MetricRegistry registry, CloseableHttpClient client,
                       SqlSessionFactory factory, AuthorshipNormalizer aNormalizer, NameIndex index,
                       NameUsageIndexService indexService, ImageService imgService) {
    this.cfg = cfg;
    this.factory = factory;
    this.downloader = new DownloadUtil(client, cfg.importer.githubToken, cfg.importer.githubTokenGeoff);
    this.index = index;
    this.aNormalizer = aNormalizer;
    this.imgService = imgService;
    this.indexService = indexService;
    importTimer = registry.timer("org.col.import.timer");
    failed = registry.counter("org.col.import.failed");
  }
  
  /**
   * Lists the current queue
   */
  public Queue<ImportRequest> queue() {
    return queue;
  }
  
  /**
   * Cancels a running import job by its dataset key
   */
  public void cancel(int datasetKey) {
    queue.remove(new ImportRequest(datasetKey, Users.IMPORTER));
    Future f = futures.remove(datasetKey);
    if (f != null) {
      f.cancel(true);
      LOG.info("Canceled import for dataset {}: {}", datasetKey);
      
    } else {
      LOG.info("No import existing for dataset {}. Ignore", datasetKey);
    }
  }
  
  /**
   * @throws IllegalArgumentException if dataset was scheduled for importing already, queue was full
   *                                  or dataset does not exist or is of origin managed
   */
  public synchronized ImportRequest submit(final ImportRequest req) throws IllegalArgumentException {
    validDataset(req.datasetKey);
    return submitValidDataset(req);
  }
  
  /**
   * Uploads a new dataset and submits a forced, high priority import request.
   *
   * @throws IllegalArgumentException if dataset was scheduled for importing already, queue was full,
   *                                  dataset does not exist or is not of matching origin
   */
  public ImportRequest submit(final int datasetKey, final InputStream content, ColUser user) throws IOException {
    Dataset d = validDataset(datasetKey);
    if (d.getOrigin()!= DatasetOrigin.UPLOADED) {
      throw new IllegalArgumentException("Dataset " + datasetKey + " is not of origin uploaded");
    }
    uploadArchive(d, content);
    return submitValidDataset(new ImportRequest(datasetKey, user.getKey(), true, true));
  }
  
  /**
   * @throws IllegalArgumentException if dataset was scheduled for importing already, queue was full
   *                                  or dataset does not exist or is of origin managed
   */
  private synchronized ImportRequest submitValidDataset(final ImportRequest req) throws IllegalArgumentException {
    // is this dataset already scheduled?
    if (futures.containsKey(req.datasetKey)) {
      LOG.info("Dataset {} already queued for import", req.datasetKey);
      throw new IllegalArgumentException("Dataset " + req.datasetKey + " already queued for import");
      
    } else if (queue.size() >= cfg.importer.maxQueue) {
      LOG.info("Import queued at max {} already. Skip dataset {}", queue.size(), req.datasetKey);
      throw new IllegalArgumentException("Import queue full, skip dataset " + req.datasetKey);
    }
    
    queue.add(req);
    futures.put(req.datasetKey, exec.submit(createImport(req)));
    LOG.info("Queued import for dataset {}", req.datasetKey);
    return req;
  }
  
  private Dataset validDataset(int datasetKey){
    try (SqlSession session = factory.openSession(true)) {
      DatasetMapper dm = session.getMapper(DatasetMapper.class);
      Dataset d = dm.get(datasetKey);
      if (d == null) {
        throw NotFoundException.keyNotFound(Dataset.class, datasetKey);
      } else if (d.hasDeletedDate()) {
        throw new IllegalArgumentException("Dataset " + datasetKey + " is deleted and cannot be imported");
      } else if (d.getOrigin() == DatasetOrigin.MANAGED) {
        throw new IllegalArgumentException("Dataset " + datasetKey + " is managed and cannot be imported");
      } else if (d.getKey() < 100) {
        throw new IllegalArgumentException("Dataset " + datasetKey + " is an assembled catalogue and cannot be imported");
      }
      return d;
    }
  }
  
  /**
   * We use old school callbacks here as you cannot easily cancel CopletableFutures.
   */
  private void successCallBack(ImportRequest req) {
    Duration durQueued = Duration.between(req.created, req.started);
    Duration durRun = Duration.between(req.started, LocalDateTime.now());
    LOG.info("Dataset import {} finished. {} min queued, {} min to execute", req.datasetKey, durQueued.toMinutes(), durRun.toMinutes());
    importTimer.update(durRun.getSeconds(), TimeUnit.SECONDS);
    futures.remove(req.datasetKey);
  }
  
  /**
   * We use old school callbacks here as you cannot easily cancel CopletableFutures.
   */
  private void errorCallBack(ImportRequest req, Exception err) {
    LOG.error("Dataset import {} failed: {}", req.datasetKey, err.getCause().getMessage(), err.getCause());
    failed.inc();
    futures.remove(req.datasetKey);
  }
  
  /**
   * Uploads an input stream to a tmp file and if no errors moves it to the archive source path.
   */
  private void uploadArchive(Dataset d, InputStream content) throws NotFoundException, IOException {
    try (SqlSession session = factory.openSession(true)) {
      DatasetMapper dm = session.getMapper(DatasetMapper.class);
      
      Path tmp = Files.createTempFile(cfg.normalizer.scratchDir.toPath(), "upload-", "");
      LOG.info("Upload data for dataset {} to tmp file {}", d.getKey(), tmp);
      Files.copy(content, tmp, StandardCopyOption.REPLACE_EXISTING);
      
      Path source = cfg.normalizer.source(d.getKey()).toPath();
      LOG.debug("Move uploaded data for dataset {} to source repo at {}", d.getKey(), source);
      Files.move(tmp, source, StandardCopyOption.REPLACE_EXISTING);
      
      // finally update dataset metadata
      d.setReleased(LocalDate.now());
      dm.update(d);
    }
  }
  
  /**
   * @throws NotFoundException        if dataset does not exist or was deleted
   * @throws IllegalArgumentException if dataset is of type managed
   */
  private ImportJob createImport(ImportRequest req) throws NotFoundException, IllegalArgumentException {
    try (SqlSession session = factory.openSession(true)) {
      Dataset d = session.getMapper(DatasetMapper.class).get(req.datasetKey);
      if (d == null) {
        throw NotFoundException.keyNotFound(Dataset.class, req.datasetKey);
        
      } else if (d.hasDeletedDate()) {
        LOG.warn("Dataset {} was deleted and cannot be imported", req.datasetKey);
        throw NotFoundException.keyNotFound(Dataset.class, req.datasetKey);
      }
      ImportJob job = new ImportJob(req, d, cfg, downloader, factory, aNormalizer, index, indexService, imgService,
          new StartNotifier() {
            @Override
            public void started() {
              req.start();
              queue.remove(req);
            }
          }, this::successCallBack, this::errorCallBack);
      return job;
    }
  }
  
  /**
   * @return true if queue is empty
   */
  public boolean hasEmptyQueue() {
    return queue.isEmpty();
  }
  
  /**
   * @return true if imports are running or queued
   */
  public boolean hasRunning() {
    return !futures.isEmpty();
  }
  
  @Override
  public void start() throws Exception {
    LOG.info("Starting import manager with {} import threads and a queue of {} max.",
        cfg.importer.threads, cfg.importer.maxQueue);
    exec = new ThreadPoolExecutor(cfg.importer.threads, cfg.importer.threads, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(cfg.importer.maxQueue),
        new NamedThreadFactory(THREAD_NAME, Thread.NORM_PRIORITY, true),
        new ThreadPoolExecutor.AbortPolicy());
    exec.allowCoreThreadTimeOut(true);
    
    // read hanging imports in db, truncate if half inserted and add as new requests to the queue
    cancelAndReschedule(ImportState.DOWNLOADING, false);
    cancelAndReschedule(ImportState.PROCESSING, false);
    cancelAndReschedule(ImportState.INSERTING, true);
  }
  
  private void cancelAndReschedule(ImportState state, boolean truncate) {
    int counter = 0;
    DatasetImportDao dao = new DatasetImportDao(factory, cfg.textTreeRepo);
    Iterator<DatasetImport> iter = PagingUtil.pageAll(p -> dao.list(null, Lists.newArrayList(state), p));
    while (iter.hasNext()) {
      DatasetImport di = iter.next();
      dao.updateImportCancelled(di);
      // truncate data?
      if (truncate) {
        try (SqlSession session = factory.openSession(true)) {
          DatasetPartitionMapper dm = session.getMapper(DatasetPartitionMapper.class);
          LOG.info("Drop partially imported data for dataset {}", di.getDatasetKey());
          dm.delete(di.getDatasetKey());
        }
      }
      // add back to queue
      try {
        submit(new ImportRequest(di.getDatasetKey(), Users.IMPORTER, true, false));
        counter++;
      } catch (IllegalArgumentException e) {
        // swallow
      }
    }
    LOG.info("Cancelled and resubmitted {} {} imports.", counter, state);
  }
  
  @Override
  public void stop() throws Exception {
    // orderly shutdown running imports
    for (Future f : futures.values()) {
      f.cancel(true);
    }
    // fully shutdown threadpool within given time
    ExecutorUtils.shutdown(exec, MILLIS_TO_DIE, TimeUnit.MILLISECONDS);
  }
}