package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
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
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;

/**
 * Implements a user interface component for visibly switching values on and off.
 */

public class Toggle extends CustomButton implements Editor {

  private boolean nullable = true;

  private boolean editing = false;

  private String options = null;

  private boolean handlesTabulation = false;
  
  public Toggle() {
    super();
    init(null);
    setDefaultFaces();
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

  @Override
  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
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
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "toggle";
  }

  @Override
  public String getNormalizedValue() {
    Boolean v = isDown();
    if (!v && isNullable()) {
      v = null;
    }
    return BooleanValue.pack(v);
  }
  
  @Override
  public String getOptions() {
    return options;
  }
  
  @Override
  public String getValue() {
    return BooleanValue.pack(isDown());
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.TOGGLE;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return false;
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  public void invert() {
    setDown(!isDown());
  }
  
  @Override
  public boolean isDown() {
    return super.isDown();
  }

  @Override
  public boolean isEditing() {
    return editing;
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
  public void normalizeDisplay(String normalizedValue) {
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
            event.preventDefault();
            onClick();
            break;
          case KeyCodes.KEY_ESCAPE:
            if (isEditing()) {
              event.preventDefault();
              fireEvent(new EditStopEvent(State.CANCELED));
            }
        }
      } else if (type == Event.ONKEYPRESS && event.getCharCode() >= BeeConst.CHAR_SPACE) {
        event.preventDefault();
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
  
  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    this.handlesTabulation = handlesTabulation;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  @Override
  public void setValue(String value) {
    setDown(BeeUtils.toBoolean(value));
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    setValue(oldValue);
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    return Collections.emptyList();
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return Collections.emptyList();
  }
  
  @Override
  protected void onClick() {
    if (isEditing()) {
      fireEvent(new EditStopEvent(State.CHANGED));
    } else {
      ValueChangeEvent.fire(this, BooleanValue.pack(isDown()));
    }
  }

  private void init(String styleName) {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(BeeUtils.notEmpty(styleName, "bee-Toggle"));
  }

  private void setDefaultFaces() {
    getUpFace().setHTML(BeeUtils.toString(BeeConst.BALLOT));
    getDownFace().setHTML(BeeUtils.toString(BeeConst.HEAVY_CHECK_MARK));
  }
}
