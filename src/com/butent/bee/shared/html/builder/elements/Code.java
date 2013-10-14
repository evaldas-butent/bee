package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Code extends FertileElement {

  public Code() {
    super();
  }

  public Code addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Code append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Code append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Code id(String value) {
    setId(value);
    return this;
  }

  public Code insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Code lang(String value) {
    setLang(value);
    return this;
  }

  public Code remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Code text(String text) {
    super.appendText(text);
    return this;
  }

  public Code title(String value) {
    setTitle(value);
    return this;
  }
}
