package com.butent.bee.shared.time;

import com.google.common.collect.Range;

public class DateRange implements HasDateRange {
  
  public static DateRange closed(JustDate min, JustDate max) {
    return new DateRange(Range.closed(min, max));
  }
  
  private final Range<JustDate> range;

  public DateRange(Range<JustDate> range) {
    this.range = range;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }
}
