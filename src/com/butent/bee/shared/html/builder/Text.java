package com.butent.bee.shared.html.builder;

import com.google.common.base.Strings;

import com.butent.bee.shared.utils.BeeUtils;

public class Text extends Node {
  
  private String text;

  public Text(boolean text) {
    this(Boolean.toString(text));
  }

  public Text(double text) {
    this(BeeUtils.toString(text));
  }

  public Text(int text) {
    this(Integer.toString(text));
  }

  public Text(long text) {
    this(Long.toString(text));
  }

  public Text(String text) {
    super();
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String write() {
    return Strings.nullToEmpty(text);
  }
}
