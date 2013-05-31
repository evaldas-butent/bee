package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

public class CustomDiv extends CustomWidget {

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
}
