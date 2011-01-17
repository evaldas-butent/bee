package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class FromSingle implements IsFrom {

  private Object source;
  private String alias;

  public FromSingle(String source) {
    this(source, null);
  }

  public FromSingle(String source, String alias) {
    Assert.notEmpty(source);

    this.source = source;
    this.alias = alias;
  }

  public FromSingle(SqlSelect source, String alias) {
    Assert.notNull(source);
    Assert.state(!source.isEmpty());
    Assert.notEmpty(alias);

    this.source = source;
    this.alias = alias;
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
  public Object getSource() {
    return source;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    if (source instanceof SqlSelect) {
      paramList = ((SqlSelect) source).getSqlParams();
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder from = new StringBuilder();

    if (source instanceof SqlSelect) {
      from.append("(").append(((SqlSelect) source).getSqlString(builder, paramMode)).append(")");
    } else {
      from.append(SqlUtils.name((String) source).getSqlString(builder, paramMode));
    }

    if (!BeeUtils.isEmpty(alias)) {
      from.append(" ").append(SqlUtils.name(alias).getSqlString(builder, paramMode));
    }
    return from.toString();
  }
}
