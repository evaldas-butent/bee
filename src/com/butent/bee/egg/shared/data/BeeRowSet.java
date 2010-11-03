package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeRowSet {

  public class BeeRow {
    private String[] data;

    private BeeRow(String[] row) {
      int cnt = getColumnCount();
      Assert.arrayLength(row, cnt, cnt);

      data = row;
    }

    public int getInt(int col) {
      return BeeUtils.toInt(getValue(col));
    }

    public int getInt(String colName) {
      return getInt(getColumnIndex(colName));
    }

    public long getLong(int col) {
      return BeeUtils.toLong(getValue(col));
    }

    public long getLong(String colName) {
      return getLong(getColumnIndex(colName));
    }

    public String getString(int col) {
      return BeeUtils.ifString(getValue(col), BeeConst.STRING_EMPTY);
    }

    public String getString(String colName) {
      return getString(getColumnIndex(colName));
    }

    public String getValue(int col) {
      Assert.betweenExclusive(col, 0, getColumnCount());

      return data[col];
    }
  }

  private final BeeColumn[] columns;
  private List<BeeRow> rows;

  public BeeRowSet(BeeColumn[] columns) {
    Assert.notEmpty(columns);

    this.columns = columns;
  }

  public void addRow(String[] row) {
    if (BeeUtils.isEmpty(rows)) {
      rows = new ArrayList<BeeRow>();
    }
    rows.add(new BeeRow(row));
  }

  public int getColumnCount() {
    return columns.length;
  }

  public BeeColumn[] getColumns() {
    return columns;
  }

  public BeeRow getRow(int row) {
    Assert.betweenExclusive(row, 0, getRowCount());

    return rows.get(row);
  }

  public int getRowCount() {
    int cnt = 0;

    if (!BeeUtils.isEmpty(rows)) {
      cnt = rows.size();
    }
    return cnt;
  }

  public List<BeeRow> getRows() {
    return rows;
  }

  public boolean isEmpty() {
    return getRowCount() <= 0;
  }

  private int getColumnIndex(String colName) {
    for (int i = 0; i < getColumnCount(); i++) {
      if (BeeUtils.same(colName, columns[i].getName())) {
        return i;
      }
    }
    return -1;
  }
}
