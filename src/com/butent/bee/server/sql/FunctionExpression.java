package com.butent.bee.server.sql;

import com.google.common.collect.Maps;

import com.butent.bee.server.sql.BeeConstants.Function;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Generates complex expressions for SQL statements depending on specific SQL server requirements.
 */

class FunctionExpression implements IsExpression {

  private final Function function;
  private final Map<String, Object> parameters;

  public FunctionExpression(Function function, Map<String, Object> parameters) {
    Assert.notEmpty(function);

    this.function = function;
    this.parameters = parameters;
  }

  public Function getFunction() {
    return function;
  }

  @Override
  public List<Object> getSqlParams() {
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    Map<String, Object> params = Maps.newHashMap();

    if (!BeeUtils.isEmpty(parameters)) {
      for (String prm : parameters.keySet()) {
        Object value = parameters.get(prm);

        if (value instanceof IsSql) {
          value = ((IsSql) value).getSqlString(builder, paramMode);
        }
        params.put(prm, value);
      }
    }
    return builder.sqlFunction(function, params);
  }

  @Override
  public Map<String, Object> getValue() {
    return parameters;
  }

}
