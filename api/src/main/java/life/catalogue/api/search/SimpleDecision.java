package life.catalogue.api.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import life.catalogue.api.model.DSID;
import life.catalogue.api.model.EditorialDecision;

import java.util.Objects;

public class SimpleDecision {
  private Integer id;
  private Integer datasetKey;
  private EditorialDecision.Mode mode;
  
  public Integer getId() {
    return id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  public Integer getDatasetKey() {
    return datasetKey;
  }
  
  public void setDatasetKey(Integer datasetKey) {
    this.datasetKey = datasetKey;
  }

  @JsonIgnore
  public DSID<Integer> getKey() {
    return DSID.of(datasetKey, id);
  }

  public EditorialDecision.Mode getMode() {
    return mode;
  }
  
  public void setMode(EditorialDecision.Mode mode) {
    this.mode = mode;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimpleDecision that = (SimpleDecision) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(datasetKey, that.datasetKey) &&
        mode == that.mode;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(id, datasetKey, mode);
  }
}
