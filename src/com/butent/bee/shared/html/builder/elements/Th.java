package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
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
    setAttribute(Attribute.ABBR, value);
    return this;
  }

  public Th addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Th append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Th append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Th colSpan(int value) {
    setAttribute(Attribute.COLSPAN, value);
    return this;
  }

  

  public Th headers(String value) {
    setAttribute(Attribute.HEADERS, value);
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
    setAttribute(Attribute.ROWSPAN, value);
    return this;
  }

  public Th scopeCol() {
    setAttribute(Attribute.SCOPE, SCOPE_COL);
    return this;
  }

  public Th scopeColGroup() {
    setAttribute(Attribute.SCOPE, SCOPE_COL_GROUP);
    return this;
  }

  public Th scopeRow() {
    setAttribute(Attribute.SCOPE, SCOPE_ROW);
    return this;
  }

  public Th scopeRowGroup() {
    setAttribute(Attribute.SCOPE, SCOPE_ROW_GROUP);
    return this;
  }

  public Th sorted(int value) {
    setAttribute(Attribute.SORTED, value);
    return this;
  }

  public Th sorted(String value) {
    setAttribute(Attribute.SORTED, value);
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
