package com.butent.bee.client.widget;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.State;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implements a list box user interface component that presents a list of choices to the user.
 */

public class BeeListBox extends ListBox implements Editor, HasItems, HasValueStartIndex,
    AcceptsCaptions {

  private boolean nullable = true;

  private boolean editing = false;

  private boolean valueNumeric = false;
  private int valueStartIndex = 0;

  private int minSize = BeeConst.UNDEF;
  private int maxSize = BeeConst.UNDEF;

  private boolean changePending = false;

  private String options = null;

  private boolean handlesTabulation = false;
  
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

  @Override
  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addItems(Captions.getCaptions(clazz));
  }

  @Override
  public void addCaptions(String captionKey) {
    addItems(Captions.getCaptions(captionKey));
  }

  @Override
  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public void addItem(String item) {
    super.addItem(item);
    updateSize();
  }

  @Override
  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String it : items) {
      addItem(it);
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clear() {
    super.clear();
    updateSize();
  }

  @Override
  public void clearValue() {
    deselect();
  }

  public void deselect() {
    setSelectedIndex(-1);
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
    return "list";
  }
  
  @Override
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

  @Override
  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public OptionElement getOptionElement(int index) {
    if (isIndex(index)) {
      return getSelectElement().getOptions().getItem(index);
    } else {
      return null;
    }
  }

  @Override
  public String getOptions() {
    return options;
  }
  
  @Override
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

  @Override
  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.LIST_BOX;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN);
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  @Override
  public boolean isEditing() {
    return editing;
  }
  
  @Override
  public boolean isEmpty() {
    return getItemCount() <= 0;
  }

  @Override
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }
  
  @Override
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
  public void normalizeDisplay(String normalizedValue) {
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isChange(event.getType())) {
      setChangePending(true);
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
    setSelectedIndex(getIndex(value));
  }

  public void setValueNumeric(boolean valueNumeric) {
    this.valueNumeric = valueNumeric;
  }

  @Override
  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
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

  @Override
  public List<String> validate(boolean checkForNull) {
    return Collections.emptyList();
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return Collections.emptyList();
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

  private SelectElement getSelectElement() {
    return getElement().cast();
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    addStyleName("bee-ListBox");
    sinkEvents(Event.ONCHANGE | Event.ONMOUSEDOWN | Event.ONMOUSEUP);
  }

  private boolean isChangePending() {
    return changePending;
  }

  private void setChangePending(boolean changePending) {
    this.changePending = changePending;
  }
}
