package com.butent.bee.egg.shared.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class FromSingle implements FromSource {

  private Object source;
  private String alias;

  public FromSingle(QueryBuilder source, String alias) {
    Assert.notNull(source);
    Assert.state(!source.isEmpty(),
        "[Assertion failed] - QueryBuilder source must not be empty");
    Assert.notEmpty(alias);

    this.source = source;
    this.alias = alias;
  }

  public FromSingle(String source, String alias) {
    Assert.notEmpty(source);

    this.source = source;
    this.alias = alias;
  }

  public FromSingle(String source) {
    this(source, null);
  }

  @Override
  public Object getSource() {
    return source;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public String getJoinMode() {
    return "";
  }

  @Override
  public String getCondition(boolean queryMode) {
    StringBuilder from = new StringBuilder();

    if (source instanceof QueryBuilder) {
      from.append("(" + ((QueryBuilder) source).getQuery(queryMode) + ")");
    } else {
      from.append(SqlUtils.sqlQuote((String) source));
    }

    if (!BeeUtils.isEmpty(alias)) {
      from.append(" ").append(SqlUtils.sqlQuote(alias));
    }
    return from.toString();
  }

  @Override
  public List<Object> getQueryParameters() {
    List<Object> paramList = null;

    if (source instanceof QueryBuilder) {
      Map<Integer, Object> paramMap = ((QueryBuilder) source)
          .getParameters(true);

      if (!BeeUtils.isEmpty(paramMap)) {
        paramList = new ArrayList<Object>(paramMap.size());

        for (int i = 0; i < paramMap.size(); i++) {
          paramList.add(paramMap.get(i + 1));
        }
      }
    }
    return paramList;
  }
}
