package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Small extends FertileElement {

  public Small() {
    super("small");
  }

  public Small insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Small append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Small append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Small text(String text) {
    super.appendText(text);
    return this;
  }

  public Small remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Small id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Small addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Small title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Small style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Small dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Small lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

}
