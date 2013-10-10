package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dl extends FertileElement {

  public Dl() {
    super();
  }

  public Dl addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Dl append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dl append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Dl id(String value) {
    setId(value);
    return this;
  }

  public Dl insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dl lang(String value) {
    setLang(value);
    return this;
  }

  public Dl remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Dl text(String text) {
    super.appendText(text);
    return this;
  }

  public Dl title(String value) {
    setTitle(value);
    return this;
  }
}
