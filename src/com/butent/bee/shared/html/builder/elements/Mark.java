package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Mark extends FertileElement {

  public Mark() {
    super();
  }

  public Mark addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Mark append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Mark append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Mark id(String value) {
    setId(value);
    return this;
  }

  public Mark insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Mark lang(String value) {
    setLang(value);
    return this;
  }

  public Mark remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Mark text(String text) {
    super.appendText(text);
    return this;
  }

  public Mark title(String value) {
    setTitle(value);
    return this;
  }
}
