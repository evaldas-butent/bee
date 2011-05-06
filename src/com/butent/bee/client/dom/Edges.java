package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Style.Unit;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Manages bottom, left, right and top margins of the application windows.
 */

public class Edges {

  /**
   * Lists all possible edges on the screen.
   */

  public enum Edge {
    BOTTOM, LEFT, RIGHT, TOP
  }

  public static final String EMPTY_CSS_VALUE = BeeConst.STRING_ZERO;

  private static final String CSS_VALUE_SEPARATOR = BeeConst.STRING_SPACE;
  private static final Unit DEFAULT_UNIT = Unit.PX;

  public static Edges copyOf(Edges original) {
    if (original == null) {
      return null;
    }

    return new Edges(original.getTopValue(), original.getTopUnit(),
        original.getRightValue(), original.getRightUnit(),
        original.getBottomValue(), original.getBottomUnit(),
        original.getLeftValue(), original.getLeftUnit());
  }

  public static boolean hasPositiveHorizontalValue(Edges edges) {
    if (edges == null) {
      return false;
    }
    return BeeUtils.isPositive(edges.getLeftValue()) || BeeUtils.isPositive(edges.getRightValue());
  }

  public static boolean hasPositiveVerticalValue(Edges edges) {
    if (edges == null) {
      return false;
    }
    return BeeUtils.isPositive(edges.getTopValue()) || BeeUtils.isPositive(edges.getBottomValue());
  }

  private Unit bottomUnit = null;
  private Double bottomValue = null;

  private Unit leftUnit = null;
  private Double leftValue = null;

  private Unit rightUnit = null;
  private Double rightValue = null;

  private Unit topUnit = null;
  private Double topValue = null;

  public Edges() {
  }

  public Edges(int value) {
    this((double) value);
  }

  public Edges(Double value) {
    this(value, DEFAULT_UNIT);
  }

  public Edges(int verticalValue, int horizontalValue) {
    this((double) verticalValue, (double) horizontalValue);
  }

  public Edges(Double verticalValue, Double horizontalValue) {
    this(verticalValue, DEFAULT_UNIT, horizontalValue, DEFAULT_UNIT);
  }

  public Edges(int topValue, int horizontalValue, int bottomValue) {
    this((double) topValue, (double) horizontalValue, (double) bottomValue);
  }

  public Edges(Double topValue, Double horizontalValue, Double bottomValue) {
    this(topValue, DEFAULT_UNIT, horizontalValue, DEFAULT_UNIT, bottomValue, DEFAULT_UNIT);
  }

  public Edges(int topValue, int rightValue, int bottomValue, int leftValue) {
    this((double) topValue, (double) rightValue, (double) bottomValue, (double) leftValue);
  }

  public Edges(Double topValue, Double rightValue, Double bottomValue, Double leftValue) {
    this(topValue, DEFAULT_UNIT, rightValue, DEFAULT_UNIT, bottomValue, DEFAULT_UNIT,
        leftValue, DEFAULT_UNIT);
  }

  public Edges(Double value, Unit unit) {
    this(value, unit, value, unit);
  }

  public Edges(Double verticalValue, Unit verticalUnit,
      Double horizontalValue, Unit horizontalUnit) {
    this(verticalValue, verticalUnit, horizontalValue, horizontalUnit,
        verticalValue, verticalUnit, horizontalValue, horizontalUnit);
  }

  public Edges(Double topValue, Unit topUnit, Double horizontalValue, Unit horizontalUnit,
      Double bottomValue, Unit bottomUnit) {
    this(topValue, topUnit, horizontalValue, horizontalUnit, bottomValue, bottomUnit,
        horizontalValue, horizontalUnit);
  }

  public Edges(Double topValue, Unit topUnit, Double rightValue, Unit rightUnit,
      Double bottomValue, Unit bottomUnit, Double leftValue, Unit leftUnit) {
    this.topValue = topValue;
    this.topUnit = topUnit;
    this.rightValue = rightValue;
    this.rightUnit = rightUnit;
    this.bottomValue = bottomValue;
    this.bottomUnit = bottomUnit;
    this.leftValue = leftValue;
    this.leftUnit = leftUnit;
  }

  public void clearBottom() {
    setBottom(null, null);
  }

  public void clearLeft() {
    setLeft(null, null);
  }

  public void clearRight() {
    setRight(null, null);
  }

  public void clearTop() {
    setTop(null, null);
  }

  public Unit getBottomUnit() {
    return bottomUnit;
  }

  public Double getBottomValue() {
    return bottomValue;
  }

  public String getCssBottom() {
    return getCssEdge(Edge.BOTTOM);
  }

  public String getCssEdge(Edge edge) {
    Assert.notNull(edge);

    Double value;
    Unit unit;

    switch (edge) {
      case LEFT:
        value = getLeftValue();
        unit = getLeftUnit();
        break;
      case RIGHT:
        value = getRightValue();
        unit = getRightUnit();
        break;
      case TOP:
        value = getTopValue();
        unit = getTopUnit();
        break;
      case BOTTOM:
        value = getBottomValue();
        unit = getBottomUnit();
        break;
      default:
        Assert.untouchable();
        value = null;
        unit = null;
    }

    if (value == null) {
      return EMPTY_CSS_VALUE;
    } else {
      if (unit == null) {
        unit = DEFAULT_UNIT;
      }
      return BeeUtils.toString(value) + unit.getType();
    }
  }

  public String getCssLeft() {
    return getCssEdge(Edge.LEFT);
  }

  public String getCssRight() {
    return getCssEdge(Edge.RIGHT);
  }

  public String getCssTop() {
    return getCssEdge(Edge.TOP);
  }

  public String getCssValue() {
    if (isEmpty()) {
      return EMPTY_CSS_VALUE;
    }
    return getCssEdge(Edge.TOP) + CSS_VALUE_SEPARATOR
        + getCssEdge(Edge.RIGHT) + CSS_VALUE_SEPARATOR
        + getCssEdge(Edge.BOTTOM) + CSS_VALUE_SEPARATOR
        + getCssEdge(Edge.LEFT);
  }

  public int getIntBottom() {
    return BeeUtils.toInt(getBottomValue());
  }

  public int getIntLeft() {
    return BeeUtils.toInt(getLeftValue());
  }

  public int getIntRight() {
    return BeeUtils.toInt(getRightValue());
  }

  public int getIntTop() {
    return BeeUtils.toInt(getTopValue());
  }

  public Unit getLeftUnit() {
    return leftUnit;
  }

  public Double getLeftValue() {
    return leftValue;
  }

  public Unit getRightUnit() {
    return rightUnit;
  }

  public Double getRightValue() {
    return rightValue;
  }

  public Unit getTopUnit() {
    return topUnit;
  }

  public Double getTopValue() {
    return topValue;
  }

  public boolean isEmpty() {
    return getLeftValue() == null && getRightValue() == null && getTopValue() == null
        && getBottomValue() == null;
  }

  public void setBottom(int value) {
    setBottom((double) value);
  }

  public void setBottom(Double value) {
    setBottom(value, DEFAULT_UNIT);
  }

  public void setBottom(Double value, Unit unit) {
    setBottomValue(value);
    setBottomUnit(unit);
  }

  public void setBottomUnit(Unit bottomUnit) {
    this.bottomUnit = bottomUnit;
  }

  public void setBottomValue(Double bottomValue) {
    this.bottomValue = bottomValue;
  }

  public void setEdge(Edge edge, Double value) {
    setEdge(edge, value, DEFAULT_UNIT);
  }

  public void setEdge(Edge edge, Double value, Unit unit) {
    Assert.notNull(edge);

    switch (edge) {
      case LEFT:
        setLeft(value, unit);
        break;
      case RIGHT:
        setRight(value, unit);
        break;
      case TOP:
        setTop(value, unit);
        break;
      case BOTTOM:
        setBottom(value, unit);
        break;
    }
  }

  public void setLeft(int value) {
    setLeft((double) value);
  }

  public void setLeft(Double value) {
    setLeft(value, DEFAULT_UNIT);
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

  public void setRight(int value) {
    setRight((double) value);
  }

  public void setRight(Double value) {
    setRight(value, DEFAULT_UNIT);
  }

  public void setRight(Double value, Unit unit) {
    setRightValue(value);
    setRightUnit(unit);
  }

  public void setRightUnit(Unit rightUnit) {
    this.rightUnit = rightUnit;
  }

  public void setRightValue(Double rightValue) {
    this.rightValue = rightValue;
  }

  public void setTop(int value) {
    setTop((double) value);
  }

  public void setTop(Double value) {
    setTop(value, DEFAULT_UNIT);
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
}
