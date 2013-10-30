package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Head extends FertileElement {

  public Head() {
    super();
  }

  public Head addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Head append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Head append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Head id(String value) {
    setId(value);
    return this;
  }

  public Head insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Head lang(String value) {
    setLang(value);
    return this;
  }

  public Head remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Head text(String text) {
    super.appendText(text);
    return this;
  }

  public Head title(String value) {
    setTitle(value);
    return this;
  }
}
