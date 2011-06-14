package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
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

public class InputText extends TextBox implements Editor, HasBeeValueChangeHandler<String>,
    HasCharacterFilter {

  private HasStringValue source = null;

  private CharMatcher charMatcher = null;

  private boolean nullable = true;

  private boolean editing = false;

  public InputText() {
    super();
    init();
  }

  public InputText(Element element) {
    super(element);
    init();
  }

  public InputText(HasStringValue source) {
    this();

    if (source != null) {
      setSource(source);
      String v = source.getString();
      if (!BeeUtils.isEmpty(v)) {
        setValue(v);
      }
    }
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

  public void createId() {
    DomUtils.createId(this, getDefaultIdPrefix());
  }

  public CharMatcher getCharMatcher() {
    return charMatcher;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public HasStringValue getSource() {
    return source;
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
    if (event.getTypeInt() == Event.ONKEYPRESS && !acceptChar((char) event.getCharCode())) {
      EventUtils.eatEvent(event);
    } else {
      super.onBrowserEvent(event);
    }
  }

  public boolean onValueChange(String value) {
    if (source != null) {
      source.setValue(value);
    }
    return true;
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

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    EditorAction action = (onEntry == null) ? getDefaultEntryAction() : onEntry;
    UiHelper.doEditorAction(this, oldValue, charCode, action);
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

  protected String getDefaultIdPrefix() {
    return "txt";
  }

  protected String getDefaultStyleName() {
    return "bee-InputText";
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addStringVch(this);
  }

  private void init() {
    setStyleName(getDefaultStyleName());
    createId();
    addDefaultHandlers();

    setCharMatcher(getDefaultCharMatcher());
    sinkEvents(Event.ONKEYPRESS);
  }
}
