package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Figcaption extends FertileElement {

  public Figcaption() {
    super();
  }

  public Figcaption addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Figcaption append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Figcaption append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Figcaption id(String value) {
    setId(value);
    return this;
  }

  public Figcaption insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Figcaption lang(String value) {
    setLang(value);
    return this;
  }

  public Figcaption remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Figcaption text(String text) {
    super.appendText(text);
    return this;
  }

  public Figcaption title(String value) {
    setTitle(value);
    return this;
  }
}
