package com.butent.bee.shared.data.cache;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRow;

import java.util.Collection;
import java.util.List;

class CachedData extends SimpleCache<Long, BeeRow> {
  static int defaultMaxSize = 0xffff;
  static ReplacementPolicy defaultReplacementPolicy = ReplacementPolicy.LEAST_FREQUENTLY_USED;

  CachedData() {
    this(defaultMaxSize, defaultReplacementPolicy);
  }

  CachedData(int maxSize) {
    this(maxSize, defaultReplacementPolicy);
  }
  
  CachedData(ReplacementPolicy replacementPolicy) {
    this(defaultMaxSize, replacementPolicy);
  }
  
  CachedData(int maxSize, ReplacementPolicy replacementPolicy) {
    super(maxSize, replacementPolicy);
  }
  
  void addRows(Collection<BeeRow> rows) {
    Assert.notEmpty(rows);
    for (BeeRow row : rows) {
      add(row.getId(), row);
    }
  }
  
  List<BeeRow> getRows(List<Long> rowIds) {
    Assert.notEmpty(rowIds);
    List<BeeRow> result = null;
    
    BeeRow row;
    for (Long id : rowIds) {
      row = get(id);
      if (row == null) {
        result = null;
        break;
      }
      
      if (result == null) {
        result = Lists.newArrayListWithCapacity(rowIds.size());
      }
      result.add(row);
    }
    return result;
  }
}
