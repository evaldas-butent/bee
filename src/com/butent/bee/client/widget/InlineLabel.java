package com.butent.bee.client.widget;

/**
 * Enables using inline label user interface component.
 */

public class InlineLabel extends Label {

  public InlineLabel() {
    super(true);
  }

  public InlineLabel(String text) {
    this();
    setText(text);
  }

  @Override
  public String getIdPrefix() {
    return "inline";
  }
}
