package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

import com.butent.bee.client.utils.BeeCommand;

public class InlineLabel extends BeeLabel {

  public InlineLabel() {
    super(Document.get().createSpanElement());
  }

  public InlineLabel(HorizontalAlignmentConstant align) {
    super(align);
  }

  public InlineLabel(String text, BeeCommand cmnd) {
    super(text, cmnd);
  }

  public InlineLabel(String text, HorizontalAlignmentConstant align) {
    super(text, align);
  }

  public InlineLabel(String text) {
    super(text);
  }
}
