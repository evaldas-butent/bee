package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlUpdate extends HasFrom<SqlUpdate> {

  static final int FIELD_INDEX = 0;
  static final int VALUE_INDEX = 1;

  private final IsFrom target;
  private List<IsExpression[]> updates;
  private IsCondition whereClause;

  public SqlUpdate(String target) {
    this.target = new FromSingle(target);
  }

  public SqlUpdate(String target, String alias) {
    this.target = new FromSingle(target, alias);
  }

  public SqlUpdate addConstant(String field, Object value) {
    return addExpression(field, SqlUtils.constant(value));
  }

  public SqlUpdate addExpression(String field, IsExpression value) {
    IsExpression[] updateEntry = new IsExpression[2];
    updateEntry[FIELD_INDEX] = SqlUtils.name(field);
    updateEntry[VALUE_INDEX] = value;

    if (BeeUtils.isEmpty(updates)) {
      updates = new ArrayList<IsExpression[]>();
    }
    updates.add(updateEntry);

    return getReference();
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    for (Object[] update : updates) {
      IsExpression val = (IsExpression) update[VALUE_INDEX];
      SqlUtils.addParams(paramList, val.getSqlParams());
    }
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

  public SqlUpdate setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }

  @Override
  protected SqlUpdate getReference() {
    return this;
  }
}
