package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

import elemental.js.html.JsProgressElement;

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

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "progress";
  }

  public double getMax() {
    return getProgressElement().getMax();
  }

  public double getPosition() {
    return getProgressElement().getPosition();
  }

  public double getValue() {
    return getProgressElement().getValue();
  }
  
  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMax(double max) {
    getProgressElement().setMax(max);
  }

  public void setValue(double value) {
    getProgressElement().setValue(value);
  }

  protected void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Progress");
  }
  
  private JsProgressElement getProgressElement() {
    return getElement().cast();
  }
}
