package com.butent.bee.client.render;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

public class JoinRenderer extends AbstractCellRenderer implements HasItems {
  
  private class Item {
    private final int index;
    private final ValueType type;

    private Item(int index, ValueType type) {
      super();
      this.index = index;
      this.type = type;
    }
    
    private String getValue(IsRow row) {
      return DataUtils.render(row, index, type);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(JoinRenderer.class);
  
  public static final String DEFAULT_SEPARATOR = BeeConst.STRING_SPACE;
  
  private final List<? extends IsColumn> dataColumns;
  
  private final List<Item> list = Lists.newArrayList();

  private final String separator;

  public JoinRenderer(List<? extends IsColumn> dataColumns, String sep, List<String> items) {
    super(null);
    this.dataColumns = dataColumns;
    
    if (BeeUtils.isDigit(sep)) {
      this.separator = BeeUtils.space(BeeUtils.toInt(sep));
    } else if (BeeUtils.hasLength(sep, 1)) {
      this.separator = sep;
    } else {
      this.separator = DEFAULT_SEPARATOR;
    }
    
    if (!BeeUtils.isEmpty(items)) {
      addItems(items);
    }
  }

  public void addItem(String item) {
    Assert.notEmpty(item);
    
    int index = DataUtils.getColumnIndex(item, dataColumns);
    if (BeeConst.isUndef(index)) {
      logger.severe(NameUtils.getName(this), "column not found:", item);
      return;
    }
    
    list.add(new Item(index, dataColumns.get(index).getType()));
  }

  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String item : items) {
      addItem(item);
    }
  }

  public int getItemCount() {
    return list.size();
  }

  public List<String> getItems() {
    List<String> result = Lists.newArrayList();
    for (Item item : list) {
      result.add(dataColumns.get(item.index).getId());
    }
    return result;
  }

  public boolean isEmpty() {
    return getItemCount() <= 0;
  }
  
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }
  
  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    StringBuilder sb = new StringBuilder();
    for (Item item : list) {
      String value = item.getValue(row);
      if (!BeeUtils.isEmpty(value)) {
        if (sb.length() > 0) {
          sb.append(separator);
        }
        sb.append(value.trim());
      }
    }
    return sb.toString();
  }

  public void setItems(Collection<String> items) {
    if (!list.isEmpty()) {
      list.clear();
    }
    addItems(items);
  }
}
