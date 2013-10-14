package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Fieldset extends FertileElement {

  public Fieldset() {
    super();
  }

  public Fieldset addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Fieldset append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Fieldset append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Fieldset disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Fieldset enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Fieldset form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Fieldset id(String value) {
    setId(value);
    return this;
  }

  public Fieldset insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Fieldset lang(String value) {
    setLang(value);
    return this;
  }

  public Fieldset name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Fieldset remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Fieldset text(String text) {
    super.appendText(text);
    return this;
  }

  public Fieldset title(String value) {
    setTitle(value);
    return this;
  }
}
