package com.butent.bee.shared.data.cache;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.List;

/**
 * Enables storing frequently used queries and their results into memory.
 */

class CachedQuery extends SimpleCache<Integer, Long> {
  static int defaultMaxSize = 0xffff;
  static ReplacementPolicy defaultReplacementPolicy = ReplacementPolicy.LEAST_FREQUENTLY_USED;

  private final String strFilter;
  private final String strOrder;

  private int rowCount = BeeConst.SIZE_UNKNOWN;

  CachedQuery(Filter filter, Order order) {
    this(filter, order, defaultMaxSize, defaultReplacementPolicy);
  }

  CachedQuery(Filter filter, Order order, int maxSize) {
    this(filter, order, maxSize, defaultReplacementPolicy);
  }

  CachedQuery(Filter filter, Order order, int maxSize, ReplacementPolicy replacementPolicy) {
    super(maxSize, replacementPolicy);
    this.strFilter = transformFilter(filter);
    this.strOrder = transformOrder(order);
  }

  CachedQuery(Filter filter, Order order, ReplacementPolicy replacementPolicy) {
    this(filter, order, defaultMaxSize, replacementPolicy);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CachedQuery)) {
      return false;
    }
    CachedQuery other = (CachedQuery) obj;
    return BeeUtils.same(this.strFilter, other.strFilter)
        && BeeUtils.same(this.strOrder, other.strOrder);
  }

  @Override
  public List<Property> getInfo() {
    List<Property> lst = Lists.newArrayList();

    if (!BeeUtils.isEmpty(strFilter)) {
      lst.add(new Property("Filter", strFilter));
    }
    if (!BeeUtils.isEmpty(strOrder)) {
      lst.add(new Property("Order", strOrder));
    }
    lst.add(new Property("Row Count", BeeUtils.toString(getRowCount())));

    lst.addAll(super.getInfo());
    return lst;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(strFilter, strOrder);
  }

  void addRange(int offset, List<Long> idList) {
    Assert.nonNegative(offset);
    Assert.notNull(idList);

    for (int i = 0; i < idList.size(); i++) {
      add(offset + i, idList.get(i));
    }
  }

  int getRowCount() {
    return rowCount;
  }

  List<Long> getRowIds(int start, int length) {
    Assert.nonNegative(start);
    Assert.isPositive(length);

    List<Long> result = null;
    Long rowId;

    for (int i = 0; i < length; i++) {
      rowId = get(start + i);
      if (rowId == null) {
        result = null;
        break;
      }
      if (result == null) {
        result = Lists.newArrayListWithCapacity(length);
      }
      result.add(rowId);
    }
    return result;
  }

  boolean same(Filter flt, Order ord) {
    return sameFilter(flt) && sameOrder(ord);
  }

  boolean sameFilter(Filter flt) {
    return BeeUtils.same(this.strFilter, transformFilter(flt));
  }

  boolean sameOrder(Order ord) {
    return BeeUtils.same(this.strOrder, transformOrder(ord));
  }

  void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  private String transformFilter(Filter flt) {
    return BeeUtils.transform(flt);
  }

  private String transformOrder(Order ord) {
    return BeeUtils.transform(ord);
  }
}
