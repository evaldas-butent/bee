package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

public class CustomDiv extends CustomWidget {

  public CustomDiv() {
    super(Document.get().createDivElement());
  }

  public CustomDiv(String style) {
    super(Document.get().createDivElement(), style);
  }

  @Override
  public String getIdPrefix() {
    return "div";
  }
}
