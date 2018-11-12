package org.col.es.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoolQuery extends AbstractQuery {
  
  private static class Clause {
    List<Query> must;
    @JsonProperty("must_not")
    List<Query> mustNot;
    List<Query> should;
  }
  
  private final Clause bool;
  
  public BoolQuery() {
    this.bool = new Clause();
  }
  
  public BoolQuery must(Query query) {
    if (bool.must == null) {
      bool.must = new ArrayList<>();
    }
    bool.must.add(query);
    return this;
  }
  
  public BoolQuery mustNot(Query query) {
    if (bool.mustNot == null) {
      bool.mustNot = new ArrayList<>();
    }
    bool.mustNot.add(query);
    return this;
  }
  
  public BoolQuery should(Query query) {
    if (bool.should == null) {
      bool.should = new ArrayList<>();
    }
    bool.should.add(query);
    return this;
  }
  
  Clause getBool() {
    return bool;
  }
  
}
