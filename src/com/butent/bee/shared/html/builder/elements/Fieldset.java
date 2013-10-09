package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Fieldset extends FertileElement {

  public Fieldset() {
    super("fieldset");
  }

  public Fieldset insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Fieldset append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Fieldset append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Fieldset text(String text) {
    super.appendText(text);
    return this;
  }

  public Fieldset remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Fieldset id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Fieldset addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Fieldset title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Fieldset style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Fieldset dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Fieldset lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Fieldset setXMLLang(String value) {
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
