package com.butent.bee.egg.client.pst;

import com.google.gwt.user.client.ui.Widget;

public class DefaultCellRenderer<RowType, ColType> implements
    CellRenderer<RowType, ColType> {

  private boolean asHtml;

  public DefaultCellRenderer() {
  }

  public DefaultCellRenderer(boolean asHtml) {
    this.asHtml = asHtml;
  }

  public void renderRowValue(RowType rowValue,
      ColumnDefinition<RowType, ColType> columnDef,
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
