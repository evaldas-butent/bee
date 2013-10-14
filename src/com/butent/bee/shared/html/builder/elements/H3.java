package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H3 extends FertileElement {

  public H3() {
    super();
  }

  public H3 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H3 append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H3 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H3 id(String value) {
    setId(value);
    return this;
  }

  public H3 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H3 lang(String value) {
    setLang(value);
    return this;
  }

  public H3 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H3 text(String text) {
    super.appendText(text);
    return this;
  }

  public H3 title(String value) {
    setTitle(value);
    return this;
  }
}
