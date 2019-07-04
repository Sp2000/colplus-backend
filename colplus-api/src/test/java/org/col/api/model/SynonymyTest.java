package org.col.api.model;

import org.col.api.TestEntityGenerator;
import org.col.api.jackson.SerdeTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class SynonymyTest extends SerdeTestBase<Synonymy> {
  
  public SynonymyTest() {
    super(Synonymy.class);
  }
  
  @Override
  public Synonymy genTestValue() throws Exception {
    return TestEntityGenerator.newSynonymy();
  }
  
  @Test
  public void isEmpty() throws Exception {
    Synonymy s = new Synonymy();
    assertTrue(s.isEmpty());
    
    s = TestEntityGenerator.newSynonymy();
    assertFalse(s.isEmpty());
  }
  
  @Test
  public void size() throws Exception {
    Synonymy s = new Synonymy();
    s.addHeterotypicGroup(TestEntityGenerator.newNames(4));
    s.addHeterotypicGroup(TestEntityGenerator.newNames(1));
    s.addHeterotypicGroup(TestEntityGenerator.newNames(8));
    assertEquals(13, s.size());
  }
  
}