package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Optgroup extends FertileNode {

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

  public Optgroup setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Optgroup setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Optgroup setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Optgroup setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Optgroup setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Optgroup setLang(String value) {
    setAttribute("lang", value);
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
