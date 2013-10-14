package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Template extends FertileElement {

  public Template() {
    super();
  }

  public Template addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Template append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Template append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Template id(String value) {
    setId(value);
    return this;
  }

  public Template insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Template lang(String value) {
    setLang(value);
    return this;
  }

  public Template remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Template text(String text) {
    super.appendText(text);
    return this;
  }

  public Template title(String value) {
    setTitle(value);
    return this;
  }
}
