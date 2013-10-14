package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Details extends FertileElement {

  public Details() {
    super();
  }

  public Details addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Details append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Details append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Details closed() {
    setAttribute(Attribute.OPEN, false);
    return this;
  }

  public Details id(String value) {
    setId(value);
    return this;
  }

  public Details insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Details lang(String value) {
    setLang(value);
    return this;
  }

  public Details open() {
    setAttribute(Attribute.OPEN, true);
    return this;
  }

  public Details remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Details text(String text) {
    super.appendText(text);
    return this;
  }

  public Details title(String value) {
    setTitle(value);
    return this;
  }
}
