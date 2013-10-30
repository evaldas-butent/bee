package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Em extends FertileElement {

  public Em() {
    super();
  }

  public Em addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Em append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Em append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Em id(String value) {
    setId(value);
    return this;
  }

  public Em insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Em lang(String value) {
    setLang(value);
    return this;
  }

  public Em remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Em text(String text) {
    super.appendText(text);
    return this;
  }

  public Em title(String value) {
    setTitle(value);
    return this;
  }
}
