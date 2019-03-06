package org.col.admin.assembly;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.col.api.model.*;
import org.col.api.search.DatasetSearchRequest;
import org.col.api.vocab.Datasets;
import org.col.db.dao.MatchingDao;
import org.col.db.mapper.DatasetMapper;
import org.col.db.mapper.DecisionMapper;
import org.col.db.mapper.SectorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionRematcher implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(DecisionRematcher.class);
  private final SqlSessionFactory factory;
  private final Integer datasetKey;
  private SectorMapper sm;
  private DecisionMapper em;
  private MatchingDao mdao;
  private int sectorTotal = 0;
  private int sectorFailed  = 0;
  private int decisionTotal = 0;
  private int decisionFailed  = 0;
  private int datasets  = 0;
  
  public DecisionRematcher(SqlSessionFactory factory, Integer datasetKey) {
    this.factory = factory;
    this.datasetKey = datasetKey;
  }
  
  @Override
  public void run() {
    try (SqlSession session = factory.openSession(true)) {
      sm = session.getMapper(SectorMapper.class);
      em = session.getMapper(DecisionMapper.class);
      mdao = new MatchingDao(session);
      
      if (datasetKey == null) {
        DatasetMapper dm = session.getMapper(DatasetMapper.class);
        DatasetSearchRequest req = new DatasetSearchRequest();
        req.setContributesTo(Datasets.DRAFT_COL);
        req.setSortBy(DatasetSearchRequest.SortBy.SIZE);
        Page page = new Page(0, 20);
        List<Dataset> resp = null;
        while(resp == null || resp.get(resp.size()-1).getSize() != null) {
          resp = dm.search(req, page);
          for (Dataset d : resp) {
            matchDataset(d.getKey());
          }
        }
      } else {
        matchDataset(datasetKey);
      }
      if (datasetKey == null) {
        // log totals across all datasets
        LOG.info("Rematched {} sectors from all {} datasets, {} failed", sectorTotal, datasets, sectorFailed);
        LOG.info("Rematched {} decisions from all {} datasets, {} failed", decisionTotal, datasets, decisionFailed);
      }
    }
  }
  
  private void matchDataset(final int datasetKey) {
    datasets++;
    int counter = 0;
    int failed  = 0;
    for (Sector s : sm.list(datasetKey)) {
      List<Taxon> matches = mdao.matchDataset(s.getSubject(), s.getDatasetKey());
      if (matches.isEmpty()) {
        LOG.warn("Sector {} cannot be rematched to dataset {} - lost {}", s.getKey(), s.getDatasetKey(), s.getSubject());
        s.getSubject().setId(null);
        failed++;
      } else if (matches.size() > 1) {
        LOG.warn("Sector {} cannot be rematched to dataset {} - multiple names like {}", s.getKey(), s.getDatasetKey(), s.getSubject());
        s.getSubject().setId(null);
        failed++;
      } else {
        s.getSubject().setId(matches.get(0).getId());
      }
      counter++;
      sm.update(s);
    }
    sectorTotal  += counter;
    sectorFailed += failed;
    LOG.info("Rematched {} sectors from dataset {}, {} failed", counter, datasetKey, failed);
    
    counter = 0;
    failed = 0;
    for (EditorialDecision e : em.list(datasetKey, null)) {
      List<Taxon> matches = mdao.matchDataset(e.getSubject(), e.getDatasetKey());
      if (matches.isEmpty()) {
        LOG.warn("Decision {} cannot be rematched to dataset {} - lost {}", e.getKey(), e.getDatasetKey(), e.getSubject());
        e.getSubject().setId(null);
        failed++;
      } else if (matches.size() > 1) {
        LOG.warn("Decision {} cannot be rematched to dataset {} - multiple names like {}", e.getKey(), e.getDatasetKey(), e.getSubject());
        e.getSubject().setId(null);
        failed++;
      } else {
        e.getSubject().setId(matches.get(0).getId());
        counter++;
      }
      em.update(e);
      counter++;
    }
    decisionTotal  += counter;
    decisionFailed += failed;
    LOG.info("Rematched {} decisions from dataset {}, {} failed", counter, datasetKey, failed);
  }
}
