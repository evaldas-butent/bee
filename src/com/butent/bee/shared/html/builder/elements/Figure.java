package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Figure extends FertileNode {

  public Figure() {
    super("figure");
  }

  public Figure insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Figure append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Figure append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Figure text(String text) {
    super.appendText(text);
    return this;
  }

  public Figure remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
