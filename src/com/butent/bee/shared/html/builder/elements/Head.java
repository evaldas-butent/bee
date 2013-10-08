package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Head extends FertileNode {

  public Head() {
    super("head");
  }

  public Head insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Head append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Head append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Head text(String text) {
    super.appendText(text);
    return this;
  }

  public Head remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Head setProfile(String value) {
    setAttribute("profile", value);
    return this;
  }

  public String getProfile() {
    return getAttribute("profile");
  }

  public boolean removeProfile() {
    return removeAttribute("profile");
  }

  public Head setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Head setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Head setXMLLang(String value) {
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
