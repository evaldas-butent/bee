package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Pre extends FertileElement {

  public Pre() {
    super("pre");
  }

  public Pre insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Pre append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Pre append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Pre text(String text) {
    super.appendText(text);
    return this;
  }

  public Pre remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Pre setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Pre id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Pre addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Pre title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Pre style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Pre dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Pre lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Pre setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Pre setXmlspace(String value) {
    setAttribute("xmlspace", value);
    return this;
  }

  public String getXmlspace() {
    return getAttribute("xmlspace");
  }

  public boolean removeXmlspace() {
    return removeAttribute("xmlspace");
  }

}
