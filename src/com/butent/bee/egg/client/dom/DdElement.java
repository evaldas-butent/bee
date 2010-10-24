package com.butent.bee.egg.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TagName;

import com.butent.bee.egg.shared.Assert;

@TagName(DdElement.TAG)
public class DdElement extends Element {

  static final String TAG = "dd";

  public static DdElement as(Element elem) {
    Assert.notNull(elem);
    Assert.isTrue(elem.getTagName().equalsIgnoreCase(TAG));
    
    return (DdElement) elem;
  }

  protected DdElement() {
  }

}
