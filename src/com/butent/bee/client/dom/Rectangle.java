package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables to create rectangle objects and manage their size and style.
 */

public class Rectangle implements HasDimensions {

  private static final CssUnit DEFAULT_UNIT = CssUnit.PX;

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

  private CssUnit leftUnit;
  private Double leftValue;

  private CssUnit topUnit;
  private Double topValue;

  private CssUnit widthUnit;
  private Double widthValue;

  private CssUnit heightUnit;
  private Double heightValue;

  public Rectangle() {
    super();
  }

  public Rectangle(Double leftValue, CssUnit leftUnit, Double topValue, CssUnit topUnit,
      Double widthValue, CssUnit widthUnit, Double heightValue, CssUnit heightUnit) {
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

  public Rectangle(Style style) {
    this();
    setFromStyle(style);
  }

  public void applyHeight(Style st) {
    if (st != null && getHeightValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_HEIGHT, getHeightValue(), getHeightUnit());
    }
  }

  public void applyLeft(Style st) {
    if (st != null && getLeftValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_LEFT, getLeftValue(), getLeftUnit());
    }
  }

  public void applyTo(Element el) {
    Assert.notNull(el);
    applyTo(el.getStyle());
  }

  public void applyTo(Style st) {
    Assert.notNull(st);

    applyLeft(st);
    applyTop(st);
    applyWidth(st);
    applyHeight(st);
  }

  public void applyTo(UIObject obj) {
    Assert.notNull(obj);
    applyTo(obj.getElement());
  }

  public void applyTop(Style st) {
    if (st != null && getTopValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_TOP, getTopValue(), getTopUnit());
    }
  }

  public void applyWidth(Style st) {
    if (st != null && getWidthValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_WIDTH, getWidthValue(), getWidthUnit());
    }
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

  public boolean contains(double x, double y) {
    if (isValid()) {
      return x >= getLeftValue() && x <= getLeftValue() + getWidthValue()
          && y >= getTopValue() && y <= getTopValue() + getHeightValue();
    } else {
      return false;
    }
  }

  public boolean contains(int x, int y) {
    return contains((double) x, (double) y);
  }

  @Override
  public CssUnit getHeightUnit() {
    return heightUnit;
  }

  @Override
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

  public CssUnit getLeftUnit() {
    return leftUnit;
  }

  public Double getLeftValue() {
    return leftValue;
  }

  public CssUnit getTopUnit() {
    return topUnit;
  }

  public Double getTopValue() {
    return topValue;
  }

  @Override
  public CssUnit getWidthUnit() {
    return widthUnit;
  }

  @Override
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

  public void setHeight(int value) {
    setHeight((double) value, DEFAULT_UNIT);
  }

  public void setHeight(Double value, CssUnit unit) {
    setHeightValue(value);
    setHeightUnit(unit);
  }

  @Override
  public void setHeightUnit(CssUnit heightUnit) {
    this.heightUnit = heightUnit;
  }

  @Override
  public void setHeightValue(Double heightValue) {
    this.heightValue = heightValue;
  }

  public void setLeft(int value) {
    setLeft((double) value, DEFAULT_UNIT);
  }

  public void setLeft(Double value, CssUnit unit) {
    setLeftValue(value);
    setLeftUnit(unit);
  }

  public void setLeftUnit(CssUnit leftUnit) {
    this.leftUnit = leftUnit;
  }

  public void setLeftValue(Double leftValue) {
    this.leftValue = leftValue;
  }

  public void setTop(int value) {
    setTop((double) value, DEFAULT_UNIT);
  }

  public void setTop(Double value, CssUnit unit) {
    setTopValue(value);
    setTopUnit(unit);
  }

  public void setTopUnit(CssUnit topUnit) {
    this.topUnit = topUnit;
  }

  public void setTopValue(Double topValue) {
    this.topValue = topValue;
  }

  public void setWidth(int value) {
    setWidth((double) value, DEFAULT_UNIT);
  }

  public void setWidth(Double value, CssUnit unit) {
    setWidthValue(value);
    setWidthUnit(unit);
  }

  @Override
  public void setWidthUnit(CssUnit widthUnit) {
    this.widthUnit = widthUnit;
  }

  @Override
  public void setWidthValue(Double widthValue) {
    this.widthValue = widthValue;
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "Rectangle: instance is empty";
    }
    return StyleUtils.buildStyle(
        (getLeftValue() != null) ? StyleUtils.buildLeft(getLeftValue(), getLeftUnit()) : null,
        (getTopValue() != null) ? StyleUtils.buildTop(getTopValue(), getTopUnit()) : null,
        (getWidthValue() != null) ? StyleUtils.buildWidth(getWidthValue(), getWidthUnit()) : null,
        (getHeightValue() != null) ? StyleUtils.buildHeight(getHeightValue(), getHeightUnit())
            : null).asString();
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
    Pair<Double, CssUnit> pair = StyleUtils.parseCssLength(length);
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

    CssUnit unit = pair.getB();
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

  private static void setStyleProperty(Style style, String name, Double value, CssUnit unit) {
    if (value == null) {
      return;
    }
    if (unit == null) {
      StyleUtils.setProperty(style, name, value, DEFAULT_UNIT);
    } else {
      StyleUtils.setProperty(style, name, value, unit);
    }
  }
}
