package com.butent.bee.server.sql;

/**
 * Extends {@code IsSql, HasSource} interfaces, sets necessary requirements for query classes.
 */

public interface IsQuery extends IsSql, HasSource {

  String getQuery();

  boolean isEmpty();
}
