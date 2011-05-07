package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeClickHandler;
import com.butent.bee.shared.HasBooleanValue;
import com.butent.bee.shared.HasId;

/**
 * Implements a checkbox user interface component without label.
 */

public class BeeSimpleCheckBox extends SimpleCheckBox implements HasId, HasBeeClickHandler {
  private HasBooleanValue source = null;

  public BeeSimpleCheckBox() {
    super();
    init();
  }

  public BeeSimpleCheckBox(boolean value) {
    this();
    setValue(value);
  }

  public BeeSimpleCheckBox(HasBooleanValue source) {
    this();
    if (source != null) {
      initSource(source);
      addDefaultHandler();
    }
  }

  public void createId() {
    DomUtils.createId(this, "sc");
  }

  public String getId() {
    return DomUtils.getId(this);
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
    createId();
    setStyleName("bee-SimpleCheckBox");
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
