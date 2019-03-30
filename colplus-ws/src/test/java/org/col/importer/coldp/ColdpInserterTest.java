package org.col.importer.coldp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.col.api.model.Dataset;
import org.col.api.model.Reference;
import org.col.api.vocab.DataFormat;
import org.col.api.vocab.DatasetType;
import org.col.api.vocab.License;
import org.col.config.NormalizerConfig;
import org.col.importer.neo.NeoDb;
import org.col.importer.neo.NeoDbFactory;
import org.col.importer.reference.ReferenceFactory;
import org.gbif.nameparser.api.NomCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ColdpInserterTest {
  Dataset d;
  NeoDb store;
  NormalizerConfig cfg;
  
  @Before
  public void initCfg() throws Exception {
    cfg = new NormalizerConfig();
    cfg.archiveDir = Files.createTempDir();
    cfg.scratchDir = Files.createTempDir();
  }
  
  @After
  public void cleanup() throws Exception {
    if (store != null) {
      store.closeAndDelete();
    }
    FileUtils.deleteQuietly(cfg.archiveDir);
    FileUtils.deleteQuietly(cfg.scratchDir);
  }
  
  void insert(String resource) {
    try {
      store = NeoDbFactory.create(1, 1, cfg);
      d = new Dataset();
      d.setKey(1);
      store.put(d);
  
      URL url = getClass().getResource(resource);
      Path coldp = Paths.get(url.toURI());
  
      ColdpInserter ins = new ColdpInserter(store, coldp, new ReferenceFactory(store));
      ins.insertAll();

    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Test
  public void readMetadata() throws Exception {
    URL url = getClass().getResource("/coldp/0");
    Path coldp0 = Paths.get(url.toURI());
    
    ColdpInserter ins = new ColdpInserter(null, coldp0, null);
    Dataset d = ins.readMetadata().get();
    
    assertEquals(DatasetType.OTHER, d.getType());
    assertEquals(DataFormat.COLDP, d.getDataFormat());
    assertEquals("The full dataset title", d.getTitle());
    assertNotNull(d.getDescription());
    assertEquals(10, d.getOrganisations().size());
    assertEquals("Nicolas Bailly <nbailly@hcmr.gr>", d.getContact());
    assertEquals(3, d.getAuthorsAndEditors().size());
    assertEquals(License.CC_BY_NC, d.getLicense());
    assertEquals("ver. (06/2018)", d.getVersion());
    assertEquals("2018-06-01", d.getReleased().toString());
    assertEquals("https://www.fishbase.org", d.getWebsite().toString());
    assertEquals("https://www.fishbase.de/images/gifs/fblogo_new.gif", d.getLogo().toString());
    assertEquals("Froese R. & Pauly D. (eds) (2018). FishBase (version 06/2018).", d.getCitation());
  
    assertEquals(NomCode.BOTANICAL, d.getCode());
    assertEquals((Integer)4, d.getConfidence());
    assertEquals((Integer)32, d.getCompleteness());
    assertEquals("my personal,\n" +
                          "very long notes", d.getNotes());
    assertEquals("shortname", d.getAlias());
  }
  
  @Test
  public void bibtex() throws Exception {
    insert("/coldp/bibtex");
  
    Reference r = store.refById("greene");
    assertEquals("title", r.getCsl().getTitle());
    assertEquals(1895, r.getCsl().getIssued().getDateParts()[0][0]);
    assertEquals((Integer) 1895, r.getYear());
  
    r = store.refById("Droege_2016");
    assertEquals("The Global Genome Biodiversity Network (GGBN) Data Standard specification", r.getCsl().getTitle());
    assertEquals(2016, r.getCsl().getIssued().getDateParts()[0][0]);
    assertEquals((Integer) 2016, r.getYear());
  }
}
