package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasHTML;

public class CustomHasHtml extends CustomWidget implements HasHTML {

  public CustomHasHtml(Element element) {
    super(element);
  }

  public CustomHasHtml(Element element, String styleName) {
    super(element, styleName);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }
}
