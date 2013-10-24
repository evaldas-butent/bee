package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Progress extends FertileElement {

  public Progress() {
    super();
  }

  public Progress addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Progress append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Progress append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Progress id(String value) {
    setId(value);
    return this;
  }

  public Progress insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Progress lang(String value) {
    setLang(value);
    return this;
  }

  public Progress max(double value) {
    setAttribute(Attributes.MAX, value);
    return this;
  }

  public Progress max(int value) {
    setAttribute(Attributes.MAX, value);
    return this;
  }

  public Progress remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Progress text(String text) {
    super.appendText(text);
    return this;
  }

  public Progress title(String value) {
    setTitle(value);
    return this;
  }

  public Progress value(double value) {
    setAttribute(Attributes.VALUE, value);
    return this;
  }

  public Progress value(int value) {
    setAttribute(Attributes.VALUE, value);
    return this;
  }
}
