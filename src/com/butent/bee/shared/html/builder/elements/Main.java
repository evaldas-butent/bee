package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Main extends FertileElement {

  public Main() {
    super();
  }

  public Main addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Main append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Main append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Main id(String value) {
    setId(value);
    return this;
  }

  public Main insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Main lang(String value) {
    setLang(value);
    return this;
  }

  public Main remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Main text(String text) {
    super.appendText(text);
    return this;
  }

  public Main title(String value) {
    setTitle(value);
    return this;
  }
}
