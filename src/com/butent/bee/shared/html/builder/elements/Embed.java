package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Embed extends Element {

  public Embed() {
    super();
  }

  public Embed addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Embed attribute(String name, String value) {
    setAttribute(name, value);
    return this;
  }

  public Embed height(int value) {
    setAttribute(Attribute.HEIGHT, value);
    return this;
  }

  public Embed id(String value) {
    setId(value);
    return this;
  }

  public Embed lang(String value) {
    setLang(value);
    return this;
  }

  public Embed src(String value) {
    setAttribute(Attribute.SRC, value);
    return this;
  }

  public Embed title(String value) {
    setTitle(value);
    return this;
  }

  public Embed type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }

  public Embed width(int value) {
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
}
