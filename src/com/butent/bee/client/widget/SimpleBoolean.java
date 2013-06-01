package com.butent.bee.client.widget;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import com.butent.bee.client.dom.DomUtils;

/**
 * Implements a checkbox user interface component without label.
 */

public class SimpleBoolean extends SimpleCheckBox implements BooleanWidget {

  public SimpleBoolean() {
    super();
    init();
  }

  public SimpleBoolean(boolean value) {
    this();
    setValue(value);
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "bool";
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-SimpleBoolean");
    
    sinkEvents(Event.ONCLICK);
  }
}
