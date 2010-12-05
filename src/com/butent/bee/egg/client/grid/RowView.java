package com.butent.bee.egg.client.grid;

import java.util.Iterator;
import java.util.List;

public class RowView<RowType> {
  private int rowIndex = 0;
  private AbstractCellView<RowType> cellView;

  public RowView(AbstractCellView<RowType> cellView) {
    this.cellView = cellView;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public HasTableDefinition<RowType> getSourceTableDefinition() {
    return cellView.getSourceTableDefinition();
  }

  protected void renderRowImpl(int rowIdx, RowType rowValue,
      List<ColumnDefinition<RowType, ?>> visibleColumns) {
    this.rowIndex = rowIdx;
    int numColumns = visibleColumns.size();
    for (int i = 0; i < numColumns; i++) {
      cellView.renderCellImpl(rowIndex, i, rowValue, visibleColumns.get(i));
    }
  }

  protected void renderRowsImpl(int startRowIndex, Iterator<RowType> rowValues,
      List<ColumnDefinition<RowType, ?>> visibleColumns) {
    int curRow = startRowIndex;
    while (rowValues.hasNext()) {
      renderRowImpl(curRow, rowValues.next(), visibleColumns);
      curRow++;
    }
  }
}
