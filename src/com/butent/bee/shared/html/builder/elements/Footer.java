package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Footer extends FertileElement {

  public Footer() {
    super("footer");
  }

  public Footer insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Footer append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Footer append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Footer text(String text) {
    super.appendText(text);
    return this;
  }

  public Footer remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
