package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class H1 extends FertileNode {

  public H1() {
    super("h1");
  }

  public H1 insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public H1 append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H1 append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public H1 text(String text) {
    super.appendText(text);
    return this;
  }

  public H1 remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public H1 setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public H1 setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public H1 setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public H1 setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public H1 setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public H1 setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public H1 setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public H1 setXMLLang(String value) {
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
