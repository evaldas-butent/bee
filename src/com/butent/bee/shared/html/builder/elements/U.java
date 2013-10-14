package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class U extends FertileElement {

  public U() {
    super();
  }

  public U addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public U append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public U append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public U id(String value) {
    setId(value);
    return this;
  }

  public U insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public U lang(String value) {
    setLang(value);
    return this;
  }

  public U remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public U text(String text) {
    super.appendText(text);
    return this;
  }

  public U title(String value) {
    setTitle(value);
    return this;
  }
}
