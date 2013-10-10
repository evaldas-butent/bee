package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Data extends FertileElement {

  public Data() {
    super();
  }

  public Data addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Data append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Data append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Data id(String value) {
    setId(value);
    return this;
  }

  public Data insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Data lang(String value) {
    setLang(value);
    return this;
  }

  public Data remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Data text(String text) {
    super.appendText(text);
    return this;
  }

  public Data title(String value) {
    setTitle(value);
    return this;
  }
  
  public Data value(String value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }
}
