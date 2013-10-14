package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H1 extends FertileElement {

  public H1() {
    super();
  }

  public H1 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H1 append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H1 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H1 id(String value) {
    setId(value);
    return this;
  }

  public H1 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H1 lang(String value) {
    setLang(value);
    return this;
  }

  public H1 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H1 text(String text) {
    super.appendText(text);
    return this;
  }

  public H1 title(String value) {
    setTitle(value);
    return this;
  }
}
