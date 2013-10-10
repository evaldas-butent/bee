package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Article extends FertileElement {

  public Article() {
    super();
  }

  public Article addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Article append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Article append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Article id(String value) {
    setId(value);
    return this;
  }

  public Article insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Article lang(String value) {
    setLang(value);
    return this;
  }

  public Article remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Article text(String text) {
    super.appendText(text);
    return this;
  }

  public Article title(String value) {
    setTitle(value);
    return this;
  }
}
