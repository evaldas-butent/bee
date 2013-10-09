package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Del extends FertileElement {

  public Del() {
    super("del");
  }

  public Del insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Del append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Del append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Del text(String text) {
    super.appendText(text);
    return this;
  }

  public Del remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Del setCite(String value) {
    setAttribute("cite", value);
    return this;
  }

  public String getCite() {
    return getAttribute("cite");
  }

  public boolean removeCite() {
    return removeAttribute("cite");
  }

  public Del setDatetime(String value) {
    setAttribute("datetime", value);
    return this;
  }

  public String getDatetime() {
    return getAttribute("datetime");
  }

  public boolean removeDatetime() {
    return removeAttribute("datetime");
  }

  public Del id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Del addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Del title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Del style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Del dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Del lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Del setXMLLang(String value) {
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
