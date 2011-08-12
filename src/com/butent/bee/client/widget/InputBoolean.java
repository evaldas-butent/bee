package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements checkbox user interface component.
 */

public class InputBoolean extends Composite implements Editor {

  private final BooleanWidget checkBox;
  private boolean nullable = true;

  public InputBoolean(String label) {
    this(label, false);
  }

  public InputBoolean(String label, boolean asHTML) {
    super();

    if (BeeUtils.isEmpty(label)) {
      SimpleBoolean simpleBoolean = new SimpleBoolean();
      this.checkBox = simpleBoolean;
      initWidget(simpleBoolean);
    } else {
      BeeCheckBox beeCheckBox = new BeeCheckBox(label, asHTML);
      this.checkBox = beeCheckBox;
      initWidget(beeCheckBox);
    }

    checkBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        ValueChangeEvent.fire(InputBoolean.this, getValue());
      }
    });
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public String getId() {
    return getCheckBox().getId();
  }

  public String getIdPrefix() {
    return getCheckBox().getIdPrefix();
  }

  public String getNormalizedValue() {
    Boolean v = getCheckBox().getValue();
    if (!v && isNullable()) {
      v = null;
    }
    return BooleanValue.pack(v);
  }

  public int getTabIndex() {
    return getCheckBox().getTabIndex();
  }

  public String getValue() {
    return BooleanValue.pack(getCheckBox().getValue());
  }

  public boolean handlesKey(int keyCode) {
    return false;
  }

  public boolean isEditing() {
    return false;
  }

  public boolean isEnabled() {
    return getCheckBox().isEnabled();
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setAccessKey(char key) {
    getCheckBox().setAccessKey(key);
  }

  public void setEditing(boolean editing) {
  }

  public void setEnabled(boolean enabled) {
    getCheckBox().setEnabled(enabled);
  }

  public void setFocus(boolean focused) {
    getCheckBox().setFocus(focused);
  }

  public void setId(String id) {
    DomUtils.setId(getWidget(), id);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setTabIndex(int index) {
    getCheckBox().setTabIndex(index);
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    boolean oldValue = getCheckBox().getValue();
    boolean newValue = BeeUtils.toBoolean(value);

    if (oldValue != newValue) {
      getCheckBox().setValue(newValue);
      if (fireEvents) {
        ValueChangeEvent.fire(this, value);
      }
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
  }

  public String validate() {
    return null;
  }

  private BooleanWidget getCheckBox() {
    return checkBox;
  }
}
