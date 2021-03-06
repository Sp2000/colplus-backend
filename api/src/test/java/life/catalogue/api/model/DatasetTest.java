package life.catalogue.api.model;

import life.catalogue.api.jackson.ApiModule;
import life.catalogue.api.jackson.SerdeTestBase;
import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.DatasetType;
import life.catalogue.api.vocab.License;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class DatasetTest extends SerdeTestBase<Dataset> {
  
  public DatasetTest() {
    super(Dataset.class);
  }

  public static Dataset generateTestDataset() {
    Dataset d = new Dataset();
    d.setKey(12345);
    d.setSourceKey(12345);
    d.setTitle("gfdscdscw");
    d.setDescription("gefzw fuewh gczew fw hfueh j ijdfeiw jfie eö.. few . few .");
    d.setOrigin(DatasetOrigin.EXTERNAL);
    d.setType(DatasetType.TAXONOMIC);
    d.setWebsite(URI.create("www.gbif.org"));
    d.setLogo(URI.create("www.gbif.org"));
    d.setLicense(License.CC0);
    d.setCitation("cf5twv867cwcgewcwe");
    d.setGeographicScope("North Africa");
    d.setContact(Person.parse("Me"));
    d.getOrganisations().add(new Organisation("bla"));
    d.getOrganisations().add(new Organisation("bla"));
    d.getOrganisations().add(new Organisation("bla"));
    d.setContact(Person.parse("foo"));
    d.setNotes("cuzdsghazugbe67wqt6c g cuzdsghazugbe67wqt6c g  nhjs");
    return d;
  }

  @Override
  public Dataset genTestValue() throws Exception {
    return generateTestDataset();
  }

  @Test
  public void patch() throws Exception {
    Dataset d = genTestValue();

    DatasetMetadata patch = new Dataset();
    patch.setTitle("Grundig");
    patch.setAlias("grr");
    d.applyPatch(patch);

    assertEquals("Grundig", d.getTitle());
    assertEquals("grr", d.getAlias());
  }
  
  @Test
  public void testEmptyString() throws Exception {
    String json = ApiModule.MAPPER.writeValueAsString(genTestValue());
    json = json.replaceAll("www\\.gbif\\.org", "");
    json = json.replaceAll("cc0", "");
    
    Dataset d = ApiModule.MAPPER.readValue(json, Dataset.class);
    assertNull(d.getWebsite());
    assertNull(d.getLogo());
    assertNull(d.getLicense());
  }
}