package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class P extends FertileElement {

  public P() {
    super("p");
  }

  public P insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public P append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public P append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public P text(String text) {
    super.appendText(text);
    return this;
  }

  public P remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public P setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public P id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public P addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public P title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public P style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public P dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public P lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public P setXMLLang(String value) {
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
