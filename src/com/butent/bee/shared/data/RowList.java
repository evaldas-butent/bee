package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ListSequence;
import com.butent.bee.shared.data.sort.SortInfo;

import java.util.Collections;

public abstract class RowList<RowType extends IsRow, ColType extends IsColumn> extends
    AbstractTable<RowType, ColType> {
  private ListSequence<RowType> rows;

  public RowList() {
    super();
    this.rows = new ListSequence<RowType>(0);
  }

  public RowList(ListSequence<RowType> rows) {
    super();
    this.rows = rows;
  }

  @Override
  public int getNumberOfRows() {
    return getRows().length();
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

  public void setRows(ListSequence<RowType> rows) {
    this.rows = rows;
  }

  @Override
  public void sort(SortInfo... sortInfo) {
    if (getNumberOfRows() > 1) {
      Collections.sort(getRows().getList(), new RowOrdering(sortInfo));
    }
  }

  @Override
  protected void assertRowIndex(int rowIndex) {
    Assert.isIndex(getRows(), rowIndex);
  }

  @Override
  protected void clearRows() {
    getRows().clear();
  }

  @Override
  protected void insertRow(int rowIndex, RowType row) {
    Assert.betweenInclusive(rowIndex, 0, getNumberOfRows());
    getRows().insert(rowIndex, row);
  }
}
