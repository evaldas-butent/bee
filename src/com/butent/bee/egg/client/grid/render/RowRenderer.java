package com.butent.bee.egg.client.grid.render;

import com.butent.bee.egg.client.grid.AbstractRowView;

public interface RowRenderer<RowType> {
  void renderRowValue(RowType rowValue, AbstractRowView<RowType> view);
}
