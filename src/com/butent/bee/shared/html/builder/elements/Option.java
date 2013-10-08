package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Option extends FertileNode {

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

  public Option setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Option setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Option setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Option setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Option setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Option setLang(String value) {
    setAttribute("lang", value);
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

  public Option setTabindex(String value) {
    setAttribute("tabindex", value);
    return this;
  }

  public String getTabindex() {
    return getAttribute("tabindex");
  }

  public boolean removeTabindex() {
    return removeAttribute("tabindex");
  }

}
