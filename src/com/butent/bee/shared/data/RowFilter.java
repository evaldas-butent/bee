package com.butent.bee.shared.data;

public interface RowFilter {
  boolean isMatch(IsTable<?, ?> table, IsRow row);
}
