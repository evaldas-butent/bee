package com.butent.bee.client.widget;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RichTextArea;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables to use a component for input of rich text type values.
 */

public class RichText extends RichTextArea implements Editor {

  private boolean nullable = true;

  public RichText() {
    super();
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void createId() {
    DomUtils.createId(this, "rich");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    if (getValue() == null) {
      return null;
    }
    return BeeUtils.trimRight(getValue());
  }

  public String getValue() {
    return getText();
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    setText(value);
  }

  public void startEdit(String oldValue, char charCode) {
    setValue(BeeUtils.trimRight(oldValue));
  }

  public String validate() {
    return null;
  }
}
