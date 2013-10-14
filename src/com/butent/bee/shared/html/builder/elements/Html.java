package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Html extends FertileElement {

  public Html() {
    super();
  }

  public Html addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Html append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Html append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Html id(String value) {
    setId(value);
    return this;
  }

  public Html insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Html lang(String value) {
    setLang(value);
    return this;
  }

  public Html manifest(String value) {
    setAttribute(Attribute.MANIFEST, value);
    return this;
  }

  public Html remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Html text(String text) {
    super.appendText(text);
    return this;
  }

  public Html title(String value) {
    setTitle(value);
    return this;
  }
}
