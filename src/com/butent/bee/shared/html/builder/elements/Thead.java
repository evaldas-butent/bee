package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Thead extends FertileElement {

  public Thead() {
    super("thead");
  }

  public Thead insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Thead append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Thead append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Thead text(String text) {
    super.appendText(text);
    return this;
  }

  public Thead remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Thead setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Thead setChar(String value) {
    setAttribute("char", value);
    return this;
  }

  public String getChar() {
    return getAttribute("char");
  }

  public boolean removeChar() {
    return removeAttribute("char");
  }

  public Thead setCharoff(String value) {
    setAttribute("charoff", value);
    return this;
  }

  public String getCharoff() {
    return getAttribute("charoff");
  }

  public boolean removeCharoff() {
    return removeAttribute("charoff");
  }

  public Thead setValign(String value) {
    setAttribute("valign", value);
    return this;
  }

  public String getValign() {
    return getAttribute("valign");
  }

  public boolean removeValign() {
    return removeAttribute("valign");
  }

  public Thead id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Thead addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Thead title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Thead style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Thead dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Thead lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Thead setXMLLang(String value) {
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
