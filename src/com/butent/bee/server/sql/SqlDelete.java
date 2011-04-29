package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class SqlDelete extends HasFrom<SqlDelete> {

  private final IsFrom target;
  private IsCondition whereClause;

  public SqlDelete(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  public SqlDelete(String target, String alias) {
    this.target = FromJoin.fromSingle(target, alias);
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = SqlUtils.addCollection(target.getSources(), super.getSources());

    if (!BeeUtils.isEmpty(whereClause)) {
      sources = SqlUtils.addCollection(sources, whereClause.getSources());
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());
    List<Object> paramList = super.getSqlParams();

    if (!BeeUtils.isEmpty(whereClause)) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, whereClause.getSqlParams());
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
