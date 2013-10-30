package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Q extends FertileElement {

  public Q() {
    super();
  }

  public Q addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Q append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q cite(String value) {
    setAttribute(Attributes.CITE, value);
    return this;
  }

  public Q id(String value) {
    setId(value);
    return this;
  }

  public Q insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Q lang(String value) {
    setLang(value);
    return this;
  }

  public Q remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Q text(String text) {
    super.appendText(text);
    return this;
  }

  public Q title(String value) {
    setTitle(value);
    return this;
  }
}
