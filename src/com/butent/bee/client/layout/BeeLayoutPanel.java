package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasAfterAddHandler;
import com.butent.bee.client.event.HasBeforeAddHandler;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

public class BeeLayoutPanel extends LayoutPanel implements HasId {

  public BeeLayoutPanel() {
    createId();
  }

  public BeeLayoutPanel(Widget widget) {
    this();
    add(widget);
  }
  
  @Override
  public void add(Widget widget) {
    Assert.notNull(widget);
    Widget w = widget;
    if (w instanceof HasBeforeAddHandler) {
      w = ((HasBeforeAddHandler) w).onBeforeAdd(this);
    }

    super.add(w);
    DomUtils.createId(getWidgetContainerElement(w), "container");
    getWidgetContainerElement(w).setClassName("bee-LayoutContainer");
    
    if (w instanceof HasAfterAddHandler) {
      ((HasAfterAddHandler) w).onAfterAdd(this);
    }
  }
  
  public void add(Widget widget, boolean scroll) {
    add(widget);
    
    if (scroll) {
      getWidgetContainerElement(widget).getStyle().setOverflow(Overflow.AUTO);
    }
  }

  public String addLeftTop(Widget widget, int left, int top) {
    return addLeftTop(widget, left, Unit.PX, top, Unit.PX);
  }
  
  public String addLeftTop(Widget widget, double left, Unit leftUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetLeftRight(widget, left, leftUnit, 0, Unit.PX);
    setWidgetHorizontalPosition(widget, Alignment.BEGIN);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);
    
    return DomUtils.getId(widget);
  }

  public String addLeftWidthTop(Widget widget, int left, int width, int top) {
    return addLeftWidthTop(widget, left, Unit.PX, width, Unit.PX, top, Unit.PX);
  }
  
  public String addLeftWidthTop(Widget widget, double left, Unit leftUnit, 
      double width, Unit widthUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetLeftWidth(widget, left, leftUnit, width, widthUnit);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);

    return DomUtils.getId(widget);
  }
  
  public void createId() {
    DomUtils.createId(this, "layout");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
