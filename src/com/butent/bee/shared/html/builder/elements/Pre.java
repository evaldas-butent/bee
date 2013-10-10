package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Pre extends FertileElement {

  public Pre() {
    super();
  }

  public Pre addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Pre append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Pre append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Pre id(String value) {
    setId(value);
    return this;
  }

  public Pre insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Pre lang(String value) {
    setLang(value);
    return this;
  }

  public Pre remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Pre text(String text) {
    super.appendText(text);
    return this;
  }

  public Pre title(String value) {
    setTitle(value);
    return this;
  }
}
