package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Comparator;
import java.util.List;

public class RowOrdering<RowType extends IsRow> implements Comparator<RowType> {

  private final List<Integer> indexes = Lists.newArrayList();
  private final List<Boolean> ascending = Lists.newArrayList();
  private final List<ValueType> types = Lists.newArrayList();

  public RowOrdering(List<? extends IsColumn> columns, List<Pair<Integer, Boolean>> sortInfo) {
    Assert.notEmpty(columns);
    Assert.notNull(sortInfo);
    Assert.isTrue(sortInfo.size() >= 1);

    ValueType type;

    for (int i = 0; i < sortInfo.size(); i++) {
      Assert.notNull(sortInfo.get(i).getA());
      int index = sortInfo.get(i).getA();
      if (containsIndex(index)) {
        continue;
      }

      switch (index) {
        case DataUtils.ID_INDEX:
          type = DataUtils.ID_TYPE;
          break;

        case DataUtils.VERSION_INDEX:
          type = DataUtils.VERSION_TYPE;
          break;

        default:
          Assert.isIndex(columns, index);
          type = columns.get(index).getType();
      }

      indexes.add(index);
      ascending.add(BeeUtils.unbox(sortInfo.get(i).getB()));
      types.add(type);
    }
  }

  public int compare(RowType row1, RowType row2) {
    if (row1 == row2) {
      return BeeConst.COMPARE_EQUAL;
    }
    if (row1 == null) {
      return ascending.get(0) ? BeeConst.COMPARE_LESS : BeeConst.COMPARE_MORE;
    }
    if (row2 == null) {
      return ascending.get(0) ? BeeConst.COMPARE_MORE : BeeConst.COMPARE_LESS;
    }

    int z;
    for (int i = 0; i < indexes.size(); i++) {
      int index = indexes.get(i);

      switch (index) {
        case DataUtils.ID_INDEX:
          z = BeeUtils.compare(row1.getId(), row2.getId());
          break;
        case DataUtils.VERSION_INDEX:
          z = BeeUtils.compare(row1.getVersion(), row2.getVersion());
          break;

        default:
          switch (types.get(i)) {
            case BOOLEAN:
              z = BeeUtils.compare(row1.getBoolean(index), row2.getBoolean(index));
              break;
            case DATE:
              z = BeeUtils.compare(row1.getDate(index), row2.getDate(index));
              break;
            case DATETIME:
              z = BeeUtils.compare(row1.getDateTime(index), row2.getDateTime(index));
              break;
            case NUMBER:
              z = BeeUtils.compare(row1.getDouble(index), row2.getDouble(index));
              break;
            case INTEGER:
              z = BeeUtils.compare(row1.getInteger(index), row2.getInteger(index));
              break;
            case LONG:
              z = BeeUtils.compare(row1.getLong(index), row2.getLong(index));
              break;
            case DECIMAL:
              z = BeeUtils.compare(row1.getDecimal(index), row2.getDecimal(index));
              break;
            default:
              z = BeeUtils.compare(row1.getString(index), row2.getString(index));
          }
      }

      if (z != BeeConst.COMPARE_EQUAL) {
        return ascending.get(i) ? z : -z;
      }
    }
    return BeeConst.COMPARE_EQUAL;
  }

  private boolean containsIndex(int index) {
    return indexes.contains(index);
  }
}
