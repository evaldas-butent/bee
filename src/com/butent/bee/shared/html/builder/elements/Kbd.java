package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Kbd extends FertileElement {

  public Kbd() {
    super();
  }

  public Kbd addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Kbd append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Kbd append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Kbd id(String value) {
    setId(value);
    return this;
  }

  public Kbd insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Kbd lang(String value) {
    setLang(value);
    return this;
  }

  public Kbd remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Kbd text(String text) {
    super.appendText(text);
    return this;
  }

  public Kbd title(String value) {
    setTitle(value);
    return this;
  }
}
