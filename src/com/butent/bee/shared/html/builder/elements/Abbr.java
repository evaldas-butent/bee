package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Abbr extends FertileElement {

  public Abbr() {
    super();
  }

  public Abbr addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Abbr append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Abbr append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Abbr id(String value) {
    setId(value);
    return this;
  }

  public Abbr insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Abbr lang(String value) {
    setLang(value);
    return this;
  }

  public Abbr remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Abbr text(String text) {
    super.appendText(text);
    return this;
  }

  public Abbr title(String value) {
    setTitle(value);
    return this;
  }
}
