package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text box that allows a single line of text to be entered.
 */

public class InputText extends TextBox implements Editor, HasBeeValueChangeHandler<String> {
  
  private HasStringValue source = null;
  
  private CharMatcher charMatcher = null;
  
  private boolean nullable = true;

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

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }
  
  public void startEdit(String oldValue, char charCode) {
    if (acceptChar(charCode)) {
      setValue(BeeUtils.toString(charCode));
    } else {
      String v = BeeUtils.trimRight(oldValue);
      setValue(v);
      if (!BeeUtils.isEmpty(v)) {
        setSelectionRange(0, v.length());
      }
    }
  }

  public boolean validate() {
    return true;
  }

  protected boolean acceptChar(char charCode) {
    if (getCharMatcher() == null) {
      return true;
    } else {
      return getCharMatcher().matches(charCode);
    }
  }

  protected CharMatcher getDefaultCharMatcher() {
    return CharMatcher.inRange(BeeConst.CHAR_SPACE, Character.MAX_VALUE);
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
