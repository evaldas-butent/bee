package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Output extends FertileElement {

  public Output() {
    super();
  }

  public Output addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Output append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Output append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Output form(String value) {
    setAttribute(Attributes.FORM, value);
    return this;
  }

  public Output htmlFor(String value) {
    setAttribute(Attributes.FOR, value);
    return this;
  }

  public Output id(String value) {
    setId(value);
    return this;
  }

  public Output insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Output lang(String value) {
    setLang(value);
    return this;
  }

  public Output name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Output remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Output text(String text) {
    super.appendText(text);
    return this;
  }

  public Output title(String value) {
    setTitle(value);
    return this;
  }
}
