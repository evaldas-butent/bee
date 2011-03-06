package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Grego;

import java.util.Date;

public class JustDate implements BeeSerializable, Comparable<JustDate> {
  private int day;
  private int[] fields = null;

  public JustDate() {
    this(System.currentTimeMillis());
  }

  public JustDate(Date date) {
    this(date.getTime());
  }
  
  public JustDate(int day) {
    this.day = day;
  }

  public JustDate(long time) {
    this(new DateTime(time));
  }

  public JustDate(DateTime dateTime) {
    this(dateTime.getYear(), dateTime.getMonth(), dateTime.getDom());
  }
  
  public JustDate(int year, int month, int dom) {
    this.day = Grego.fieldsToDay(year, month, dom);
  }
  
  public int compareTo(JustDate other) {
    int thisVal = getDay();
    int anotherVal = other.getDay();
    return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
  }

  public void deserialize(String s) {
    day = Integer.parseInt(s);
    fields = null;
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
  
  public int getMonth() {
    ensureFields();
    return fields[Grego.IDX_MONTH];
  }
  
  public int getYear() {
    ensureFields();
    return fields[Grego.IDX_YEAR];
  }

  public String serialize() {
    return Integer.toString(day);
  }

  public void setDay(int day) {
    this.day = day;
    this.fields = null;
  }

  @Override
  public String toString() {
    return BeeUtils.toLeadingZeroes(getYear(), 4) + "."
        + BeeUtils.toLeadingZeroes(getMonth(), 2) + "."
        + BeeUtils.toLeadingZeroes(getDom(), 2);
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
