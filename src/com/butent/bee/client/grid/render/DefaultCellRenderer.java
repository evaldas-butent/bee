package com.butent.bee.client.grid.render;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.AbstractCellView;
import com.butent.bee.client.grid.ColumnDefinition;

public class DefaultCellRenderer<RowType, ColType> implements CellRenderer<RowType, ColType> {

  private boolean asHtml;

  public DefaultCellRenderer() {
  }

  public DefaultCellRenderer(boolean asHtml) {
    this.asHtml = asHtml;
  }

  public void renderRowValue(RowType rowValue, ColumnDefinition<RowType, ColType> columnDef,
      AbstractCellView<RowType> view) {
    Object cellValue = columnDef.getCellValue(rowValue);
    if (cellValue == null) {
      view.setText("");
    } else if (cellValue instanceof Widget) {
      view.setWidget((Widget) cellValue);
    } else if (asHtml) {
      view.setHTML(cellValue.toString());
    } else {
      view.setText(cellValue.toString());
    }
  }
}
