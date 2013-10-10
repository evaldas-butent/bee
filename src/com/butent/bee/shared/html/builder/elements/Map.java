package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Map extends FertileElement {

  public Map() {
    super();
  }

  public Map addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Map append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Map append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  

  public Map id(String value) {
    setId(value);
    return this;
  }

  public Map insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Map lang(String value) {
    setLang(value);
    return this;
  }

  public Map name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Map remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Map text(String text) {
    super.appendText(text);
    return this;
  }

  public Map title(String value) {
    setTitle(value);
    return this;
  }
}
