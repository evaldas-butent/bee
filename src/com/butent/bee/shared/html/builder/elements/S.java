package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class S extends FertileElement {

  public S() {
    super();
  }

  public S addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public S append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public S append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public S id(String value) {
    setId(value);
    return this;
  }

  public S insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public S lang(String value) {
    setLang(value);
    return this;
  }

  public S remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public S text(String text) {
    super.appendText(text);
    return this;
  }

  public S title(String value) {
    setTitle(value);
    return this;
  }
}
