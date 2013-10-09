package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Form extends FertileElement {

  private static final String ACCEPT_CHARSET = "accept-charset";

  public Form(String action) {
    super("form");
    setAction(action);
  }

  public Form setAction(String value) {
    setAttribute("action", value);
    return this;
  }

  public String getAction() {
    return getAttribute("action");
  }

  public boolean removeAction() {
    return removeAttribute("action");
  }

  public Form insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Form append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Form append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Form text(String text) {
    super.appendText(text);
    return this;
  }

  public Form remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Form setAccept(String value) {
    setAttribute("accept", value);
    return this;
  }

  public String getAccept() {
    return getAttribute("accept");
  }

  public boolean removeAccept() {
    return removeAttribute("accept");
  }

  public Form setAcceptCharset(String value) {
    setAttribute(ACCEPT_CHARSET, value);
    return this;
  }

  public String getAcceptCharset() {
    return getAttribute(ACCEPT_CHARSET);
  }

  public boolean removeAcceptCharset() {
    return removeAttribute(ACCEPT_CHARSET);
  }

  public Form setEnctype(String value) {
    setAttribute("enctype", value);
    return this;
  }

  public String getEnctype() {
    return getAttribute("enctype");
  }

  public boolean removeEnctype() {
    return removeAttribute("enctype");
  }

  public Form setMethod(String value) {
    setAttribute("method", value);
    return this;
  }

  public String getMethod() {
    return getAttribute("method");
  }

  public boolean removeMethod() {
    return removeAttribute("method");
  }

  public Form setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Form setTarget(String value) {
    setAttribute("target", value);
    return this;
  }

  public String getTarget() {
    return getAttribute("target");
  }

  public boolean removeTarget() {
    return removeAttribute("target");
  }

  public Form id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Form addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Form title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Form style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Form dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Form lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Form setXMLLang(String value) {
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
