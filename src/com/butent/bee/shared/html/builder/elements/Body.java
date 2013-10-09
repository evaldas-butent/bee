package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Body extends FertileElement {

  public Body() {
    super("body");
  }

  public Body insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Body append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Body append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Body text(String text) {
    super.appendText(text);
    return this;
  }

  public Body remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Body setAlink(String value) {
    setAttribute("alink", value);
    return this;
  }

  public String getAlink() {
    return getAttribute("alink");
  }

  public boolean removeAlink() {
    return removeAttribute("alink");
  }

  public Body setBackground(String value) {
    setAttribute("background", value);
    return this;
  }

  public String getBackground() {
    return getAttribute("background");
  }

  public boolean removeBackground() {
    return removeAttribute("background");
  }

  public Body setBgcolor(String value) {
    setAttribute("bgcolor", value);
    return this;
  }

  public String getBgcolor() {
    return getAttribute("bgcolor");
  }

  public boolean removeBgcolor() {
    return removeAttribute("bgcolor");
  }

  public Body setLink(String value) {
    setAttribute("link", value);
    return this;
  }

  public String getLink() {
    return getAttribute("link");
  }

  public boolean removeLink() {
    return removeAttribute("link");
  }

  public Body setText(String value) {
    setAttribute("text", value);
    return this;
  }

  public String getText() {
    return getAttribute("text");
  }

  public boolean removeText() {
    return removeAttribute("text");
  }

  public Body setVlink(String value) {
    setAttribute("vlink", value);
    return this;
  }

  public String getVlink() {
    return getAttribute("vlink");
  }

  public boolean removeVlink() {
    return removeAttribute("vlink");
  }

  public Body id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Body addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Body title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Body style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Body dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Body lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Body setXMLLang(String value) {
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
