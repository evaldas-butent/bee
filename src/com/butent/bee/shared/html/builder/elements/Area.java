package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.Element;

public class Area extends Element {

  private enum Shape {
    CIRCLE("circle"), DEFAULT("default"), POLY("poly"), RECT("rect");

    private final String keyword;

    Shape(String keyword) {
      this.keyword = keyword;
    }
  }

  public Area() {
    super();
  }

  public Area accessKey(String value) {
    setAccessKey(value);
    return this;
  }

  public Area addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Area alt(String value) {
    setAttribute(Attributes.ALT, value);
    return this;
  }

  public Area coords(String value) {
    setAttribute(Attributes.COORDS, value);
    return this;
  }

  public Area download(String value) {
    setAttribute(Attributes.DOWNLOAD, value);
    return this;
  }

  public Area href(String value) {
    setAttribute(Attributes.HREF, value);
    return this;
  }

  public Area hrefLang(String value) {
    setAttribute(Attributes.HREF_LANG, value);
    return this;
  }

  public Area id(String value) {
    setId(value);
    return this;
  }

  public Area lang(String value) {
    setLang(value);
    return this;
  }

  public Area rel(String value) {
    setAttribute(Attributes.REL, value);
    return this;
  }

  public Area shapeCircle() {
    return setShape(Shape.CIRCLE.keyword);
  }

  public Area shapeDefault() {
    return setShape(Shape.DEFAULT.keyword);
  }

  public Area shapePoly() {
    return setShape(Shape.POLY.keyword);
  }

  public Area shapeRect() {
    return setShape(Shape.RECT.keyword);
  }

  public Area tabIndex(int value) {
    setTabIndex(value);
    return this;
  }

  public Area target(String value) {
    setAttribute(Attributes.TARGET, value);
    return this;
  }

  public Area targetBlank() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Area targetParent() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Area targetSelf() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Area targetTop() {
    setAttribute(Attributes.TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public Area title(String value) {
    setTitle(value);
    return this;
  }

  public Area type(String value) {
    setAttribute(Attributes.TYPE, value);
    return this;
  }

  private Area setShape(String value) {
    setAttribute(Attributes.SHAPE, value);
    return this;
  }
}
