package com.butent.bee.shared.html.builder;

import com.butent.bee.shared.BeeConst;

public class Style {

  private final String name;
  private String value;

  public Style(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return write();
  }

  public String write() {
    if (getValue() == null) {
      return BeeConst.STRING_EMPTY; 
    } else {
      return getName() + ": \"" + getValue() + "\";";
    }
  }
}
