package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Label extends FertileElement {

  public Label() {
    super("label");
  }

  public Label insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Label append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Label append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Label text(String text) {
    super.appendText(text);
    return this;
  }

  public Label remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Label setFor(String value) {
    setAttribute("for", value);
    return this;
  }

  public String getFor() {
    return getAttribute("for");
  }

  public boolean removeFor() {
    return removeAttribute("for");
  }

  public Label id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Label addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Label title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Label style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Label dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Label lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Label setXMLLang(String value) {
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
