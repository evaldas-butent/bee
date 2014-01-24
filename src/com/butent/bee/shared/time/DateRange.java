package com.butent.bee.shared.time;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import java.util.List;

public class DateRange implements HasDateRange {

  public static DateRange closed(JustDate min, JustDate max) {
    return new DateRange(Range.closed(min, max));
  }

  public static DateRange day(JustDate date) {
    return closed(date, date);
  }

  private final Range<JustDate> range;

  public DateRange(Range<JustDate> range) {
    this.range = range;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  public List<JustDate> getValues() {
    List<JustDate> values = Lists.newArrayList();

    if (range != null && range.hasLowerBound() && range.hasUpperBound() && !range.isEmpty()) {
      int min = range.lowerEndpoint().getDays();
      if (range.lowerBoundType() == BoundType.OPEN) {
        min++;
      }

      int max = range.upperEndpoint().getDays();
      if (range.upperBoundType() == BoundType.OPEN) {
        max--;
      }

      for (int days = min; days <= max; days++) {
        values.add(new JustDate(days));
      }
    }

    return values;
  }
}
