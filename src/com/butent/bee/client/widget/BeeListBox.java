package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ListBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasBeeChangeHandler;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.State;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

/**
 * Implements a list box user interface component that presents a list of choices to the user.
 */

public class BeeListBox extends ListBox implements Editor, HasItems, HasBeeChangeHandler {
  
  public static int changeTimeout = 200;

  private HasStringValue source = null;

  private boolean valueChangeHandlerInitialized = false;

  private boolean nullable = true;

  private boolean editing = false;

  private boolean editorInitialized = false;
  
  private boolean changePending = false;
  
  private Timer changeTimer = null;
  
  private int startChar = 0; 

  public BeeListBox() {
    super();
    init();
  }

  public BeeListBox(boolean isMultipleSelect) {
    super(isMultipleSelect);
    init();
  }

  public BeeListBox(Element element) {
    super(element);
    init();
  }

  public BeeListBox(HasStringValue source) {
    this();
    this.source = source;

    if (source instanceof Variable) {
      initVar((Variable) source);
    }
  }

  public BeeListBox(HasStringValue source, boolean allVisible) {
    this(source);
    if (allVisible) {
      setAllVisible();
    }
  }

  public BeeListBox(HasStringValue source, int cnt) {
    this(source);
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public void addItems(Collection<String> items) {
    Assert.notNull(items);

    for (String it : items) {
      addItem(it);
    }
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    if (!valueChangeHandlerInitialized) {
      valueChangeHandlerInitialized = true;
      addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
          ValueChangeEvent.fire(BeeListBox.this, getValue());
        }
      });
    }
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "list";
  }

  public int getIndex(String text) {
    int index = -1;
    if (text == null) {
      return index;
    }

    for (int i = 0; i < getItemCount(); i++) {
      if (BeeUtils.same(getValue(i), text)) {
        index = i;
        break;
      }
    }
    return index;
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

  public String getValue() {
    int index = getSelectedIndex();
    if (isIndex(index)) {
      return getValue(index);
    } else {
      return null;
    }
  }

  public boolean handlesKey(int keyCode) {
    return keyCode != KeyCodes.KEY_TAB;
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (isEditing() && EventUtils.isKeyUp(event.getType())) {
      switch (event.getKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          setChangePending(false);
          fireEvent(new EditStopEvent(State.CANCELED));
          return;
        case KeyCodes.KEY_ENTER:
          if (getStartChar() == EditorFactory.START_KEY_ENTER) {
            setStartChar(0);
          } else if (!isChangePending()) {
            fireEvent(new EditStopEvent(State.CHANGED));
            return;
          }
      }
    }
    super.onBrowserEvent(event);
  }

  public void onChange() {
    if (getSource() != null) {
      getSource().setValue(getValue(getSelectedIndex()));
    }
    if (isEditing() && !isChangePending()) {
      if (changeTimer != null && changeTimeout > 0) {
        setChangePending(true);
        changeTimer.schedule(changeTimeout);
      } else {
        fireEvent(new EditStopEvent(State.CHANGED));
      }
    }
  }

  public void setAllVisible() {
    int cnt = getItemCount();
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setItems(Collection<String> items) {
    if (items != null) {
      if (getItemCount() > 0) {
        clear();
      }
      addItems(items);
    }
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    String oldValue = getValue();
    setSelectedIndex(getIndex(value));
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    setStartChar(charCode);
    setChangePending(false);

    if (!isEditorInitialized()) {
      initEditor();
      setEditorInitialized(true);
    }

    if (charCode > BeeConst.CHAR_SPACE) {
      for (int i = 0; i < getItemCount(); i++) {
        if (BeeUtils.startsWith(getValue(i), charCode)) {
          setSelectedIndex(i);
          return;
        }
      }
    }
    setSelectedIndex(Math.max(getIndex(oldValue), 0));
  }

  public String validate() {
    return null;
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addVch(this);
  }

  private int getStartChar() {
    return startChar;
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-ListBox");
    addDefaultHandlers();
  }

  private void initEditor() {
    getElement().addClassName(StyleUtils.NAME_CONTENT_BOX);
    sinkEvents(Event.ONKEYUP);
    
    if (changeTimeout > 0) {
      this.changeTimer = new Timer() {
        @Override
        public void run() {
          if (isChangePending()) {
            fireEvent(new EditStopEvent(State.CHANGED));
          }
        }
      };
    }
  }

  private void initVar(Variable var) {
    addItems(var.getItems());

    String v = var.getValue();
    if (!BeeUtils.isEmpty(v)) {
      setSelectedIndex(getIndex(v));
    }
  }

  private boolean isChangePending() {
    return changePending;
  }

  private boolean isEditorInitialized() {
    return editorInitialized;
  }
  
  private boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }

  private void setChangePending(boolean changePending) {
    this.changePending = changePending;
  }

  private void setEditorInitialized(boolean editorInitialized) {
    this.editorInitialized = editorInitialized;
  }

  private void setStartChar(int startChar) {
    this.startChar = startChar;
  }
}
