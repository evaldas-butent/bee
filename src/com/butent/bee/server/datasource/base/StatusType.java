package com.butent.bee.server.datasource.base;

public enum StatusType { OK, ERROR, WARNING;
  public String lowerCaseString() {
    return this.toString().toLowerCase();
  }
}
