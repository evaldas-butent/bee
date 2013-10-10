package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dd extends FertileElement {

  public Dd() {
    super();
  }

  public Dd addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Dd append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dd append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dd id(String value) {
    setId(value);
    return this;
  }

  public Dd insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dd lang(String value) {
    setLang(value);
    return this;
  }

  public Dd remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Dd text(String text) {
    super.appendText(text);
    return this;
  }

  public Dd title(String value) {
    setTitle(value);
    return this;
  }
}
