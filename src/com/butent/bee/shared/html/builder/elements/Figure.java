package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Figure extends FertileElement {

  public Figure() {
    super();
  }

  public Figure addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Figure append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Figure append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Figure id(String value) {
    setId(value);
    return this;
  }

  public Figure insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Figure lang(String value) {
    setLang(value);
    return this;
  }

  public Figure remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Figure text(String text) {
    super.appendText(text);
    return this;
  }

  public Figure title(String value) {
    setTitle(value);
    return this;
  }
}
