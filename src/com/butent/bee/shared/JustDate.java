package com.butent.bee.shared;

import com.google.common.primitives.Ints;

import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Grego;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class JustDate extends AbstractDate implements BeeSerializable, Comparable<JustDate> {
  public static final char FIELD_SEPARATOR = '.';
  
  public static JustDate parse(String s) {
    Assert.notEmpty(s);
    if (BeeUtils.isDigit(s)) {
      return new JustDate(BeeUtils.toInt(s));
    }
    
    int[] arr = TimeUtils.parseFields(s);
    Assert.minLength(arr, 3);
    Assert.isTrue(Ints.max(arr) > 0);
    
    return new JustDate(arr[0], arr[1], arr[2]);
  }

  private int day;
  private int[] fields = null;

  public JustDate() {
    this(System.currentTimeMillis());
  }

  public JustDate(Date date) {
    this(date == null ? 0L : date.getTime());
  }
  
  public JustDate(DateTime dateTime) {
    if (dateTime == null) {
      setDay(0);
    } else {
      setDate(dateTime.getYear(), dateTime.getMonth(), dateTime.getDom());
    }
  }

  public JustDate(int day) {
    this.day = day;
  }

  public JustDate(int year, int month, int dom) {
    setDate(year, month, dom);
  }
  
  public JustDate(long time) {
    this(new DateTime(time));
  }
  
  public int compareTo(JustDate other) {
    if (other == null) {
      return BeeConst.COMPARE_MORE;
    }
    return Ints.compare(getDay(), other.getDay());
  }

  public void deserialize(String s) {
    day = Integer.parseInt(s);
    fields = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JustDate) {
      return getDay() == ((JustDate) obj).getDay();
    }
    return false;
  }

  @Override
  public JustDate getDate() {
    return this;
  }

  @Override
  public DateTime getDateTime() {
    return new DateTime(this);
  }

  public int getDay() {
    return day;
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
  
  @Override
  public Date getJava() {
    return new Date(new DateTime(this).getTime());
  }

  public int getMonth() {
    ensureFields();
    return fields[Grego.IDX_MONTH];
  }
  
  @Override
  public ValueType getType() {
    return ValueType.DATE;
  }

  public int getYear() {
    ensureFields();
    return fields[Grego.IDX_YEAR];
  }
  
  @Override
  public int hashCode() {
    return getDay();
  }
 
  public String serialize() {
    return Integer.toString(day);
  }

  public void setDate(int year, int month, int dom) {
    setDay(Grego.fieldsToDay(year, month, dom));
  }

  public void setDay(int day) {
    this.day = day;
    this.fields = null;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(10);
    sb.append(TimeUtils.yearToString(getYear())).append(FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getMonth())).append(FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getDom()));
    return sb.toString();
  }

  private void computeFields() {
    fields = Grego.dayToFields(day);
  }

  private void ensureFields() {
    if (fields == null) {
      computeFields();
    }
  }
}
