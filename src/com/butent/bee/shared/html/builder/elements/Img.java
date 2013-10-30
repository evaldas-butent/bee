package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.Element;

public class Img extends Element {

  public Img() {
    super();
  }

  public Img addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Img alt(String value) {
    setAttribute(Attributes.ALT, value);
    return this;
  }

  public Img crossoriginAnonymous() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Img crossoriginUseCredentials() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Img height(int value) {
    setAttribute(Attributes.HEIGHT, value);
    return this;
  }

  public Img id(String value) {
    setId(value);
    return this;
  }

  public Img isMap(boolean value) {
    setAttribute(Attributes.IS_MAP, value);
    return this;
  }

  public Img lang(String value) {
    setLang(value);
    return this;
  }

  public Img src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Img title(String value) {
    setTitle(value);
    return this;
  }

  public Img useMap(String value) {
    setAttribute(Attributes.USE_MAP, value);
    return this;
  }

  public Img width(int value) {
    setAttribute(Attributes.WIDTH, value);
    return this;
  }
}
