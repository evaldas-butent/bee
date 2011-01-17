package com.butent.bee.shared.data;

import com.google.common.collect.Maps;

import java.util.Map;

public enum Aggregation { SUM("sum"), COUNT("count"), MIN("min"), MAX("max"), AVG("avg");
  private static Map<String, Aggregation> codeToAggregationType;

  static {
    codeToAggregationType = Maps.newHashMap();
    for (Aggregation type : Aggregation.values()) {
      codeToAggregationType.put(type.code, type);
    }
  }

  public static Aggregation getByCode(String code) {
    return codeToAggregationType.get(code);
  }

  private String code;

  private Aggregation(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
