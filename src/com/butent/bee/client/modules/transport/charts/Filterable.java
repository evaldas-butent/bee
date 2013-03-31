package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Maps;

import com.butent.bee.shared.utils.BeeUtils;

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
    if (filterType != null && filterResults.containsKey(filterType)) {
      return BeeUtils.unbox(filterResults.get(filterType));
    } else {
      return true;
    }
  }
  
  boolean persistFilter() {
    boolean match = matched(FilterType.TENTATIVE);
    setMatch(FilterType.PERSISTENT, match);
    
    return match;
  }

  boolean setMatch(FilterType filterType, boolean match) {
    if (matched(filterType) == match) {
      return false;
    } else {
      filterResults.put(filterType, match);
      return true;
    }
  }
}
