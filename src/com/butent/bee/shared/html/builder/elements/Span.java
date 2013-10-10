package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Span extends FertileElement {

  public Span() {
    super();
  }

  public Span addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Span append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Span append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Span id(String value) {
    setId(value);
    return this;
  }

  public Span insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Span lang(String value) {
    setLang(value);
    return this;
  }

  public Span remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Span text(String text) {
    super.appendText(text);
    return this;
  }

  public Span title(String value) {
    setTitle(value);
    return this;
  }
}
