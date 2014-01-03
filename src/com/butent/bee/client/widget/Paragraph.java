package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

public class Paragraph extends CustomHasHtml {

  public Paragraph() {
    super(Document.get().createPElement());
  }

  public Paragraph(String styleName) {
    super(Document.get().createPElement(), styleName);
  }

  @Override
  public String getIdPrefix() {
    return "p";
  }
}
