package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableDefinition.AbstractCellView;

/**
 * Cell renderers can be used to customize the display of data in a table cell.
 * 
 * @param <RowType> the type of the row value
 * @param <ColType> the data type of the column
 */
public interface CellRenderer<RowType, ColType> {
  /**
   * Render the contents of a cell as a
   * {@link com.google.gwt.user.client.ui.Widget} or text or HTML.
   * 
   * @param rowValue the object associated with the row
   * @param columnDef the associated column definition
   * @param view the view used to set the cell contents
   */
  void renderRowValue(RowType rowValue,
      ColumnDefinition<RowType, ColType> columnDef,
      AbstractCellView<RowType> view);
}
