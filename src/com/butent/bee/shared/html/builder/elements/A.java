package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class A extends FertileElement {

  public A() {
    super();
  }

  public A accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public A addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public A append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A dir(String value) {
    setDir(value);
    return this;
  }

  public A href(String value) {
    setAttribute(Attribute.HREF, value);
    return this;
  }

  public A hrefLang(String value) {
    setAttribute(Attribute.HREFLANG, value);
    return this;
  }

  public A id(String value) {
    setId(value);
    return this;
  }

  public A insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public A lang(String value) {
    setLang(value);
    return this;
  }

  public A rel(String value) {
    setAttribute(Attribute.REL, value);
    return this;
  }

  public A remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public A style(String value) {
    setStyle(value);
    return this;
  }

  public A tabIndex(int value) {
    setTabIndex(value);
    return this;
  }

  public A target(String value) {
    setAttribute(Attribute.TARGET, value);
    return this;
  }

  public A text(String text) {
    super.appendText(text);
    return this;
  }

  public A type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }

  public A title(String value) {
    setTitle(value);
    return this;
  }
}
