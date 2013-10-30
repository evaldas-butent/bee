package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Noscript extends FertileElement {

  public Noscript() {
    super();
  }

  public Noscript addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Noscript append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Noscript append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Noscript id(String value) {
    setId(value);
    return this;
  }

  public Noscript insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Noscript lang(String value) {
    setLang(value);
    return this;
  }

  public Noscript remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Noscript text(String text) {
    super.appendText(text);
    return this;
  }

  public Noscript title(String value) {
    setTitle(value);
    return this;
  }
}
