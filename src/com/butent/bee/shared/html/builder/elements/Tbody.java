package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Tbody extends FertileElement {

  public Tbody() {
    super("tbody");
  }

  public Tbody insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Tbody append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tbody append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Tbody text(String text) {
    super.appendText(text);
    return this;
  }

  public Tbody remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Tbody setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Tbody setChar(String value) {
    setAttribute("char", value);
    return this;
  }

  public String getChar() {
    return getAttribute("char");
  }

  public boolean removeChar() {
    return removeAttribute("char");
  }

  public Tbody setCharoff(String value) {
    setAttribute("charoff", value);
    return this;
  }

  public String getCharoff() {
    return getAttribute("charoff");
  }

  public boolean removeCharoff() {
    return removeAttribute("charoff");
  }

  public Tbody setValign(String value) {
    setAttribute("valign", value);
    return this;
  }

  public String getValign() {
    return getAttribute("valign");
  }

  public boolean removeValign() {
    return removeAttribute("valign");
  }

  public Tbody id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Tbody addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Tbody title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Tbody style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Tbody dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Tbody lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Tbody setXMLLang(String value) {
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
