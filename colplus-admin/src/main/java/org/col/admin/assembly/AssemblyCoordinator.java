package org.col.admin.assembly;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import io.dropwizard.lifecycle.Managed;
import org.apache.ibatis.session.SqlSessionFactory;
import org.col.api.model.ColUser;
import org.col.api.model.SectorImport;
import org.col.common.concurrent.ExecutorUtils;
import org.gbif.nameparser.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.col.admin.AdminServer.MILLIS_TO_DIE;

public class AssemblyCoordinator implements Managed {
  private static final Logger LOG = LoggerFactory.getLogger(AssemblyCoordinator.class);
  private static final String THREAD_NAME = "assembly-sync";
  
  private ExecutorService exec;
  private final SqlSessionFactory factory;
  private final Map<Integer, Future> syncs = new ConcurrentHashMap<Integer, Future>();
  private final Map<Integer, SectorImport> syncStates = new ConcurrentHashMap<Integer, SectorImport>();
  private final Timer timer;
  private final Counter counter;
  private final Counter failed;
  
  public AssemblyCoordinator(SqlSessionFactory factory, MetricRegistry registry) {
    this.factory = factory;
    timer = registry.timer("org.col.assembly.timer");
    counter = registry.counter("org.col.assembly.counter");
    failed = registry.counter("org.col.assembly.failed");
  }
  
  @Override
  public void start() throws Exception {
    LOG.info("Starting assembly coordinator");
    exec = Executors.newSingleThreadExecutor(new NamedThreadFactory(THREAD_NAME, Thread.NORM_PRIORITY, true));
  }
  
  @Override
  public void stop() throws Exception {
    // orderly shutdown running imports
    for (Future f : syncs.values()) {
      f.cancel(true);
    }
    // fully shutdown threadpool within given time
    ExecutorUtils.shutdown(exec, MILLIS_TO_DIE, TimeUnit.MILLISECONDS);
  }
  
  public AssemblyState getState() {
    return new AssemblyState(Lists.newArrayList(syncStates.values()), (int) failed.getCount(), (int) counter.getCount(), null);
  }
  
  public synchronized void syncSector(int sectorKey, ColUser user) {
    // is this sector already syncing?
    if (syncs.containsKey(sectorKey)) {
      LOG.info("Sector {} already syncing", sectorKey);
      // ignore
    } else {
      SectorSync ss = new SectorSync(sectorKey, factory, null, this::successCallBack, this::errorCallBack, user);
      syncs.put(sectorKey, exec.submit(ss));
      syncStates.put(sectorKey, ss.getState());
      LOG.info("Queued sync of sector {}", sectorKey);
    }
  }
  
  /**
   * We use old school callbacks here as you cannot easily cancel CopletableFutures.
   */
  private void successCallBack(SectorSync sync) {
    Duration durQueued = Duration.between(sync.getCreated(), sync.getStarted());
    Duration durRun = Duration.between(sync.getStarted(), LocalDateTime.now());
    LOG.info("Sector Sync {} finished. {} min queued, {} min to execute", sync.getSectorKey(), durQueued.toMinutes(), durRun.toMinutes());
    counter.inc();
    timer.update(durRun.getSeconds(), TimeUnit.SECONDS);
    syncs.remove(sync.getSectorKey());
    syncStates.remove(sync.getSectorKey());
  }
  
  /**
   * We use old school callbacks here as you cannot easily cancel CopletableFutures.
   */
  private void errorCallBack(SectorSync sync, Exception err) {
    LOG.error("Sector Sync {} failed: {}", sync.getSectorKey(), err.getCause().getMessage(), err.getCause());
    failed.inc();
    syncs.remove(sync.getSectorKey());
    syncStates.remove(sync.getSectorKey());
  }
}