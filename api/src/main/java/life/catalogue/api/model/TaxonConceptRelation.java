package life.catalogue.api.model;

import life.catalogue.api.vocab.TaxonConceptRelType;

import java.util.Objects;

/**
 * A taxon concept relation between two taxa.
 */
public class TaxonConceptRelation extends DatasetScopedEntity<Integer> implements SectorEntity, VerbatimEntity, Referenced {
  private Integer datasetKey;
  private Integer sectorKey;
  private Integer verbatimKey;
  private TaxonConceptRelType type;
  private String taxonId;
  private String relatedTaxonId;
  private String referenceId;
  private String remarks;

  @Override
  public Integer getSectorKey() {
    return sectorKey;
  }

  @Override
  public void setSectorKey(Integer sectorKey) {
    this.sectorKey = sectorKey;
  }

  @Override
  public Integer getVerbatimKey() {
    return verbatimKey;
  }
  
  @Override
  public void setVerbatimKey(Integer verbatimKey) {
    this.verbatimKey = verbatimKey;
  }
  
  public Integer getDatasetKey() {
    return datasetKey;
  }
  
  public void setDatasetKey(Integer datasetKey) {
    this.datasetKey = datasetKey;
  }

  public TaxonConceptRelType getType() {
    return type;
  }

  public void setType(TaxonConceptRelType type) {
    this.type = type;
  }

  public String getTaxonId() {
    return taxonId;
  }

  public void setTaxonId(String taxonId) {
    this.taxonId = taxonId;
  }

  public String getRelatedTaxonId() {
    return relatedTaxonId;
  }

  public void setRelatedTaxonId(String relatedTaxonId) {
    this.relatedTaxonId = relatedTaxonId;
  }

  @Override
  public String getReferenceId() {
    return referenceId;
  }

  @Override
  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public String getRemarks() {
    return remarks;
  }
  
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TaxonConceptRelation)) return false;
    if (!super.equals(o)) return false;
    TaxonConceptRelation that = (TaxonConceptRelation) o;
    return Objects.equals(sectorKey, that.sectorKey) &&
      Objects.equals(verbatimKey, that.verbatimKey) &&
      Objects.equals(datasetKey, that.datasetKey) &&
      type == that.type &&
      Objects.equals(taxonId, that.taxonId) &&
      Objects.equals(relatedTaxonId, that.relatedTaxonId) &&
      Objects.equals(referenceId, that.referenceId) &&
      Objects.equals(remarks, that.remarks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), sectorKey, verbatimKey, datasetKey, type, taxonId, relatedTaxonId, referenceId, remarks);
  }
}