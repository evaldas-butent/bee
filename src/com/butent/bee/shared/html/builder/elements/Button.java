package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Button extends FertileElement {

  public Button() {
    super("button");
  }

  public Button insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Button append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Button append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Button text(String text) {
    super.appendText(text);
    return this;
  }

  public Button remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Button setDisabled(String value) {
    setAttribute("disabled", value);
    return this;
  }

  public String getDisabled() {
    return getAttribute("disabled");
  }

  public boolean removeDisabled() {
    return removeAttribute("disabled");
  }

  public Button setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Button setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Button setValue(String value) {
    setAttribute("value", value);
    return this;
  }

  public String getValue() {
    return getAttribute("value");
  }

  public boolean removeValue() {
    return removeAttribute("value");
  }

  public Button id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Button addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Button title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Button style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Button dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Button lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Button setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Button accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public String getAccesskey() {
    return getAttribute("accesskey");
  }

  public boolean removeAccesskey() {
    return removeAttribute("accesskey");
  }

  public Button tabIndex(int value) {
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
