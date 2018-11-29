package org.col.es.query;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Basically just the value you are matching your documents against, plus an extra option to boost matching documents.
 */
public class MatchValue {

  public static enum Operator {
    AND, OR;

    @JsonValue
    public String toString() {
      return name();
    }
  }

  // This is actually just the search string
  final String query;
  final Float boost;
  final Operator operator;

  public MatchValue(String query) {
    this(query, null);
  }

  public MatchValue(String query, Float boost) {
    this(query, boost, Operator.AND);
  }

  public MatchValue(String query, Float boost, Operator operator) {
    this.query = query;
    this.boost = boost;
    this.operator = operator;
  }

}