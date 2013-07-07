package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.CssUnit;

/**
 * Implements a class for panels that can contain multiple child widgets and manages their creation
 * at the specific areas of the layout.
 */

public class Complex extends ComplexPanel implements IdentifiableWidget, ProvidesResize,
    RequiresResize, InsertPanel, HasAllDragAndDropHandlers {

  public Complex() {
    this(Position.ABSOLUTE);
  }

  public Complex(Position position) {
    this(position, Overflow.HIDDEN);
  }
  
  public Complex(Position position, Overflow overflow) {
    super();
    setElement(DOM.createDiv());

    init(position, overflow);
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

  @Override
  public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
    return addBitlessDomHandler(handler, DragEndEvent.getType());
  }

  @Override
  public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
    return addBitlessDomHandler(handler, DragEnterEvent.getType());
  }

  @Override
  public HandlerRegistration addDragHandler(DragHandler handler) {
    return addBitlessDomHandler(handler, DragEvent.getType());
  }

  @Override
  public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
    return addBitlessDomHandler(handler, DragLeaveEvent.getType());
  }

  @Override
  public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
    return addBitlessDomHandler(handler, DragOverEvent.getType());
  }

  @Override
  public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
    return addBitlessDomHandler(handler, DragStartEvent.getType());
  }

  @Override
  public HandlerRegistration addDropHandler(DropHandler handler) {
    return addBitlessDomHandler(handler, DropEvent.getType());
  }
  
  public void addLeftBottom(Widget child, double left, CssUnit leftCssUnit,
      double bottom, CssUnit bottomCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildBottom(child, bottom, bottomCssUnit);
    add(child);
  }

  public void addLeftBottom(Widget child, int left, int bottom) {
    addLeftBottom(child, left, CssUnit.PX, bottom, CssUnit.PX);
  }

  public void addLeftRightTop(Widget child, double left, CssUnit leftCssUnit,
      double right, CssUnit rightCssUnit, double top, CssUnit topCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildRight(child, right, rightCssUnit);
    setChildTop(child, top, topCssUnit);
    add(child);
  }

  public void addLeftRightTop(Widget child, int left, int right, int top) {
    addLeftRightTop(child, left, CssUnit.PX, right, CssUnit.PX, top, CssUnit.PX);
  }

  public void addLeftRightTopBottom(Widget child, double left, CssUnit leftCssUnit,
      double right, CssUnit rightCssUnit, double top, CssUnit topCssUnit,
      double bottom, CssUnit bottomCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildRight(child, right, rightCssUnit);
    setChildTop(child, top, topCssUnit);
    setChildBottom(child, bottom, bottomCssUnit);
    add(child);
  }

  public void addLeftRightTopBottom(Widget child, int left, int right, int top, int bottom) {
    addLeftRightTopBottom(child, left, CssUnit.PX, right, CssUnit.PX, top, CssUnit.PX,
        bottom, CssUnit.PX);
  }

  public void addLeftRightTopHeight(Widget child, double left, CssUnit leftCssUnit,
      double right, CssUnit rightCssUnit, double top, CssUnit topCssUnit,
      double height, CssUnit heightCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildRight(child, right, rightCssUnit);
    setChildTop(child, top, topCssUnit);
    setChildHeight(child, height, heightCssUnit);
    add(child);
  }

  public void addLeftRightTopHeight(Widget child, int left, int right, int top, int height) {
    addLeftRightTopHeight(child, left, CssUnit.PX, right, CssUnit.PX, top, CssUnit.PX,
        height, CssUnit.PX);
  }

  public void addLeftTop(Widget child, double left, CssUnit leftCssUnit,
      double top, CssUnit topCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildTop(child, top, topCssUnit);
    add(child);
  }

  public void addLeftTop(Widget child, int left, int top) {
    addLeftTop(child, left, CssUnit.PX, top, CssUnit.PX);
  }

  public void addLeftWidthTopBottom(Widget child, double left, CssUnit leftCssUnit,
      double width, CssUnit widthCssUnit, double top, CssUnit topCssUnit,
      double bottom, CssUnit bottomCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildLeft(child, left, leftCssUnit);
    setChildWidth(child, width, widthCssUnit);
    setChildTop(child, top, topCssUnit);
    setChildBottom(child, bottom, bottomCssUnit);
    add(child);
  }

  public void addLeftWidthTopBottom(Widget child, int left, int width, int top, int bottom) {
    addLeftWidthTopBottom(child, left, CssUnit.PX, width, CssUnit.PX, top, CssUnit.PX,
        bottom, CssUnit.PX);
  }

  public void addRightBottom(Widget child, double right, CssUnit rightCssUnit,
      double bottom, CssUnit bottomCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightCssUnit);
    setChildBottom(child, bottom, bottomCssUnit);
    add(child);
  }

  public void addRightBottom(Widget child, int right, int bottom) {
    addRightBottom(child, right, CssUnit.PX, bottom, CssUnit.PX);
  }

  public void addRightTop(Widget child, double right, CssUnit rightCssUnit,
      double top, CssUnit topCssUnit) {
    Assert.notNull(child);
    setChildPosition(child);
    setChildRight(child, right, rightCssUnit);
    setChildTop(child, top, topCssUnit);
    add(child);
  }

  public void addRightTop(Widget child, int right, int top) {
    addRightTop(child, right, CssUnit.PX, top, CssUnit.PX);
  }

  public void addTopBottomFillHorizontal(Widget child, double top, CssUnit topCssUnit,
      double bottom, CssUnit bottomCssUnit) {
    addLeftRightTopBottom(child, 0, CssUnit.PX, 0, CssUnit.PX, top, topCssUnit,
        bottom, bottomCssUnit);
  }

  public void addTopBottomFillHorizontal(Widget child, int top, int bottom) {
    addTopBottomFillHorizontal(child, top, CssUnit.PX, bottom, CssUnit.PX);
  }

  public void addTopHeightFillHorizontal(Widget child, double top, CssUnit topCssUnit,
      double height, CssUnit heightCssUnit) {
    addLeftRightTopHeight(child, 0, CssUnit.PX, 0, CssUnit.PX, top, topCssUnit,
        height, heightCssUnit);
  }

  public void addTopHeightFillHorizontal(Widget child, int top, int height) {
    addTopHeightFillHorizontal(child, top, CssUnit.PX, height, CssUnit.PX);
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "complex";
  }

  @Override
  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }

  @Override
  public void onResize() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresResize) {
        ((RequiresResize) child).onResize();
      }
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init(Position position, Overflow overflow) {
    DomUtils.createId(this, getIdPrefix());
    if (position != null) {
      getElement().getStyle().setPosition(position);
    }
    if (overflow != null) {
      getElement().getStyle().setOverflow(overflow);
    }
  }

  private static void setChildBottom(Widget child, double value, CssUnit unit) {
    StyleUtils.setBottom(child, value, unit);
  }

  private static void setChildHeight(Widget child, double value, CssUnit unit) {
    StyleUtils.setHeight(child, value, unit);
  }

  private static void setChildLeft(Widget child, double value, CssUnit unit) {
    StyleUtils.setLeft(child, value, unit);
  }

  private static void setChildPosition(Widget child) {
    child.getElement().getStyle().setPosition(Position.ABSOLUTE);
  }

  private static void setChildRight(Widget child, double value, CssUnit unit) {
    StyleUtils.setRight(child, value, unit);
  }

  private static void setChildTop(Widget child, double value, CssUnit unit) {
    StyleUtils.setTop(child, value, unit);
  }

  private static void setChildWidth(Widget child, double value, CssUnit unit) {
    StyleUtils.setWidth(child, value, unit);
  }
}
