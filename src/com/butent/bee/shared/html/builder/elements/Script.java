package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Script extends FertileElement {

  public Script(String type) {
    super("script");
    setType(type);
  }

  public Script insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Script append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Script append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Script text(String text) {
    super.appendText(text);
    return this;
  }

  public Script remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Script setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Script setCharset(String value) {
    setAttribute("charset", value);
    return this;
  }

  public String getCharset() {
    return getAttribute("charset");
  }

  public boolean removeCharset() {
    return removeAttribute("charset");
  }

  public Script setDefer(String value) {
    setAttribute("defer", value);
    return this;
  }

  public String getDefer() {
    return getAttribute("defer");
  }

  public boolean removeDefer() {
    return removeAttribute("defer");
  }

  public Script setLanguage(String value) {
    setAttribute("language", value);
    return this;
  }

  public String getLanguage() {
    return getAttribute("language");
  }

  public boolean removeLanguage() {
    return removeAttribute("language");
  }

  public Script setSrc(String value) {
    setAttribute("src", value);
    return this;
  }

  public String getSrc() {
    return getAttribute("src");
  }

  public boolean removeSrc() {
    return removeAttribute("src");
  }

  public Script setXmlspace(String value) {
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
