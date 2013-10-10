package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Td extends FertileElement {

  public Td() {
    super();
  }

  public Td addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Td append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Td append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Td colSpan(int value) {
    setAttribute(Attribute.COLSPAN, value);
    return this;
  }

  

  public Td headers(String value) {
    setAttribute(Attribute.HEADERS, value);
    return this;
  }

  public Td id(String value) {
    setId(value);
    return this;
  }

  public Td insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Td lang(String value) {
    setLang(value);
    return this;
  }

  public Td remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Td rowSpan(int value) {
    setAttribute(Attribute.ROWSPAN, value);
    return this;
  }

  public Td text(String text) {
    super.appendText(text);
    return this;
  }

  public Td title(String value) {
    setTitle(value);
    return this;
  }
}
