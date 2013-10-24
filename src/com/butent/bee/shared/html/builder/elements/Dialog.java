package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dialog extends FertileElement {

  public Dialog() {
    super();
  }

  public Dialog addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Dialog append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dialog append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dialog closed() {
    setAttribute(Attributes.OPEN, false);
    return this;
  }

  public Dialog id(String value) {
    setId(value);
    return this;
  }

  public Dialog insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dialog lang(String value) {
    setLang(value);
    return this;
  }

  public Dialog open() {
    setAttribute(Attributes.OPEN, true);
    return this;
  }

  public Dialog remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Dialog text(String text) {
    super.appendText(text);
    return this;
  }

  public Dialog title(String value) {
    setTitle(value);
    return this;
  }
}
