package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Node;

public class Img extends Node {

  public Img(String alt, String src) {
    super("img");
    setAlt(alt);
    setSrc(src);
  }

  public Img setAlt(String value) {
    setAttribute("alt", value);
    return this;
  }

  public String getAlt() {
    return getAttribute("alt");
  }

  public boolean removeAlt() {
    return removeAttribute("alt");
  }

  public Img setSrc(String value) {
    setAttribute("src", value);
    return this;
  }

  public String getSrc() {
    return getAttribute("src");
  }

  public boolean removeSrc() {
    return removeAttribute("src");
  }

  public Img setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Img setBorder(String value) {
    setAttribute("border", value);
    return this;
  }

  public String getBorder() {
    return getAttribute("border");
  }

  public boolean removeBorder() {
    return removeAttribute("border");
  }

  public Img setHeight(String value) {
    setAttribute("height", value);
    return this;
  }

  public String getHeight() {
    return getAttribute("height");
  }

  public boolean removeHeight() {
    return removeAttribute("height");
  }

  public Img setHspace(String value) {
    setAttribute("hspace", value);
    return this;
  }

  public String getHspace() {
    return getAttribute("hspace");
  }

  public boolean removeHspace() {
    return removeAttribute("hspace");
  }

  public Img setIsmap(String value) {
    setAttribute("ismap", value);
    return this;
  }

  public String getIsmap() {
    return getAttribute("ismap");
  }

  public boolean removeIsmap() {
    return removeAttribute("ismap");
  }

  public Img setLongdesc(String value) {
    setAttribute("longdesc", value);
    return this;
  }

  public String getLongdesc() {
    return getAttribute("longdesc");
  }

  public boolean removeLongdesc() {
    return removeAttribute("longdesc");
  }

  public Img setUsemap(String value) {
    setAttribute("usemap", value);
    return this;
  }

  public String getUsemap() {
    return getAttribute("usemap");
  }

  public boolean removeUsemap() {
    return removeAttribute("usemap");
  }

  public Img setVspace(String value) {
    setAttribute("vspace", value);
    return this;
  }

  public String getVspace() {
    return getAttribute("vspace");
  }

  public boolean removeVspace() {
    return removeAttribute("vspace");
  }

  public Img setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Img setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Img setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Img setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Img setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Img setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Img setXMLLang(String value) {
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
