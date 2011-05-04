package com.butent.bee.server.sql;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Builds an INSERT SQL statement for a given target using specified field and value lists.
 */

public class SqlInsert extends SqlQuery<SqlInsert> {

  private final IsFrom target;
  private Set<String> fieldList = Sets.newLinkedHashSet();
  private List<IsExpression> valueList;
  private SqlSelect dataSource;

  /**
   * Creates an SqlInserte statement with a specified target {@code target}. 
   * Target type is FromSingle.
   * 
   * @param target the FromSingle target
   */
  public SqlInsert(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  /**
   * Adds a constant value expression in a field for an SqlInsert statement.
   * @param field the field's name
   * @param value the field's value
   * @return object's SqlInsert instance.
   */
  public SqlInsert addConstant(String field, Object value) {
    if (value != null) {
      addExpression(field, SqlUtils.constant(value));
    }
    return getReference();
  }

  /**
   * Adds an expression for an SqlInsert statement.
   * @param field the field to add
   * @param value the expression to add
   * @return object's SqlInsert instance.
   */
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

  /**
   * Adds the specified fields {@code fields} to the field list.
   * 
   * @param fields the fields to add
   * @return object's SqlInsert instance.
   */
  public SqlInsert addFields(String... fields) {
    Assert.state(BeeUtils.isEmpty(valueList));

    for (String fld : fields) {
      addField(fld);
    }
    return getReference();
  }

  /**
   * @return the current {@code dataSource}.
   */
  public SqlSelect getDataSource() {
    return dataSource;
  }

  /**
   * Counts how many fields are in the field list {@code fieldList} and 
   * returns the amount.
   * 
   * @return the amount of fields
   */
  public int getFieldCount() {
    return fieldList.size();
  }

  /**
   * @return a list of field currently added to the field list {@code fieldList}.
   */
  public List<IsExpression> getFields() {
    List<IsExpression> fields = new ArrayList<IsExpression>();

    for (String field : fieldList) {
      fields.add(SqlUtils.name(field));
    }
    return fields;
  }

  /**
   * Returns a list of sources found in the {@code dataSource}. 
   * 
   * @returns a list of sources found in the {@code dataSource}.
   */
  @Override
  public Collection<String> getSources() {
    Collection<String> sources = target.getSources();

    if (!BeeUtils.isEmpty(dataSource)) {
      sources = SqlUtils.addCollection(sources, dataSource.getSources());
    }
    return sources;
  }

  /**
   * Returns a list of parameters found in the {@code dataSource} and 
   * {@code valueList} . For more  details see {@link com.butent.bee.shared.
   * sql.SqlSelect#getParams()}. 
   * 
   * @returns a list of parameters found in the {@code dataSource}.
   */
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

  /**
   * @param builder the builder to use
   * @param paramMode sets param mode on or off
   * @return a generated SqlInsert query with a specified SqlBuilder 
   * {@code builder} and parameter mode {@code paramMode}.
   */
  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getInsert(this, paramMode);
  }

  /**
   * @return the current target {@code target}
   */
  public IsFrom getTarget() {
    return target;
  }

  /**
   * @return the current value list.
   */
  public List<IsExpression> getValues() {
    return valueList;
  }

  /**
   * Checks if a field name {@code field} is already in a field list.
   * 
   * @param field teh field to check
   * @return true if the field exist in the list, otherwise false.
   */
  public boolean hasField(String field) {
    return fieldList.contains(field);
  }

  /**
   * Checks if the current instance of SqlInsert is empty.
   * 
   * @returns true if it is empty, otherwise false.
   */
  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(fieldList)
        || (BeeUtils.isEmpty(valueList) && BeeUtils.isEmpty(dataSource));
  }

  /**
   * Clears {@code fieldList}, {@code valueList} and {@code dataSource}.
   * 
   * @return object's SqlInsert instance.
   */
  public SqlInsert reset() {
    fieldList.clear();
    if (!BeeUtils.isEmpty(valueList)) {
      valueList.clear();
    }
    dataSource = null;

    return getReference();
  }

  /**
   * If there are no values in the {@code valueList} created sets the 
   * {@code dataSource} from an SqlSelect query {@code query}.
   * 
   * @param query the query to use for setting the dataSource
   * @return object's SqlInsert instance
   */
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
