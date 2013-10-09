package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ul extends FertileElement {

  public Ul() {
    super("ul");
  }

  public Ul insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ul append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ul append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ul text(String text) {
    super.appendText(text);
    return this;
  }

  public Ul remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ul setCompact(String value) {
    setAttribute("compact", value);
    return this;
  }

  public String getCompact() {
    return getAttribute("compact");
  }

  public boolean removeCompact() {
    return removeAttribute("compact");
  }

  public Ul setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Ul id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Ul addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Ul title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Ul style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Ul dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Ul lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Ul setXMLLang(String value) {
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
