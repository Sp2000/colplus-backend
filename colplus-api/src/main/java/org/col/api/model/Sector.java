package org.col.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.col.api.vocab.Datasets;

/**
 * A taxonomic sector definition within a dataset that is used to assemble the Catalogue of Life.
 * Sectors will also serve to show the taxonomic coverage in the CoL portal.
 * The subject of the sector is the root taxon in the original source dataset.
 * The target is the matching taxon in the assembled catalogue,
 * the subject should be replacing in ATTACH mode which is the default.
 * In MERGE mode the subject taxon itself should be skipped and only its descendants be included if they do
 * not already exist.
 *
 * A sector can be really small and the subject even be a species, but usually it is some higher taxon.
 */
public class Sector extends Decision {
  private SimpleName target;
  private Mode mode = Sector.Mode.ATTACH;
  
  public static enum Mode {
    /**
     * Attach the entire subject and its descendants under its target parent.
     */
    ATTACH,

    /**
     * Merge all descendants of subject under the target taxon, but exclude the subject taxon itself.
     */
    MERGE
  }
  
  public Mode getMode() {
    return mode;
  }
  
  public void setMode(Mode mode) {
    this.mode = mode;
  }
 
  /**
   * The attachment point in the CoL tree, i.e. the CoL parent taxon for the sector root
   */
  public SimpleName getTarget() {
    return target;
  }
  
  public void setTarget(SimpleName target) {
    this.target = target;
  }
  
  @JsonIgnore
  public DatasetID getSourceAsDatasetID() {
    return new DatasetID(datasetKey, subject.getId());
  }
  
  @JsonIgnore
  public DatasetID getTargetAsDatasetID() {
    return new DatasetID(Datasets.DRAFT_COL, target.getId());
  }

  @Override
  public String toString() {
    return "Sector{" + getKey() +
        ", datasetKey=" + datasetKey +
        ", mode=" + mode +
        ", subject=" + getSubject() +
        '}';
  }
}
