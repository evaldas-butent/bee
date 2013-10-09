package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Style extends FertileElement {

  public Style(String type) {
    super("style");
    setType(type);
  }

  public Style insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Style append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Style append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Style text(String text) {
    super.appendText(text);
    return this;
  }

  public Style remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Style setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Style setMedia(String value) {
    setAttribute("media", value);
    return this;
  }

  public String getMedia() {
    return getAttribute("media");
  }

  public boolean removeMedia() {
    return removeAttribute("media");
  }

  public Style title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Style dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Style lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Style setXmlspace(String value) {
    setAttribute("xmlspace", value);
    return this;
  }

  public String getXmlspace() {
    return getAttribute("xmlspace");
  }

  public boolean removeXmlspace() {
    return removeAttribute("xmlspace");
  }

}
