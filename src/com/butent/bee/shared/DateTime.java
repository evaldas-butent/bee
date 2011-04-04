package com.butent.bee.shared;

import com.google.common.primitives.Ints;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Grego;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class DateTime implements BeeSerializable, Comparable<DateTime> {
  public static final char DATE_FIELD_SEPARATOR = '.';

  public static final char DATE_TIME_SEPARATOR = ' ';
  public static final char TIME_FIELD_SEPARATOR = ':';
  public static final char MILLIS_SEPARATOR = '.';
  public static DateTime parse(String s) {
    Assert.notEmpty(s);
    if (BeeUtils.isDigit(s)) {
      return new DateTime(BeeUtils.toLong(s));
    }
    
    int[] arr = TimeUtils.parseFields(s);
    Assert.minLength(arr, 7);
    Assert.isTrue(Ints.max(arr) > 0);
    
    return new DateTime(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5], arr[6]);
  }
  
  private long time;
  private int[] fields = null;
  private int[] utcFields = null;

  public DateTime() {
    this(System.currentTimeMillis());
  }

  public DateTime(Date date) {
    this(date.getTime());
    @SuppressWarnings("deprecation")
    int z = getTimezoneOffset() - date.getTimezoneOffset();
    if (z != 0) {
      setTime(getTime() + z * TimeUtils.MILLIS_PER_MINUTE);
    }
  }

  public DateTime(int year, int month, int dom) {
    this(year, month, dom, 0, 0, 0, 0);
  }

  public DateTime(int year, int month, int dom, int hour, int minute, int second) {
    this(year, month, dom, hour, minute, second, 0);
  }

  public DateTime(int year, int month, int dom, int hour, int minute, int second, int millis) {
    long z = Grego.fieldsToDay(year, month, dom);
    z *= TimeUtils.MILLIS_PER_DAY;

    if (hour != 0) {
      z += hour * TimeUtils.MILLIS_PER_HOUR;
    }
    if (minute != 0) {
      z += minute * TimeUtils.MILLIS_PER_MINUTE;
    }
    if (second != 0) {
      z += second * TimeUtils.MILLIS_PER_SECOND;
    }
    
    setLocalTime(z + millis);
  }

  public DateTime(JustDate date) {
    this(date.getYear(), date.getMonth(), date.getDom());
  }

  public DateTime(long time) {
    this.time = time;
  }

  public int compareTo(DateTime other) {
    long thisVal = getTime();
    long anotherVal = other.getTime();
    return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
  }

  public void deserialize(String s) {
    setTime(Long.parseLong(s));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DateTime) {
      return getTime() == ((DateTime) obj).getTime();
    }
    return false;
  }

  public int getDom() {
    ensureFields();
    return fields[Grego.IDX_DOM];
  }

  public int getDow() {
    ensureFields();
    return fields[Grego.IDX_DOW];
  }

  public int getDoy() {
    ensureFields();
    return fields[Grego.IDX_DOY];
  }

  public int getHour() {
    ensureFields();
    return fields[Grego.IDX_HOUR];
  }

  public int getMillis() {
    ensureFields();
    return fields[Grego.IDX_MILLIS];
  }

  public int getMinute() {
    ensureFields();
    return fields[Grego.IDX_MINUTE];
  }

  public int getMonth() {
    ensureFields();
    return fields[Grego.IDX_MONTH];
  }

  public int getSecond() {
    ensureFields();
    return fields[Grego.IDX_SECOND];
  }

  public long getTime() {
    return time;
  }

  @SuppressWarnings("deprecation")
  public int getTimezoneOffset() {
    return new Date(getTime()).getTimezoneOffset();
  }

  public int getUtcDom() {
    ensureUtcFields();
    return utcFields[Grego.IDX_DOM];
  }

  public int getUtcDow() {
    ensureUtcFields();
    return utcFields[Grego.IDX_DOW];
  }

  public int getUtcDoy() {
    ensureUtcFields();
    return utcFields[Grego.IDX_DOY];
  }

  public int getUtcHour() {
    ensureUtcFields();
    return utcFields[Grego.IDX_HOUR];
  }

  public int getUtcMillis() {
    ensureUtcFields();
    return utcFields[Grego.IDX_MILLIS];
  }

  public int getUtcMinute() {
    ensureUtcFields();
    return utcFields[Grego.IDX_MINUTE];
  }

  public int getUtcMonth() {
    ensureUtcFields();
    return utcFields[Grego.IDX_MONTH];
  }

  public int getUtcSecond() {
    ensureUtcFields();
    return utcFields[Grego.IDX_SECOND];
  }

  public int getUtcYear() {
    ensureUtcFields();
    return utcFields[Grego.IDX_YEAR];
  }

  public int getYear() {
    ensureFields();
    return fields[Grego.IDX_YEAR];
  }

  @Override
  public int hashCode() {
    return Long.valueOf(getTime()).hashCode();
  }
  
  public String serialize() {
    return Long.toString(time);
  }

  public void setTime(long time) {
    this.time = time;
    resetComputedFields();
  }

  public String toDateString() {
    StringBuilder sb = new StringBuilder(10);
    sb.append(TimeUtils.yearToString(getYear())).append(DATE_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getMonth())).append(DATE_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getDom()));
    return sb.toString();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(23);
    sb.append(toDateString()).append(DATE_TIME_SEPARATOR).append(toTimeString());
    return sb.toString();
  }

  public String toTimeString() {
    StringBuilder sb = new StringBuilder(12);
    sb.append(TimeUtils.padTwo(getHour())).append(TIME_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getMinute())).append(TIME_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getSecond()));
    int z = getMillis();
    if (z != 0) {
      sb.append(MILLIS_SEPARATOR).append(TimeUtils.millisToString(z));
    }
    return sb.toString();
  }

  public String toUtcDateString() {
    StringBuilder sb = new StringBuilder(10);
    sb.append(TimeUtils.yearToString(getUtcYear())).append(DATE_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getUtcMonth())).append(DATE_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getUtcDom()));
    return sb.toString();
  }
  
  public String toUtcString() {
    StringBuilder sb = new StringBuilder(23);
    sb.append(toUtcDateString()).append(DATE_TIME_SEPARATOR).append(toUtcTimeString());
    return sb.toString();
  }

  public String toUtcTimeString() {
    StringBuilder sb = new StringBuilder(12);
    sb.append(TimeUtils.padTwo(getUtcHour())).append(TIME_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getUtcMinute())).append(TIME_FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getUtcSecond()));
    int z = getUtcMillis();
    if (z != 0) {
      sb.append(MILLIS_SEPARATOR).append(TimeUtils.millisToString(z));
    }
    return sb.toString();
  }
  
  private void computeFields() {
    fields = Grego.timeToFields(time - getTimezoneOffsetInMillis());
  }

  private void computeUtcFields() {
    utcFields = Grego.timeToFields(time);
  }

  private void ensureFields() {
    if (fields == null) {
      computeFields();
    }
  }

  private void ensureUtcFields() {
    if (utcFields == null) {
      computeUtcFields();
    }
  }

  private int getTimezoneOffsetInMillis() {
    return getTimezoneOffset() * TimeUtils.MILLIS_PER_MINUTE;
  }
  
  private void resetComputedFields() {
    fields = null;
    utcFields = null;
  }

  private void setLocalTime(long localTime) {
    setTime(localTime);
    int tzo = getTimezoneOffsetInMillis();
    if (tzo == 0) {
      return;
    }

    int diff = new DateTime(localTime + tzo).getTimezoneOffsetInMillis() - tzo;
    setTime(localTime + tzo + diff);
  }
}
