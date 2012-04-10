package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

/**
 * Implements a class for panels that can contain multiple child widgets and manages their creation
 * at the specific areas of the layout.
 */

public class Complex extends ComplexPanel implements HasId, ProvidesResize, RequiresResize {

  public Complex() {
    this(Position.ABSOLUTE);
  }
  
  public Complex(Position position) {
    super();
    setElement(DOM.createDiv());
    
    if (position != null) {
      getElement().getStyle().setPosition(position);
    }
    getElement().getStyle().setOverflow(Overflow.HIDDEN);

    init();
  }

  @Override
  public void add(Widget child) {
    Assert.notNull(child);
    super.add(child, getElement());
  }

  public void add(Widget child, Edges edges) {
    Assert.notNull(child);
    if (edges != null && !edges.isEmpty()) {
      setChildPosition(child);
      edges.applyPosition(child);
    }
    add(child);
  }
  
  public void addLeftBottom(Widget child, double left, Unit leftUnit, double bottom,
      Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }

  public void addLeftBottom(Widget child, int left, int bottom) {
    addLeftBottom(child, left, Unit.PX, bottom, Unit.PX);
  }

  public void addLeftRightTop(Widget child, double left, Unit leftUnit,
      double right, Unit rightUnit, double top, Unit topUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildRight(child, right, rightUnit);
    setChildTop(child, top, topUnit);
    add(child);
  }
  
  public void addLeftRightTop(Widget child, int left, int right, int top) {
    addLeftRightTop(child, left, Unit.PX, right, Unit.PX, top, Unit.PX);
  }

  public void addLeftRightTopBottom(Widget child, double left, Unit leftUnit,
      double right, Unit rightUnit, double top, Unit topUnit, double bottom, Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildRight(child, right, rightUnit);
    setChildTop(child, top, topUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }
  
  public void addLeftRightTopBottom(Widget child, int left, int right, int top, int bottom) {
    addLeftRightTopBottom(child, left, Unit.PX, right, Unit.PX, top, Unit.PX, bottom, Unit.PX);
  }

  public void addLeftRightTopHeight(Widget child, double left, Unit leftUnit,
      double right, Unit rightUnit, double top, Unit topUnit, double height, Unit heightUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildRight(child, right, rightUnit);
    setChildTop(child, top, topUnit);
    setChildHeight(child, height, heightUnit);
    add(child);
  }
  
  public void addLeftRightTopHeight(Widget child, int left, int right, int top, int height) {
    addLeftRightTopHeight(child, left, Unit.PX, right, Unit.PX, top, Unit.PX, height, Unit.PX);
  }
  
  public void addLeftTop(Widget child, double left, Unit leftUnit, double top, Unit topUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildTop(child, top, topUnit);
    add(child);
  }
  
  public void addLeftTop(Widget child, int left, int top) {
    addLeftTop(child, left, Unit.PX, top, Unit.PX);
  }

  public void addLeftWidthTopBottom(Widget child, double left, Unit leftUnit,
      double width, Unit widthUnit, double top, Unit topUnit, double bottom, Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftUnit);
    setChildWidth(child, width, widthUnit);
    setChildTop(child, top, topUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }
  
  public void addLeftWidthTopBottom(Widget child, int left, int width, int top, int bottom) {
    addLeftWidthTopBottom(child, left, Unit.PX, width, Unit.PX, top, Unit.PX, bottom, Unit.PX);
  }

  public void addRightBottom(Widget child, double right, Unit rightUnit, double bottom,
      Unit bottomUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightUnit);
    setChildBottom(child, bottom, bottomUnit);
    add(child);
  }

  public void addRightBottom(Widget child, int right, int bottom) {
    addRightBottom(child, right, Unit.PX, bottom, Unit.PX);
  }
  
  public void addRightTop(Widget child, double right, Unit rightUnit, double top, Unit topUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightUnit);
    setChildTop(child, top, topUnit);
    add(child);
  }

  public void addRightTop(Widget child, int right, int top) {
    addRightTop(child, right, Unit.PX, top, Unit.PX);
  }

  public void addTopBottomFillHorizontal(Widget child, double top, Unit topUnit,
      double bottom, Unit bottomUnit) {
    addLeftRightTopBottom(child, 0, Unit.PX, 0, Unit.PX, top, topUnit, bottom, bottomUnit);
  }

  public void addTopBottomFillHorizontal(Widget child, int top, int bottom) {
    addTopBottomFillHorizontal(child, top, Unit.PX, bottom, Unit.PX);
  }

  public void addTopHeightFillHorizontal(Widget child, double top, Unit topUnit,
      double height, Unit heightUnit) {
    addLeftRightTopHeight(child, 0, Unit.PX, 0, Unit.PX, top, topUnit, height, heightUnit);
  }

  public void addTopHeightFillHorizontal(Widget child, int top, int height) {
    addTopHeightFillHorizontal(child, top, Unit.PX, height, Unit.PX);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "complex";
  }

  public void onResize() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresResize) {
        ((RequiresResize) child).onResize();
      }
    }
  }
  
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }

  private void setChildBottom(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setBottom(value, unit);
  }

  private void setChildHeight(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setHeight(value, unit);
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

  private void setChildWidth(Widget child, double value, Unit unit) {
    child.getElement().getStyle().setWidth(value, unit);
  }
}
