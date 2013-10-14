package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Rp extends FertileElement {

  public Rp() {
    super();
  }

  public Rp addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Rp append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Rp append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Rp id(String value) {
    setId(value);
    return this;
  }

  public Rp insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Rp lang(String value) {
    setLang(value);
    return this;
  }

  public Rp remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Rp text(String text) {
    super.appendText(text);
    return this;
  }

  public Rp title(String value) {
    setTitle(value);
    return this;
  }
}
