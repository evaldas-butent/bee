package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Summary extends FertileElement {

  public Summary() {
    super();
  }

  public Summary addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Summary append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Summary append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Summary id(String value) {
    setId(value);
    return this;
  }

  public Summary insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Summary lang(String value) {
    setLang(value);
    return this;
  }

  public Summary remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Summary text(String text) {
    super.appendText(text);
    return this;
  }

  public Summary title(String value) {
    setTitle(value);
    return this;
  }
}
