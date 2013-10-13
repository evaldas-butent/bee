package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Meta extends Element {

  public Meta() {
    super();
  }

  public Meta addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Meta charset(String value) {
    setAttribute(Attribute.CHARSET, value);
    return this;
  }

  public Meta content(String value) {
    setAttribute(Attribute.CONTENT, value);
    return this;
  }

  public Meta httpEquiv(String value) {
    setAttribute(Attribute.HTTP_EQUIV, value);
    return this;
  }

  public Meta id(String value) {
    setId(value);
    return this;
  }

  public Meta lang(String value) {
    setLang(value);
    return this;
  }

  public Meta name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Meta title(String value) {
    setTitle(value);
    return this;
  }
}
