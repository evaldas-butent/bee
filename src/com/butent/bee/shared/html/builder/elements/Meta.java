package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Meta extends FertileNode {

  public Meta(String content) {
    super("meta");
    setContent(content);
  }

  public Meta insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Meta append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Meta append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Meta text(String text) {
    super.appendText(text);
    return this;
  }

  public Meta remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Meta setContent(String value) {
    setAttribute("content", value);
    return this;
  }

  public String getContent() {
    return getAttribute("content");
  }

  public boolean removeContent() {
    return removeAttribute("content");
  }

  public Meta setHttpEquiv(String value) {
    setAttribute("http-equiv", value);
    return this;
  }

  public String getHttpEquiv() {
    return getAttribute("http-equiv");
  }

  public boolean removeHttpEquiv() {
    return removeAttribute("http-equiv");
  }

  public Meta setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Meta setScheme(String value) {
    setAttribute("scheme", value);
    return this;
  }

  public String getScheme() {
    return getAttribute("scheme");
  }

  public boolean removeScheme() {
    return removeAttribute("scheme");
  }

  public Meta setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Meta setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Meta setXMLLang(String value) {
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
