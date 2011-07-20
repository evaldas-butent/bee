package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a progress bar user interface component for gauging actual stage of processes against
 * 100% of the process.
 */

public class Progress extends Widget implements HasId {

  public Progress() {
    super();

    setElement(DomUtils.createElement(DomUtils.TAG_PROGRESS));
    init();
  }

  public Progress(double max) {
    this();
    setMax(max);
  }

  public Progress(double max, double value) {
    this();
    setMax(max);
    setValue(value);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "progress";
  }

  public double getMax() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_MAX);
  }

  public double getPosition() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_POSITION);
  }

  public double getValue() {
    return getElement().getPropertyDouble(DomUtils.ATTRIBUTE_VALUE);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMax(double max) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_MAX, max);
  }

  public void setValue(double value) {
    getElement().setPropertyDouble(DomUtils.ATTRIBUTE_VALUE, value);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Progress");
  }
}
