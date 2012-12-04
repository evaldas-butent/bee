package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBoxBase;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasInputHandlers;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.edit.HasCharacterFilter;
import com.butent.bee.client.view.edit.HasTextBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasCapsLock;
import com.butent.bee.shared.ui.HasMaxLength;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text box that allows a single line of text to be entered.
 */

public class InputText extends TextBoxBase implements Editor, HasCharacterFilter, HasInputHandlers,
    HasTextBox, HasCapsLock, HasMaxLength {

  private HasStringValue source = null;

  private CharMatcher charMatcher = null;

  private boolean nullable = true;

  private boolean editing = false;

  private String oldValue = null;
  
  private boolean upperCase = false;

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

  @Override
  public boolean acceptChar(char charCode) {
    if (getCharMatcher() == null) {
      return true;
    } else {
      return getCharMatcher().matches(charCode);
    }
  }

  @Override
  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addInputHandler(InputHandler handler) {
    return Binder.addInputHandler(this, handler);
  }
  
  @Override
  public void clearValue() {
    setValue(BeeConst.STRING_EMPTY);
  }
  
  public CharMatcher getCharMatcher() {
    return charMatcher;
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return EditorAction.SELECT;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "txt";
  }

  @Override
  public int getMaxLength() {
    return getInputElement().getMaxLength();
  }

  @Override
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

  @Override
  public TextBoxBase getTextBox() {
    return this;
  }

  public int getVisibleLength() {
    return getInputElement().getSize();
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_TEXT;
  }
  
  @Override
  public boolean handlesKey(int keyCode) {
    return false;
  }

  public boolean isAllSelected() {
    String text = getText();
    if (BeeUtils.isEmpty(text)) {
      return false;
    } else {
      return BeeUtils.equalsTrim(text, getSelectedText());
    }
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
    return getElement().equals(node);
  }
  
  public boolean isUpperCase() {
    return upperCase;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isKeyPress(event.getType())) {
      char charCode = (char) event.getCharCode();

      if (!acceptChar(charCode)) {
        event.preventDefault();
      } else if (isUpperCase()) {
        char upper = Character.toUpperCase(charCode);
        if (upper != charCode) {
          event.preventDefault();
          UiHelper.pressKey(this, upper);
        }
      }
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

  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setMaxLength(int maxLength) {
    getInputElement().setMaxLength(maxLength);
  }

  @Override
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  @Override
  public void setUpperCase(boolean upperCase) {
    this.upperCase = upperCase;
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    String v = (isUpperCase() && !BeeUtils.isEmpty(value)) ? value.toUpperCase() : value;
    super.setValue(v, fireEvents);
    setOldValue(v);
  }

  public void setVisibleLength(int length) {
    getInputElement().setSize(length);
  }

  @Override
  public void startEdit(String value, char charCode, EditorAction onEntry, Element sourceElement) {
    EditorAction action = (onEntry == null) ? getDefaultEntryAction() : onEntry;
    EditorAssistant.doEditorAction(this, value, charCode, action);
  }

  @Override
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
