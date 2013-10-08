package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Q extends FertileNode {

  public Q() {
    super("q");
  }

  public Q insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Q append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Q text(String text) {
    super.appendText(text);
    return this;
  }

  public Q remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Q setCite(String value) {
    setAttribute("cite", value);
    return this;
  }

  public String getCite() {
    return getAttribute("cite");
  }

  public boolean removeCite() {
    return removeAttribute("cite");
  }

  public Q setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Q setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Q setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Q setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Q setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Q setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Q setXMLLang(String value) {
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
