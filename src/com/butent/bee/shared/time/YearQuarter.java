package com.butent.bee.shared.time;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;

public final class YearQuarter implements Comparable<YearQuarter>, BeeSerializable {

  public static final char SEPARATOR = ' ';

  public static YearQuarter copyOf(YearQuarter original) {
    if (original == null) {
      return null;
    } else {
      return new YearQuarter(original.getYear(), original.getQuarter());
    }
  }

  public static YearQuarter of(HasYearMonth ref) {
    if (ref == null) {
      return null;
    } else {
      return new YearQuarter(ref.getYear(), ref.getQuarter());
    }
  }

  public static YearQuarter of(Integer year, Integer quarter) {
    if (TimeUtils.isYear(year) && TimeUtils.isQuarter(quarter)) {
      return new YearQuarter(year, quarter);
    } else {
      return null;
    }
  }

  public static YearQuarter parse(String s) {
    String ys = BeeUtils.getPrefix(s, SEPARATOR);
    String qs = BeeUtils.getSuffix(s, SEPARATOR);

    return parse(ys, qs);
  }

  public static YearQuarter parse(String ys, String qs) {
    Integer y = TimeUtils.parseYear(ys);
    Integer q = TimeUtils.parseQuarter(qs);

    if (TimeUtils.isYear(y) && TimeUtils.isQuarter(q)) {
      return new YearQuarter(y, q);
    } else {
      return null;
    }
  }

  private int year;
  private int quarter;

  private YearQuarter(int year, int quarter) {
    this.year = year;
    this.quarter = quarter;
  }

  @Override
  public int compareTo(YearQuarter other) {
    int result = BeeUtils.compareNullsFirst(getYear(), other.getYear());
    if (result == BeeConst.COMPARE_EQUAL) {
      result = BeeUtils.compareNullsFirst(getQuarter(), other.getQuarter());
    }
    return result;
  }

  @Override
  public void deserialize(String s) {
    YearQuarter ym = parse(s);
    Assert.notNull(ym);
    setYearQuarter(ym);
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
      return year == ((YearQuarter) obj).year && quarter == ((YearQuarter) obj).quarter;
    }
  }

  public JustDate getFirstDate() {
    return new JustDate(getYear(), getFirstMonth(), 1);
  }

  public int getFirstMonth() {
    return (getQuarter() - 1) * 3 + 1;
  }

  public JustDate getLastDate() {
    int lastMonth = getLastMonth();
    return new JustDate(getYear(), lastMonth, Grego.monthLength(getYear(), lastMonth));
  }

  public int getLastMonth() {
    return getQuarter() * 3;
  }

  public int getQuarter() {
    return quarter;
  }

  public DateRange getRange() {
    return DateRange.closed(getFirstDate(), getLastDate());
  }

  public int getYear() {
    return year;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + quarter;
    result = prime * result + year;
    return result;
  }

  public YearQuarter nextQuarter() {
    return nextQuarter(1);
  }

  public YearQuarter nextQuarter(int increment) {
    return copyOf(this).shiftQuarter(increment);
  }

  public YearQuarter nextYear() {
    return nextYear(1);
  }

  public YearQuarter nextYear(int increment) {
    return copyOf(this).shiftYear(increment);
  }

  public YearQuarter previousQuarter() {
    return nextQuarter(-1);
  }

  public YearQuarter previousQuarter(int increment) {
    return nextQuarter(-increment);
  }

  public YearQuarter previousYear() {
    return nextYear(-1);
  }

  public YearQuarter previousYear(int increment) {
    return nextYear(-increment);
  }

  @Override
  public String serialize() {
    return toString();
  }

  public void setQuarter(int quarter) {
    checkQuarter(quarter);
    this.quarter = quarter;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public void setYearQuarter(int y, int m) {
    setYear(y);
    setQuarter(m);
  }

  public void setYearQuarter(YearQuarter source) {
    Assert.notNull(source);
    setYearQuarter(source.getYear(), source.getQuarter());
  }

  public YearQuarter shiftQuarter(int deltaQuarters) {
    increment(deltaQuarters);
    return this;
  }

  public YearQuarter shiftYear(int deltaYears) {
    setYear(getYear() + deltaYears);
    return this;
  }

  public YearQuarter shiftYearQuarter(int deltaYears, int deltaQuarters) {
    return shiftYear(deltaYears).shiftQuarter(deltaQuarters);
  }

  @Override
  public String toString() {
    return TimeUtils.yearToString(getYear()) + SEPARATOR + TimeUtils.quarterToString(getQuarter());
  }

  private static void checkQuarter(int value) {
    Assert.betweenInclusive(value, 1, 4, "invalid quarter");
  }

  private void increment(int value) {
    if (value == 0) {
      return;
    }
    int z = getQuarter() + value;

    if (z < 1) {
      setYear(getYear() + z / 4 - 1);
      setQuarter(z % 4 + 4);
    } else if (z > 4) {
      setYear(getYear() + (z - 1) / 4);
      setQuarter((z - 1) % 4 + 1);
    } else {
      setQuarter(z);
    }
  }
}
