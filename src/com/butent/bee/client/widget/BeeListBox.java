package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeChangeHandler;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a list box user interface component that presents a list of choices to the user.
 */

public class BeeListBox extends ListBox implements Editor, HasBeeChangeHandler {

  private HasStringValue source = null;

  private boolean valueChangeHandlerInitialized = false;

  private boolean nullable = true;
  
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
    addDefaultHandlers();

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

  public void addItems(List<String> items) {
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

  public void createId() {
    DomUtils.createId(this, "list");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getIndex(String text) {
    Assert.notNull(text);
    int index = -1;

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
    return getValue(getSelectedIndex());
  }

  public boolean isNullable() {
    return nullable;
  }

  public boolean onChange() {
    if (getSource() != null) {
      getSource().setValue(getValue(getSelectedIndex()));
    }
    return true;
  }
  
  public void setAllVisible() {
    int cnt = getItemCount();
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
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

  public void startEdit(String oldValue, char charCode) {
  }

  public String validate() {
    return null;
  }
  
  private void addDefaultHandlers() {
    BeeKeeper.getBus().addVch(this);
  }

  private void init() {
    createId();
    setStyleName("bee-ListBox");
  }

  private void initVar(Variable var) {
    addItems(var.getItems());

    String v = var.getValue();
    if (!BeeUtils.isEmpty(v)) {
      setSelectedIndex(getIndex(v));
    }
  }
}
