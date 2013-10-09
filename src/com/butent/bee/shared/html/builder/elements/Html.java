package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Html extends FertileElement {

  public Html() {
    super("html");
    setXmlns("http://www.w3.org/1999/xhtml");
  }

  public Html setXmlns(String value) {
    setAttribute("xmlns", value);
    return this;
  }

  public String getXmlns() {
    return getAttribute("xmlns");
  }

  public boolean removeXmlns() {
    return removeAttribute("xmlns");
  }

  public Html insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Html append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Html append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Html text(String text) {
    super.appendText(text);
    return this;
  }

  public Html remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Html dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Html lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Html setXMLLang(String value) {
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
