package com.butent.bee.server.sql;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class SqlQuery<T> implements IsQuery {

  private boolean paramMode = false;

  @Override
  public boolean getParamMode() {
    return paramMode;
  }

  @Override
  public String getQuery() {
    return getQuery(SqlBuilderFactory.getBuilder());
  }

  @Override
  public String getQuery(SqlBuilder builder) {
    return getSqlString(builder, paramMode);
  }

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

  @Override
  public void setParamMode(boolean mode) {
    paramMode = mode;
  }

  protected abstract T getReference();
}