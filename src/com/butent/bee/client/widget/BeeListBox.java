package com.butent.bee.client.widget;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.State;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Implements a list box user interface component that presents a list of choices to the user.
 */

public class BeeListBox extends ListBox implements Editor, HasItems, HasValueStartIndex,
    AcceptsCaptions {

  private HasStringValue source = null;

  private boolean nullable = true;

  private boolean editing = false;

  private boolean editorInitialized = false;

  private boolean valueNumeric = false;
  private int valueStartIndex = 0;

  private int minSize = BeeConst.UNDEF;
  private int maxSize = BeeConst.UNDEF;

  private boolean changePending = false;

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

  public BeeListBox(HasStringValue source, int size) {
    this(source);
    if (size > 0) {
      setVisibleItemCount(size);
    }
  }

  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addItems(UiHelper.getCaptions(clazz));
  }

  public void addCaptions(String captionKey) {
    addItems(Global.getCaptions(captionKey));
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public void addItem(String item) {
    super.addItem(item);
    updateSize();
  }

  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String it : items) {
      addItem(it);
    }
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clear() {
    super.clear();
    updateSize();
  }
  
  public void deselect() {
    setSelectedIndex(-1);
  }

  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "list";
  }

  public List<String> getItems() {
    List<String> items = Lists.newArrayList();
    for (int i = 0; i < getItemCount(); i++) {
      items.add(getItemText(i));
    }
    return items;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getMinSize() {
    return minSize;
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
    if (!isIndex(index)) {
      return null;
    } else if (isValueNumeric()) {
      return BeeUtils.toString(index + getValueStartIndex());
    } else {
      return getValue(index);
    }
  }

  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.LIST_BOX;
  }
  
  public boolean handlesKey(int keyCode) {
    return BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN);
  }

  public boolean isEditing() {
    return editing;
  }
  
  public boolean isEmpty() {
    return getItemCount() <= 0;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }
  
  public boolean isValueNumeric() {
    return valueNumeric;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isChange(event.getType())) {
      setChangePending(true);
      if (getSource() != null) {
        getSource().setValue(getValue());
      }
      ValueChangeEvent.fire(this, getValue());

    } else if (EventUtils.isMouseDown(event.getType())) {
      setChangePending(false);
    } else if (EventUtils.isMouseUp(event.getType())) {
      if (isChangePending() && isEditing()) {
        setChangePending(false);
        fireEvent(new EditStopEvent(State.CHANGED));
      }
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void removeItem(int index) {
    super.removeItem(index);
    updateSize();
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
    if (getItemCount() > 0) {
      clear();
    }
    if (items != null) {
      addItems(items);
    }
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  public void setMinSize(int minSize) {
    this.minSize = minSize;
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

  public void setValueNumeric(boolean valueNumeric) {
    this.valueNumeric = valueNumeric;
  }

  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
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

  public void updateSize() {
    int size = Math.max(getMinSize(), 1);
    if (getMaxSize() > 0) {
      size = Math.max(size, getItemCount());
      size = Math.min(size, getMaxSize());
    }

    if (size != getVisibleItemCount()) {
      setVisibleItemCount(size);
    }
  }

  public String validate() {
    return null;
  }

  private int getIndex(String text) {
    int index = BeeConst.UNDEF;
    if (text == null) {
      return index;
    }

    if (isValueNumeric()) {
      if (BeeUtils.isDigit(BeeUtils.trim(text))) {
        int z = BeeUtils.toInt(text) - getValueStartIndex();
        if (isIndex(z)) {
          index = z;
        }
      }
    } else {
      for (int i = 0; i < getItemCount(); i++) {
        if (BeeUtils.same(getValue(i), text)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-ListBox");
    sinkEvents(Event.ONCHANGE | Event.ONMOUSEDOWN | Event.ONMOUSEUP);
  }

  private void initEditor() {
    getElement().addClassName(StyleUtils.NAME_CONTENT_BOX);
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
}
