package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;

public class SubProp extends StringProp {
  public static String[] COLUMN_HEADERS = new String[]{
      "Name", "Sub", "Value", "Date"};
  public static int COLUMN_COUNT = COLUMN_HEADERS.length;

  private String sub;
  private BeeDate date = new BeeDate();

  public SubProp() {
    super();
  }

  public SubProp(String name) {
    super(name);
    this.sub = BeeConst.STRING_EMPTY;
  }

  public SubProp(String name, String value) {
    super(name, value);
    this.sub = BeeConst.STRING_EMPTY;
  }

  public SubProp(String name, String sub, String value) {
    super(name, value);
    this.sub = sub;
  }

  public SubProp(SubProp sp) {
    this(sp.getName(), sp.getSub(), sp.getValue());
  }

  public BeeDate getDate() {
    return date;
  }

  public String getSub() {
    return sub;
  }

  public void setDate(BeeDate date) {
    this.date = date;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_VALUE_SEPARATOR, BeeUtils.concat(
        BeeConst.DEFAULT_PROPERTY_SEPARATOR, getName(), getSub()),
        BeeUtils.transform(getValue()));
  }

  public String transform() {
    return toString();
  }

}
