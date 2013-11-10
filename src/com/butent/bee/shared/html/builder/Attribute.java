package com.butent.bee.shared.html.builder;

import com.google.common.html.HtmlEscapers;

import com.butent.bee.shared.BeeConst;

public class Attribute {

  public static String quote(String v) {
    if (v == null) {
      return BeeConst.STRING_QUOT + BeeConst.STRING_QUOT;

    } else if (v.contains(BeeConst.STRING_QUOT)) {
      if (v.contains(BeeConst.STRING_APOS)) {
        return BeeConst.STRING_QUOT + HtmlEscapers.htmlEscaper().escape(v.trim())
            + BeeConst.STRING_QUOT;
      } else {
        return BeeConst.STRING_APOS + v.trim() + BeeConst.STRING_APOS;
      }

    } else {
      return BeeConst.STRING_QUOT + v.trim() + BeeConst.STRING_QUOT;
    }
  }

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
      return BeeConst.STRING_SPACE + getName() + BeeConst.STRING_EQ + quote(getValue());
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
