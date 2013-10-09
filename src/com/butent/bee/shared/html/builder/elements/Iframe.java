package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Element;

public class Iframe extends Element {

  public Iframe() {
    super("iframe");
  }

  public Iframe setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Iframe setFrameborder(String value) {
    setAttribute("frameborder", value);
    return this;
  }

  public String getFrameborder() {
    return getAttribute("frameborder");
  }

  public boolean removeFrameborder() {
    return removeAttribute("frameborder");
  }

  public Iframe setHeight(String value) {
    setAttribute("height", value);
    return this;
  }

  public String getHeight() {
    return getAttribute("height");
  }

  public boolean removeHeight() {
    return removeAttribute("height");
  }

  public Iframe setLongdesc(String value) {
    setAttribute("longdesc", value);
    return this;
  }

  public String getLongdesc() {
    return getAttribute("longdesc");
  }

  public boolean removeLongdesc() {
    return removeAttribute("longdesc");
  }

  public Iframe setMarginheight(String value) {
    setAttribute("marginheight", value);
    return this;
  }

  public String getMarginheight() {
    return getAttribute("marginheight");
  }

  public boolean removeMarginheight() {
    return removeAttribute("marginheight");
  }

  public Iframe setMarginwidth(String value) {
    setAttribute("marginwidth", value);
    return this;
  }

  public String getMarginwidth() {
    return getAttribute("marginwidth");
  }

  public boolean removeMarginwidth() {
    return removeAttribute("marginwidth");
  }

  public Iframe setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Iframe setScrolling(String value) {
    setAttribute("scrolling", value);
    return this;
  }

  public String getScrolling() {
    return getAttribute("scrolling");
  }

  public boolean removeScrolling() {
    return removeAttribute("scrolling");
  }

  public Iframe setSrc(String value) {
    setAttribute("src", value);
    return this;
  }

  public String getSrc() {
    return getAttribute("src");
  }

  public boolean removeSrc() {
    return removeAttribute("src");
  }

  public Iframe setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Iframe id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Iframe addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Iframe title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Iframe style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

}
