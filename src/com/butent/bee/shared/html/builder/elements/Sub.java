package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Sub extends FertileElement {

  public Sub() {
    super();
  }

  public Sub addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Sub append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Sub append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Sub id(String value) {
    setId(value);
    return this;
  }

  public Sub insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Sub lang(String value) {
    setLang(value);
    return this;
  }

  public Sub remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Sub text(String text) {
    super.appendText(text);
    return this;
  }

  public Sub title(String value) {
    setTitle(value);
    return this;
  }
}
