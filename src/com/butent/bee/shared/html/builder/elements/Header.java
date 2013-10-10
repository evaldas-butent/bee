package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Header extends FertileElement {

  public Header() {
    super();
  }

  public Header addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Header append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Header append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Header id(String value) {
    setId(value);
    return this;
  }

  public Header insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Header lang(String value) {
    setLang(value);
    return this;
  }

  public Header remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Header text(String text) {
    super.appendText(text);
    return this;
  }

  public Header title(String value) {
    setTitle(value);
    return this;
  }
}
