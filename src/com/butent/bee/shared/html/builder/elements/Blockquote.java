package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Blockquote extends FertileElement {

  public Blockquote() {
    super();
  }

  public Blockquote addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Blockquote append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Blockquote append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Blockquote cite(String value) {
    setAttribute(Attributes.CITE, value);
    return this;
  }

  public Blockquote id(String value) {
    setId(value);
    return this;
  }

  public Blockquote insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Blockquote lang(String value) {
    setLang(value);
    return this;
  }

  public Blockquote remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Blockquote text(String text) {
    super.appendText(text);
    return this;
  }

  public Blockquote title(String value) {
    setTitle(value);
    return this;
  }
}
