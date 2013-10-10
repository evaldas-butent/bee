package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Meter extends FertileElement {

  public Meter() {
    super();
  }

  public Meter addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Meter append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Meter append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Meter high(double value) {
    setAttribute(Attribute.HIGH, value);
    return this;
  }

  public Meter high(int value) {
    setAttribute(Attribute.HIGH, value);
    return this;
  }

  public Meter id(String value) {
    setId(value);
    return this;
  }

  public Meter insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Meter lang(String value) {
    setLang(value);
    return this;
  }

  public Meter low(double value) {
    setAttribute(Attribute.LOW, value);
    return this;
  }

  public Meter low(int value) {
    setAttribute(Attribute.LOW, value);
    return this;
  }

  public Meter max(double value) {
    setAttribute(Attribute.MAX, value);
    return this;
  }

  public Meter max(int value) {
    setAttribute(Attribute.MAX, value);
    return this;
  }

  public Meter min(double value) {
    setAttribute(Attribute.MIN, value);
    return this;
  }

  public Meter min(int value) {
    setAttribute(Attribute.MIN, value);
    return this;
  }

  public Meter optimum(double value) {
    setAttribute(Attribute.OPTIMUM, value);
    return this;
  }

  public Meter optimum(int value) {
    setAttribute(Attribute.OPTIMUM, value);
    return this;
  }

  public Meter remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Meter text(String text) {
    super.appendText(text);
    return this;
  }

  public Meter title(String value) {
    setTitle(value);
    return this;
  }

  public Meter value(double value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }

  public Meter value(int value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }
}
