package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Bdi extends FertileElement {

  public Bdi() {
    super();
  }

  public Bdi addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Bdi append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdi append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdi id(String value) {
    setId(value);
    return this;
  }

  public Bdi insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Bdi lang(String value) {
    setLang(value);
    return this;
  }

  public Bdi remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Bdi text(String text) {
    super.appendText(text);
    return this;
  }

  public Bdi title(String value) {
    setTitle(value);
    return this;
  }
}
