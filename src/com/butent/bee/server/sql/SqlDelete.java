package com.butent.bee.server.sql;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

/**
 * Builds a DELETE SQL statement for a given target using specified WHERE clause.
 */

public class SqlDelete extends HasFrom<SqlDelete> {

  private final IsFrom target;
  private IsCondition whereClause;

  /**
   * Creates an SqlDelete statement with a specified target {@code target}. Target type is
   * FromSingle.
   * 
   * @param target the FromSingle target
   */
  public SqlDelete(String target) {
    this.target = FromJoin.fromSingle(target, null);
  }

  /**
   * @return a list of sources found.
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
   * @return the current target {@code target}
   */
  public IsFrom getTarget() {
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
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(whereClause);
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
