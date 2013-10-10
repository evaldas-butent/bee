package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dfn extends FertileElement {

  public Dfn() {
    super();
  }

  public Dfn addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Dfn append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dfn append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dfn id(String value) {
    setId(value);
    return this;
  }

  public Dfn insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dfn lang(String value) {
    setLang(value);
    return this;
  }

  public Dfn remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Dfn text(String text) {
    super.appendText(text);
    return this;
  }

  public Dfn title(String value) {
    setTitle(value);
    return this;
  }
}
