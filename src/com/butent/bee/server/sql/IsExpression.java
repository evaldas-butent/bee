package com.butent.bee.server.sql;

/**
 * Extends IsSql interface and ensures that all implementing classes of this interface would have
 * <code>getValue</code> method.
 */

public interface IsExpression extends IsSql {

  Object getValue();
}
