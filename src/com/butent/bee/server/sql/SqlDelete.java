package com.butent.bee.server.sql;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;

import java.util.Collection;

/**
 * Builds a DELETE SQL statement for a given target using specified WHERE clause.
 */

public class SqlDelete extends SqlQuery<SqlDelete> implements HasTarget {

  private final String target;
  private IsCondition whereClause;

  /**
   * Creates an SqlDelete statement with a specified target {@code target}. Target type is
   * FromSingle.
   * 
   * @param target the FromSingle target
   */
  public SqlDelete(String target) {
    Assert.notEmpty(target);
    this.target = target;
  }

  /**
   * @return a list of sources found.
   */
  @Override
  public Collection<String> getSources() {
    Collection<String> sources = Sets.newHashSet(getTarget());

    if (whereClause != null) {
      sources = SqlUtils.addCollection(sources, whereClause.getSources());
    }
    return sources;
  }

  /**
   * @return the current target {@code target}
   */
  @Override
  public String getTarget() {
    return target;
  }

  /**
   * @return a Where clause.
   */
  public IsCondition getWhere() {
    return whereClause;
  }

  /**
   * Checks if an SqlDelete instance is empty. Checks the target and Where clause.
   * 
   * @return true if it is empty, otherwise false.
   */
  @Override
  public boolean isEmpty() {
    return whereClause == null;
  }

  @Override
  public SqlDelete reset() {
    whereClause = null;
    return getReference();
  }

  /**
   * Sets the Where clause from {@code clause}.
   * 
   * @param clause the clause to set
   * @return object's SqlDelete instance
   */
  public SqlDelete setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }
}
