package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Form extends FertileElement {

  public Form() {
    super();
  }

  public Form acceptCharset(String value) {
    setAttribute(Attribute.ACCEPT_CHARSET, value);
    return this;
  }

  public Form action(String value) {
    setAttribute(Attribute.ACTION, value);
    return this;
  }

  public Form addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Form append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Form append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Form autocompleteOff() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_OFF);
    return this;
  }

  public Form autocompleteOn() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_ON);
    return this;
  }

  public Form encTypeMultipart() {
    setAttribute(Attribute.ENC_TYPE, Keywords.ENC_TYPE_MULTIPART_DATA);
    return this;
  }

  public Form encTypeText() {
    setAttribute(Attribute.ENC_TYPE, Keywords.ENC_TYPE_TEXT_PLAIN);
    return this;
  }

  public Form encTypeUrl() {
    setAttribute(Attribute.ENC_TYPE, Keywords.ENC_TYPE_URL_ENCODED);
    return this;
  }

  public Form id(String value) {
    setId(value);
    return this;
  }

  public Form insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Form lang(String value) {
    setLang(value);
    return this;
  }

  public Form methodGet() {
    setAttribute(Attribute.METHOD, Keywords.METHOD_GET);
    return this;
  }

  public Form methodPost() {
    setAttribute(Attribute.METHOD, Keywords.METHOD_POST);
    return this;
  }

  public Form name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Form noValidate() {
    setAttribute(Attribute.NO_VALIDATE, true);
    return this;
  }

  public Form remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Form target(String value) {
    setAttribute(Attribute.TARGET, value);
    return this;
  }

  public Form targetBlank() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Form targetParent() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Form targetSelf() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Form targetTop() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public Form text(String text) {
    super.appendText(text);
    return this;
  }

  public Form title(String value) {
    setTitle(value);
    return this;
  }
}
