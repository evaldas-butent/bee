package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBoxBase;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.HasCharacterFilter;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text box that allows a single line of text to be entered.
 */

public class InputText extends TextBoxBase implements Editor, HasCharacterFilter {

  private HasStringValue source = null;

  private CharMatcher charMatcher = null;

  private boolean nullable = true;

  private boolean editing = false;
  
  private String oldValue = null;

  public InputText() {
    super(Document.get().createTextInputElement());
    init();
  }

  public InputText(Element element) {
    super(element);
    init();
  }

  public InputText(HasStringValue source) {
    this();
    initSource(source);
  }

  public boolean acceptChar(char charCode) {
    if (getCharMatcher() == null) {
      return true;
    } else {
      return getCharMatcher().matches(charCode);
    }
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public CharMatcher getCharMatcher() {
    return charMatcher;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "txt";
  }

  public int getMaxLength() {
    return getInputElement().getMaxLength();
  }

  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public String getOldValue() {
    return oldValue;
  }

  public HasStringValue getSource() {
    return source;
  }

  public int getVisibleLength() {
    return getInputElement().getSize();
  }

  public boolean handlesKey(int keyCode) {
    return false;
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isKeyPress(event.getType()) && !acceptChar((char) event.getCharCode())) {
      EventUtils.eatEvent(event);
      return;
    }
    
    super.onBrowserEvent(event);

    if (EventUtils.isChange(event.getType())) {
      if (getSource() != null) {
        getSource().setValue(getValue());
      }
      setOldValue(getValue());
    }
  }

  public void setCharMatcher(CharMatcher charMatcher) {
    this.charMatcher = charMatcher;
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMaxLength(int length) {
    getInputElement().setMaxLength(length);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }
  
  public void setSource(HasStringValue source) {
    this.source = source;
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    super.setValue(value, fireEvents);
    setOldValue(value);
  }

  public void setVisibleLength(int length) {
    getInputElement().setSize(length);
  }

  public void startEdit(String value, char charCode, EditorAction onEntry) {
    EditorAction action = (onEntry == null) ? getDefaultEntryAction() : onEntry;
    UiHelper.doEditorAction(this, value, charCode, action);
  }

  public String validate() {
    return null;
  }

  protected CharMatcher getDefaultCharMatcher() {
    return CharMatcher.inRange(BeeConst.CHAR_SPACE, Character.MAX_VALUE);
  }

  protected EditorAction getDefaultEntryAction() {
    return EditorAction.REPLACE;
  }
  
  protected String getDefaultStyleName() {
    return "bee-InputText";
  }

  protected void initSource(HasStringValue src) {
    if (src != null) {
      setSource(src);
      String v = src.getString();
      if (!BeeUtils.isEmpty(v)) {
        setValue(v);
      }
    }
  }

  private InputElement getInputElement() {
    return getElement().cast();
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(getDefaultStyleName());

    setCharMatcher(getDefaultCharMatcher());
    sinkEvents(Event.ONKEYPRESS | Event.ONCHANGE);
  }

  private void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }
}
