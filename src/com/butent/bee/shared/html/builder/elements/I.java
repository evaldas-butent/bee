package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class I extends FertileElement {

  public I() {
    super();
  }

  public I addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public I append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public I append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public I id(String value) {
    setId(value);
    return this;
  }

  public I insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public I lang(String value) {
    setLang(value);
    return this;
  }

  public I remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public I text(String text) {
    super.appendText(text);
    return this;
  }

  public I title(String value) {
    setTitle(value);
    return this;
  }
}
