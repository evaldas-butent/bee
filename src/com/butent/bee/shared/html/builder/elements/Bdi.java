package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Bdi extends FertileElement {

  public Bdi() {
    super("big");
  }

  public Bdi insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Bdi append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdi append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdi text(String text) {
    super.appendText(text);
    return this;
  }

  public Bdi remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Bdi id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Bdi addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Bdi title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Bdi style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Bdi dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Bdi lang(String value) {
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
