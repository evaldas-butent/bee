package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Section extends FertileElement {

  public Section() {
    super();
  }

  public Section addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Section append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Section append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Section id(String value) {
    setId(value);
    return this;
  }

  public Section insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Section lang(String value) {
    setLang(value);
    return this;
  }

  public Section remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Section text(String text) {
    super.appendText(text);
    return this;
  }

  public Section title(String value) {
    setTitle(value);
    return this;
  }
}
