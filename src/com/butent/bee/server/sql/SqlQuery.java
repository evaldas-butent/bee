package com.butent.bee.server.sql;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Is an abstract class for SQL queries forming classes and indicates to use SQL builder classes.
 * 
 * @param <T> used for reference getting.
 */

abstract class SqlQuery<T> implements IsQuery {

  private boolean paramMode = false;

  /**
   * @return the currently set parameter mode {@code paramMode}
   */
  @Override
  public boolean getParamMode() {
    return paramMode;
  }

  /**
   * @return a query using a currently set builder and parameter mode.
   */
  @Override
  public String getQuery() {
    return getQuery(SqlBuilderFactory.getBuilder());
  }

  /**
   * @return a query using the specified builder {@code builder} and parameter mode
   */
  @Override
  public String getQuery(SqlBuilder builder) {
    return getSqlString(builder, paramMode);
  }

  /**
   * Returns a map with parameters listed from the beginning to the end. E.g {1 = "table1", 2 =
   * '5'}.
   * 
   * @return a parameter map.
   */
  @Override
  public Map<Integer, Object> getQueryParams() {
    Map<Integer, Object> paramMap = null;
    List<Object> paramList = getSqlParams();

    if (!BeeUtils.isEmpty(paramList)) {
      paramMap = new HashMap<Integer, Object>(paramList.size());

      for (int i = 0; i < paramList.size(); i++) {
        paramMap.put(i + 1, paramList.get(i));
      }
    }
    return paramMap;
  }

  /**
   * Sets the parameter mode {@code paramMode} to {@code mode}.
   */
  @Override
  public void setParamMode(boolean mode) {
    paramMode = mode;
  }

  protected abstract T getReference();
}