package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.HasHtml;

public class CustomHasHtml extends CustomWidget implements HasHtml {

  public CustomHasHtml(Element element) {
    super(element);
  }

  public CustomHasHtml(Element element, String styleName) {
    super(element, styleName);
  }

  @Override
  public String getHtml() {
    return getElement().getInnerHTML();
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setHtml(String html) {
    getElement().setInnerHTML(html);
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }
}
