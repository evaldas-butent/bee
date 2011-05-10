package com.butent.bee.shared.data;

import java.util.List;

/**
 * Extends {@code IsColumn} and {@code IsRow} interfaces, determines how to filter rows.
 */

public interface RowFilter {
  boolean isMatch(List<? extends IsColumn> columns, IsRow row);
}
