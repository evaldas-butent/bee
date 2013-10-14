package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Select extends FertileElement {

  public Select() {
    super();
  }

  public Select addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Select append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Select append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Select autofocus() {
    setAttribute(Attribute.AUTOFOCUS, true);
    return this;
  }

  public Select disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Select enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Select form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Select id(String value) {
    setId(value);
    return this;
  }

  public Select insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Select lang(String value) {
    setLang(value);
    return this;
  }

  public Select multiple() {
    setAttribute(Attribute.MULTIPLE, true);
    return this;
  }

  public Select name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Select remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Select required() {
    setAttribute(Attribute.REQUIRED, true);
    return this;
  }

  public Select size(int value) {
    setAttribute(Attribute.SIZE, value);
    return this;
  }

  public Select text(String text) {
    super.appendText(text);
    return this;
  }

  public Select title(String value) {
    setTitle(value);
    return this;
  }
}
