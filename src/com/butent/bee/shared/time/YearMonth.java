package com.butent.bee.shared.time;

import com.google.common.primitives.Ints;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;

public class YearMonth implements Comparable<YearMonth>, HasDateTimeFormat,
    BeeSerializable, HasYearMonth {

  public static final char SEPARATOR = '-';

  public static YearMonth copyOf(YearMonth original) {
    if (original == null) {
      return null;
    } else {
      return new YearMonth(original.getYear(), original.getMonth(), original.getDateTimeFormat());
    }
  }

  public static YearMonth get(HasYearMonth ref) {
    if (ref == null) {
      return null;
    } else {
      return new YearMonth(ref);
    }
  }
  
  public static YearMonth parse(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }

    int[] arr = TimeUtils.parseFields(s);
    if (Ints.max(arr) <= 0) {
      return null;
    } else {
      return new YearMonth(TimeUtils.normalizeYear(arr[0]), arr[1]);
    }
  }

  private int year;

  private int month;

  private DateTimeFormat format;
  
  public YearMonth(HasYearMonth ref) {
    this(ref.getYear(), ref.getMonth());
  }

  public YearMonth(int year, int month) {
    this(year, month, null);
  }

  public YearMonth(int year, int month, DateTimeFormat format) {
    super();
    checkMonth(month);
    this.year = year;
    this.month = month;
    this.format = format;
  }
  
  public int compareTo(YearMonth other) {
    int result = BeeUtils.compare(getYear(), other.getYear());
    if (result == BeeConst.COMPARE_EQUAL) {
      result = BeeUtils.compare(getMonth(), other.getMonth());
    }
    return result;
  }

  public void deserialize(String s) {
    YearMonth ym = parse(s);
    Assert.notNull(ym);
    setYearMonth(ym);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      return year == ((YearMonth) obj).year && month == ((YearMonth) obj).month;
    }
  }

  public String format() {
    if (getDateTimeFormat() != null) {
      return getDateTimeFormat().format(getDate());
    } else {
      return toString();
    }
  }

  public JustDate getDate() {
    return new JustDate(getYear(), getMonth(), 1);
  }

  public DateTimeFormat getDateTimeFormat() {
    return format;
  }

  public JustDate getLast() {
    return new JustDate(getYear(), getMonth(), Grego.monthLength(getYear(), getMonth()));
  }

  public int getMonth() {
    return month;
  }

  public int getYear() {
    return year;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + month;
    result = prime * result + year;
    return result;
  }
  
  public YearMonth nextMonth() {
    return nextMonth(1);
  }

  public YearMonth nextMonth(int increment) {
    return copyOf(this).shiftMonth(increment);
  }

  public YearMonth nextYear() {
    return nextYear(1);
  }

  public YearMonth nextYear(int increment) {
    return copyOf(this).shiftYear(increment);
  }
  
  public YearMonth previousMonth() {
    return nextMonth(-1);
  }
  
  public YearMonth previousMonth(int increment) {
    return nextMonth(-increment);
  }

  public YearMonth previousYear() {
    return nextYear(-1);
  }

  public YearMonth previousYear(int increment) {
    return nextYear(-increment);
  }

  public String serialize() {
    return toString();
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }

  public void setMonth(int month) {
    checkMonth(month);
    this.month = month;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public void setYearMonth(int year, int month) {
    setYear(year);
    setMonth(month);
  }

  public void setYearMonth(YearMonth source) {
    Assert.notNull(source);
    setYearMonth(source.getYear(), source.getMonth());
  }
  
  public YearMonth shiftMonth(int deltaMonths) {
    increment(deltaMonths);
    return this;
  }

  public YearMonth shiftYear(int deltaYears) {
    setYear(getYear() + deltaYears);
    return this;
  }
  
  public YearMonth shiftYearMonth(int deltaYears, int deltaMonths) {
    return shiftYear(deltaYears).shiftMonth(deltaMonths);
  }

  @Override
  public String toString() {
    return TimeUtils.yearToString(getYear()) + SEPARATOR + TimeUtils.monthToString(getMonth());
  }

  private void checkMonth(int value) {
    Assert.betweenInclusive(value, 1, 12, "invalid month");
  }

  private void increment(int value) {
    if (value == 0) {
      return;
    }
    int z = getMonth() + value;

    if (z < 1) {
      setYear(getYear() + z / 12 - 1);
      setMonth(z % 12 + 12);
    } else if (z > 12) {
      setYear(getYear() + (z - 1) / 12);
      setMonth((z - 1) % 12 + 1);
    } else {
      setMonth(z);
    }
  }
}
