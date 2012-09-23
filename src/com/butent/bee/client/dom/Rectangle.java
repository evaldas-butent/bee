package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables to create rectangle objects and manage their size and style.
 */

public class Rectangle implements Transformable, HasDimensions {

  private static final Unit DEFAULT_UNIT = Unit.PX;

  public static Rectangle createFromAbsoluteCoordinates(Element element) {
    Assert.notNull(element);
    return new Rectangle(element.getAbsoluteLeft(), element.getAbsoluteTop(),
        element.getOffsetWidth(), element.getOffsetHeight());
  }

  public static Rectangle createFromParentOffset(Element element) {
    Assert.notNull(element);
    return new Rectangle(element.getOffsetLeft(), element.getOffsetTop(),
        element.getOffsetWidth(), element.getOffsetHeight());
  }

  private Unit leftUnit = null;
  private Double leftValue = null;

  private Unit topUnit = null;
  private Double topValue = null;

  private Unit widthUnit = null;
  private Double widthValue = null;

  private Unit heightUnit = null;
  private Double heightValue = null;

  public Rectangle() {
    super();
  }

  public Rectangle(Style style) {
    this();
    setFromStyle(style);
  }

  public Rectangle(Double leftValue, Unit leftUnit, Double topValue, Unit topUnit,
      Double widthValue, Unit widthUnit, Double heightValue, Unit heightUnit) {
    this.leftValue = leftValue;
    this.leftUnit = leftUnit;
    this.topValue = topValue;
    this.topUnit = topUnit;

    this.widthValue = widthValue;
    this.widthUnit = widthUnit;
    this.heightValue = heightValue;
    this.heightUnit = heightUnit;
  }

  public Rectangle(int leftValue, int topValue, int widthValue, int heightValue) {
    this((double) leftValue, DEFAULT_UNIT, (double) topValue, DEFAULT_UNIT,
        (double) widthValue, DEFAULT_UNIT, (double) heightValue, DEFAULT_UNIT);
  }

  public void applyTo(Element el) {
    Assert.notNull(el);
    applyTo(el.getStyle());
  }

  public void applyTo(Style st) {
    Assert.notNull(st);

    if (getLeftValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_LEFT, getLeftValue(), getLeftUnit());
    }
    if (getTopValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_TOP, getTopValue(), getTopUnit());
    }

    if (getWidthValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_WIDTH, getWidthValue(), getWidthUnit());
    }
    if (getHeightValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_HEIGHT, getHeightValue(), getHeightUnit());
    }
  }

  public void applyTo(UIObject obj) {
    Assert.notNull(obj);
    applyTo(obj.getElement());
  }

  public void clearHeight() {
    setHeight(null, null);
  }

  public void clearLeft() {
    setLeft(null, null);
  }

  public void clearTop() {
    setTop(null, null);
  }

  public void clearWidth() {
    setWidth(null, null);
  }

  public boolean contains(int x, int y) {
    return contains((double) x, (double) y);
  }

  public boolean contains(double x, double y) {
    if (isValid()) {
      return x >= getLeftValue() && x <= getLeftValue() + getWidthValue()
          && y >= getTopValue() && y <= getTopValue() + getHeightValue();
    } else {
      return false;
    }
  }

  public Unit getHeightUnit() {
    return heightUnit;
  }

  public Double getHeightValue() {
    return heightValue;
  }

  public int getIntHeight() {
    return BeeUtils.toInt(getHeightValue());
  }

  public int getIntLeft() {
    return BeeUtils.toInt(getLeftValue());
  }

  public int getIntTop() {
    return BeeUtils.toInt(getTopValue());
  }

  public int getIntWidth() {
    return BeeUtils.toInt(getWidthValue());
  }

  public Unit getLeftUnit() {
    return leftUnit;
  }

  public Double getLeftValue() {
    return leftValue;
  }

  public Unit getTopUnit() {
    return topUnit;
  }

  public Double getTopValue() {
    return topValue;
  }

  public Unit getWidthUnit() {
    return widthUnit;
  }

  public Double getWidthValue() {
    return widthValue;
  }

  public boolean isEmpty() {
    return getLeftValue() == null && getWidthValue() == null && getTopValue() == null
        && getHeightValue() == null;
  }

  public boolean isValid() {
    return getLeftValue() != null && BeeUtils.isNonNegative(getWidthValue())
        && getTopValue() != null && BeeUtils.isNonNegative(getHeightValue());
  }

  public void setHeight(Double value, Unit unit) {
    setHeightValue(value);
    setHeightUnit(unit);
  }

  public void setHeightUnit(Unit heightUnit) {
    this.heightUnit = heightUnit;
  }

  public void setHeightValue(Double heightValue) {
    this.heightValue = heightValue;
  }

  public void setLeft(Double value, Unit unit) {
    setLeftValue(value);
    setLeftUnit(unit);
  }

  public void setLeftUnit(Unit leftUnit) {
    this.leftUnit = leftUnit;
  }

  public void setLeftValue(Double leftValue) {
    this.leftValue = leftValue;
  }

  public void setTop(Double value, Unit unit) {
    setTopValue(value);
    setTopUnit(unit);
  }

  public void setTopUnit(Unit topUnit) {
    this.topUnit = topUnit;
  }

  public void setTopValue(Double topValue) {
    this.topValue = topValue;
  }

  public void setWidth(Double value, Unit unit) {
    setWidthValue(value);
    setWidthUnit(unit);
  }

  public void setWidthUnit(Unit widthUnit) {
    this.widthUnit = widthUnit;
  }

  public void setWidthValue(Double widthValue) {
    this.widthValue = widthValue;
  }

  @Override
  public String toString() {
    return transform();
  }

  public String transform() {
    if (isEmpty()) {
      return "Rectangle: instance is empty";
    }
    return StyleUtils.buildStyle(
        (getLeftValue() != null) ? 
            StyleUtils.buildLeft(getLeftValue(), getLeftUnit()) : null,
        (getTopValue() != null) ? 
            StyleUtils.buildTop(getTopValue(), getTopUnit()) : null,
        (getWidthValue() != null) ? 
            StyleUtils.buildWidth(getWidthValue(), getWidthUnit()) : null,
        (getHeightValue() != null) ? 
            StyleUtils.buildHeight(getHeightValue(), getHeightUnit()) : null).asString();
  }

  private void setFromStyle(Style style) {
    Assert.notNull(style);

    setFromStyleProperty(style, StyleUtils.STYLE_LEFT);
    setFromStyleProperty(style, StyleUtils.STYLE_TOP);
    setFromStyleProperty(style, StyleUtils.STYLE_WIDTH);
    setFromStyleProperty(style, StyleUtils.STYLE_HEIGHT);
  }

  private void setFromStyleProperty(Style style, String name) {
    String length = style.getProperty(name);
    if (!BeeUtils.isEmpty(length)) {
      return;
    }
    Pair<Double, Unit> pair = StyleUtils.parseCssLength(length);
    if (pair == null) {
      return;
    }

    Double value = pair.getA();
    if (value == null) {
      return;
    }

    if (BeeUtils.same(name, StyleUtils.STYLE_LEFT)) {
      setLeftValue(value);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_TOP)) {
      setTopValue(value);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_WIDTH)) {
      setWidthValue(value);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_HEIGHT)) {
      setHeightValue(value);
    } else {
      Assert.untouchable();
    }

    Unit unit = pair.getB();
    if (unit == null) {
      return;
    }

    if (BeeUtils.same(name, StyleUtils.STYLE_LEFT)) {
      setLeftUnit(unit);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_TOP)) {
      setTopUnit(unit);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_WIDTH)) {
      setWidthUnit(unit);
    } else if (BeeUtils.same(name, StyleUtils.STYLE_HEIGHT)) {
      setHeightUnit(unit);
    }
  }

  private void setStyleProperty(Style style, String name, Double value, Unit unit) {
    if (value == null) {
      return;
    }
    if (unit == null) {
      style.setProperty(name, value, DEFAULT_UNIT);
    } else {
      style.setProperty(name, value, unit);
    }
  }
}
