package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Button extends FertileElement {

  private static final String TYPE_BUTTON = "button";
  private static final String TYPE_MENU = "menu";
  private static final String TYPE_RESET = "reset";
  private static final String TYPE_SUBMIT = "submit";

  public Button() {
    super();
  }

  public Button addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Button append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Button append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Button autofocus() {
    setAttribute(Attribute.AUTOFOCUS, true);
    return this;
  }

  public Button disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Button enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Button form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Button formAction(String value) {
    setAttribute(Attribute.FORM_ACTION, value);
    return this;
  }

  public Button formEncTypeMultipart() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_MULTIPART_DATA);
    return this;
  }

  public Button formEncTypeText() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_TEXT_PLAIN);
    return this;
  }

  public Button formEncTypeUrl() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_URL_ENCODED);
    return this;
  }

  public Button formMethodGet() {
    setAttribute(Attribute.FORM_METHOD, Keywords.METHOD_GET);
    return this;
  }

  public Button formMethodPost() {
    setAttribute(Attribute.FORM_METHOD, Keywords.METHOD_POST);
    return this;
  }

  public Button formNoValidate() {
    setAttribute(Attribute.FORM_NO_VALIDATE, true);
    return this;
  }

  public Button formTarget(String value) {
    setAttribute(Attribute.FORM_TARGET, value);
    return this;
  }

  public Button formTargetBlank() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Button formTargetParent() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Button formTargetSelf() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Button formTargetTop() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public Button id(String value) {
    setId(value);
    return this;
  }

  public Button insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Button lang(String value) {
    setLang(value);
    return this;
  }

  public Button menu(String value) {
    setAttribute(Attribute.MENU, value);
    return this;
  }

  public Button name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Button remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Button text(String text) {
    super.appendText(text);
    return this;
  }

  public Button title(String value) {
    setTitle(value);
    return this;
  }

  public Button typeButton() {
    setAttribute(Attribute.TYPE, TYPE_BUTTON);
    return this;
  }

  public Button typeMenu() {
    setAttribute(Attribute.TYPE, TYPE_MENU);
    return this;
  }

  public Button typeReset() {
    setAttribute(Attribute.TYPE, TYPE_RESET);
    return this;
  }

  public Button typeSubmit() {
    setAttribute(Attribute.TYPE, TYPE_SUBMIT);
    return this;
  }

  public Button value(String value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }
}
