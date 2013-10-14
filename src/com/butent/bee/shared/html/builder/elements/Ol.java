package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Ol extends FertileElement {

  private static final String TYPE_DECIMAL = "1";
  private static final String TYPE_LOWER_ALPHA = "a";
  private static final String TYPE_UPPER_ALPHA = "A";
  private static final String TYPE_LOWER_ROMAN = "i";
  private static final String TYPE_UPPER_ROMAN = "I";

  public Ol() {
    super();
  }

  public Ol addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Ol append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ol append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Ol id(String value) {
    setId(value);
    return this;
  }

  public Ol insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Ol lang(String value) {
    setLang(value);
    return this;
  }

  public Ol remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Ol reversed() {
    setAttribute(Attribute.REVERSED, true);
    return this;
  }

  public Ol start(int value) {
    setAttribute(Attribute.START, value);
    return this;
  }

  public Ol text(String text) {
    super.appendText(text);
    return this;
  }

  public Ol title(String value) {
    setTitle(value);
    return this;
  }

  public Ol type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }

  public Ol typeDecimal() {
    return type(TYPE_DECIMAL);
  }

  public Ol typeLowerAlpha() {
    return type(TYPE_LOWER_ALPHA);
  }

  public Ol typeLowerRoman() {
    return type(TYPE_LOWER_ROMAN);
  }

  public Ol typeUpperAlpha() {
    return type(TYPE_UPPER_ALPHA);
  }

  public Ol typeUpperRoman() {
    return type(TYPE_UPPER_ROMAN);
  }
}
