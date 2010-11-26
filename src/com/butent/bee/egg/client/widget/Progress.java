package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

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
  
  public void createId() {
    DomUtils.createId(this, "progress");
  }

  public String getId() {
    return DomUtils.getId(this);
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
    createId();
    setStyleName("bee-Progress");
  }
}
