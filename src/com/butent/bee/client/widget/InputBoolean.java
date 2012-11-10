package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.FormWidget;
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
      @Override
      public void onClick(ClickEvent event) {
        ValueChangeEvent.fire(InputBoolean.this, getValue());
      }
    });
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  @Override
  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }
  
  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clearValue() {
    setValue(null);
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getId() {
    return getCheckBox().getId();
  }

  @Override
  public String getIdPrefix() {
    return getCheckBox().getIdPrefix();
  }

  @Override
  public String getNormalizedValue() {
    Boolean v = getCheckBox().getValue();
    if (!v && isNullable()) {
      v = null;
    }
    return BooleanValue.pack(v);
  }

  @Override
  public int getTabIndex() {
    return getCheckBox().getTabIndex();
  }

  @Override
  public String getValue() {
    return BooleanValue.pack(getCheckBox().getValue());
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.CHECK_BOX;
  }
  
  @Override
  public boolean handlesKey(int keyCode) {
    return false;
  }

  @Override
  public boolean isEditing() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return getCheckBox().isEnabled();
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }
  
  @Override
  public void setAccessKey(char key) {
    getCheckBox().setAccessKey(key);
  }

  @Override
  public void setEditing(boolean editing) {
  }

  @Override
  public void setEnabled(boolean enabled) {
    getCheckBox().setEnabled(enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    getCheckBox().setFocus(focused);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(getWidget(), id);
  }

  @Override
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public void setTabIndex(int index) {
    getCheckBox().setTabIndex(index);
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
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

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
  }

  @Override
  public String validate() {
    return null;
  }

  private BooleanWidget getCheckBox() {
    return checkBox;
  }
}
