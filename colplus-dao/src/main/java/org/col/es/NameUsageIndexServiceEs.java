package org.col.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.col.api.model.Sector;
import org.col.api.search.NameUsageWrapper;
import org.col.db.mapper.BatchResultHandler;
import org.col.db.mapper.DatasetMapper;
import org.col.db.mapper.NameUsageWrapperMapper;
import org.col.db.mapper.SectorMapper;
import org.col.es.model.EsNameUsage;
import org.col.es.query.EsSearchRequest;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.col.es.EsConfig.ES_INDEX_NAME_USAGE;

public class NameUsageIndexServiceEs implements NameUsageIndexService {

  private static final Logger LOG = LoggerFactory.getLogger(NameUsageIndexServiceEs.class);

  private final RestClient client;
  private final EsConfig esConfig;
  private final String index;
  private final SqlSessionFactory factory;

  public NameUsageIndexServiceEs(RestClient client, EsConfig esConfig, SqlSessionFactory factory) {
    this.client = client;
    this.index = esConfig.indexName(ES_INDEX_NAME_USAGE);
    this.esConfig = esConfig;
    this.factory = factory;
  }

  @VisibleForTesting
  NameUsageIndexServiceEs(RestClient client, EsConfig esConfig, SqlSessionFactory factory, String index) {
    this.client = client;
    this.index = index;
    this.esConfig = esConfig;
    this.factory = factory;
  }

  @Override
  public void indexDataset(int datasetKey) {
    NameUsageIndexer indexer = new NameUsageIndexer(client, index);
    int tCount, bCount;
    try (SqlSession session = factory.openSession()) {
      createOrEmptyIndex(datasetKey);
      NameUsageWrapperMapper mapper = session.getMapper(NameUsageWrapperMapper.class);
      try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
        LOG.debug("Indexing usages from dataset {}", datasetKey);
        mapper.processDatasetUsages(datasetKey, null, handler);
      }
      EsUtil.refreshIndex(client, index);
      tCount = indexer.documentsIndexed();
      indexer.reset();
      try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
        LOG.debug("Indexing bare names from dataset {}", datasetKey);
        mapper.processDatasetBareNames(datasetKey, null, handler);
      }
      EsUtil.refreshIndex(client, index);
      bCount = indexer.documentsIndexed();
    } catch (IOException e) {
      throw new EsException(e);
    }
    logDatasetTotals(datasetKey, tCount, bCount);
  }

  @Override
  public void indexSector(int sectorKey) {
    NameUsageIndexer indexer = new NameUsageIndexer(client, index);
    int tCount, bCount;
    try (SqlSession session = factory.openSession()) {
      Integer datasetKey = clearSector(session, sectorKey);
      NameUsageWrapperMapper mapper = session.getMapper(NameUsageWrapperMapper.class);
      try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
        LOG.debug("Indexing usages from sector {}", sectorKey);
        mapper.processDatasetUsages(datasetKey, sectorKey, handler);
      }
      EsUtil.refreshIndex(client, index);
      tCount = indexer.documentsIndexed();
      indexer.reset();
      try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
        LOG.debug("Indexing bare names from sector {}", sectorKey);
        mapper.processDatasetBareNames(datasetKey, sectorKey, handler);
      }
      EsUtil.refreshIndex(client, index);
      bCount = indexer.documentsIndexed();
    } catch (IOException e) {
      throw new EsException(e);
    }
    logSectorTotals(sectorKey, tCount, bCount);
  }

  @Override
  public void deleteSector(int sectorKey) {
    try (SqlSession session = factory.openSession()) {
      clearSector(session, sectorKey);
    } catch (IOException e) {
      throw new EsException(e);
    }
  }

  @Override
  public void indexTaxa(int datasetKey, Collection<String> taxonIds) {
    int inserted = indexNameUsages(datasetKey, taxonIds);
    LOG.info("Finished indexing taxa. Taxon IDs provided: {}. Name usages indexed: {}", taxonIds.size(), inserted);
  }

  @Override
  public void sync(int datasetKey, Collection<String> taxonIds) {
    try {
      int deleted = EsUtil.deleteNameUsages(client, index, datasetKey, taxonIds);
      int inserted = indexNameUsages(datasetKey, taxonIds);
      LOG.info("Finished indexing taxa. Taxon IDs provided: {}. Name usages indexed: {}. Name usages deleted: {}", taxonIds.size(),
          inserted, deleted);
      EsUtil.refreshIndex(client, index);
    } catch (IOException e) {
      throw new EsException(e);
    }
  }

  @Override
  public void updateClassification(int datasetKey, String rootTaxonId) {
    NameUsageIndexer indexer = new NameUsageIndexer(client, index);
    try (SqlSession session = factory.openSession()) {
      NameUsageWrapperMapper mapper = session.getMapper(NameUsageWrapperMapper.class);
      try (ClassificationUpdater updater = new ClassificationUpdater(indexer, datasetKey)) {
        mapper.processTree(datasetKey, rootTaxonId, updater);
      }
      EsUtil.refreshIndex(client, index);
    } catch (IOException e) {
      throw new EsException(e);
    }
    LOG.info("Successfully updated {} name usages", indexer.documentsIndexed());
  }

  @Override
  public void indexAll() {
    NameUsageIndexer indexer = new NameUsageIndexer(client, index);
    int tCount = 0, bCount = 0;
    try (SqlSession session = factory.openSession()) {
      EsUtil.deleteIndex(client, index);
      EsUtil.createIndex(client, index, esConfig.nameUsage);
      List<Integer> keys = session.getMapper(DatasetMapper.class).keys();
      NameUsageWrapperMapper mapper = session.getMapper(NameUsageWrapperMapper.class);
      for (Integer datasetKey : keys) {
        int tc, bc;
        try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
          LOG.debug("Indexing taxa for dataset {}", datasetKey);
          mapper.processDatasetUsages(datasetKey, null, handler);
        }
        EsUtil.refreshIndex(client, index);
        tc = indexer.documentsIndexed();
        indexer.reset();
        try (BatchResultHandler<NameUsageWrapper> handler = new BatchResultHandler<>(indexer, 4096)) {
          LOG.debug("Indexing bare names for dataset {}", datasetKey);
          mapper.processDatasetBareNames(datasetKey, null, handler);
        }
        EsUtil.refreshIndex(client, index);
        bc = indexer.documentsIndexed();
        indexer.reset();
        tCount += tc;
        bCount += bc;
        logDatasetTotals(datasetKey, tc, bc);
      }
      logTotals(keys.size(), tCount, bCount);
    } catch (IOException e) {
      throw new EsException(e);
    }
  }

  private void createOrEmptyIndex(int datasetKey) throws IOException {
    if (EsUtil.indexExists(client, index)) {
      EsUtil.deleteDataset(client, index, datasetKey);
      EsUtil.refreshIndex(client, index);
    } else {
      EsUtil.createIndex(client, index, esConfig.nameUsage);
    }
  }

  /*
   * Indexes documents but does not refresh the index! Must be done by caller.
   */
  private int indexNameUsages(int datasetKey, Collection<String> usageIds) {
    NameUsageIndexer indexer = new NameUsageIndexer(client, index);
    try (SqlSession session = factory.openSession()) {
      NameUsageWrapperMapper mapper = session.getMapper(NameUsageWrapperMapper.class);
      List<NameUsageWrapper> usages = usageIds.stream()
          .map(id -> mapper.get(datasetKey, id))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      if (usages.isEmpty()) {
        LOG.warn("None of the provided name usage IDs belong to dataset {}: {}.", datasetKey,
            usageIds.stream().collect(joining(", ")));
        return 0;
      }
      if (usages.size() != usageIds.size()) {
        List<String> ids = new ArrayList<>(usageIds);
        ids.removeAll(usages.stream().map(nuw -> nuw.getUsage().getId()).collect(toList()));
        LOG.warn("Some usage IDs not found in dataset {}: {}", datasetKey,
            ids.stream().collect(joining(", ")));
      }
      LOG.info("(Re-)indexing name usages from dataset {}", datasetKey);
      indexer.accept(usages);
      return indexer.documentsIndexed();
    }
  }

  /**
   * @return datasetKey of the deleted sector
   */
  private Integer clearSector(SqlSession session, Integer sectorKey) throws IOException {
    EsSearchRequest query = EsSearchRequest.emptyRequest();
    query.select("datasetKey").whereEquals("sectorKey", sectorKey).size(1);
    NameUsageSearchService svc = new NameUsageSearchService(index, client);
    List<EsNameUsage> result = svc.getDocuments(query);
    if (result.size() != 0) {
      int cnt = EsUtil.deleteSector(client, index, sectorKey);
      LOG.debug("Deleted all {} documents from sector {} from index {}", cnt, sectorKey, index);
      return result.get(0).getDatasetKey();
    }
    SectorMapper mapper = session.getMapper(SectorMapper.class);
    Sector sector = mapper.get(sectorKey);
    if (sector == null) {
      throw new IllegalArgumentException("No such sector: " + sectorKey);
    }
    return sector.getDatasetKey();
  }

  private void logDatasetTotals(int datasetKey, int tCount, int bCount) {
    String fmt = "Successfully indexed dataset {}. Index: {}. Usages: {}. Bare names: {}. Total: {}.";
    LOG.info(fmt, datasetKey, index, tCount, bCount, (tCount + bCount));
  }

  private void logSectorTotals(int sectorKey, int tCount, int bCount) {
    String fmt = "Successfully indexed sector {}. Index: {}. Usages: {}. Bare names: {}. Total: {}.";
    LOG.info(fmt, sectorKey, index, tCount, bCount, (tCount + bCount));
  }

  private void logTotals(int numDatasets, int tCount, int bCount) {
    String fmt = "Successfully indexed {} datasets. Index: {}. Usages: {}. Bare names: {}. Total: {}.";
    LOG.info(fmt, index, numDatasets, tCount, bCount, (tCount + bCount));
  }
}