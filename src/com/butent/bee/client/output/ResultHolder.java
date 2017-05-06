package com.butent.bee.client.output;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.report.ReportInfoItem;
import com.butent.bee.shared.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ResultHolder {

  public enum ResultLevel {
    CELL, COL, GROUP, GROUP_COL, ROW, TOTAL
  }

  private final Multimap<ReportValue, ReportValue> rowGroups = TreeMultimap.create();
  private final Set<ReportValue> colGroups = new TreeSet<>();
  private final Table<ReportValue, String, Object> values = HashBasedTable.create();

  public void addValues(ReportValue rowGroup, ReportValue[] rowValues, ReportValue colGroup,
      ReportInfoItem infoItem, ReportValue value) {

    ReportValue rowValue = ReportValue.of(rowValues);

    ReportValue key = getKey(ResultLevel.CELL, rowGroup, rowValue, colGroup);
    ReportItem item = infoItem.getItem();
    String col = item.getName();
    Object calc = item.calculate(getValue(key, col), value, infoItem.getFunction());

    if (calc == null) {
      return;
    }
    if (!rowGroups.containsEntry(rowGroup, rowValue)) {
      rowGroups.put(rowGroup, rowValue);
    }
    if (!colGroups.contains(colGroup)) {
      colGroups.add(colGroup);
    }
    putValue(key, col, calc);

    if (infoItem.isRowSummary() || infoItem.isSorted()) {
      key = getKey(ResultLevel.ROW, rowGroup, rowValue);
      putValue(key, col, item.calculate(getValue(key, col), value, infoItem.getFunction()));
    }
    if (infoItem.isColSummary()) {
      if (infoItem.isRowSummary()) {
        key = getKey(ResultLevel.TOTAL);
        putValue(key, col, item.calculate(getValue(key, col), value, infoItem.getFunction()));
      }
      key = getKey(ResultLevel.COL, colGroup);
      putValue(key, col, item.calculate(getValue(key, col), value, infoItem.getFunction()));
    }
    if (infoItem.isGroupSummary()) {
      if (infoItem.isRowSummary() || infoItem.isSorted()) {
        key = getKey(ResultLevel.GROUP, rowGroup);
        putValue(key, col, item.calculate(getValue(key, col), value, infoItem.getFunction()));
      }
      key = getKey(ResultLevel.GROUP_COL, rowGroup, colGroup);
      putValue(key, col, item.calculate(getValue(key, col), value, infoItem.getFunction()));
    }
  }

  public Object getCellValue(ReportValue rowGroup, ReportValue[] row, ReportValue colGroup,
      String col) {
    return getValue(getKey(ResultLevel.CELL, rowGroup, ReportValue.of(row), colGroup), col);
  }

  public Collection<ReportValue> getColGroups() {
    return colGroups;
  }

  public Object getColTotal(ReportValue colGroup, String col) {
    return getValue(getKey(ResultLevel.COL, colGroup), col);
  }

  public Object getGroupTotal(ReportValue rowGroup, String col) {
    return getValue(getKey(ResultLevel.GROUP, rowGroup), col);
  }

  public Object getGroupValue(ReportValue rowGroup, ReportValue colGroup, String col) {
    return getValue(getKey(ResultLevel.GROUP_COL, rowGroup, colGroup), col);
  }

  public Collection<ReportValue> getRowGroups(ReportInfoItem sortedItem) {
    List<ReportValue> result = new ArrayList<>(rowGroups.keySet());

    if (sortedItem != null) {
      Map<ReportValue, Object> items = new HashMap<>();

      for (ReportValue rowGroup : result) {
        items.put(rowGroup, getGroupTotal(rowGroup, sortedItem.getItem().getName()));
      }
      sort(result, items, sortedItem.getDescending());
    }
    return result;
  }

  public Collection<ReportValue[]> getRows(ReportValue rowGroup, ReportInfoItem sortedItem) {
    List<ReportValue> rows = new ArrayList<>(rowGroups.get(rowGroup));

    if (sortedItem != null) {
      Map<ReportValue, Object> items = new HashMap<>();

      for (ReportValue row : rows) {
        items.put(row, getRowTotal(rowGroup, row.getValues(), sortedItem.getItem().getName()));
      }
      sort(rows, items, sortedItem.getDescending());
    }
    List<ReportValue[]> result = new ArrayList<>();

    for (ReportValue row : rows) {
      result.add(row.getValues());
    }
    return result;
  }

  public Object getRowTotal(ReportValue rowGroup, ReportValue[] row, String col) {
    return getValue(getKey(ResultLevel.ROW, rowGroup, ReportValue.of(row)), col);
  }

  public Object getTotal(String col) {
    return getValue(getKey(ResultLevel.TOTAL), col);
  }

  private static ReportValue getKey(ResultLevel level, ReportValue... prm) {
    int l = ArrayUtils.length(prm);
    ReportValue[] params = new ReportValue[l + 1];
    params[0] = ReportValue.of(level.name());

    System.arraycopy(prm, 0, params, 1, l);

    return ReportValue.of(params);
  }

  private Object getValue(ReportValue row, String col) {
    return values.get(row, col);
  }

  private void putValue(ReportValue row, String col, Object value) {
    if (value != null) {
      values.put(row, col, value);
    }
  }

  private static void sort(List<ReportValue> result, final Map<ReportValue, Object> items,
      boolean descending) {

    result.sort((value1, value2) -> {
      Object item1;
      Object item2;

      if (descending) {
        item1 = items.get(value2);
        item2 = items.get(value1);
      } else {
        item1 = items.get(value1);
        item2 = items.get(value2);
      }

      if (item1 == null) {
        if (item2 == null) {
          return BeeConst.COMPARE_EQUAL;
        } else {
          return BeeConst.COMPARE_LESS;
        }
      } else if (item2 == null) {
        return BeeConst.COMPARE_MORE;
      } else if (item1 instanceof Comparable) {
        @SuppressWarnings("unchecked") int res = ((Comparable<Object>) item1).compareTo(item2);
        return res;
      } else {
        return BeeConst.COMPARE_EQUAL;
      }
    });
  }
}
