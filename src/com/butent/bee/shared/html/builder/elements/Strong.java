package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Strong extends FertileElement {

  public Strong() {
    super();
  }

  public Strong addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Strong append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Strong append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Strong id(String value) {
    setId(value);
    return this;
  }

  public Strong insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Strong lang(String value) {
    setLang(value);
    return this;
  }

  public Strong remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Strong text(String text) {
    super.appendText(text);
    return this;
  }

  public Strong title(String value) {
    setTitle(value);
    return this;
  }
}
