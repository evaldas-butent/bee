package com.butent.bee.shared.data;

import java.util.List;

@FunctionalInterface
public interface RowFilter {
  boolean isMatch(List<? extends IsColumn> columns, IsRow row);
}
