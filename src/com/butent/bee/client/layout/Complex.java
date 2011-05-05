package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

/**
 * Implements a class for panels that can contain multiple child widgets and manages their creation
 * at the specific areas of the layout.
 */

public class Complex extends ComplexPanel implements HasId {

  public Complex() {
    super();
    setElement(DOM.createDiv());

    getElement().getStyle().setPosition(Position.RELATIVE);
    getElement().getStyle().setOverflow(Overflow.HIDDEN);

    init();
  }

  @Override
  public void add(Widget child) {
    Assert.notNull(child);
    super.add(child, getElement());
  }

  public void addLeftBottom(Widget child, int left, int bottom) {
    addLeftBottom(child, left, Unit.PX, bottom, Unit.PX);
  }

  public void addLeftBottom(Widget child, double left, Unit leftUnit, double bottom,
      Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }

  public void addLeftTop(Widget child, int left, int top) {
    addLeftTop(child, left, Unit.PX, top, Unit.PX);
  }

  public void addLeftTop(Widget child, double left, Unit leftUnit, double top, Unit topUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildTop(child, top, topUnit);
    add(child);
  }

  public void addRightBottom(Widget child, int right, int bottom) {
    addRightBottom(child, right, Unit.PX, bottom, Unit.PX);
  }

  public void addRightBottom(Widget child, double right, Unit rightUnit, double bottom,
      Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }

  public void addRightTop(Widget child, int right, int top) {
    addRightTop(child, right, Unit.PX, top, Unit.PX);
  }

  public void addRightTop(Widget child, double right, Unit rightUnit, double top, Unit topUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightUnit);
    setChildTop(child, top, topUnit);
    add(child);
  }

  public void createId() {
    DomUtils.createId(this, "complex");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
  }

  private void setChildBottom(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setBottom(value, unit);
  }

  private void setChildLeft(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setLeft(value, unit);
  }

  private void setChildPosition(Widget child) {
    child.getElement().getStyle().setPosition(Position.ABSOLUTE);
  }

  private void setChildRight(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setRight(value, unit);
  }

  private void setChildTop(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setTop(value, unit);
  }
}
