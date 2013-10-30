package com.butent.bee.shared.html.builder;

import com.butent.bee.shared.BeeConst;

public class Attribute {

  private final String name;
  private String value;

  public Attribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String build() {
    if (getValue() == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return " " + getName() + "=\"" + getValue() + "\"";
    }
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
    return build();
  }
}
