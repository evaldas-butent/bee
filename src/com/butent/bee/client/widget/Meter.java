package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Implements a meter user interface component for gauging actual values against the target ones.
 */

public class Meter extends Widget implements IdentifiableWidget {

  public Meter() {
    super();
    setElement(DomUtils.createElement(DomUtils.TAG_METER));
    init();
  }

  public Meter(double min, double max, double value) {
    this();
    setMin(min);
    setMax(max);
    setValue(value);
  }

  public Meter(double min, double max, double value, double low, double high, double optimum) {
    this();
    setMin(min);
    setMax(max);
    setValue(value);
    setLow(low);
    setHigh(high);
    setOptimum(optimum);
  }

  public double getHigh() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_HIGH);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "meter";
  }

  public double getLow() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_LOW);
  }

  public double getMax() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_MAX);
  }

  public double getMin() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_MIN);
  }

  public double getOptimum() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_OPTIMUM);
  }

  public double getValue() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_VALUE);
  }

  public void setHigh(double high) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_HIGH, high);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setLow(double low) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_LOW, low);
  }

  public void setMax(double max) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_MAX, max);
  }

  public void setMin(double min) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_MIN, min);
  }

  public void setOptimum(double optimum) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_OPTIMUM, optimum);
  }

  public void setValue(double value) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_VALUE, value);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Meter");
  }
}
