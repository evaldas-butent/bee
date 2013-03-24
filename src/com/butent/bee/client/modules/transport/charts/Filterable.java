package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.EnumMap;

abstract class Filterable {
  
  private final EnumMap<Filterable.FilterType, Boolean> filterResults =
      Maps.newEnumMap(Filterable.FilterType.class);

  enum FilterType {
    TENTATIVE, PERSISTENT
  }
  
  abstract boolean filter(FilterType filterType, Collection<ChartData> data);
  
  boolean matched(FilterType filterType) {
    return FilterHelper.matched(filterResults, filterType);
  }

  void setMatch(FilterType filterType, boolean match) {
    filterResults.put(filterType, match);
  }
}
