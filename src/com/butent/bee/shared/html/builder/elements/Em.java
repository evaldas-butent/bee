package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Em extends FertileElement {

  public Em() {
    super("em");
  }

  public Em insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Em append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Em append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Em text(String text) {
    super.appendText(text);
    return this;
  }

  public Em remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Em id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Em addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Em title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Em style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Em dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Em lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Em setXMLLang(String value) {
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
