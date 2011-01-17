package com.butent.bee.client.grid.render;

import com.butent.bee.client.grid.AbstractCellView;
import com.butent.bee.client.grid.ColumnDefinition;

public interface CellRenderer<RowType, ColType> {
  void renderRowValue(RowType rowValue, ColumnDefinition<RowType, ColType> columnDef,
      AbstractCellView<RowType> view);
}
