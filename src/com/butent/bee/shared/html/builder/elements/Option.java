package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Option extends FertileElement {

  public Option() {
    super("option");
  }

  public Option insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Option append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Option append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Option text(String text) {
    super.appendText(text);
    return this;
  }

  public Option remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Option setDisabled(String value) {
    setAttribute("disabled", value);
    return this;
  }

  public String getDisabled() {
    return getAttribute("disabled");
  }

  public boolean removeDisabled() {
    return removeAttribute("disabled");
  }

  public Option setLabel(String value) {
    setAttribute("label", value);
    return this;
  }

  public String getLabel() {
    return getAttribute("label");
  }

  public boolean removeLabel() {
    return removeAttribute("label");
  }

  public Option setSelected(String value) {
    setAttribute("selected", value);
    return this;
  }

  public String getSelected() {
    return getAttribute("selected");
  }

  public boolean removeSelected() {
    return removeAttribute("selected");
  }

  public Option setValue(String value) {
    setAttribute("value", value);
    return this;
  }

  public String getValue() {
    return getAttribute("value");
  }

  public boolean removeValue() {
    return removeAttribute("value");
  }

  public Option id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Option addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Option title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Option style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Option dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Option lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Option setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Option tabIndex(int value) {
    setTabIndex(value);
    return this;
  }

  public String getTabindex() {
    return getAttribute("tabindex");
  }

  public boolean removeTabindex() {
    return removeAttribute("tabindex");
  }

}
