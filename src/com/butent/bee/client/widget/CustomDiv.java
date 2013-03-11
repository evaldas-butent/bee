package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;

public class CustomDiv extends CustomWidget implements HasText {

  public CustomDiv() {
    super(Document.get().createDivElement());
  }

  public CustomDiv(String styleName) {
    super(Document.get().createDivElement(), styleName);
  }

  @Override
  public String getIdPrefix() {
    return "div";
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }
}
