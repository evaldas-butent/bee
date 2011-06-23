package com.butent.bee.server.sql;

/**
 * Extends {@code IsCondition} interface, is the interface for complex conditions building classes.
 */

public interface HasConditions extends IsCondition {

  void add(IsCondition... conditions);
}
