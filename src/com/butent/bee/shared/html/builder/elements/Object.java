package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Object extends FertileElement {

  public Object() {
    super();
  }

  public Object addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Object append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object data(String value) {
    setAttribute(Attribute.DATA, value);
    return this;
  }

  

  public Object form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Object height(int value) {
    setAttribute(Attribute.HEIGHT, value);
    return this;
  }

  public Object id(String value) {
    setId(value);
    return this;
  }

  public Object insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Object lang(String value) {
    setLang(value);
    return this;
  }
  
  public Object name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Object remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Object text(String text) {
    super.appendText(text);
    return this;
  }

  public Object title(String value) {
    setTitle(value);
    return this;
  }

  public Object type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }
  
  public Object typeMustMatch() {
    setAttribute(Attribute.TYPEMUSTMATCH, true);
    return this;
  }

  public Object useMap(String value) {
    setAttribute(Attribute.USEMAP, value);
    return this;
  }

  public Object width(int value) {
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
  
}
