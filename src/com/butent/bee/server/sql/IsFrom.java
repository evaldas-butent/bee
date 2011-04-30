package com.butent.bee.server.sql;

/**
 * Extends <code>IsSql, HasSource</code> interfaces, ensures that all implementing classes of this
 * interface would be able to return their source and alias information.
 */

public interface IsFrom extends IsSql, HasSource {

  String getAlias();

  Object getSource();
}
