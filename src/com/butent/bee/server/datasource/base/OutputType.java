package com.butent.bee.server.datasource.base;

public enum OutputType {
  HTML("html"),
  JSON("json"),
  JSONP("jsonp"),
  CSV("csv"),
  TSV_EXCEL("tsv-excel");

  public static OutputType defaultValue() {
    return JSON;
  }

  public static OutputType findByCode(String code) {
    for (OutputType t : values()) {
      if (t.code.equals(code)) {
        return t;
      }
    }
    return null;
  }

  private String code;

  OutputType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
