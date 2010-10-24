package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableDefinition.AbstractCellView;

public interface CellRenderer<RowType, ColType> {
  void renderRowValue(RowType rowValue,
      ColumnDefinition<RowType, ColType> columnDef,
      AbstractCellView<RowType> view);
}
