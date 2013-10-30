package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Caption extends FertileElement {

  public Caption() {
    super();
  }

  public Caption addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Caption append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Caption append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Caption id(String value) {
    setId(value);
    return this;
  }

  public Caption insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Caption lang(String value) {
    setLang(value);
    return this;
  }

  public Caption remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Caption text(String text) {
    super.appendText(text);
    return this;
  }

  public Caption title(String value) {
    setTitle(value);
    return this;
  }
}
