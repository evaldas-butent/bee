package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Wbr extends Element {

  public Wbr() {
    super();
  }

  public Wbr addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Wbr id(String value) {
    setId(value);
    return this;
  }

  public Wbr lang(String value) {
    setLang(value);
    return this;
  }

  public Wbr title(String value) {
    setTitle(value);
    return this;
  }
}
