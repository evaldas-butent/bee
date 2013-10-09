package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H2 extends FertileElement {

  public H2() {
    super("h2");
  }

  public H2 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H2 append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H2 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H2 text(String text) {
    super.appendText(text);
    return this;
  }

  public H2 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H2 setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public H2 id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public H2 addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public H2 title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public H2 style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public H2 dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public H2 lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public H2 setXMLLang(String value) {
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
