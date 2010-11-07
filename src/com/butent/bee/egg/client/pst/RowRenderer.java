package com.butent.bee.egg.client.pst;

public interface RowRenderer<RowType> {
  void renderRowValue(RowType rowValue, AbstractRowView<RowType> view);
}
