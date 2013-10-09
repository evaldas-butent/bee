package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Hr extends Element {

  public Hr() {
    super("hr");
  }

  public Hr setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Hr setNoshade(String value) {
    setAttribute("noshade", value);
    return this;
  }

  public String getNoshade() {
    return getAttribute("noshade");
  }

  public boolean removeNoshade() {
    return removeAttribute("noshade");
  }

  public Hr setSize(String value) {
    setAttribute("size", value);
    return this;
  }

  public String getSize() {
    return getAttribute("size");
  }

  public boolean removeSize() {
    return removeAttribute("size");
  }

  public Hr setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Hr id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Hr addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Hr title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Hr style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Hr dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Hr lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Hr setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

}
