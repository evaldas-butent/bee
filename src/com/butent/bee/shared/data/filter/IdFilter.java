package com.butent.bee.shared.data.filter;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enables to compare ID column against some value.
 */
public class IdFilter extends ComparisonFilter {

  protected IdFilter() {
    super();
  }

  protected IdFilter(Operator operator, long value) {
    super(DataUtils.ID_TAG, operator, Sets.newHashSet(value));
  }

  protected IdFilter(Collection<Long> values) {
    super(DataUtils.ID_TAG, Operator.IN, Sets.newHashSet(values));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Long> getValue() {
    return (Set<Long>) super.getValue();
  }

  @Override
  public boolean involvesColumn(String colName) {
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    long rowId = row.getId();

    if (getOperator() == Operator.IN) {
      return getValue().contains(rowId);
    } else {
      boolean ok = false;

      for (long id : getValue()) {
        switch (getOperator()) {
          case EQ:
            ok = rowId == id;
            break;
          case NE:
            ok = rowId != id;
            break;
          case LT:
            ok = rowId < id;
            break;
          case GT:
            ok = rowId > id;
            break;
          case LE:
            ok = rowId <= id;
            break;
          case GE:
            ok = rowId >= id;
            break;
          default:
            Assert.unsupported();
        }
        if ((getOperator() == Operator.EQ) == ok) {
          break;
        }
      }
      return ok;
    }
  }

  @Override
  protected Set<Long> restoreValue(String s) {
    Set<Long> ids = new HashSet<>();

    for (String id : Codec.beeDeserializeCollection(s)) {
      ids.add(BeeUtils.toLong(id));
    }
    return ids;
  }
}
