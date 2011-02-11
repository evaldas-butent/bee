package com.butent.bee.client.grid.render;

import com.butent.bee.client.grid.AbstractCellView;
import com.butent.bee.client.grid.ColumnDefinition;
import com.butent.bee.shared.data.IsRow;

public interface CellRenderer {
  void renderRowValue(IsRow rowValue, ColumnDefinition columnDef, AbstractCellView view);
}
