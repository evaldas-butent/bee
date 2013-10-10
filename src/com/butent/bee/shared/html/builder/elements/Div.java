package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Div extends FertileElement {

  public Div() {
    super();
  }

  public Div addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Div append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Div append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Div id(String value) {
    setId(value);
    return this;
  }

  public Div insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Div lang(String value) {
    setLang(value);
    return this;
  }

  public Div remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Div text(String text) {
    super.appendText(text);
    return this;
  }

  public Div title(String value) {
    setTitle(value);
    return this;
  }
}
