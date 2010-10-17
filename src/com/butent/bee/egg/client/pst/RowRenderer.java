package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableDefinition.AbstractRowView;

public interface RowRenderer<RowType> {
  void renderRowValue(RowType rowValue, AbstractRowView<RowType> view);
}
