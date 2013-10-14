package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Thead extends FertileElement {

  public Thead() {
    super();
  }

  public Thead addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Thead append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Thead append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Thead id(String value) {
    setId(value);
    return this;
  }

  public Thead insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Thead lang(String value) {
    setLang(value);
    return this;
  }

  public Thead remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Thead text(String text) {
    super.appendText(text);
    return this;
  }

  public Thead title(String value) {
    setTitle(value);
    return this;
  }
}
