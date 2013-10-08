package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dialog extends FertileNode {

  public Dialog() {
    super("dialog");
  }

  public Dialog insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dialog append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dialog append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dialog text(String text) {
    super.appendText(text);
    return this;
  }

  public Dialog remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
