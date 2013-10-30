package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H4 extends FertileElement {

  public H4() {
    super();
  }

  public H4 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H4 append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H4 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H4 id(String value) {
    setId(value);
    return this;
  }

  public H4 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H4 lang(String value) {
    setLang(value);
    return this;
  }

  public H4 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H4 text(String text) {
    super.appendText(text);
    return this;
  }

  public H4 title(String value) {
    setTitle(value);
    return this;
  }
}
