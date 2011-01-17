package com.butent.bee.shared.utils;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Transformable;

public class Property implements Comparable<Property>, Transformable {
  public static String[] HEADERS = new String[]{"Property", "Value"};
  public static int HEADER_COUNT = HEADERS.length;

  private String name;
  private String value;

  public Property(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public int compareTo(Property oth) {
    if (name == null) {
      if (oth.name == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (name == null) {
      return 1;
    } else {
      return name.compareTo(oth.name);
    }
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return name + BeeConst.DEFAULT_VALUE_SEPARATOR + value;
  }

  public String transform() {
    return toString();
  }
}
