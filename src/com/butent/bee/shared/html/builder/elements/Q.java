package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Q extends FertileElement {

  public Q() {
    super("q");
  }

  public Q insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Q append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q text(String text) {
    super.appendText(text);
    return this;
  }

  public Q remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Q setCite(String value) {
    setAttribute("cite", value);
    return this;
  }

  public String getCite() {
    return getAttribute("cite");
  }

  public boolean removeCite() {
    return removeAttribute("cite");
  }

  public Q id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Q addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Q title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Q style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Q dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Q lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Q setXMLLang(String value) {
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
