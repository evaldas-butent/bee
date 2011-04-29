package com.butent.bee.server.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class SqlUpdate extends HasFrom<SqlUpdate> {

  static final int FIELD = 0;
  static final int VALUE = 1;

  private final IsFrom target;
  private List<IsExpression[]> updates;
  private IsCondition whereClause;

  public SqlUpdate(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  public SqlUpdate(String target, String alias) {
    this.target = FromJoin.fromSingle(target, alias);
  }

  public SqlUpdate addConstant(String field, Object value) {
    return addExpression(field, SqlUtils.constant(value));
  }

  public SqlUpdate addExpression(String field, IsExpression value) {
    IsExpression[] updateEntry = new IsExpression[2];
    updateEntry[FIELD] = SqlUtils.name(field);
    updateEntry[VALUE] = value;

    if (BeeUtils.isEmpty(updates)) {
      updates = Lists.newArrayList();
    }
    updates.add(updateEntry);

    return getReference();
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

    List<Object> paramList = null;

    for (Object[] update : updates) {
      IsExpression val = (IsExpression) update[VALUE];
      paramList = (List<Object>) SqlUtils.addCollection(paramList, val.getSqlParams());
    }
    paramList = (List<Object>) SqlUtils.addCollection(paramList, super.getSqlParams());

    if (!BeeUtils.isEmpty(whereClause)) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, whereClause.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getUpdate(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  public List<IsExpression[]> getUpdates() {
    return updates;
  }

  public IsCondition getWhere() {
    return whereClause;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(updates);
  }

  public SqlUpdate reset() {
    if (!BeeUtils.isEmpty(updates)) {
      updates.clear();
    }
    whereClause = null;

    return getReference();
  }

  public SqlUpdate setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }

  @Override
  protected SqlUpdate getReference() {
    return this;
  }
}
