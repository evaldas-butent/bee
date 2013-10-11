package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Keywords;
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

  public A append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public A download(String value) {
    setAttribute(Attribute.DOWNLOAD, value);
    return this;
  }

  public A href(String value) {
    setAttribute(Attribute.HREF, value);
    return this;
  }

  public A hrefLang(String value) {
    setAttribute(Attribute.HREF_LANG, value);
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
    setAttribute(Attribute.REL, value);
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
    setAttribute(Attribute.TARGET, value);
    return this;
  }
  
  public A targetBlank() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }
  
  public A targetParent() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public A targetSelf() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }
  
  public A targetTop() {
    setAttribute(Attribute.TARGET, Keywords.BROWSING_CONTEXT_TOP);
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
    setAttribute(Attribute.TYPE, value);
    return this;
  }
}
