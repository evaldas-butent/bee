package com.butent.bee.client.render;

import com.google.common.collect.Lists;

import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.HasValueStartIndex;

import java.util.Collection;
import java.util.List;

public class ListRenderer extends AbstractCellRenderer implements HasItems, HasValueStartIndex {

  private final List<String> itemList;
  private final HasItems itemProxy;

  private int valueStartIndex = 0;

  public ListRenderer(int dataIndex, IsColumn dataColumn) {
    this(dataIndex, dataColumn, null);
  }

  public ListRenderer(int dataIndex, IsColumn dataColumn, Collection<String> items) {
    this(dataIndex, dataColumn, items, 0);
  }

  public ListRenderer(int dataIndex, IsColumn dataColumn, Collection<String> items,
      int valueStartIndex) {
    this(dataIndex, dataColumn, items, valueStartIndex, false, null);
  }

  public ListRenderer(int dataIndex, IsColumn dataColumn, boolean useProxy, HasItems proxy) {
    this(dataIndex, dataColumn, null, 0, useProxy, proxy);

    if (useProxy && proxy instanceof HasValueStartIndex) {
      setValueStartIndex(((HasValueStartIndex) proxy).getValueStartIndex());
    }
  }

  public ListRenderer(int dataIndex, IsColumn dataColumn, boolean useProxy, HasItems proxy,
      int valueStartIndex) {
    this(dataIndex, dataColumn, null, valueStartIndex, useProxy, proxy);
  }

  private ListRenderer(int dataIndex, IsColumn dataColumn, Collection<String> items,
      int valueStartIndex, boolean useProxy, HasItems proxy) {
    super(dataIndex, dataColumn);

    if (!useProxy || proxy == null) {
      this.itemList = Lists.newArrayList();
      this.itemProxy = null;
      addItems(items);
    } else {
      this.itemList = null;
      this.itemProxy = proxy;
    }

    this.valueStartIndex = valueStartIndex;
  }

  public void addItem(String item) {
    getItems().add(item);
  }

  public void addItems(Collection<String> items) {
    if (items != null) {
      getItems().addAll(items);
    }
  }

  public int getItemCount() {
    return getItems().size();
  }

  public List<String> getItems() {
    if (itemProxy == null) {
      return itemList;
    } else {
      return itemProxy.getItems();
    }
  }

  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Integer value = row.getInteger(getDataIndex());
    if (value == null) {
      return null;
    }

    int index = value - getValueStartIndex();
    if (index >= 0 && index < getItemCount()) {
      return getItems().get(index);
    } else {
      return null;
    }
  }

  public void setItems(Collection<String> items) {
    if (!getItems().isEmpty()) {
      getItems().clear();
    }
    addItems(items);
  }

  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }
}
