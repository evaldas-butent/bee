package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

/**
 * Implements a user interface component that can contain arbitrary HTML and uses a span element,
 * causing it to be displayed with inline layout.
 */

public class InlineHtml extends Html {

  public InlineHtml() {
    super(Document.get().createSpanElement());
  }

  public InlineHtml(String html) {
    this();
    setHTML(html);
  }

  @Override
  public String getIdPrefix() {
    return "inline";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InlineHtml";
  }
}
