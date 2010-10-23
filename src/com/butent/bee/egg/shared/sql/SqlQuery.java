package com.butent.bee.egg.shared.sql;

import java.util.Map;

public abstract class SqlQuery {

  private boolean paramMode;

  public boolean getParamMode() {
    return paramMode;
  }

  public String getQuery() {
    return getQuery(SqlBuilderFactory.getBuilder());
  }

  public String getQuery(SqlBuilder builder) {
    return getQuery(builder, paramMode);
  }

  public abstract boolean isEmpty();

  public void setParamMode(boolean mode) {
    paramMode = mode;
  }

  abstract Map<Integer, Object> getParameters();

  abstract String getQuery(SqlBuilder builder, boolean paramMode);
}