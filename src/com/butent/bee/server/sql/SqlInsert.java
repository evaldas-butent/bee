package com.butent.bee.server.sql;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SqlInsert extends SqlQuery<SqlInsert> {

  private final IsFrom target;
  private Set<String> fieldList = Sets.newLinkedHashSet();
  private List<IsExpression> valueList;
  private SqlSelect dataSource;

  public SqlInsert(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  public SqlInsert addConstant(String field, Object value) {
    if (value != null) {
      addExpression(field, SqlUtils.constant(value));
    }
    return getReference();
  }

  public SqlInsert addExpression(String field, IsExpression value) {
    Assert.notNull(value);
    Assert.state(BeeUtils.isEmpty(dataSource));

    addField(field);

    if (BeeUtils.isEmpty(valueList)) {
      valueList = Lists.newArrayList();
    }
    valueList.add(value);

    return getReference();
  }

  public SqlInsert addFields(String... fields) {
    Assert.state(BeeUtils.isEmpty(valueList));

    for (String fld : fields) {
      addField(fld);
    }
    return getReference();
  }

  public SqlSelect getDataSource() {
    return dataSource;
  }

  public int getFieldCount() {
    return fieldList.size();
  }

  public List<IsExpression> getFields() {
    List<IsExpression> fields = new ArrayList<IsExpression>();

    for (String field : fieldList) {
      fields.add(SqlUtils.name(field));
    }
    return fields;
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = target.getSources();

    if (!BeeUtils.isEmpty(dataSource)) {
      sources = SqlUtils.addCollection(sources, dataSource.getSources());
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(dataSource)) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, dataSource.getSqlParams());
    } else {
      for (IsExpression value : valueList) {
        paramList = (List<Object>) SqlUtils.addCollection(paramList, value.getSqlParams());
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

  public List<IsExpression> getValues() {
    return valueList;
  }

  public boolean hasField(String field) {
    return fieldList.contains(field);
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(fieldList)
        || (BeeUtils.isEmpty(valueList) && BeeUtils.isEmpty(dataSource));
  }

  public SqlInsert reset() {
    fieldList.clear();
    if (!BeeUtils.isEmpty(valueList)) {
      valueList.clear();
    }
    dataSource = null;

    return getReference();
  }

  public SqlInsert setDataSource(SqlSelect query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    Assert.state(BeeUtils.isEmpty(valueList));

    dataSource = query;

    return getReference();
  }

  @Override
  protected SqlInsert getReference() {
    return this;
  }

  private void addField(String field) {
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");
    fieldList.add(field);
  }
}
