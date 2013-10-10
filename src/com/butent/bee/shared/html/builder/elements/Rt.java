package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Rt extends FertileElement {

  public Rt() {
    super();
  }

  public Rt addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Rt append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Rt append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Rt id(String value) {
    setId(value);
    return this;
  }

  public Rt insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Rt lang(String value) {
    setLang(value);
    return this;
  }

  public Rt remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Rt text(String text) {
    super.appendText(text);
    return this;
  }

  public Rt title(String value) {
    setTitle(value);
    return this;
  }
}
