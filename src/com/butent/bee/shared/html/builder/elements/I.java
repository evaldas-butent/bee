package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class I extends FertileElement {

  public I() {
    super("i");
  }

  public I insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public I append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public I append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public I text(String text) {
    super.appendText(text);
    return this;
  }

  public I remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public I id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public I addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public I title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public I style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public I dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public I lang(String value) {
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
