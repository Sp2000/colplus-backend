package org.col.assembly;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;
import org.col.api.model.NameUsageBase;
import org.col.api.model.Sector;
import org.col.api.model.SimpleName;
import org.col.api.vocab.DataFormat;
import org.col.api.vocab.Datasets;
import org.col.api.vocab.Origin;
import org.col.dao.DatasetImportDao;
import org.col.dao.NamesTreeDao;
import org.col.dao.TreeRepoRule;
import org.col.db.PgSetupRule;
import org.col.db.mapper.NameUsageMapper;
import org.col.db.mapper.SectorMapper;
import org.col.db.mapper.TaxonMapper;
import org.col.db.mapper.TestDataRule;
import org.col.db.tree.TextTreePrinter;
import org.col.es.NameUsageIndexService;
import org.col.importer.PgImportRule;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SectorSyncIT {
  
  public final static PgSetupRule pg = new PgSetupRule();
  public final static TestDataRule dataRule = TestDataRule.draft();
  public final static PgImportRule importRule = PgImportRule.create(
      NomCode.BOTANICAL,
        DataFormat.ACEF,  1,
        DataFormat.COLDP, 0,
      NomCode.ZOOLOGICAL,
        DataFormat.ACEF,  5, 6,
        DataFormat.COLDP, 2
  );
  public final static TreeRepoRule treeRepoRule = new TreeRepoRule();
  
  @ClassRule
  public final static TestRule chain = RuleChain
      .outerRule(pg)
      .around(dataRule)
      .around(treeRepoRule)
      .around(importRule);

  DatasetImportDao diDao;
  NamesTreeDao treeDao;
  
  @Before
  public void init () throws IOException, SQLException {
    diDao = new DatasetImportDao(PgSetupRule.getSqlSessionFactory(), treeRepoRule.getRepo());
    treeDao = new NamesTreeDao(PgSetupRule.getSqlSessionFactory(), treeRepoRule.getRepo());
    // reset draft
    dataRule.truncateDraft();
    dataRule.loadData(true);
  }
  
  
  public int datasetKey(int key, DataFormat format) {
    return importRule.datasetKey(key, format);
  }
  
  NameUsageBase getByName(int datasetKey, Rank rank, String name) {
    try (SqlSession session = PgSetupRule.getSqlSessionFactory().openSession(true)) {
      List<NameUsageBase> taxa = session.getMapper(NameUsageMapper.class).listByName(datasetKey, name, rank);
      if (taxa.size() > 1) throw new IllegalStateException("Multiple taxa found for name="+name);
      return taxa.get(0);
    }
  }
  
  NameUsageBase getByID(String id) {
    try (SqlSession session = PgSetupRule.getSqlSessionFactory().openSession(true)) {
      return session.getMapper(TaxonMapper.class).get(Datasets.DRAFT_COL, id);
    }
  }
  
  static int createSector(Sector.Mode mode, NameUsageBase src, NameUsageBase target) {
    try (SqlSession session = PgSetupRule.getSqlSessionFactory().openSession(true)) {
      Sector sector = new Sector();
      sector.setMode(mode);
      sector.setDatasetKey(src.getDatasetKey());
      sector.setSubject(new SimpleName(src.getId(), src.getName().canonicalNameComplete(), src.getName().getRank()));
      sector.setTarget(new SimpleName(target.getId(), target.getName().canonicalNameComplete(), target.getName().getRank()));
      sector.applyUser(TestDataRule.TEST_USER);
      session.getMapper(SectorMapper.class).create(sector);
      return sector.getKey();
    }
  }
    
  void syncAll() throws IOException {
    try (SqlSession session = PgSetupRule.getSqlSessionFactory().openSession(true)) {
      for (Sector s : session.getMapper(SectorMapper.class).list(null)) {
        sync(s);
      }
    }
  }
  
  void sync(Sector s) {
    
    SectorSync ss = new SectorSync(s, PgSetupRule.getSqlSessionFactory(), NameUsageIndexService.passThru(), diDao,
        SectorSyncTest::successCallBack, SectorSyncTest::errorCallBack, TestDataRule.TEST_USER);
    System.out.println("\n*** SECTOR SYNC " + s.getKey() + " ***");
    ss.run();
  }
  
  void print(int datasetKey) throws Exception {
    StringWriter writer = new StringWriter();
    writer.append("\nDATASET "+datasetKey+"\n");
    TextTreePrinter.dataset(datasetKey, PgSetupRule.getSqlSessionFactory(), writer).print();
    System.out.println(writer.toString());
  }

  @Test
  public void test1_5_6() throws Exception {
    print(Datasets.DRAFT_COL);
    print(datasetKey(1, DataFormat.ACEF));
    print(datasetKey(5, DataFormat.ACEF));
    print(datasetKey(6, DataFormat.ACEF));
  
    NameUsageBase src = getByName(datasetKey(1, DataFormat.ACEF), Rank.ORDER, "Fabales");
    NameUsageBase trg = getByName(Datasets.DRAFT_COL, Rank.PHYLUM, "Tracheophyta");
    createSector(Sector.Mode.ATTACH, src, trg);
  
    src = getByName(datasetKey(5, DataFormat.ACEF), Rank.CLASS, "Insecta");
    trg = getByName(Datasets.DRAFT_COL, Rank.CLASS, "Insecta");
    createSector(Sector.Mode.MERGE, src, trg);
  
    src = getByName(datasetKey(6, DataFormat.ACEF), Rank.FAMILY, "Theridiidae");
    trg = getByName(Datasets.DRAFT_COL, Rank.CLASS, "Insecta");
    createSector(Sector.Mode.ATTACH, src, trg);

    syncAll();
    assertTree("cat1_5_6.txt");
  
    NameUsageBase vogelii   = getByName(Datasets.DRAFT_COL, Rank.SUBSPECIES, "Astragalus vogelii subsp. vogelii");
    assertEquals(1, (int) vogelii.getSectorKey());
  
    NameUsageBase sp   = getByID(vogelii.getParentId());
    assertEquals(Origin.SOURCE, vogelii.getOrigin());
  }
  
  @Test
  public void testImplicitGenus() throws Exception {
    print(Datasets.DRAFT_COL);
    print(datasetKey(0, DataFormat.COLDP));
    print(datasetKey(2, DataFormat.COLDP));
    
    NameUsageBase asteraceae   = getByName(datasetKey(0, DataFormat.COLDP), Rank.FAMILY, "Asteraceae");
    NameUsageBase tracheophyta = getByName(Datasets.DRAFT_COL, Rank.PHYLUM, "Tracheophyta");
    createSector(Sector.Mode.ATTACH, asteraceae, tracheophyta);
  
    NameUsageBase coleoptera = getByName(datasetKey(2, DataFormat.COLDP), Rank.ORDER, "Coleoptera");
    NameUsageBase insecta = getByName(Datasets.DRAFT_COL, Rank.CLASS, "Insecta");
    createSector(Sector.Mode.ATTACH, coleoptera, insecta);
    
    syncAll();
    assertTree("cat0_2.txt");
  }
 
  void assertTree(String filename) throws IOException {
    InputStream resIn = getClass().getResourceAsStream("/assembly-trees/" + filename);
    String expected = IOUtils.toString(resIn, Charsets.UTF_8).trim();
    
    Writer writer = new StringWriter();
    TextTreePrinter.dataset(Datasets.DRAFT_COL, PgSetupRule.getSqlSessionFactory(), writer).print();
    String tree = writer.toString().trim();
    assertFalse("Empty tree, probably no root node found", tree.isEmpty());
  
    // compare trees
    System.out.println("\n*** DRAFT TREE ***");
    System.out.println(tree);
    assertEquals("Assembled tree not as expected", expected, tree);
  }
}