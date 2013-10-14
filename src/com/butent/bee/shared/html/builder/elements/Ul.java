package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ul extends FertileElement {

  public Ul() {
    super();
  }

  public Ul addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Ul append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ul append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ul id(String value) {
    setId(value);
    return this;
  }

  public Ul insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ul lang(String value) {
    setLang(value);
    return this;
  }

  public Ul remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ul text(String text) {
    super.appendText(text);
    return this;
  }

  public Ul title(String value) {
    setTitle(value);
    return this;
  }
}
