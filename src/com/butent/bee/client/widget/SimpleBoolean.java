package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeClickHandler;
import com.butent.bee.shared.HasBooleanValue;

/**
 * Implements a checkbox user interface component without label.
 */

public class SimpleBoolean extends SimpleCheckBox implements BooleanWidget, HasBeeClickHandler {

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
      addDefaultHandler();
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

  public boolean onBeeClick(ClickEvent event) {
    updateSource(getValue());
    return true;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasBooleanValue source) {
    this.source = source;
  }

  private void addDefaultHandler() {
    BeeKeeper.getBus().addClickHandler(this);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-SimpleBoolean");
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
