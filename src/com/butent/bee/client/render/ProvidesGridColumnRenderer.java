package com.butent.bee.client.render;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.ui.ColumnDescription;

import java.util.List;

public interface ProvidesGridColumnRenderer {
  AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource);
}
