package life.catalogue.es.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import life.catalogue.api.model.EditorialDecision.Mode;
import life.catalogue.api.search.SimpleDecision;
import life.catalogue.es.mapping.ESDataType;
import life.catalogue.es.mapping.MapToType;

public class EsDecision {

  public static EsDecision from(SimpleDecision decision) {
    return new EsDecision(decision.getDatasetKey(), decision.getMode());
  }

  @MapToType(ESDataType.KEYWORD)
  private final Integer catalogueKey;
  @MapToType(ESDataType.KEYWORD)
  private final Mode mode;

  @JsonCreator
  public EsDecision(@JsonProperty("catalogueKey") Integer catalogueKey, @JsonProperty("mode") Mode mode) {
    this.catalogueKey = catalogueKey;
    this.mode = mode;
  }

  public Integer getCatalogueKey() {
    return catalogueKey;
  }

  public Mode getMode() {
    return mode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(catalogueKey, mode);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EsDecision other = (EsDecision) obj;
    return Objects.equals(catalogueKey, other.catalogueKey) && mode == other.mode;
  }

}
