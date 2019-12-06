package life.catalogue.api.model;

import java.net.URI;

import com.google.common.collect.ImmutableSet;
import life.catalogue.api.jackson.ApiModule;
import life.catalogue.api.jackson.SerdeTestBase;
import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.DatasetType;
import life.catalogue.api.vocab.Frequency;
import life.catalogue.api.vocab.License;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 *
 */
public class DatasetTest extends SerdeTestBase<Dataset> {
  
  public DatasetTest() {
    super(Dataset.class);
  }
  
  @Override
  public Dataset genTestValue() throws Exception {
    Dataset d = new Dataset();
    d.setKey(12345);
    d.setTitle("gfdscdscw");
    d.setDescription("gefzw fuewh gczew fw hfueh j ijdfeiw jfie eö.. few . few .");
    d.setOrigin(DatasetOrigin.UPLOADED);
    d.setType(DatasetType.TAXONOMIC);
    d.setImportFrequency(Frequency.MONTHLY);
    d.setDataAccess(URI.create("www.gbif.org"));
    d.setWebsite(URI.create("www.gbif.org"));
    d.setLogo(URI.create("www.gbif.org"));
    d.setLicense(License.CC0);
    d.setCitation("cf5twv867cwcgewcwe");
    d.setGeographicScope("North Africa");
    d.setContact("Me");
    d.getOrganisations().add("bla");
    d.getOrganisations().add("bla");
    d.getOrganisations().add("bla");
    d.setContact("foo");
    d.setNotes("cuzdsghazugbe67wqt6c g cuzdsghazugbe67wqt6c g  nhjs");
    return d;
  }
  
  @Test
  public void testContributesTo() throws Exception {
    Dataset d = genTestValue();
    d.setContributesTo(ImmutableSet.of(1,2));

    String json = ApiModule.MAPPER.writeValueAsString(d);
    Dataset d2 = ApiModule.MAPPER.readValue(json, Dataset.class);
  
    json = json.replaceAll("\"contributesTo\":\\[1,2\\]", "\"contributesTo\":\"col\"");
    System.out.println(json);
    d2 = ApiModule.MAPPER.readValue(json, Dataset.class);
    assertNull(d2.getContributesTo());
  
    json = json.replaceAll("\"contributesTo\":\"col\"", "\"contributesTo\":[\"col\", \"dCol\"]");
    d2 = ApiModule.MAPPER.readValue(json, Dataset.class);
    assertNull(d2.getContributesTo());
  }
  
  @Test
  public void testEmptyString() throws Exception {
    String json = ApiModule.MAPPER.writeValueAsString(genTestValue());
    json = json.replaceAll("www\\.gbif\\.org", "");
    json = json.replaceAll("cc0", "");
    
    Dataset d = ApiModule.MAPPER.readValue(json, Dataset.class);
    assertNull(d.getWebsite());
    assertNull(d.getDataAccess());
    assertNull(d.getLogo());
    assertNull(d.getLicense());
  }

  @Override
  protected void debug(String json, Wrapper<Dataset> wrapper, Wrapper<Dataset> wrapper2) {
    System.out.println(json);
  }
}