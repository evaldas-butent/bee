package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Source extends FertileNode {

  public Source() {
    super("source");
  }

  public Source insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Source append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Source append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Source text(String text) {
    super.appendText(text);
    return this;
  }

  public Source remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
