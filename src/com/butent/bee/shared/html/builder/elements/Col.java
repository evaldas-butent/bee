package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Col extends Element {

  public Col() {
    super();
  }

  public Col addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Col id(String value) {
    setId(value);
    return this;
  }

  public Col lang(String value) {
    setLang(value);
    return this;
  }

  public Col span(int value) {
    setAttribute(Attribute.SPAN, value);
    return this;
  }

  public Col title(String value) {
    setTitle(value);
    return this;
  }
}
