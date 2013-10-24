package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.Element;

public class Base extends Element {

  public Base() {
    super();
  }

  public Base href(String value) {
    setAttribute(Attributes.HREF, value);
    return this;
  }

  public Base target(String value) {
    setAttribute(Attributes.TARGET, value);
    return this;
  }

  public Base targetBlank() {
    return target(Keywords.BROWSING_CONTEXT_BLANK);
  }

  public Base targetParent() {
    return target(Keywords.BROWSING_CONTEXT_PARENT);
  }

  public Base targetSelf() {
    return target(Keywords.BROWSING_CONTEXT_SELF);
  }

  public Base targetTop() {
    return target(Keywords.BROWSING_CONTEXT_TOP);
  }
}
