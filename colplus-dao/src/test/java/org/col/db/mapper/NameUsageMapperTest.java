package org.col.db.mapper;

import java.util.ArrayList;
import java.util.List;

import org.col.api.TestEntityGenerator;
import org.col.api.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.col.api.TestEntityGenerator.DATASET11;
import static org.junit.Assert.*;

public class NameUsageMapperTest extends MapperTestBase<NameUsageMapper> {
  
  TaxonMapper tm;
  SynonymMapper sm;
  
  public NameUsageMapperTest() {
    super(NameUsageMapper.class);
  }
  
  @Before
  public void init() {
    tm = mapper(TaxonMapper.class);
    sm = mapper(SynonymMapper.class);
  }
  @Test
  public void list() throws Exception {
    List<Taxon> taxa = new ArrayList<>();
    taxa.add(TestEntityGenerator.newTaxon("t1"));
    taxa.add(TestEntityGenerator.newTaxon("t2"));
    taxa.add(TestEntityGenerator.newTaxon("t3"));
    taxa.add(TestEntityGenerator.newTaxon("t4"));
    taxa.add(TestEntityGenerator.newTaxon("t5"));
    taxa.add(TestEntityGenerator.newTaxon("t6"));
    taxa.add(TestEntityGenerator.newTaxon("t7"));
    taxa.add(TestEntityGenerator.newTaxon("t8"));
    taxa.add(TestEntityGenerator.newTaxon("t9"));
    for (Taxon t : taxa) {
      tm.create(t);
    }
  
    List<Synonym> syns = new ArrayList<>();
    syns.add(TestEntityGenerator.newSynonym(taxa.get(2)));
    syns.add(TestEntityGenerator.newSynonym(taxa.get(2)));
    syns.add(TestEntityGenerator.newSynonym(taxa.get(5)));
    for (Synonym t : syns) {
      sm.create(t);
    }
    commit();
    
    // get first page
    Page p = new Page(0, 3);
    
    List<NameUsageBase> res = mapper().list(DATASET11.getKey(), p);
    assertEquals(3, res.size());
    // First 2 taxa in dataset 11 are pre-inserted taxa
    // next 2 are preinserted synonyms
    // then our 3 created syns
    // finally 9 new taxa
    assertIdClassEquals(TestEntityGenerator.TAXON1, res.get(0));
    assertIdClassEquals(TestEntityGenerator.TAXON2, res.get(1));
    assertIdClassEquals(TestEntityGenerator.SYN1, res.get(2));

    p.next();
    res = mapper().list(DATASET11.getKey(), p);
    assertEquals(3, res.size());
    assertIdClassEquals(TestEntityGenerator.SYN2, res.get(0));
    assertIdClassEquals(syns.get(0), res.get(1));
    assertIdClassEquals(syns.get(1), res.get(2));
  
    p.next();
    res = mapper().list(DATASET11.getKey(), p);
    assertEquals(3, res.size());
    assertIdClassEquals(syns.get(2), res.get(0));
    assertIdClassEquals(taxa.get(0), res.get(1));
    assertIdClassEquals(taxa.get(1), res.get(2));
  }
  
  void assertIdClassEquals(NameUsageBase o1, NameUsageBase o2) {
    assertEquals(o1.getId(), o2.getId());
    assertEquals(o1.getClass(), o2.getClass());
  }
}