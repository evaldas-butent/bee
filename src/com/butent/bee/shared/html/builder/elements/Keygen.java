package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Keygen extends Element {

  private static final String KEY_TYPE_RSA = "rsa";

  public Keygen() {
    super();
  }

  public Keygen addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Keygen autofocus() {
    setAttribute(Attribute.AUTOFOCUS, true);
    return this;
  }

  public Keygen challenge(String value) {
    setAttribute(Attribute.CHALLENGE, value);
    return this;
  }

  

  public Keygen disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Keygen enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Keygen form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Keygen id(String value) {
    setId(value);
    return this;
  }

  public Keygen keyTypeRsa() {
    setAttribute(Attribute.KEY_TYPE, KEY_TYPE_RSA);
    return this;
  }

  public Keygen lang(String value) {
    setLang(value);
    return this;
  }

  public Keygen name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Keygen title(String value) {
    setTitle(value);
    return this;
  }
}
