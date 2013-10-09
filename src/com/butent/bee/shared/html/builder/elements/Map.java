package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Map extends FertileElement {

  public Map(String id) {
    super("map");
    setId(id);
  }

  public Map insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Map append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Map append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Map text(String text) {
    super.appendText(text);
    return this;
  }

  public Map remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Map id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Map setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Map addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Map title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Map style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Map dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Map lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Map setXMLLang(String value) {
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
