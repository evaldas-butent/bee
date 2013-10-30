package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
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
    setAttribute(Attributes.AUTOFOCUS, true);
    return this;
  }

  public Keygen challenge(String value) {
    setAttribute(Attributes.CHALLENGE, value);
    return this;
  }

  public Keygen disabled() {
    setAttribute(Attributes.DISABLED, true);
    return this;
  }

  public Keygen enabled() {
    setAttribute(Attributes.DISABLED, false);
    return this;
  }

  public Keygen form(String value) {
    setAttribute(Attributes.FORM, value);
    return this;
  }

  public Keygen id(String value) {
    setId(value);
    return this;
  }

  public Keygen keyTypeRsa() {
    setAttribute(Attributes.KEY_TYPE, KEY_TYPE_RSA);
    return this;
  }

  public Keygen lang(String value) {
    setLang(value);
    return this;
  }

  public Keygen name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Keygen title(String value) {
    setTitle(value);
    return this;
  }
}
