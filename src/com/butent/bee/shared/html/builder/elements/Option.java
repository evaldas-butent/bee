package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;

public class Option extends FertileElement {

  public Option() {
    super();
  }

  public Option addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Option disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Option enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Option id(String value) {
    setId(value);
    return this;
  }

  public Option label(String value) {
    setAttribute(Attribute.LABEL, value);
    return this;
  }

  public Option lang(String value) {
    setLang(value);
    return this;
  }

  public Option selected() {
    setAttribute(Attribute.SELECTED, true);
    return this;
  }

  public Option text(String text) {
    super.appendText(text);
    return this;
  }

  public Option title(String value) {
    setTitle(value);
    return this;
  }

  public Option value(String value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }
}
