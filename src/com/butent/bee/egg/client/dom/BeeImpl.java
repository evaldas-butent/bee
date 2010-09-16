package com.butent.bee.egg.client.dom;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.shared.Assert;

public class BeeImpl {
  public static void blur(Element elem) {
    Assert.notNull(elem);
    elem.blur();
  }

  public static Element createFocusable() {
    Element elem = Document.get().createDivElement().cast();
    elem.setTabIndex(0);
    return elem;
  }

  public static void focus(Element elem) {
    Assert.notNull(elem);
    elem.focus();
  }

}
