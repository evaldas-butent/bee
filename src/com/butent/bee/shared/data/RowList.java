package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Is an abstract class for table structure implementing classes which are realized through row list
 * principle.
 */

public abstract class RowList<R extends IsRow, C extends IsColumn> extends AbstractTable<R, C> {

  private final List<R> rows = new ArrayList<>();

  public RowList() {
    super();
  }

  public RowList(List<R> rows) {
    this();
    if (rows != null && !rows.isEmpty()) {
      this.rows.addAll(rows);
    }
  }

  @Override
  public void clearRows() {
    getRows().clear();
  }

  @Override
  public int getNumberOfRows() {
    return rows.size();
  }

  @Override
  public R getRow(int rowIndex) {
    assertRowIndex(rowIndex);
    return rows.get(rowIndex);
  }

  @Override
  public List<R> getRows() {
    return rows;
  }

  @Override
  public Iterator<R> iterator() {
    return rows.iterator();
  }

  @Override
  public void removeRow(int rowIndex) {
    assertRowIndex(rowIndex);
    getRows().remove(rowIndex);
  }

  public void setRows(List<R> list) {
    if (!rows.isEmpty()) {
      rows.clear();
    }
    if (list != null && !list.isEmpty()) {
      rows.addAll(list);
    }
  }

  @Override
  public void sort(List<Pair<Integer, Boolean>> sortInfo, Comparator<String> collator) {
    Assert.notNull(sortInfo);
    Assert.isTrue(sortInfo.size() >= 1);

    if (getNumberOfRows() > 1) {
      Collections.sort(rows, new RowOrdering<R>(getColumns(), sortInfo, collator));
    }
  }

  @Override
  public void sortByRowId(boolean ascending) {
    if (getNumberOfRows() > 1) {
      Collections.sort(rows, new RowIdOrdering(ascending));
    }
  }

  @Override
  protected void insertRow(int rowIndex, R row) {
    Assert.betweenInclusive(rowIndex, 0, getNumberOfRows());
    rows.add(rowIndex, row);
  }
}
