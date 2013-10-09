package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Blockquote extends FertileElement {

  public Blockquote() {
    super("blockquote");
  }

  public Blockquote insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Blockquote append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Blockquote append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Blockquote text(String text) {
    super.appendText(text);
    return this;
  }

  public Blockquote remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Blockquote setCite(String value) {
    setAttribute("cite", value);
    return this;
  }

  public String getCite() {
    return getAttribute("cite");
  }

  public boolean removeCite() {
    return removeAttribute("cite");
  }

  public Blockquote id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Blockquote addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Blockquote title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Blockquote style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Blockquote dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Blockquote lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Blockquote setXMLLang(String value) {
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
