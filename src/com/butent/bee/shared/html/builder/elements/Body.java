package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Body extends FertileElement {

  public Body() {
    super();
  }

  public Body addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Body append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Body append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Body id(String value) {
    setId(value);
    return this;
  }

  public Body insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Body lang(String value) {
    setLang(value);
    return this;
  }

  public Body onAfterPrint(String value) {
    setAttribute(Attribute.ON_AFTER_PRINT, value);
    return this;
  }

  public Body onBeforePrint(String value) {
    setAttribute(Attribute.ON_BEFORE_PRINT, value);
    return this;
  }

  public Body onBeforeUnload(String value) {
    setAttribute(Attribute.ON_BEFORE_UNLOAD, value);
    return this;
  }

  public Body onHashChange(String value) {
    setAttribute(Attribute.ON_HASH_CHANGE, value);
    return this;
  }

  public Body onMessage(String value) {
    setAttribute(Attribute.ON_MESSAGE, value);
    return this;
  }

  public Body onOffline(String value) {
    setAttribute(Attribute.ON_OFFLINE, value);
    return this;
  }

  public Body onOnline(String value) {
    setAttribute(Attribute.ON_ONLINE, value);
    return this;
  }

  public Body onPageHide(String value) {
    setAttribute(Attribute.ON_PAGE_HIDE, value);
    return this;
  }

  public Body onPageShow(String value) {
    setAttribute(Attribute.ON_PAGE_SHOW, value);
    return this;
  }

  public Body onPopState(String value) {
    setAttribute(Attribute.ON_POP_STATE, value);
    return this;
  }

  public Body onResize(String value) {
    setAttribute(Attribute.ON_RESIZE, value);
    return this;
  }

  public Body onStorage(String value) {
    setAttribute(Attribute.ON_STORAGE, value);
    return this;
  }

  public Body onUnload(String value) {
    setAttribute(Attribute.ON_UNLOAD, value);
    return this;
  }

  public Body remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Body text(String text) {
    super.appendText(text);
    return this;
  }

  public Body title(String value) {
    setTitle(value);
    return this;
  }
}
