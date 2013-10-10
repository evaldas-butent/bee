package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Cite extends FertileElement {

  public Cite() {
    super();
  }

  public Cite addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Cite append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Cite append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Cite id(String value) {
    setId(value);
    return this;
  }

  public Cite insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Cite lang(String value) {
    setLang(value);
    return this;
  }

  public Cite remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Cite text(String text) {
    super.appendText(text);
    return this;
  }

  public Cite title(String value) {
    setTitle(value);
    return this;
  }
}
