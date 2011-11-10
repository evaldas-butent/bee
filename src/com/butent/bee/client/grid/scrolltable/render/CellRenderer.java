package com.butent.bee.client.grid.scrolltable.render;

import com.butent.bee.client.grid.scrolltable.AbstractCellView;
import com.butent.bee.client.grid.scrolltable.ColumnDefinition;
import com.butent.bee.shared.data.IsRow;

/**
 * Specifies cell painting methods to have {@code renderRowValue} method.
 */

public interface CellRenderer {
  void renderRowValue(IsRow rowValue, ColumnDefinition columnDef, AbstractCellView view);
}
