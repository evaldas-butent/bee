package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class B extends FertileElement {

  public B() {
    super("b");
  }

  public B insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public B append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public B append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public B text(String text) {
    super.appendText(text);
    return this;
  }

  public B remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public B id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public B addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public B title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public B style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public B dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public B lang(String value) {
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
