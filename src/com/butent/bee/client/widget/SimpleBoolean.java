package com.butent.bee.client.widget;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.HasBooleanValue;

/**
 * Implements a checkbox user interface component without label.
 */

public class SimpleBoolean extends SimpleCheckBox implements BooleanWidget {

  private HasBooleanValue source = null;

  public SimpleBoolean() {
    super();
    init();
  }

  public SimpleBoolean(boolean value) {
    this();
    setValue(value);
  }

  public SimpleBoolean(HasBooleanValue source) {
    this();
    if (source != null) {
      initSource(source);
    }
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "bool";
  }

  public HasBooleanValue getSource() {
    return source;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isClick(event)) {
      updateSource(getValue());
    }
    super.onBrowserEvent(event);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasBooleanValue source) {
    this.source = source;
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-SimpleBoolean");
    
    sinkEvents(Event.ONCLICK);
  }

  private void initSource(HasBooleanValue src) {
    if (src != null) {
      setSource(src);
      setValue(src.getBoolean());
    }
  }

  private void updateSource(boolean v) {
    if (getSource() != null) {
      source.setValue(v);
    }
  }
}
