package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Legend extends FertileElement {

  public Legend() {
    super();
  }

  public Legend addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Legend append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Legend append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Legend id(String value) {
    setId(value);
    return this;
  }

  public Legend insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Legend lang(String value) {
    setLang(value);
    return this;
  }

  public Legend remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Legend text(String text) {
    super.appendText(text);
    return this;
  }

  public Legend title(String value) {
    setTitle(value);
    return this;
  }
}
