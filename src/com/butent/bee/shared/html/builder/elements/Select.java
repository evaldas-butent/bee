package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Select extends FertileElement {

  public Select() {
    super("select");
  }

  public Select insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Select append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Select append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Select text(String text) {
    super.appendText(text);
    return this;
  }

  public Select remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Select setDisabled(String value) {
    setAttribute("disabled", value);
    return this;
  }

  public String getDisabled() {
    return getAttribute("disabled");
  }

  public boolean removeDisabled() {
    return removeAttribute("disabled");
  }

  public Select setMultiple(String value) {
    setAttribute("multiple", value);
    return this;
  }

  public String getMultiple() {
    return getAttribute("multiple");
  }

  public boolean removeMultiple() {
    return removeAttribute("multiple");
  }

  public Select setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Select setSize(String value) {
    setAttribute("size", value);
    return this;
  }

  public String getSize() {
    return getAttribute("size");
  }

  public boolean removeSize() {
    return removeAttribute("size");
  }

  public Select id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Select addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Select title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Select style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Select dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Select lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Select setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Select accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public String getAccesskey() {
    return getAttribute("accesskey");
  }

  public boolean removeAccesskey() {
    return removeAttribute("accesskey");
  }

  public Select tabIndex(int value) {
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
