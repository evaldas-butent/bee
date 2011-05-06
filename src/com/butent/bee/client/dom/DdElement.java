package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TagName;

import com.butent.bee.shared.Assert;

/**
 * Enables using dd HTML tag, which is used for tagging list item values.
 */

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
