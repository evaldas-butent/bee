package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

public class Legend extends CustomHasHtml {

  public Legend() {
    super(Document.get().createLegendElement());
  }

  public Legend(String text) {
    this();
    setText(text);
  }
  
  @Override
  public String getIdPrefix() {
    return "legend";
  }
  
  @Override
  protected void init() {
    super.init();
    addStyleName("bee-Legend");
  }
}
