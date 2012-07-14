package com.butent.bee.client.render;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.RangeOptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RangeRenderer extends AbstractCellRenderer implements HasItems {
  
  public static final String DEFAULT_SEPARATOR = ","; 

  public static final boolean DEFAULT_LOWER_OPEN = false; 
  public static final boolean DEFAULT_UPPER_OPEN = true; 
  
  private final Map<Range<Value>, String> map = Maps.newHashMap();

  private final String separator;
  private final Splitter splitter;
  
  private final RangeOptions rangeOptions;

  public RangeRenderer(int dataIndex, IsColumn dataColumn, String sep, String opt) {
    super(dataIndex, dataColumn);
    
    this.separator = BeeUtils.notEmpty(sep, DEFAULT_SEPARATOR).trim();
    this.splitter = Splitter.on(this.separator).trimResults().limit(3);
    
    this.rangeOptions = new RangeOptions(RangeOptions.hasLowerOpen(opt, DEFAULT_LOWER_OPEN),
        RangeOptions.hasUpperOpen(opt, DEFAULT_UPPER_OPEN), false, false);
  }

  public void addItem(String item) {
    Assert.notNull(item);

    Value low = null;
    Value upp = null;
    String value = null;
    int index = 0;

    for (String s : splitter.split(item)) {
      switch (index) {
        case 0:
          low = parse(s);
          break;
        case 1:
          upp = parse(s);
          break;
        case 2:
          value = s;
          break;
      }
      index++;
    }
    
    if (low == null && upp == null) {
      BeeKeeper.getLog().warning(NameUtils.getName(this), "cannot parse item:", item);
      return;
    }
    if (low != null && upp != null) {
      if (BeeUtils.isMore(low, upp)) {
        BeeKeeper.getLog().warning(NameUtils.getName(this), "invalid range:", item);
        return;
      }
      if (low.equals(upp) && (rangeOptions.isLowerOpen() || rangeOptions.isUpperOpen())) {
        BeeKeeper.getLog().warning(NameUtils.getName(this), "invalid range:", item,
            rangeOptions.isLowerOpen(), rangeOptions.isUpperOpen());
        return;
      }
    }
    
    Range<Value> range = rangeOptions.getRange(low, upp);
    if (range == null || range.isEmpty()) {
      BeeKeeper.getLog().warning(NameUtils.getName(this), "range is empty:", item);
    } else {
      map.put(range, value);
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
    String low;
    String upp;

    for (Map.Entry<Range<Value>, String> entry : map.entrySet()) {
      Range<Value> range = entry.getKey();
      if (range == null || range.isEmpty()) {
        continue;
      }
      
      low = range.hasLowerBound() ? range.lowerEndpoint().toString() : BeeConst.STRING_EMPTY;
      upp = range.hasUpperBound() ? range.upperEndpoint().toString() : BeeConst.STRING_EMPTY;
      
      result.add(BeeUtils.concat(separator, low + separator + upp, entry.getValue()));
    }
    return result;
  }

  public boolean isEmpty() {
    return getItemCount() <= 0;
  }
  
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }
  
  public String render(IsRow row) {
    Value v = getValue(row);
    if (v == null) {
      return null;
    }
    
    for (Map.Entry<Range<Value>, String> entry : map.entrySet()) {
      if (entry.getKey().contains(v)) {
        return entry.getValue();
      }
    }
    return v.toString();
  }

  public void setItems(Collection<String> items) {
    if (!map.isEmpty()) {
      map.clear();
    }
    addItems(items);
  }
}
