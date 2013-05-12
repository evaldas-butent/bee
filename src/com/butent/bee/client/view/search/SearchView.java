package com.butent.bee.client.view.search;

import com.google.common.collect.ImmutableSet;

import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public interface SearchView extends View, HasFilterHandler {
  Filter getFilter(List<? extends IsColumn> columns, String idColumnName,
      String versionColumnName, ImmutableSet<String> excludeSearchers);
}
