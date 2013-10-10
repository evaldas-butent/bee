package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H2 extends FertileElement {

  public H2() {
    super();
  }

  public H2 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H2 append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H2 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public H2 id(String value) {
    setId(value);
    return this;
  }

  public H2 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H2 lang(String value) {
    setLang(value);
    return this;
  }

  public H2 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H2 text(String text) {
    super.appendText(text);
    return this;
  }

  public H2 title(String value) {
    setTitle(value);
    return this;
  }
}
