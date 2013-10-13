package com.butent.bee.shared.html.builder;

import com.google.common.base.Strings;

public class Text extends Node {
  
  private final String text;

  public Text(String text) {
    super();
    this.text = text;
  }

  @Override
  public String build(int indentStart, int indentStep) {
    return Strings.nullToEmpty(text);
  }

  public String getText() {
    return text;
  }
}
