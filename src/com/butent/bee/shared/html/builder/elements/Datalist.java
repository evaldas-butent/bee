package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Datalist extends FertileElement {

  public Datalist() {
    super();
  }

  public Datalist addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Datalist append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Datalist append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Datalist id(String value) {
    setId(value);
    return this;
  }

  public Datalist insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Datalist lang(String value) {
    setLang(value);
    return this;
  }

  public Datalist remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Datalist text(String text) {
    super.appendText(text);
    return this;
  }

  public Datalist title(String value) {
    setTitle(value);
    return this;
  }
}
