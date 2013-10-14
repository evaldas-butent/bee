package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Optgroup extends FertileElement {

  public Optgroup() {
    super();
  }

  public Optgroup addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Optgroup append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Optgroup append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Optgroup disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Optgroup enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Optgroup id(String value) {
    setId(value);
    return this;
  }

  public Optgroup insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Optgroup label(String value) {
    setAttribute(Attribute.LABEL, value);
    return this;
  }

  public Optgroup lang(String value) {
    setLang(value);
    return this;
  }

  public Optgroup remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Optgroup text(String text) {
    super.appendText(text);
    return this;
  }

  public Optgroup title(String value) {
    setTitle(value);
    return this;
  }
}
