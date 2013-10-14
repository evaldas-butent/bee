package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Sup extends FertileElement {

  public Sup() {
    super();
  }

  public Sup addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Sup append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Sup append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Sup id(String value) {
    setId(value);
    return this;
  }

  public Sup insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Sup lang(String value) {
    setLang(value);
    return this;
  }

  public Sup remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Sup text(String text) {
    super.appendText(text);
    return this;
  }

  public Sup title(String value) {
    setTitle(value);
    return this;
  }
}
