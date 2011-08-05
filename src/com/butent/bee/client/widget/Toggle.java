package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.Image;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasBooleanValue;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component for visibly switching values on and off.
 */

public class Toggle extends CustomButton implements Editor {

  private HasBooleanValue source = null;

  private boolean nullable = true;

  private boolean editing = false;

  public Toggle() {
    super();
    init(null);
    setDefaultFaces();
  }

  public Toggle(HasBooleanValue source) {
    this();
    if (source != null) {
      initSource(source);
    }
  }

  public Toggle(Image upImage) {
    super(upImage);
    init(null);
  }

  public Toggle(Image upImage, Image downImage) {
    super(upImage, downImage);
    init(null);
  }

  public Toggle(String upText, String downText, String styleName) {
    super(upText, downText);
    init(styleName);
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "toggle";
  }

  public String getNormalizedValue() {
    Boolean v = isDown();
    if (!v && isNullable()) {
      v = null;
    }
    return BooleanValue.pack(v);
  }

  public HasBooleanValue getSource() {
    return source;
  }

  public String getValue() {
    return BooleanValue.pack(isDown());
  }

  public boolean handlesKey(int keyCode) {
    return false;
  }

  public void invert() {
    setDown(!isDown());
  }

  @Override
  public boolean isDown() {
    return super.isDown();
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (!isEnabled()) {
      return;
    }
    int type = event.getTypeInt();

    if (EventUtils.isKeyEvent(type)) {
      if (type == Event.ONKEYDOWN) {
        switch (event.getKeyCode()) {
          case KeyCodes.KEY_ENTER:
            EventUtils.eatEvent(event);
            onClick();
            break;
          case KeyCodes.KEY_ESCAPE:
            if (isEditing()) {
              EventUtils.eatEvent(event);
              fireEvent(new EditStopEvent(State.CANCELED));
            }
        }
      } else if (type == Event.ONKEYPRESS && event.getCharCode() >= BeeConst.CHAR_SPACE) {
        EventUtils.eatEvent(event);
        invert();
        return;
      } else {
        DomEvent.fireNativeEvent(event, this, this.getElement());
      }
      return;
    }

    if (EventUtils.isClick(event)) {
      DomEvent.fireNativeEvent(event, this, this.getElement());
      return;
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void setDown(boolean down) {
    super.setDown(down);
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setSource(HasBooleanValue source) {
    this.source = source;
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    boolean oldValue = isDown();
    boolean newValue = BeeUtils.toBoolean(value);
    setDown(newValue);

    if (fireEvents && oldValue != newValue) {
      ValueChangeEvent.fire(this, value);
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    setValue(oldValue);
  }

  public String validate() {
    return null;
  }

  @Override
  protected void onClick() {
    updateSource();
    if (isEditing()) {
      fireEvent(new EditStopEvent(State.CHANGED));
    } else {
      ValueChangeEvent.fire(this, BooleanValue.pack(isDown()));
    }
  }

  private void init(String styleName) {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(BeeUtils.ifString(styleName, "bee-Toggle"));
  }

  private void initSource(HasBooleanValue src) {
    if (src != null) {
      setSource(src);
      setDown(BeeUtils.unbox(src.getBoolean()));
    }
  }

  private void setDefaultFaces() {
    getUpFace().setHTML(BeeUtils.toString(BeeConst.BALLOT));
    getDownFace().setHTML(BeeUtils.toString(BeeConst.HEAVY_CHECK_MARK));
  }

  private void updateSource() {
    if (getSource() != null) {
      getSource().setValue(isDown());
    }
  }
}
