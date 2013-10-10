package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Bdo extends FertileElement {

  public Bdo() {
    super();
  }

  public Bdo addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Bdo append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdo append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdo id(String value) {
    setId(value);
    return this;
  }

  public Bdo insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Bdo lang(String value) {
    setLang(value);
    return this;
  }

  public Bdo remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Bdo text(String text) {
    super.appendText(text);
    return this;
  }

  public Bdo title(String value) {
    setTitle(value);
    return this;
  }
}
