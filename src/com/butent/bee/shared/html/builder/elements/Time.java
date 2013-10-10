package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Time extends FertileElement {

  public Time() {
    super();
  }

  public Time addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Time append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Time append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Time dateTime(String value) {
    setAttribute(Attribute.DATETIME, value);
    return this;
  }

  

  public Time id(String value) {
    setId(value);
    return this;
  }

  public Time insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Time lang(String value) {
    setLang(value);
    return this;
  }

  public Time remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Time text(String text) {
    super.appendText(text);
    return this;
  }

  public Time title(String value) {
    setTitle(value);
    return this;
  }
}
