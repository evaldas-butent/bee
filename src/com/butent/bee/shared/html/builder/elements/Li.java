package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Li extends FertileElement {

  public Li() {
    super();
  }

  public Li addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Li append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Li append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Li id(String value) {
    setId(value);
    return this;
  }

  public Li insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Li lang(String value) {
    setLang(value);
    return this;
  }

  public Li remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Li text(String text) {
    super.appendText(text);
    return this;
  }

  public Li title(String value) {
    setTitle(value);
    return this;
  }

  public Li value(int value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }
}
