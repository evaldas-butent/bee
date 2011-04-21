package com.butent.bee.shared.data;

import java.util.List;

public interface RowFilter {
  boolean isMatch(List<? extends IsColumn> columns, IsRow row);
}
