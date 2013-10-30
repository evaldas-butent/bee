package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Br extends Element {

  public Br() {
    super();
  }

  public Br addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Br id(String value) {
    setId(value);
    return this;
  }

  public Br lang(String value) {
    setLang(value);
    return this;
  }

  public Br title(String value) {
    setTitle(value);
    return this;
  }
}
