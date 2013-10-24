package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;

public class Script extends FertileElement {

  public Script() {
    super();
  }

  public Script addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Script async() {
    setAttribute(Attributes.ASYNC, true);
    return this;
  }

  public Script charset(String value) {
    setAttribute(Attributes.CHARSET, value);
    return this;
  }

  public Script crossoriginAnonymous() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Script crossoriginUseCredentials() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Script defer() {
    setAttribute(Attributes.DEFER, true);
    return this;
  }

  public Script id(String value) {
    setId(value);
    return this;
  }

  public Script lang(String value) {
    setLang(value);
    return this;
  }

  public Script src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Script text(String text) {
    super.appendText(text);
    return this;
  }

  public Script title(String value) {
    setTitle(value);
    return this;
  }

  public Script type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }
}
