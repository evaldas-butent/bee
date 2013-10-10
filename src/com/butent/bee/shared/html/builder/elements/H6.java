package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H6 extends FertileElement {

  public H6() {
    super();
  }

  public H6 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H6 append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H6 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public H6 id(String value) {
    setId(value);
    return this;
  }

  public H6 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H6 lang(String value) {
    setLang(value);
    return this;
  }

  public H6 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H6 text(String text) {
    super.appendText(text);
    return this;
  }

  public H6 title(String value) {
    setTitle(value);
    return this;
  }
}
