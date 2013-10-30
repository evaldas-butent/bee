package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Th extends FertileElement {

  private static final String SCOPE_COL = "col";
  private static final String SCOPE_COL_GROUP = "colgroup";
  private static final String SCOPE_ROW = "row";
  private static final String SCOPE_ROW_GROUP = "rowgroup";

  public Th() {
    super();
  }

  public Th abbr(String value) {
    setAttribute(Attributes.ABBR, value);
    return this;
  }

  public Th addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Th append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Th append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Th colSpan(int value) {
    setAttribute(Attributes.COL_SPAN, value);
    return this;
  }

  public Th headers(String value) {
    setAttribute(Attributes.HEADERS, value);
    return this;
  }

  public Th id(String value) {
    setId(value);
    return this;
  }

  public Th insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Th lang(String value) {
    setLang(value);
    return this;
  }

  public Th remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Th rowSpan(int value) {
    setAttribute(Attributes.ROW_SPAN, value);
    return this;
  }

  public Th scopeCol() {
    setAttribute(Attributes.SCOPE, SCOPE_COL);
    return this;
  }

  public Th scopeColGroup() {
    setAttribute(Attributes.SCOPE, SCOPE_COL_GROUP);
    return this;
  }

  public Th scopeRow() {
    setAttribute(Attributes.SCOPE, SCOPE_ROW);
    return this;
  }

  public Th scopeRowGroup() {
    setAttribute(Attributes.SCOPE, SCOPE_ROW_GROUP);
    return this;
  }

  public Th sorted(int value) {
    setAttribute(Attributes.SORTED, value);
    return this;
  }

  public Th sorted(String value) {
    setAttribute(Attributes.SORTED, value);
    return this;
  }

  public Th text(String text) {
    super.appendText(text);
    return this;
  }

  public Th title(String value) {
    setTitle(value);
    return this;
  }
}
