package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FromSingle implements FromSource {

  private Object source;
  private String alias;

  public FromSingle(SqlSelect source, String alias) {
    Assert.notNull(source);
    Assert.state(!source.isEmpty(),
        "[Assertion failed] - QueryBuilder source must not be empty");
    Assert.notEmpty(alias);

    this.source = source;
    this.alias = alias;
  }

  public FromSingle(String source) {
    this(source, null);
  }

  public FromSingle(String source, String alias) {
    Assert.notEmpty(source);

    this.source = source;
    this.alias = alias;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public String getFrom(SqlBuilder builder, boolean paramMode) {
    StringBuilder from = new StringBuilder();

    if (source instanceof SqlSelect) {
      from.append("(" + ((SqlSelect) source).getQuery(builder, paramMode) + ")");
    } else {
      from.append(SqlUtils.sqlQuote((String) source));
    }

    if (!BeeUtils.isEmpty(alias)) {
      from.append(" ").append(SqlUtils.sqlQuote(alias));
    }
    return from.toString();
  }

  @Override
  public String getJoinMode() {
    return "";
  }

  @Override
  public List<Object> getParameters() {
    List<Object> paramList = null;

    if (source instanceof SqlSelect) {
      Map<Integer, Object> paramMap = ((SqlSelect) source).getParameters();

      if (!BeeUtils.isEmpty(paramMap)) {
        paramList = new ArrayList<Object>(paramMap.size());

        for (int i = 0; i < paramMap.size(); i++) {
          paramList.add(paramMap.get(i + 1));
        }
      }
    }
    return paramList;
  }

  @Override
  public Object getSource() {
    return source;
  }
}
