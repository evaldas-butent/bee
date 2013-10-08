package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Div extends FertileNode {

  public Div() {
    super("div");
  }

  public Div insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Div append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Div append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Div text(String text) {
    super.appendText(text);
    return this;
  }

  public Div remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Div setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Div setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Div setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Div setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Div setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Div setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Div setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Div setXMLLang(String value) {
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
