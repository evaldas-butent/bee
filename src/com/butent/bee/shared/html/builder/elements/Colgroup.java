package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Colgroup extends FertileElement {

  public Colgroup() {
    super();
  }

  public Colgroup addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Colgroup append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Colgroup append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Colgroup id(String value) {
    setId(value);
    return this;
  }

  public Colgroup insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Colgroup lang(String value) {
    setLang(value);
    return this;
  }

  public Colgroup remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Colgroup span(int value) {
    setAttribute(Attribute.SPAN, value);
    return this;
  }
  
  public Colgroup text(String text) {
    super.appendText(text);
    return this;
  }

  public Colgroup title(String value) {
    setTitle(value);
    return this;
  }
}
