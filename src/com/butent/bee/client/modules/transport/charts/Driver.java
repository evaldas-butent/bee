package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class Driver implements HasDateRange, HasItemName {
  
  static String getNames(String separator, Collection<Driver> drivers) {
    if (BeeUtils.isEmpty(drivers)) {
      return null;
    }
    
    String sep = BeeUtils.notEmpty(separator, BeeConst.DEFAULT_LIST_SEPARATOR);
    StringBuilder sb = new StringBuilder();

    for (Driver driver : drivers) {
      if (driver != null) {
        if (sb.length() > 0) {
          sb.append(sep);
        }
        sb.append(driver.getItemName());
      }
    }
    return sb.toString();
  }

  private final Long id;
  private final String itemName;

  private final Range<JustDate> range;

  Driver(Long driverId, String firstName, String lastName, JustDate startDate, JustDate endDate) {
    this.id = driverId;
    this.itemName = BeeUtils.joinWords(firstName, lastName);

    this.range = ChartHelper.getActivity(startDate, endDate);
  }

  @Override
  public String getItemName() {
    return itemName;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  Long getId() {
    return id;
  }
}
