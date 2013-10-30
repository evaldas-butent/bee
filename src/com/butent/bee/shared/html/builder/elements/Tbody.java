package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Tbody extends FertileElement {

  public Tbody() {
    super();
  }

  public Tbody addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Tbody append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tbody append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tbody id(String value) {
    setId(value);
    return this;
  }

  public Tbody insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Tbody lang(String value) {
    setLang(value);
    return this;
  }

  public Tbody remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Tbody text(String text) {
    super.appendText(text);
    return this;
  }

  public Tbody title(String value) {
    setTitle(value);
    return this;
  }
}
