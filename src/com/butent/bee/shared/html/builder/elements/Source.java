package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Source extends Element {

  public Source() {
    super();
  }

  public Source addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Source id(String value) {
    setId(value);
    return this;
  }

  public Source lang(String value) {
    setLang(value);
    return this;
  }

  public Source media(String value) {
    setAttribute(Attribute.MEDIA, value);
    return this;
  }

  public Source src(String value) {
    setAttribute(Attribute.SRC, value);
    return this;
  }

  public Source title(String value) {
    setTitle(value);
    return this;
  }

  public Source type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }
}
