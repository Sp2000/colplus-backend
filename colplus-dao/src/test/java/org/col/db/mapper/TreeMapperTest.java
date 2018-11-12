package org.col.db.mapper;

import java.util.List;

import org.col.api.TestEntityGenerator;
import org.col.api.model.ColSource;
import org.col.api.model.NameRef;
import org.col.api.model.Sector;
import org.col.api.model.TreeNode;
import org.junit.Before;
import org.junit.Test;

import static org.col.api.vocab.Datasets.DRAFT_CAT;
import static org.junit.Assert.*;

public class TreeMapperTest extends MapperTestBase<TreeMapper> {
  
  private ColSource source;
  private final int dataset11 = TestEntityGenerator.DATASET11.getKey();
  
  public TreeMapperTest() {
    super(TreeMapper.class);
  }
  
  @Before
  public void initSource() {
    source = ColSourceMapperTest.create(dataset11);
    mapper(ColSourceMapper.class).create(source);
    
    commit();
  }
  
  @Test
  public void root() {
    assertEquals(2, mapper().root(dataset11).size());
  }
  
  @Test
  public void parents() {
    assertEquals(1, mapper().parents(dataset11, "root-1").size());
  }
  
  @Test
  public void children() {
    assertEquals(0, mapper().children(dataset11, "root-1").size());
  }
  
  @Test
  public void draftWithSector() {
    populateDraftTree();
    
    SectorMapper sm = mapper(SectorMapper.class);
    
    Sector s1 = new Sector();
    s1.setColSourceKey(source.getKey());
    s1.setRoot(nameref("root-1"));
    s1.setAttachment(nameref("t4"));
    sm.create(s1);
    
    Sector s2 = new Sector();
    s2.setColSourceKey(source.getKey());
    s2.setRoot(nameref("root-2"));
    s2.setAttachment(nameref("t5"));
    sm.create(s2);
    commit();
    
    List<TreeNode> nodes = mapper().children(DRAFT_CAT, "t1");
    assertEquals(1, nodes.size());
    
    nodes = mapper().children(DRAFT_CAT, "t2");
    assertEquals(1, nodes.size());
    
    nodes = mapper().children(DRAFT_CAT, "t3");
    assertEquals(2, nodes.size());
    
    nodes = mapper().parents(DRAFT_CAT, "t4");
    assertEquals(4, nodes.size());
    assertNotNull(nodes.get(0).getSector());
    assertNull(nodes.get(1).getSector());
  }
  
  private void noSector(List<TreeNode> nodes) {
    for (TreeNode n : nodes) {
      assertNull(n.getSector());
    }
  }
  
  private static NameRef nameref(String id) {
    NameRef nr = new NameRef();
    nr.setId(id);
    return nr;
  }
  
}