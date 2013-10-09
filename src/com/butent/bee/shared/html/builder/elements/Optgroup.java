package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Optgroup extends FertileElement {

  public Optgroup(String label) {
    super("optgroup");
    setLabel(label);
  }

  public Optgroup insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Optgroup append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Optgroup append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Optgroup text(String text) {
    super.appendText(text);
    return this;
  }

  public Optgroup remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Optgroup setLabel(String value) {
    setAttribute("label", value);
    return this;
  }

  public String getLabel() {
    return getAttribute("label");
  }

  public boolean removeLabel() {
    return removeAttribute("label");
  }

  public Optgroup setDisabled(String value) {
    setAttribute("disabled", value);
    return this;
  }

  public String getDisabled() {
    return getAttribute("disabled");
  }

  public boolean removeDisabled() {
    return removeAttribute("disabled");
  }

  public Optgroup id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Optgroup addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Optgroup title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Optgroup style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Optgroup dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Optgroup lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Optgroup setXMLLang(String value) {
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
