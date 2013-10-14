package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Canvas extends FertileElement {

  public Canvas() {
    super();
  }

  public Canvas addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Canvas append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Canvas append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Canvas height(int value) {
    setAttribute(Attribute.HEIGHT, value);
    return this;
  }

  public Canvas id(String value) {
    setId(value);
    return this;
  }

  public Canvas insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Canvas lang(String value) {
    setLang(value);
    return this;
  }

  public Canvas remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Canvas text(String text) {
    super.appendText(text);
    return this;
  }

  public Canvas title(String value) {
    setTitle(value);
    return this;
  }

  public Canvas width(int value) {
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
}
