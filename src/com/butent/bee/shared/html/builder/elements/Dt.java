package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Dt extends FertileElement {

  public Dt() {
    super();
  }

  public Dt addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Dt append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Dt append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Dt id(String value) {
    setId(value);
    return this;
  }

  public Dt insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Dt lang(String value) {
    setLang(value);
    return this;
  }

  public Dt remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Dt text(String text) {
    super.appendText(text);
    return this;
  }

  public Dt title(String value) {
    setTitle(value);
    return this;
  }
}
