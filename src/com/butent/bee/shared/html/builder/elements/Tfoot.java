package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Tfoot extends FertileElement {

  public Tfoot() {
    super();
  }

  public Tfoot addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Tfoot append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tfoot append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tfoot id(String value) {
    setId(value);
    return this;
  }

  public Tfoot insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Tfoot lang(String value) {
    setLang(value);
    return this;
  }

  public Tfoot remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Tfoot text(String text) {
    super.appendText(text);
    return this;
  }

  public Tfoot title(String value) {
    setTitle(value);
    return this;
  }
}
