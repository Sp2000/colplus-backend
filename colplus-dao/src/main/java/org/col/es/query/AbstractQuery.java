package org.col.es.query;

public abstract class AbstractQuery implements Query {

  public String toString() {
    return QueryUtil.toString(this);
  }

}