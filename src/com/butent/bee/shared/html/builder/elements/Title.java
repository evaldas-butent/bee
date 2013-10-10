package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;

public class Title extends FertileElement {

  public Title() {
    super();
  }

  public Title addClass(String value) {
    super.addClassName(value);
    return this;
  }

  

  public Title id(String value) {
    setId(value);
    return this;
  }

  public Title lang(String value) {
    setLang(value);
    return this;
  }

  public Title text(String text) {
    super.appendText(text);
    return this;
  }

  public Title title(String value) {
    setTitle(value);
    return this;
  }
}
