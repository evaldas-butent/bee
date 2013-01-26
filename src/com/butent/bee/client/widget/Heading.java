package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;

import com.butent.bee.shared.utils.BeeUtils;

public class Heading extends CustomWidget implements HasText {

  public static final String ATTR_RANK = "rank";
  
  public Heading(int n) {
    super(Document.get().createHElement(n));
  }

  public Heading(int n, String text) {
    this(n);
    if (!BeeUtils.isEmpty(text)) {
      setText(text);
    }
  }

  @Override
  public String getIdPrefix() {
    return "h";
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(BeeUtils.trim(text));
  }
}
