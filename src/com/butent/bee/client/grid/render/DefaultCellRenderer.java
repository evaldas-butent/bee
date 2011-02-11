package com.butent.bee.client.grid.render;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.AbstractCellView;
import com.butent.bee.client.grid.ColumnDefinition;
import com.butent.bee.shared.data.IsRow;

public class DefaultCellRenderer implements CellRenderer {

  private boolean asHtml;

  public DefaultCellRenderer() {
  }

  public DefaultCellRenderer(boolean asHtml) {
    this.asHtml = asHtml;
  }

  public void renderRowValue(IsRow rowValue, ColumnDefinition columnDef,
      AbstractCellView view) {
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
