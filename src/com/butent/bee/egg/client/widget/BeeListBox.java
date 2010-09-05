package com.butent.bee.egg.client.widget;

import java.util.List;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.event.HasBeeChangeHandler;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ListBox;

public class BeeListBox extends ListBox implements HasId, HasBeeChangeHandler {
  private String fieldName = null;

  public BeeListBox() {
    super();
    createId();
  }

  public BeeListBox(boolean isMultipleSelect) {
    super(isMultipleSelect);
    createId();
  }

  public BeeListBox(Element element) {
    super(element);
    createId();
  }

  public BeeListBox(String fieldName) {
    this();
    this.fieldName = fieldName;
    addItems(BeeGlobal.getFieldItems(fieldName));
    addDefaultHandlers();

    String v = BeeGlobal.getFieldValue(fieldName);
    if (!BeeUtils.isEmpty(v))
      setSelectedIndex(getIndex(v));
  }

  public BeeListBox(String fieldName, int cnt) {
    this(fieldName);
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
  }

  public BeeListBox(String fieldName, boolean allVisible) {
    this(fieldName);
    
    if (allVisible) {
      int cnt = getItemCount();
      if (cnt > 0) {
        setVisibleItemCount(cnt);
      }
    }
  }
  
  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void addItems(List<String> items) {
    Assert.notNull(items);

    for (String it : items)
      addItem(it);
  }

  public boolean onChange() {
    if (!BeeUtils.isEmpty(getFieldName()))
      BeeGlobal.setFieldValue(getFieldName(), getValue(getSelectedIndex()));
    return true;
  }

  private void createId() {
    BeeDom.setId(this);
  }

  private void addDefaultHandlers() {
    BeeBus.addVch(this);
  }

  private int getIndex(String v) {
    int idx = -1;

    for (int i = 0; i < getItemCount(); i++)
      if (getValue(i).equals(v)) {
        idx = i;
        break;
      }

    return idx;
  }

}
