package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.Maps;

import java.util.Map;

public enum AggregationType { SUM("sum"), COUNT("count"), MIN("min"), MAX("max"), AVG("avg");
  private static Map<String, AggregationType> codeToAggregationType;

  static {
    codeToAggregationType = Maps.newHashMap();
    for (AggregationType type : AggregationType.values()) {
      codeToAggregationType.put(type.code, type);
    }
  }

  public static AggregationType getByCode(String code) {
    return codeToAggregationType.get(code);
  }

  private String code;

  private AggregationType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
