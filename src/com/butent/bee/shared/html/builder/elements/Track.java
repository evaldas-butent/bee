package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Track extends FertileElement {

  public Track() {
    super("track");
  }

  public Track insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Track append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Track append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Track text(String text) {
    super.appendText(text);
    return this;
  }

  public Track remove(Node child) {
    super.removeChild(child);
    return this;
  }
}
