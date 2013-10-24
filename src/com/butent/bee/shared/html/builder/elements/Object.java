package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Object extends FertileElement {

  public Object() {
    super();
  }

  public Object addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Object append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object data(String value) {
    setAttribute(Attributes.DATA, value);
    return this;
  }

  public Object form(String value) {
    setAttribute(Attributes.FORM, value);
    return this;
  }

  public Object height(int value) {
    setAttribute(Attributes.HEIGHT, value);
    return this;
  }

  public Object id(String value) {
    setId(value);
    return this;
  }

  public Object insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Object lang(String value) {
    setLang(value);
    return this;
  }

  public Object name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Object remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Object text(String text) {
    super.appendText(text);
    return this;
  }

  public Object title(String value) {
    setTitle(value);
    return this;
  }

  public Object type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }

  public Object typeMustMatch() {
    setAttribute(Attributes.TYPE_MUST_MATCH, true);
    return this;
  }

  public Object useMap(String value) {
    setAttribute(Attributes.USE_MAP, value);
    return this;
  }

  public Object width(int value) {
    setAttribute(Attributes.WIDTH, value);
    return this;
  }

}
