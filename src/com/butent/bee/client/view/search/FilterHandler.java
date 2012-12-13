package com.butent.bee.client.view.search;

import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.data.filter.Filter;

import java.util.Collection;

public interface FilterHandler {
  
  Filter getEffectiveFilter(Collection<String> exclusions);
  
  void onFilterChange(Procedure<Boolean> callback);
}
