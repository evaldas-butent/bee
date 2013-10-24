package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
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
    setAttribute(Attributes.HEIGHT, value);
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
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Embed title(String value) {
    setTitle(value);
    return this;
  }

  public Embed type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }

  public Embed width(int value) {
    setAttribute(Attributes.WIDTH, value);
    return this;
  }
}
