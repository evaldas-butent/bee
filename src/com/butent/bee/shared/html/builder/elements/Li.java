package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Li extends FertileElement {

  public Li() {
    super("li");
  }

  public Li insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Li append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Li append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Li text(String text) {
    super.appendText(text);
    return this;
  }

  public Li remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Li setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Li setValue(String value) {
    setAttribute("value", value);
    return this;
  }

  public String getValue() {
    return getAttribute("value");
  }

  public boolean removeValue() {
    return removeAttribute("value");
  }

  public Li id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Li addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Li title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Li style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Li dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Li lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Li setXMLLang(String value) {
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
