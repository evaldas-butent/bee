package com.butent.bee.client.render;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MapRenderer extends AbstractCellRenderer implements HasItems {
  
  public static final String DEFAULT_SEPARATOR = "="; 
  
  private final Map<String, String> map = Maps.newHashMap();

  private final String separator;
  private final Splitter splitter;

  public MapRenderer(int dataIndex, IsColumn dataColumn, String sep) {
    super(dataIndex, dataColumn);
    
    this.separator = BeeUtils.ifString(sep, DEFAULT_SEPARATOR).trim();
    this.splitter = Splitter.on(this.separator).omitEmptyStrings().trimResults().limit(2);
  }

  public void addItem(String item) {
    Assert.notNull(item);
    Value key = null;
    String value = null;
    int index = 0;

    for (String s : splitter.split(item)) {
      switch (index) {
        case 0:
          key = parse(s);
          break;
        case 1:
          value = s;
          break;
      }
      index++;
    }
    
    if (key == null || BeeUtils.isEmpty(value)) {
      BeeKeeper.getLog().warning(NameUtils.getName(this), "cannot add item:", item);
    } else {
      map.put(key.getString(), value);
    }
  }

  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String item : items) {
      addItem(item);
    }
  }

  public int getItemCount() {
    return map.size();
  }

  public List<String> getItems() {
    List<String> result = Lists.newArrayList();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = Value.parseValue(getDataType(), entry.getKey(), false).toString();
      if (!BeeUtils.isEmpty(key)) {
        result.add(BeeUtils.concat(1, key, separator, entry.getValue()));
      }
    }
    return result;
  }

  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }
  
  public String render(IsRow row) {
    String key = getString(row);
    if (key == null) {
      return null;
    } else if (map.containsKey(key)) {
      return map.get(key);
    } else {
      return getValue(row).toString();
    }
  }

  public void setItems(Collection<String> items) {
    if (!map.isEmpty()) {
      map.clear();
    }
    addItems(items);
  }
}
