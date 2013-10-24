package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
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
    setAttribute(Attributes.MEDIA, value);
    return this;
  }

  public Source src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Source title(String value) {
    setTitle(value);
    return this;
  }

  public Source type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }
}
