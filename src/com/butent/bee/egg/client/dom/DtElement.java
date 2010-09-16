package com.butent.bee.egg.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TagName;

@TagName(DtElement.TAG)
public class DtElement extends Element {

  static final String TAG = "dt";

  public static DtElement as(Element elem) {
    assert elem.getTagName().equalsIgnoreCase(TAG);
    return (DtElement) elem;
  }

  protected DtElement() {
  }

}
