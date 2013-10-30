package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Hr extends Element {

  public Hr() {
    super();
  }

  public Hr addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Hr id(String value) {
    setId(value);
    return this;
  }

  public Hr lang(String value) {
    setLang(value);
    return this;
  }

  public Hr title(String value) {
    setTitle(value);
    return this;
  }
}
