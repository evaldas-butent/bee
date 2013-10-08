package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Strong extends FertileNode {

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

  public Strong setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Strong setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Strong setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Strong setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Strong setLang(String value) {
    setAttribute("lang", value);
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
