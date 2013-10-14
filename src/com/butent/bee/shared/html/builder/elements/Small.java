package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Small extends FertileElement {

  public Small() {
    super();
  }

  public Small addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Small append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Small append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Small id(String value) {
    setId(value);
    return this;
  }

  public Small insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Small lang(String value) {
    setLang(value);
    return this;
  }

  public Small remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Small text(String text) {
    super.appendText(text);
    return this;
  }

  public Small title(String value) {
    setTitle(value);
    return this;
  }
}
