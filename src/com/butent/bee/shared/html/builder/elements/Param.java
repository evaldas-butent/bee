package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Param extends Element {

  public Param(String name) {
    super("param");
    setName(name);
  }

  public Param setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Param setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Param setValue(String value) {
    setAttribute("value", value);
    return this;
  }

  public String getValue() {
    return getAttribute("value");
  }

  public boolean removeValue() {
    return removeAttribute("value");
  }

  public Param setValuetype(String value) {
    setAttribute("valuetype", value);
    return this;
  }

  public String getValuetype() {
    return getAttribute("valuetype");
  }

  public boolean removeValuetype() {
    return removeAttribute("valuetype");
  }

  public Param id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

}
