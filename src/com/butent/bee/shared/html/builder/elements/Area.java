package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Area extends FertileElement {

  public Area(String alt) {
    super("area");
    setAlt(alt);
  }

  public Area insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Area append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Area append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Area text(String text) {
    super.appendText(text);
    return this;
  }

  public Area remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Area setAlt(String value) {
    setAttribute("alt", value);
    return this;
  }

  public String getAlt() {
    return getAttribute("alt");
  }

  public boolean removeAlt() {
    return removeAttribute("alt");
  }

  public Area setCoords(String value) {
    setAttribute("coords", value);
    return this;
  }

  public String getCoords() {
    return getAttribute("coords");
  }

  public boolean removeCoords() {
    return removeAttribute("coords");
  }

  public Area setHref(String value) {
    setAttribute("href", value);
    return this;
  }

  public String getHref() {
    return getAttribute("href");
  }

  public boolean removeHref() {
    return removeAttribute("href");
  }

  public Area setNohref(String value) {
    setAttribute("nohref", value);
    return this;
  }

  public String getNohref() {
    return getAttribute("nohref");
  }

  public boolean removeNohref() {
    return removeAttribute("nohref");
  }

  public Area setShape(String value) {
    setAttribute("shape", value);
    return this;
  }

  public String getShape() {
    return getAttribute("shape");
  }

  public boolean removeShape() {
    return removeAttribute("shape");
  }

  public Area setTarget(String value) {
    setAttribute("target", value);
    return this;
  }

  public String getTarget() {
    return getAttribute("target");
  }

  public boolean removeTarget() {
    return removeAttribute("target");
  }

  public Area id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Area addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Area title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Area style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Area dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Area lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Area setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Area tabIndex(int value) {
    setTabIndex(value);
    return this;
  }

  public String getTabindex() {
    return getAttribute("tabindex");
  }

  public boolean removeTabindex() {
    return removeAttribute("tabindex");
  }

  public Area accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public String getAccesskey() {
    return getAttribute("accesskey");
  }

  public boolean removeAccesskey() {
    return removeAttribute("accesskey");
  }

}
