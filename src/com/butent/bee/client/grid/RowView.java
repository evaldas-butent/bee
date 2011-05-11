package com.butent.bee.client.grid;

import com.butent.bee.shared.data.IsRow;

import java.util.Iterator;
import java.util.List;

/**
 * Creates and renders row views, which enable to show only parts of a data table to the user.
 */

public class RowView {
  private int rowIndex = 0;
  private AbstractCellView cellView;

  public RowView(AbstractCellView cellView) {
    this.cellView = cellView;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public HasTableDefinition getSourceTableDefinition() {
    return cellView.getSourceTableDefinition();
  }

  protected void renderRowImpl(int rowIdx, IsRow rowValue, List<ColumnDefinition> visibleColumns) {
    this.rowIndex = rowIdx;
    int numColumns = visibleColumns.size();
    for (int i = 0; i < numColumns; i++) {
      cellView.renderCellImpl(rowIndex, i, rowValue, visibleColumns.get(i));
    }
  }

  protected void renderRowsImpl(int startRowIndex, Iterator<IsRow> rowValues,
      List<ColumnDefinition> visibleColumns) {
    int curRow = startRowIndex;
    while (rowValues.hasNext()) {
      renderRowImpl(curRow, rowValues.next(), visibleColumns);
      curRow++;
    }
  }
}
