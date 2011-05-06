package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TagName;

import com.butent.bee.shared.Assert;

/**
 * Enables using dt HTML tag, which is used for tagging list item names.
 */

@TagName(DtElement.TAG)
public class DtElement extends Element {

  static final String TAG = "dt";

  public static DtElement as(Element elem) {
    Assert.notNull(elem);
    Assert.isTrue(elem.getTagName().equalsIgnoreCase(TAG));

    return (DtElement) elem;
  }

  protected DtElement() {
  }

}
