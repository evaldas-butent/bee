package com.butent.bee.shared.time;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public final class DateRange implements HasDateRange, BeeSerializable {

  private static final JustDate DEFAULT_MIN_DATE = new JustDate(1800, 1, 1);
  private static final JustDate DEFAULT_MAX_DATE = new JustDate(2999, 12, 31);

  public static DateRange all() {
    return new DateRange(Range.closed(DEFAULT_MIN_DATE, DEFAULT_MAX_DATE));
  }

  public static DateRange closed(JustDate min, JustDate max) {
    JustDate lower = (min == null) ? DEFAULT_MIN_DATE : min;
    JustDate upper = (max == null) ? DEFAULT_MAX_DATE : max;

    if (isValidClosedRange(lower, upper)) {
      return new DateRange(Range.closed(lower, upper));
    } else {
      return null;
    }
  }

  public static DateRange day(JustDate date) {
    if (date == null) {
      return null;
    } else {
      return closed(date, date);
    }
  }

  public static boolean isValidClosedRange(JustDate min, JustDate max) {
    return min != null && max != null && min.getDays() <= max.getDays();
  }

  public static DateRange restore(String s) {
    Range<JustDate> r = deserializeRange(s);
    return (r == null) ? null : new DateRange(r);
  }

  private static Range<JustDate> deserializeRange(String s) {
    String lower = BeeUtils.getPrefix(s, BeeConst.DEFAULT_LIST_SEPARATOR);
    String upper = BeeUtils.getSuffix(s, BeeConst.DEFAULT_LIST_SEPARATOR);

    if (BeeUtils.isInt(lower) && BeeUtils.isInt(upper)) {
      return Range.closed(new JustDate(BeeUtils.toInt(lower)), new JustDate(BeeUtils.toInt(upper)));
    } else {
      return null;
    }
  }

  private Range<JustDate> range;

  private DateRange(Range<JustDate> range) {
    this.range = range;
  }

  public boolean contains(JustDate date) {
    return date != null && range.contains(date);
  }

  @Override
  public void deserialize(String s) {
    Range<JustDate> r = deserializeRange(s);

    if (r != null) {
      this.range = r;
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DateRange && range.equals(((DateRange) obj).range);
  }

  public Filter getFilter(String colName) {
    JustDate minDate = getMinDate();
    JustDate maxDate = new JustDate(getMaxDays() + 1);

    return Filter.and(Filter.isMoreEqual(colName, new DateValue(minDate)),
        Filter.isLess(colName, new DateValue(maxDate)));
  }

  public JustDate getMaxDate() {
    return new JustDate(getMaxDays());
  }

  public int getMaxDays() {
    int max = range.upperEndpoint().getDays();
    if (range.upperBoundType() == BoundType.OPEN) {
      max--;
    }

    return max;
  }

  public JustDate getMinDate() {
    return new JustDate(getMinDays());
  }

  public int getMinDays() {
    int min = range.lowerEndpoint().getDays();
    if (range.lowerBoundType() == BoundType.OPEN) {
      min++;
    }

    return min;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  public List<JustDate> getValues() {
    List<JustDate> values = new ArrayList<>();

    int min = getMinDays();
    int max = getMaxDays();

    for (int days = min; days <= max; days++) {
      values.add(new JustDate(days));
    }

    return values;
  }

  @Override
  public int hashCode() {
    return range.hashCode();
  }

  public DateRange intersection(DateRange other) {
    if (intersects(other)) {
      return new DateRange(range.intersection(other.range));
    } else {
      return null;
    }
  }

  public boolean intersects(DateRange other) {
    return other != null && BeeUtils.intersects(range, other.range);
  }

  public boolean isConnected(DateRange other) {
    return other != null && range.isConnected(other.range);
  }

  @Override
  public String serialize() {
    return BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, getMinDays(), getMaxDays());
  }

  public int size() {
    return getMaxDays() - getMinDays() + 1;
  }

  public String toCompactString() {
    return toCompactString(false);
  }

  public String toCompactString(boolean dropCurrentCentury) {
    StringBuilder sb = new StringBuilder();

    sb.append((range.lowerBoundType() == BoundType.OPEN)
        ? BeeConst.STRING_LEFT_PARENTHESIS : BeeConst.STRING_LEFT_BRACKET);

    boolean dropCentury;
    if (dropCurrentCentury) {
      int century = TimeUtils.today().getCentury();
      dropCentury = range.lowerEndpoint().getCentury() == century
          && range.upperEndpoint().getCentury() == century;
    } else {
      dropCentury = false;
    }

    sb.append(TimeUtils.dateToString(range.lowerEndpoint(), dropCentury))
        .append(TimeUtils.PERIOD_SEPARATOR);

    if (range.lowerEndpoint().getYear() == range.upperEndpoint().getYear()) {
      if (range.lowerEndpoint().getMonth() == range.upperEndpoint().getMonth()) {
        sb.append(TimeUtils.dayOfMonthToString(range.upperEndpoint().getDom()));
      } else {
        sb.append(TimeUtils.monthToString(range.upperEndpoint().getMonth()))
            .append(TimeUtils.DATE_FIELD_SEPARATOR)
            .append(TimeUtils.dayOfMonthToString(range.upperEndpoint().getDom()));
      }
    } else {
      sb.append(TimeUtils.dateToString(range.upperEndpoint(), dropCentury));
    }

    sb.append((range.upperBoundType() == BoundType.OPEN)
        ? BeeConst.STRING_RIGHT_PARENTHESIS : BeeConst.STRING_RIGHT_BRACKET);

    return sb.toString();
  }

  @Override
  public String toString() {
    return range.toString();
  }
}
