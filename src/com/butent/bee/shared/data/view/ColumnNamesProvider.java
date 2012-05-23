package com.butent.bee.shared.data.view;

import com.google.common.collect.ImmutableList;

public interface ColumnNamesProvider {
  ImmutableList<String> getColumnNames(String viewName);
}
