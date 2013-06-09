package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

public class CustomSpan extends CustomHasHtml {

  public CustomSpan() {
    super(Document.get().createSpanElement());
  }

  public CustomSpan(String styleName) {
    super(Document.get().createSpanElement(), styleName);
  }

  @Override
  public String getIdPrefix() {
    return "span";
  }
}
