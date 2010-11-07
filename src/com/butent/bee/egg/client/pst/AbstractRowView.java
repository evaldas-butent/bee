package com.butent.bee.egg.client.pst;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractRowView<RowType> {
  private int rowIndex = 0;
  private AbstractCellView<RowType> cellView;

  public AbstractRowView(AbstractCellView<RowType> cellView) {
    this.cellView = cellView;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public HasTableDefinition<RowType> getSourceTableDefinition() {
    return cellView.getSourceTableDefinition();
  }

  public abstract void setStyleAttribute(String attr, String value);

  public abstract void setStyleName(String stylename);

  protected void renderRowImpl(int rowIndex, RowType rowValue,
      RowRenderer<RowType> rowRenderer,
      List<ColumnDefinition<RowType, ?>> visibleColumns) {
    this.rowIndex = rowIndex;
    renderRowValue(rowValue, rowRenderer);
    int numColumns = visibleColumns.size();
    for (int i = 0; i < numColumns; i++) {
      cellView.renderCellImpl(rowIndex, i, rowValue, visibleColumns.get(i));
    }
  }

  protected void renderRowsImpl(int startRowIndex,
      Iterator<RowType> rowValues, RowRenderer<RowType> rowRenderer,
      List<ColumnDefinition<RowType, ?>> visibleColumns) {
    int curRow = startRowIndex;
    while (rowValues.hasNext()) {
      renderRowImpl(curRow, rowValues.next(), rowRenderer, visibleColumns);
      curRow++;
    }
  }

  protected void renderRowValue(RowType rowValue, RowRenderer<RowType> rowRenderer) {
    rowRenderer.renderRowValue(rowValue, this);
  }
}
