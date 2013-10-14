package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ruby extends FertileElement {

  public Ruby() {
    super();
  }

  public Ruby addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Ruby append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ruby append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ruby id(String value) {
    setId(value);
    return this;
  }

  public Ruby insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ruby lang(String value) {
    setLang(value);
    return this;
  }

  public Ruby remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ruby text(String text) {
    super.appendText(text);
    return this;
  }

  public Ruby title(String value) {
    setTitle(value);
    return this;
  }
}
