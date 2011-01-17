package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Grego;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class BeeDate implements BeeSerializable, Comparable<BeeDate> {
  private long time;
  private int[] fields = null;

  public BeeDate() {
    this(System.currentTimeMillis());
  }

  public BeeDate(long time) {
    this.time = time;
  }

  public BeeDate(String time) {
    this(Long.parseLong(time));
  }

  public BeeDate(Date date) {
    this(date.getTime());
  }
  
  public BeeDate(int year, int month, int dom) {
    this(year, month, dom, 0, 0, 0, 0);
  }

  public BeeDate(int year, int month, int dom, int hour, int minute, int second) {
    this(year, month, dom, hour, minute, second, 0);
  }
  
  public BeeDate(int year, int month, int dom, int hour, int minute, int second, int millis) {
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
    
    this.time = z + millis;
  }
  
  public int compareTo(BeeDate other) {
    long thisVal = getTime();
    long anotherVal = other.getTime();
    return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
  }

  public void deserialize(String s) {
    time = Long.parseLong(s);
    fields = null;
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
  
  public int getYear() {
    ensureFields();
    return fields[Grego.IDX_YEAR];
  }

  public String serialize() {
    return Long.toString(time);
  }

  public void setTime(long time) {
    this.time = time;
    this.fields = null;
  }

  public String toLog() {
    return BeeUtils.toLeadingZeroes(getHour(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getMinute(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getSecond(), 2) + "."
        + BeeUtils.toLeadingZeroes(getMillis(), 3);
  }

  @Override
  public String toString() {
    return BeeUtils.toLeadingZeroes(getYear(), 4) + "."
        + BeeUtils.toLeadingZeroes(getMonth(), 2) + "."
        + BeeUtils.toLeadingZeroes(getDom(), 2) + " "
        + BeeUtils.toLeadingZeroes(getHour(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getMinute(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getSecond(), 2);
  }
 
  private void computeFields() {
    fields = Grego.timeToFields(time);
  }

  private void ensureFields() {
    if (fields == null) {
      computeFields();
    }
  }
}
