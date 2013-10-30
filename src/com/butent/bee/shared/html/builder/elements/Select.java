package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
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
    setAttribute(Attributes.AUTOFOCUS, true);
    return this;
  }

  public Select disabled() {
    setAttribute(Attributes.DISABLED, true);
    return this;
  }

  public Select enabled() {
    setAttribute(Attributes.DISABLED, false);
    return this;
  }

  public Select form(String value) {
    setAttribute(Attributes.FORM, value);
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
    setAttribute(Attributes.MULTIPLE, true);
    return this;
  }

  public Select name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Select remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Select required() {
    setAttribute(Attributes.REQUIRED, true);
    return this;
  }

  public Select size(int value) {
    setAttribute(Attributes.SIZE, value);
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
