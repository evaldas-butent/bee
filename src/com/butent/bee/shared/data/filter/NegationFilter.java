package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a filter which applies equality satisfying conditions to a set of data.
 */

public class NegationFilter extends Filter {

  private Filter subFilter;

  public NegationFilter(Filter subFilter) {
    Assert.notNull(subFilter);
    this.subFilter = subFilter;
  }

  protected NegationFilter() {
    super();
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    subFilter = Filter.restore(s);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NegationFilter other = (NegationFilter) obj;

    if (!subFilter.equals(other.subFilter)) {
      return false;
    }
    return true;
  }

  public Filter getSubFilter() {
    return subFilter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + subFilter.hashCode();
    return result;
  }

  @Override
  public boolean involvesColumn(String colName) {
    return subFilter.involvesColumn(colName);
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    return !subFilter.isMatch(columns, row);
  }

  @Override
  public String serialize() {
    return super.serialize(subFilter);
  }

  @Override
  public String toString() {
    return CompoundType.NOT.toTextString() + BeeUtils.parenthesize(subFilter.transform());
  }
}
