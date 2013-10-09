package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ol extends FertileElement {

  public Ol() {
    super("ol");
  }

  public Ol insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ol append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ol append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ol text(String text) {
    super.appendText(text);
    return this;
  }

  public Ol remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ol setCompact(String value) {
    setAttribute("compact", value);
    return this;
  }

  public String getCompact() {
    return getAttribute("compact");
  }

  public boolean removeCompact() {
    return removeAttribute("compact");
  }

  public Ol setStart(String value) {
    setAttribute("start", value);
    return this;
  }

  public String getStart() {
    return getAttribute("start");
  }

  public boolean removeStart() {
    return removeAttribute("start");
  }

  public Ol setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Ol id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Ol addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Ol title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Ol style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Ol dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Ol lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Ol setXMLLang(String value) {
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
