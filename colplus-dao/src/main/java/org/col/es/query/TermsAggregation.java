package org.col.es.query;

import com.fasterxml.jackson.annotation.JsonCreator;

public class TermsAggregation extends Aggregation {

  public static class Terms {
    private final String field;
    private final Integer size;

    @JsonCreator
    public Terms(String field, Integer size) {
      this.field = field;
      this.size = size;
    }

    public String getField() {
      return field;
    }

    public Integer getSize() {
      return size;
    }
  }

  private final Terms terms;

  public TermsAggregation(String field) {
    this.terms = new Terms(field, null);
  }

  public TermsAggregation(String field, Integer size) {
    this.terms = new Terms(field, size);
  }

  public Terms getTerms() {
    return terms;
  }

}