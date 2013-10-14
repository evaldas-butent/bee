package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Label extends FertileElement {

  public Label() {
    super();
  }

  public Label addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Label append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Label append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Label form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Label htmlFor(String value) {
    setAttribute(Attribute.FOR, value);
    return this;
  }

  public Label id(String value) {
    setId(value);
    return this;
  }

  public Label insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Label lang(String value) {
    setLang(value);
    return this;
  }

  public Label remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Label text(String text) {
    super.appendText(text);
    return this;
  }

  public Label title(String value) {
    setTitle(value);
    return this;
  }
}
