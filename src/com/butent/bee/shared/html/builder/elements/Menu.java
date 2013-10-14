package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Menu extends FertileElement {

  private static final String TYPE_POPUP = "popup";
  private static final String TYPE_TOOLBAR = "toolbar";

  public Menu() {
    super();
  }

  public Menu addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Menu append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Menu append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Menu id(String value) {
    setId(value);
    return this;
  }

  public Menu insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Menu label(String value) {
    setAttribute(Attribute.LABEL, value);
    return this;
  }

  public Menu lang(String value) {
    setLang(value);
    return this;
  }

  public Menu remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Menu text(String text) {
    super.appendText(text);
    return this;
  }

  public Menu title(String value) {
    setTitle(value);
    return this;
  }

  public Menu typePopup() {
    setAttribute(Attribute.TYPE, TYPE_POPUP);
    return this;
  }

  public Menu typeToolbar() {
    setAttribute(Attribute.TYPE, TYPE_TOOLBAR);
    return this;
  }
}
