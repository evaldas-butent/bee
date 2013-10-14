package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Var extends FertileElement {

  public Var() {
    super();
  }

  public Var addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Var append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Var append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Var id(String value) {
    setId(value);
    return this;
  }

  public Var insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Var lang(String value) {
    setLang(value);
    return this;
  }

  public Var remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Var text(String text) {
    super.appendText(text);
    return this;
  }

  public Var title(String value) {
    setTitle(value);
    return this;
  }
}
