package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class A extends FertileElement {

  public A() {
    super();
  }

  public A accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public A addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public A append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A download(String value) {
    setAttribute(Attributes.DOWNLOAD, value);
    return this;
  }

  public A href(String value) {
    setAttribute(Attributes.HREF, value);
    return this;
  }

  public A hrefLang(String value) {
    setAttribute(Attributes.HREF_LANG, value);
    return this;
  }

  public A id(String value) {
    setId(value);
    return this;
  }

  public A insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public A lang(String value) {
    setLang(value);
    return this;
  }

  public A rel(String value) {
    setAttribute(Attributes.REL, value);
    return this;
  }

  public A remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public A tabIndex(int value) {
    setTabIndex(value);
    return this;
  }

  public A target(String value) {
    setAttribute(Attributes.TARGET, value);
    return this;
  }

  public A targetBlank() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public A targetParent() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public A targetSelf() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public A targetTop() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public A text(String text) {
    super.appendText(text);
    return this;
  }

  public A title(String value) {
    setTitle(value);
    return this;
  }

  public A type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }
}
