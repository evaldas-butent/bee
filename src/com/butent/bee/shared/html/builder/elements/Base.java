package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Base extends FertileElement {

  public Base(String href) {
    super("base");
    setHref(href);
  }

  public Base insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Base append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Base append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Base text(String text) {
    super.appendText(text);
    return this;
  }

  public Base remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Base setHref(String value) {
    setAttribute("href", value);
    return this;
  }

  public String getHref() {
    return getAttribute("href");
  }

  public boolean removeHref() {
    return removeAttribute("href");
  }

  public Base setTarget(String value) {
    setAttribute("target", value);
    return this;
  }

  public String getTarget() {
    return getAttribute("target");
  }

  public boolean removeTarget() {
    return removeAttribute("target");
  }

}
