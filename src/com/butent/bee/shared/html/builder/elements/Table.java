package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.BorderCollapse;
import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Table extends FertileElement {

  private static final String BORDER = "1";

  public Table() {
    super();
  }

  public Table addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Table append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Table append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Table border() {
    setAttribute(Attribute.BORDER, BORDER);
    return this;
  }

  public Table borderCollapse() {
    setBorderCollapse(BorderCollapse.COLLAPSE);
    return this;
  }

  public Table borderSeparate() {
    setBorderCollapse(BorderCollapse.SEPARATE);
    return this;
  }

  public Table id(String value) {
    setId(value);
    return this;
  }

  public Table insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Table lang(String value) {
    setLang(value);
    return this;
  }

  public Table marginBottom(int value, CssUnit unit) {
    setMarginBottom(value, unit);
    return this;
  }

  public Table marginLeft(int value, CssUnit unit) {
    setMarginLeft(value, unit);
    return this;
  }

  public Table marginRight(int value, CssUnit unit) {
    setMarginRight(value, unit);
    return this;
  }

  public Table marginTop(int value, CssUnit unit) {
    setMarginTop(value, unit);
    return this;
  }

  public Table remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Table sortable() {
    setAttribute(Attribute.SORTABLE, true);
    return this;
  }

  public Table text(String text) {
    super.appendText(text);
    return this;
  }

  public Table title(String value) {
    setTitle(value);
    return this;
  }
}
