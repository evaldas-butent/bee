package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Samp extends FertileElement {

  public Samp() {
    super();
  }

  public Samp addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Samp append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Samp append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Samp id(String value) {
    setId(value);
    return this;
  }

  public Samp insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Samp lang(String value) {
    setLang(value);
    return this;
  }

  public Samp remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Samp text(String text) {
    super.appendText(text);
    return this;
  }

  public Samp title(String value) {
    setTitle(value);
    return this;
  }
}
