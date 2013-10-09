package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Title extends FertileElement {

  public Title() {
    super("title");
  }

  public Title insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Title append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Title append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Title text(String text) {
    super.appendText(text);
    return this;
  }

  public Title remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Title id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Title addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Title dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Title lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Title style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Title setXMLLang(String value) {
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
