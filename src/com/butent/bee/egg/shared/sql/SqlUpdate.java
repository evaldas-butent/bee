package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlUpdate extends HasFrom<SqlUpdate> {

  static final int FIELD_INDEX = 0;
  static final int VALUE_INDEX = 1;

  private final IsFrom target;
  private List<IsExpression[]> fieldList;
  private IsCondition whereClause;

  public SqlUpdate(String source) {
    target = new FromSingle(source);
  }

  public SqlUpdate(String source, String alias) {
    target = new FromSingle(source, alias);
  }

  public SqlUpdate addField(String field, Object value) {
    return addField(field, SqlUtils.constant(value));
  }

  public SqlUpdate addField(String field, IsExpression value) {
    IsExpression[] fieldEntry = new IsExpression[2];
    fieldEntry[FIELD_INDEX] = SqlUtils.field(
        BeeUtils.ifString(target.getAlias(), (String) target.getSource()),
        field);
    fieldEntry[VALUE_INDEX] = value;

    if (BeeUtils.isEmpty(fieldList)) {
      fieldList = new ArrayList<IsExpression[]>();
    }
    fieldList.add(fieldEntry);

    return getReference();
  }

  public List<IsExpression[]> getFields() {
    return fieldList;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(fieldList)) {
      for (Object[] field : fieldList) {
        IsExpression val = (IsExpression) field[VALUE_INDEX];
        SqlUtils.addParams(paramList, val.getSqlParams());
      }
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

  public IsCondition getWhere() {
    return whereClause;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(fieldList);
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
