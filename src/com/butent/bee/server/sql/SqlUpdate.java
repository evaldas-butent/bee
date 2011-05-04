package com.butent.bee.server.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Generates UPDATE SQL statements for specified target, update values and WHERE 
 * condition.
 */

public class SqlUpdate extends HasFrom<SqlUpdate> {

  static final int FIELD = 0;
  static final int VALUE = 1;

  private final IsFrom target;
  private List<IsExpression[]> updates;
  private IsCondition whereClause;

  /**
   * Creates an SqlUpdate statement with a specified target {@code target}. 
   * Target type is FromSingle.
   * 
   * @param target the FromSingle target
   */
  public SqlUpdate(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  /**
   * Creates an SqlUpdate statement with a specified target {@code target} and
   * alias {@code alias}. Target type is FromSingle.
   * 
   * @param target the target
   * @param alias the alias to use
   */
  public SqlUpdate(String target, String alias) {
    this.target = FromJoin.fromSingle(target, alias);
  }

  /**
   * Adds a constant value expression in a field for an SqlUpdate statement.
   * @param field the field's name
   * @param value the field's value
   * @return object's SqlInsert instance.
   */
  public SqlUpdate addConstant(String field, Object value) {
    return addExpression(field, SqlUtils.constant(value));
  }

  /**
   * Adds an expression for an SqlUpdate statement.
   * @param field the field to add
   * @param value the expression to add
   * @return object's SqlInsert instance.
   */
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

  /**
   *  @return a list of sources found in the target and Where clause. 
   */
  @Override
  public Collection<String> getSources() {
    Collection<String> sources = SqlUtils.addCollection(target.getSources(), super.getSources());

    if (!BeeUtils.isEmpty(whereClause)) {
      sources = SqlUtils.addCollection(sources, whereClause.getSources());
    }
    return sources;
  }

  /**
   * Returns a list of parameters found in the {@code updates}, 
   * {@code whereClause} and From list .  
   * 
   * @returns a list of parameters
   */
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

  /**
   * @param builder the builder to use
   * @param paramMode sets param mode on or off
   * @return a generated SqlUpdate query with a specified SqlBuilder 
   * {@code builder} and parameter mode {@code paramMode}.
   */
  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getUpdate(this, paramMode);
  }

  /**
   * @return the current target {@code target}
   */
  public IsFrom getTarget() {
    return target;
  }

  /**
   * @return the current update {@code updates} list.
   */
  public List<IsExpression[]> getUpdates() {
    return updates;
  }

  /**
   * @return a Where clause.
   */
  public IsCondition getWhere() {
    return whereClause;
  }

  /**
   * Checks if the current instance of SqlUpdate is empty. Checks if the target
   * and {@code updates} list are empty.
   * 
   * @returns true if it is empty, otherwise false.
   */
  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(updates);
  }

  /**
   * Clears the update {@code updates} list and the Where clause.
   * 
   * @return object's SqlUpdate instance
   */
  public SqlUpdate reset() {
    if (!BeeUtils.isEmpty(updates)) {
      updates.clear();
    }
    whereClause = null;

    return getReference();
  }

  /**
   * Sets the Where clause to the specified clause {@code clause}.
   * @param clause a clause to set Where to.
   * 
   * @return object's SqlUpdate instance
   */
  public SqlUpdate setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }

  @Override
  protected SqlUpdate getReference() {
    return this;
  }
}
