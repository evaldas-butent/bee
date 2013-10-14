package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Keywords;

public class Img extends Element {

  public Img() {
    super();
  }

  public Img addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Img alt(String value) {
    setAttribute(Attribute.ALT, value);
    return this;
  }

  public Img crossoriginAnonymous() {
    setAttribute(Attribute.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Img crossoriginUseCredentials() {
    setAttribute(Attribute.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Img height(int value) {
    setAttribute(Attribute.HEIGHT, value);
    return this;
  }

  public Img id(String value) {
    setId(value);
    return this;
  }

  public Img isMap(boolean value) {
    setAttribute(Attribute.IS_MAP, value);
    return this;
  }

  public Img lang(String value) {
    setLang(value);
    return this;
  }

  public Img src(String value) {
    setAttribute(Attribute.SRC, value);
    return this;
  }

  public Img title(String value) {
    setTitle(value);
    return this;
  }

  public Img useMap(String value) {
    setAttribute(Attribute.USE_MAP, value);
    return this;
  }

  public Img width(int value) {
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
}
