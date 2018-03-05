package org.col.db;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NotFoundException extends IllegalArgumentException {

  private final static Joiner.MapJoiner PARAM_JOINER = Joiner.on(", ")
      .withKeyValueSeparator("=")
      .useForNull("null");

  private static String createMessage(Class<?> entity, Map<String, Object> params) {
    return "No such " + entity.getSimpleName() + ": " + PARAM_JOINER.join(params);
  }

  private final Class<?> entity;
  private final Map<String, Object> params;

  public static NotFoundException keyNotFound(Class<?> entity, int key) {
    return new NotFoundException(entity, ImmutableMap.of("key", key));
  }
  public static NotFoundException idNotFound(Class<?> entity, int datasetKey, String id) {
    return new NotFoundException(entity, ImmutableMap.of("datasetKey", datasetKey, "id", id));
  }

  public NotFoundException(Class<?> entity, Map<String, Object> params) {
    super(createMessage(entity, params));
    this.entity = entity;
    this.params = params;
  }

  public Class<?> getEntity() {
    return entity;
  }

  public Map<String, Object> getParams() {
    return params;
  }

}
