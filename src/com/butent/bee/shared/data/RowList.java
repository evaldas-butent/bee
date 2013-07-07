package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ListSequence;
import com.butent.bee.shared.Pair;

import java.util.Collections;
import java.util.List;

/**
 * Is an abstract class for table structure implementing classes which are realized through row list
 * principle.
 */

public abstract class RowList<R extends IsRow, C extends IsColumn> extends AbstractTable<R, C> {
  private final ListSequence<R> rows;

  public RowList() {
    super();
    this.rows = new ListSequence<R>();
  }

  public RowList(List<R> rows) {
    super();
    this.rows = new ListSequence<R>(rows);
  }

  @Override
  public void clearRows() {
    getRows().clear();
  }

  @Override
  public int getNumberOfRows() {
    return getRows().getLength();
  }

  @Override
  public R getRow(int rowIndex) {
    assertRowIndex(rowIndex);
    return getRows().get(rowIndex);
  }

  @Override
  public ListSequence<R> getRows() {
    return rows;
  }

  @Override
  public void removeRow(int rowIndex) {
    assertRowIndex(rowIndex);
    getRows().remove(rowIndex);
  }

  public void setRows(List<R> list) {
    this.rows.setValues(list);
  }

  @Override
  public void sort(List<Pair<Integer, Boolean>> sortInfo) {
    Assert.notNull(sortInfo);
    Assert.isTrue(sortInfo.size() >= 1);

    if (getNumberOfRows() > 1) {
      Collections.sort(getRows().getList(), new RowOrdering<R>(getColumns(), sortInfo));
    }
  }

  @Override
  public void sortByRowId(boolean ascending) {
    if (getNumberOfRows() > 1) {
      Collections.sort(getRows().getList(), new RowIdOrdering(ascending));
    }
  }

  @Override
  protected void insertRow(int rowIndex, R row) {
    Assert.betweenInclusive(rowIndex, 0, getNumberOfRows());
    getRows().insert(rowIndex, row);
  }
}
