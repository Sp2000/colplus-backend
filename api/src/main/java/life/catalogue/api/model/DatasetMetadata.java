package life.catalogue.api.model;

import life.catalogue.api.vocab.License;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * The pure descriptive metadata of a dataset, excluding all internal aspects, timestamps and settings.
 */
public interface DatasetMetadata {

  Integer getKey();

  void setKey(Integer key);

  String getTitle();

  void setTitle(String title);

  String getAlias();

  void setAlias(String alias);

  String getDescription();

  void setDescription(String description);

  List<Person> getAuthors();

  void setAuthors(List<Person> authors);

  List<Person> getEditors();

  void setEditors(List<Person> editors);

  List<Organisation> getOrganisations();

  void setOrganisations(List<Organisation> organisations);

  Person getContact();

  void setContact(Person contact);

  License getLicense();

  void setLicense(License license);

  String getVersion();

  void setVersion(String version);

  LocalDate getReleased();

  void setReleased(LocalDate released);

  String getGeographicScope();

  void setGeographicScope(String geographicScope);

  String getCitation();

  void setCitation(String citation);

  URI getWebsite();

  void setWebsite(URI website);

  String getGroup();

  void setGroup(String group);

  Integer getConfidence();

  void setConfidence(Integer confidence);

  Integer getCompleteness();

  void setCompleteness(Integer completeness);

  /**
   * Creates and returns a shallow copy of the object
   */
  static DatasetMetadata copy(DatasetMetadata obj) {
    DatasetMetadata d = new ArchivedDataset();
    d.setKey(obj.getKey());
    d.setTitle(obj.getTitle());
    d.setAlias(obj.getAlias());
    d.setDescription(obj.getDescription());
    d.setAuthors(obj.getAuthors());
    d.setEditors(obj.getEditors());
    d.setOrganisations(obj.getOrganisations());
    d.setContact(obj.getContact());
    d.setLicense(obj.getLicense());
    d.setVersion(obj.getVersion());
    d.setReleased(obj.getReleased());
    d.setGeographicScope(obj.getGeographicScope());
    d.setCitation(obj.getCitation());
    d.setWebsite(obj.getWebsite());
    d.setGroup(obj.getGroup());
    d.setConfidence(obj.getConfidence());
    d.setCompleteness(obj.getCompleteness());
    return d;
  }
}
