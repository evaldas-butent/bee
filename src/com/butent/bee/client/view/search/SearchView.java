package com.butent.bee.client.view.search;

import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.Collection;
import java.util.List;

public interface SearchView extends View, HasFilterHandler {
  
  void clearFilter();

  Filter getFilter(List<? extends IsColumn> columns, String idColumnName,
      String versionColumnName, Collection<String> excludeSearchers);
}
