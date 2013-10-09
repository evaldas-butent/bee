package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Bdo extends FertileElement {

  public Bdo(String dir) {
    super("bdo");
    setDir(dir);
  }

  public Bdo insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Bdo append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdo append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Bdo text(String text) {
    super.appendText(text);
    return this;
  }

  public Bdo remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Bdo dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Bdo id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Bdo addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Bdo title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Bdo style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Bdo lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Bdo setXMLLang(String value) {
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
