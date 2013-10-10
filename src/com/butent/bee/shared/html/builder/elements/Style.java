package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;

public class Style extends FertileElement {

  public Style() {
    super();
  }

  public Style addClass(String value) {
    super.addClassName(value);
    return this;
  }

  

  public Style id(String value) {
    setId(value);
    return this;
  }

  public Style lang(String value) {
    setLang(value);
    return this;
  }

  public Style media(String value) {
    setAttribute(Attribute.MEDIA, value);
    return this;
  }

  public Style scoped() {
    setAttribute(Attribute.SCOPED, true);
    return this;
  }

  public Style text(String text) {
    super.appendText(text);
    return this;
  }

  public Style title(String value) {
    setTitle(value);
    return this;
  }

  public Style type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }
}
