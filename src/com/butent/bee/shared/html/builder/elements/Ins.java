package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ins extends FertileElement {

  public Ins() {
    super("ins");
  }

  public Ins insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ins append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ins append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ins text(String text) {
    super.appendText(text);
    return this;
  }

  public Ins remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ins setCite(String value) {
    setAttribute("cite", value);
    return this;
  }

  public String getCite() {
    return getAttribute("cite");
  }

  public boolean removeCite() {
    return removeAttribute("cite");
  }

  public Ins setDatetime(String value) {
    setAttribute("datetime", value);
    return this;
  }

  public String getDatetime() {
    return getAttribute("datetime");
  }

  public boolean removeDatetime() {
    return removeAttribute("datetime");
  }

  public Ins id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Ins addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Ins title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Ins style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Ins dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Ins lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Ins setXMLLang(String value) {
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
