package com.butent.bee.egg.client.pst;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.pst.TableDefinition.AbstractCellView;

/**
 * The default {@link CellRenderer} used by the {@link AbstractColumnDefinition}
 * when the user does not specify one. By default, {@link Widget}s are rendered
 * as {@link Widget}s, and all other values are rendered as text.
 * 
 * 
 * @param <RowType> the type of the row value
 * @param <ColType> the data type of the column
 */
public class DefaultCellRenderer<RowType, ColType> implements
    CellRenderer<RowType, ColType> {

  /**
   * If true, text will be rendered as html.
   */
  private boolean asHtml;

  /**
   * Construct a new {@link DefaultCellRenderer}.
   */
  public DefaultCellRenderer() {
  }

  /**
   * Construct a new {@link DefaultCellRenderer}.
   * 
   * @param asHtml if true, non-widget cell contents will be rendered as html
   */
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
