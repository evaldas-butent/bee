package com.butent.bee.client.render;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

public class SimpleRenderer extends AbstractCellRenderer {

  public SimpleRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null || getCellSource() == null) {
      return null;
    } else {
      return getCellSource().render(row);
    }
  }
}
