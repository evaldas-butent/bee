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

public abstract class RowList<RowType extends IsRow, ColType extends IsColumn> extends
    AbstractTable<RowType, ColType> {
  private final ListSequence<RowType> rows;

  public RowList() {
    super();
    this.rows = new ListSequence<RowType>();
  }

  public RowList(List<RowType> rows) {
    super();
    this.rows = new ListSequence<RowType>(rows);
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
  public RowType getRow(int rowIndex) {
    assertRowIndex(rowIndex);
    return getRows().get(rowIndex);
  }

  public ListSequence<RowType> getRows() {
    return rows;
  }

  @Override
  public void removeRow(int rowIndex) {
    assertRowIndex(rowIndex);
    getRows().remove(rowIndex);
  }

  public void setRows(List<RowType> list) {
    this.rows.setValues(list);
  }

  @Override
  public void sort(List<Pair<Integer, Boolean>> sortInfo) {
    Assert.notNull(sortInfo);
    Assert.isTrue(sortInfo.size() >= 1);

    if (getNumberOfRows() > 1) {
      Collections.sort(getRows().getList(), new RowOrdering<RowType>(getColumns(), sortInfo));
    }
  }

  @Override
  public void sortByRowId(boolean ascending) {
    if (getNumberOfRows() > 1) {
      Collections.sort(getRows().getList(), new RowIdOrdering(ascending));
    }
  }

  @Override
  protected void assertRowIndex(int rowIndex) {
    Assert.isIndex(getRows(), rowIndex);
  }

  @Override
  protected void insertRow(int rowIndex, RowType row) {
    Assert.betweenInclusive(rowIndex, 0, getNumberOfRows());
    getRows().insert(rowIndex, row);
  }
}
