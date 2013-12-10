package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.html.Autocomplete;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Form extends FertileElement {

  public Form() {
    super();
  }

  public Form acceptCharset(String value) {
    setAttribute(Attributes.ACCEPT_CHARSET, value);
    return this;
  }

  public Form acceptCharsetUtf8() {
    return acceptCharset(BeeConst.CHARSET_UTF8);
  }

  public Form action(String value) {
    setAttribute(Attributes.ACTION, value);
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
    setAttribute(Attributes.AUTOCOMPLETE, Autocomplete.OFF);
    return this;
  }

  public Form autocompleteOn() {
    setAttribute(Attributes.AUTOCOMPLETE, Autocomplete.ON);
    return this;
  }

  public Form encTypeMultipart() {
    setAttribute(Attributes.ENC_TYPE, Keywords.ENC_TYPE_MULTIPART_DATA);
    return this;
  }

  public Form encTypeText() {
    setAttribute(Attributes.ENC_TYPE, Keywords.ENC_TYPE_TEXT_PLAIN);
    return this;
  }

  public Form encTypeUrl() {
    setAttribute(Attributes.ENC_TYPE, Keywords.ENC_TYPE_URL_ENCODED);
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
    setAttribute(Attributes.METHOD, Keywords.METHOD_GET);
    return this;
  }

  public Form methodPost() {
    setAttribute(Attributes.METHOD, Keywords.METHOD_POST);
    return this;
  }

  public Form name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Form noValidate() {
    setAttribute(Attributes.NO_VALIDATE, true);
    return this;
  }

  public Form onSubmit(String value) {
    setAttribute(Attributes.ON_SUBMIT, value);
    return this;
  }

  public Form remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Form target(String value) {
    setAttribute(Attributes.TARGET, value);
    return this;
  }

  public Form targetBlank() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Form targetParent() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Form targetSelf() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Form targetTop() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_TOP);
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
