package com.butent.bee.client.widget;

/**
 * Enables using inline label user interface component.
 */

public class InlineLabel extends BeeLabel {

  public InlineLabel() {
    super(true);
  }

  public InlineLabel(HorizontalAlignmentConstant align) {
    this();
    setHorizontalAlignment(align);
  }

  public InlineLabel(String text, HorizontalAlignmentConstant align) {
    this(text);
    setHorizontalAlignment(align);
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
