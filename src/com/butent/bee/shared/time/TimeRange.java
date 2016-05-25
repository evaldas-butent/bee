package com.butent.bee.shared.time;

import com.google.common.collect.Range;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasRange;
import com.butent.bee.shared.utils.BeeUtils;

public final class TimeRange implements HasRange<Long>, BeeSerializable {

  private static final String SERIALIZATION_SEPARATOR = BeeConst.STRING_COMMA;

  public static TimeRange of(Long min, Long max) {
    if (isValid(min, max)) {
      return new TimeRange(Range.closedOpen(min, max));
    } else {
      return null;
    }
  }

  public static TimeRange of(Long min, Long max, Long duration) {
    if (isValid(min, max, duration)) {
      if (BeeUtils.isPositive(max)) {
        return new TimeRange(Range.closedOpen(min, max));
      } else {
        return new TimeRange(Range.closedOpen(min, min + duration));
      }
    } else {
      return null;
    }
  }

  public static TimeRange of(String min, String max) {
    return of(parse(min), parse(max));
  }

  public static TimeRange of(String min, String max, String duration) {
    return of(parse(min), parse(max), parse(duration));
  }

  public static boolean isValid(Long min, Long max) {
    return BeeUtils.isNonNegative(min) && BeeUtils.isLess(min, max);
  }

  public static boolean isValid(Long min, Long max, Long duration) {
    if (BeeUtils.isNonNegative(min)) {
      if (BeeUtils.isPositive(max)) {
        return BeeUtils.isLess(min, max);
      } else {
        return BeeUtils.isPositive(duration);
      }

    } else {
      return false;
    }
  }

  public static TimeRange restore(String s) {
    Range<Long> r = deserializeRange(s);
    return (r == null) ? null : new TimeRange(r);
  }

  private static Range<Long> deserializeRange(String s) {
    Long min = BeeUtils.toLongOrNull(BeeUtils.getPrefix(s, SERIALIZATION_SEPARATOR));
    Long max = BeeUtils.toLongOrNull(BeeUtils.getSuffix(s, SERIALIZATION_SEPARATOR));

    if (isValid(min, max)) {
      return Range.closedOpen(min, max);
    } else {
      return null;
    }
  }

  private static String format(long millis) {
    return TimeUtils.renderTime(millis, false);
  }

  private static Long parse(String time) {
    return TimeUtils.parseTime(time);
  }

  private Range<Long> range;

  private TimeRange(Range<Long> range) {
    this.range = range;
  }

  public boolean contains(Long millis) {
    return millis != null && range.contains(millis);
  }

  public boolean contains(String time) {
    return contains(parse(time));
  }

  @Override
  public void deserialize(String s) {
    Range<Long> r = deserializeRange(s);

    if (r != null) {
      this.range = r;
    }
  }

  public boolean encloses(TimeRange other) {
    return other != null && range.encloses(other.range);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TimeRange && range.equals(((TimeRange) obj).range);
  }

  public String getDuration() {
    return format(getDurationMillis());
  }

  public long getDurationMillis() {
    return getUpperMillis() - getLowerMillis();
  }

  public String getLower() {
    return format(getLowerMillis());
  }

  public long getLowerMillis() {
    return range.lowerEndpoint();
  }

  public String getUpper() {
    return format(getUpperMillis());
  }

  public long getUpperMillis() {
    return range.upperEndpoint();
  }

  @Override
  public Range<Long> getRange() {
    return range;
  }

  @Override
  public int hashCode() {
    return range.hashCode();
  }

  public TimeRange intersection(TimeRange other) {
    if (intersects(other)) {
      return new TimeRange(range.intersection(other.range));
    } else {
      return null;
    }
  }

  public boolean intersects(TimeRange other) {
    return other != null && BeeUtils.intersects(range, other.range);
  }

  public boolean isConnected(TimeRange other) {
    return other != null && range.isConnected(other.range);
  }

  @Override
  public String serialize() {
    return BeeUtils.join(SERIALIZATION_SEPARATOR, getLowerMillis(), getUpperMillis());
  }

  @Override
  public String toString() {
    return TimeUtils.renderPeriod(getLower(), getUpper());
  }
}
