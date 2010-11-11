package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SqlInsert extends SqlQuery<SqlInsert> {

  private final IsFrom target;
  private Set<String> fieldList = new LinkedHashSet<String>();
  private List<IsExpression> valueList;
  private SqlSelect valueQuery;

  public SqlInsert(String source) {
    target = new FromSingle(source);
  }

  public SqlInsert addField(String field, IsExpression value) {
    Assert.notNull(value);
    Assert.state(BeeUtils.isEmpty(valueQuery));

    addField(field);

    if (BeeUtils.isEmpty(valueList)) {
      valueList = new ArrayList<IsExpression>();
    }
    valueList.add(value);

    return getReference();
  }

  public SqlInsert addField(String field, Object value) {
    return addField(field, SqlUtils.constant(value));
  }

  public SqlInsert addFields(String... fields) {
    Assert.state(BeeUtils.isEmpty(valueList));

    for (String fld : fields) {
      addField(fld);
    }
    return getReference();
  }

  public List<IsExpression> getFields() {
    List<IsExpression> fields = new ArrayList<IsExpression>();

    for (String field : fieldList) {
      fields.add(SqlUtils.field(field));
    }
    return fields;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(valueQuery)) {
      SqlUtils.addParams(paramList, valueQuery.getSqlParams());

    } else if (!BeeUtils.isEmpty(valueList)) {

      for (IsExpression value : valueList) {
        SqlUtils.addParams(paramList, value.getSqlParams());
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getInsert(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  public SqlSelect getValueQuery() {
    return valueQuery;
  }

  public List<IsExpression> getValues() {
    return valueList;
  }

  public boolean hasField(String field) {
    return fieldList.contains(field);
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(fieldList)
        || (BeeUtils.isEmpty(valueList) && BeeUtils.isEmpty(valueQuery));
  }

  public SqlInsert setValueQuery(SqlSelect query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    Assert.state(BeeUtils.isEmpty(valueList));

    valueQuery = query;

    return getReference();
  }

  @Override
  protected SqlInsert getReference() {
    return this;
  }

  private void addField(String field) {
    Assert.notEmpty(field);
    Assert.state(!fieldList.contains(field),
        "Field " + field + " already exist");
    fieldList.add(field);
  }
}
