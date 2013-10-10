package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Address extends FertileElement {

  public Address() {
    super();
  }

  public Address addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Address append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Address append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Address id(String value) {
    setId(value);
    return this;
  }

  public Address insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Address lang(String value) {
    setLang(value);
    return this;
  }

  public Address remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Address text(String text) {
    super.appendText(text);
    return this;
  }

  public Address title(String value) {
    setTitle(value);
    return this;
  }
}
