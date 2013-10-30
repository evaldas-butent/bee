package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class B extends FertileElement {

  public B() {
    super();
  }

  public B addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public B append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public B append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public B id(String value) {
    setId(value);
    return this;
  }

  public B insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public B lang(String value) {
    setLang(value);
    return this;
  }

  public B remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public B text(String text) {
    super.appendText(text);
    return this;
  }

  public B title(String value) {
    setTitle(value);
    return this;
  }
}
