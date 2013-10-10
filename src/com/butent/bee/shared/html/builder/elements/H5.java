package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H5 extends FertileElement {

  public H5() {
    super();
  }

  public H5 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public H5 append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H5 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public H5 id(String value) {
    setId(value);
    return this;
  }

  public H5 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H5 lang(String value) {
    setLang(value);
    return this;
  }

  public H5 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H5 text(String text) {
    super.appendText(text);
    return this;
  }

  public H5 title(String value) {
    setTitle(value);
    return this;
  }
}
