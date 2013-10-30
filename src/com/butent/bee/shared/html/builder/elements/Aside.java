package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Aside extends FertileElement {

  public Aside() {
    super();
  }

  public Aside addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Aside append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Aside append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Aside id(String value) {
    setId(value);
    return this;
  }

  public Aside insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Aside lang(String value) {
    setLang(value);
    return this;
  }

  public Aside remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Aside text(String text) {
    super.appendText(text);
    return this;
  }

  public Aside title(String value) {
    setTitle(value);
    return this;
  }
}
