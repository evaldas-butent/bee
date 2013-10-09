package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Strong extends FertileElement {

  public Strong() {
    super("strong");
  }

  public Strong insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Strong append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Strong append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Strong text(String text) {
    super.appendText(text);
    return this;
  }

  public Strong remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Strong setD(String value) {
    setAttribute("d", value);
    return this;
  }

  public String getD() {
    return getAttribute("d");
  }

  public boolean removeD() {
    return removeAttribute("d");
  }

  public Strong addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Strong title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Strong style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Strong dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Strong lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Strong setXMLLang(String value) {
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
