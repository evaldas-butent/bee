package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

public class BeeLayoutPanel extends LayoutPanel implements HasId {
  
  public static Unit defaultUnit = Unit.PX;

  public BeeLayoutPanel() {
    super();
    DomUtils.createId(this, getIdPrefix());
  }

  public BeeLayoutPanel(Widget widget) {
    this();
    add(widget);
  }

  @Override
  public void add(Widget widget) {
    Assert.notNull(widget);
    super.add(widget);

    DomUtils.createId(getWidgetContainerElement(widget), "container");
    getWidgetContainerElement(widget).setClassName("bee-LayoutContainer");
  }

  public void add(Widget widget, boolean scroll) {
    add(widget);

    if (scroll) {
      getWidgetContainerElement(widget).getStyle().setOverflow(Overflow.AUTO);
    }
  }

  public String addLeftRight(Widget widget, double left, Unit leftUnit,
      double right, Unit rightUnit) {
    add(widget);
    setWidgetLeftRight(widget, left, leftUnit, right, rightUnit);

    return DomUtils.getId(widget);
  }
  
  public String addLeftRight(Widget widget, int left, int right) {
    return addLeftRight(widget, left, defaultUnit, right, defaultUnit);
  }
  
  public String addLeftRightTop(Widget widget, double left, Unit leftUnit,
      double right, Unit rightUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetLeftRight(widget, left, leftUnit, right, rightUnit);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);

    return DomUtils.getId(widget);
  }

  public String addLeftRightTop(Widget widget, int left, int right, int top) {
    return addLeftRightTop(widget, left, defaultUnit, right, defaultUnit, top, defaultUnit);
  }

  public String addLeftTop(Widget widget, double left, Unit leftUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetLeftRight(widget, left, leftUnit, 0, Unit.PX);
    setWidgetHorizontalPosition(widget, Alignment.BEGIN);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);

    return DomUtils.getId(widget);
  }

  public String addLeftTop(Widget widget, int left, int top) {
    return addLeftTop(widget, left, defaultUnit, top, defaultUnit);
  }

  public String addLeftWidthTop(Widget widget, double left, Unit leftUnit,
      double width, Unit widthUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetLeftWidth(widget, left, leftUnit, width, widthUnit);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);

    return DomUtils.getId(widget);
  }

  public String addLeftWidthTop(Widget widget, int left, int width, int top) {
    return addLeftWidthTop(widget, left, defaultUnit, width, defaultUnit, top, defaultUnit);
  }

  public String addRightWidthTop(Widget widget, double right, Unit rightUnit,
      double width, Unit widthUnit, double top, Unit topUnit) {
    add(widget);
    setWidgetRightWidth(widget, right, rightUnit, width, widthUnit);
    setWidgetHorizontalPosition(widget, Alignment.END);

    setWidgetTopBottom(widget, top, topUnit, 0, Unit.PX);
    setWidgetVerticalPosition(widget, Alignment.BEGIN);

    return DomUtils.getId(widget);
  }

  public String addRightWidthTop(Widget widget, int right, int width, int top) {
    return addRightWidthTop(widget, right, defaultUnit, width, defaultUnit, top, defaultUnit);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "layout";
  }
  
  public void setHorizontalLayout(Widget widget, Double left, Unit leftUnit,
      Double right, Unit rightUnit, Double width, Unit widthUnit) {
    Assert.notNull(widget);
    
    boolean hasLeft = (left != null);
    boolean hasRight = (right != null);
    boolean hasWidth = BeeUtils.isPositive(width);
    
    if (hasLeft && hasRight) {
      setWidgetLeftRight(widget, left, normalizeUnit(leftUnit), right, normalizeUnit(rightUnit));
    } else if (hasLeft && hasWidth) {
      setWidgetLeftWidth(widget, left, normalizeUnit(leftUnit), width, normalizeUnit(widthUnit));
    } else if (hasRight && hasWidth) {
      setWidgetRightWidth(widget, right, normalizeUnit(rightUnit), width, normalizeUnit(widthUnit));
      setWidgetHorizontalPosition(widget, Alignment.END);

    } else if (hasLeft) {
      setWidgetLeftRight(widget, left, normalizeUnit(leftUnit), 0, Unit.PX);
    } else if (hasRight) {
      setWidgetLeftRight(widget, 0, Unit.PX, right, normalizeUnit(rightUnit));
      setWidgetHorizontalPosition(widget, Alignment.END);
    } else if (hasWidth) {
      setWidgetLeftWidth(widget, 0, Unit.PX, width, normalizeUnit(widthUnit));
    }
  }
  
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setVerticalLayout(Widget widget, Double top, Unit topUnit,
      Double bottom, Unit bottomUnit, Double height, Unit heightUnit) {
    Assert.notNull(widget);
    
    boolean hasTop = (top != null);
    boolean hasBottom = (bottom != null);
    boolean hasHeight = BeeUtils.isPositive(height);
    
    if (hasTop && hasBottom) {
      setWidgetTopBottom(widget, top, normalizeUnit(topUnit), bottom, normalizeUnit(bottomUnit));
    } else if (hasTop && hasHeight) {
      setWidgetTopHeight(widget, top, normalizeUnit(topUnit), height, normalizeUnit(heightUnit));
    } else if (hasBottom && hasHeight) {
      setWidgetBottomHeight(widget, bottom, normalizeUnit(bottomUnit),
          height, normalizeUnit(heightUnit));
      setWidgetVerticalPosition(widget, Alignment.END);

    } else if (hasTop) {
      setWidgetTopBottom(widget, top, normalizeUnit(topUnit), 0, Unit.PX);
    } else if (hasBottom) {
      setWidgetTopBottom(widget, 0, Unit.PX, bottom, normalizeUnit(bottomUnit));
      setWidgetVerticalPosition(widget, Alignment.END);
    } else if (hasHeight) {
      setWidgetTopHeight(widget, 0, Unit.PX, height, normalizeUnit(heightUnit));
    }
  }
  
  @Override
  protected WidgetCollection getChildren() {
    return super.getChildren();
  }

  private Unit normalizeUnit(Unit unit) {
    return (unit == null) ? defaultUnit : unit;
  }
}
