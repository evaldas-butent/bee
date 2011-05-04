package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class Box {
  
  private static final Unit DEFAULT_UNIT = Unit.PX;

  private Unit leftUnit = null;
  private Double leftValue = null;
  
  private Unit widthUnit = null;
  private Double widthValue = null;

  private Unit topUnit = null;
  private Double topValue = null;

  private Unit heightUnit = null;
  private Double heightValue = null;

  public Box() {
  }

  public Box(Double leftValue, Double widthValue, Double topValue, Double heightValue) {
    this(leftValue, DEFAULT_UNIT, widthValue, DEFAULT_UNIT, topValue, DEFAULT_UNIT,
        heightValue, DEFAULT_UNIT);
  }
  
  public Box(Double leftValue, Unit leftUnit, Double widthValue, Unit widthUnit,
      Double topValue, Unit topUnit, Double heightValue, Unit heightUnit) {
    this.leftValue = leftValue;
    this.leftUnit = leftUnit;
    this.widthValue = widthValue;
    this.widthUnit = widthUnit;

    this.topValue = topValue;
    this.topUnit = topUnit;
    this.heightValue = heightValue;
    this.heightUnit = heightUnit;
  }

  public Box(int leftValue, int widthValue, int topValue, int heightValue) {
    this((double) leftValue, (double) widthValue, (double) topValue, (double) heightValue);
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
    if (getWidthValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_WIDTH, getWidthValue(), getWidthUnit());
    }

    if (getTopValue() != null) {
      setStyleProperty(st, StyleUtils.STYLE_TOP, getTopValue(), getTopUnit());
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

  public void setHeight(Double value) {
    setHeight(value, DEFAULT_UNIT);
  }
  
  public void setHeight(Double value, Unit unit) {
    setHeightValue(value);
    setHeightUnit(unit);
  }

  public void setHeight(int value) {
    setHeight((double) value);
  }

  public void setHeightUnit(Unit heightUnit) {
    this.heightUnit = heightUnit;
  }

  public void setHeightValue(Double heightValue) {
    this.heightValue = heightValue;
  }

  public void setLeft(Double value) {
    setLeft(value, DEFAULT_UNIT);
  }
  
  public void setLeft(Double value, Unit unit) {
    setLeftValue(value);
    setLeftUnit(unit);
  }

  public void setLeft(int value) {
    setLeft((double) value);
  }

  public void setLeftUnit(Unit leftUnit) {
    this.leftUnit = leftUnit;
  }

  public void setLeftValue(Double leftValue) {
    this.leftValue = leftValue;
  }

  public void setTop(Double value) {
    setTop(value, DEFAULT_UNIT);
  }
  
  public void setTop(Double value, Unit unit) {
    setTopValue(value);
    setTopUnit(unit);
  }

  public void setTop(int value) {
    setTop((double) value);
  }

  public void setTopUnit(Unit topUnit) {
    this.topUnit = topUnit;
  }

  public void setTopValue(Double topValue) {
    this.topValue = topValue;
  }

  public void setWidth(Double value) {
    setWidth(value, DEFAULT_UNIT);
  }
  
  public void setWidth(Double value, Unit unit) {
    setWidthValue(value);
    setWidthUnit(unit);
  }

  public void setWidth(int value) {
    setWidth((double) value);
  }

  public void setWidthUnit(Unit widthUnit) {
    this.widthUnit = widthUnit;
  }

  public void setWidthValue(Double widthValue) {
    this.widthValue = widthValue;
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

