package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class P extends FertileElement {

  public P() {
    super();
  }

  public P addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public P append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public P append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public P id(String value) {
    setId(value);
    return this;
  }

  public P insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public P lang(String value) {
    setLang(value);
    return this;
  }

  public P remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public P text(String text) {
    super.appendText(text);
    return this;
  }

  public P title(String value) {
    setTitle(value);
    return this;
  }
}
