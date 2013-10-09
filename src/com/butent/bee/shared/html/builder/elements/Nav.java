package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Nav extends FertileElement {

  public Nav() {
    super("nav");
  }

  public Nav insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Nav append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Nav append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Nav text(String text) {
    super.appendText(text);
    return this;
  }

  public Nav remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
