package com.butent.bee.egg.client.pst;

public interface CellRenderer<RowType, ColType> {
  void renderRowValue(RowType rowValue,
      ColumnDefinition<RowType, ColType> columnDef,
      AbstractCellView<RowType> view);
}
