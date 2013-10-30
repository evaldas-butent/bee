package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Del extends FertileElement {

  public Del() {
    super();
  }

  public Del addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Del append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Del append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Del cite(String value) {
    setAttribute(Attributes.CITE, value);
    return this;
  }

  public Del dateTime(String value) {
    setAttribute(Attributes.DATE_TIME, value);
    return this;
  }

  public Del id(String value) {
    setId(value);
    return this;
  }

  public Del insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Del lang(String value) {
    setLang(value);
    return this;
  }

  public Del remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Del text(String text) {
    super.appendText(text);
    return this;
  }

  public Del title(String value) {
    setTitle(value);
    return this;
  }
}
