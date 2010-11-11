package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class SqlDelete extends HasFrom<SqlDelete> {

  private final IsFrom target;
  private IsCondition whereClause;

  public SqlDelete(String source) {
    target = new FromSingle(source);
  }

  public SqlDelete(String source, String alias) {
    target = new FromSingle(source, alias);
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(getFrom())) {
      for (IsFrom from : getFrom()) {
        SqlUtils.addParams(paramList, from.getSqlParams());
      }
    }
    if (!BeeUtils.isEmpty(whereClause)) {
      SqlUtils.addParams(paramList, whereClause.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);

    return builder.getDelete(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  public IsCondition getWhere() {
    return whereClause;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(whereClause);
  }

  public SqlDelete setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }

  @Override
  protected SqlDelete getReference() {
    return this;
  }
}
