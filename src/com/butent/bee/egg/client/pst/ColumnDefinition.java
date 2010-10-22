package com.butent.bee.egg.client.pst;

public interface ColumnDefinition<RowType, ColType> {
  /**
   * Get the {@link CellEditor} that should be used to edit the contents of
   * cells in this column.
   * 
   * @return the associated {@link CellEditor}
   */
  CellEditor<ColType> getCellEditor();

  /**
   * Get the cell renderer associated with the column. The return value should
   * not be null.
   * 
   * @return the associated {@link CellRenderer}
   */
  CellRenderer<RowType, ColType> getCellRenderer();

  /**
   * Get the cell value associated with the row value.
   * 
   * @param rowValue the row value
   * @return the cell value for the given row value
   */
  ColType getCellValue(RowType rowValue);

  /**
   * <p>
   * Get the {@link ColumnProperty} associated with the specified
   * {@link ColumnProperty.Type}. If the property is not defined, the default
   * value will be returned.
   * </p>
   * <p>
   * This method should never return null. Instead, it should return the default
   * property from {@link ColumnProperty.Type#getDefault()}.
   * </p>
   * 
   * @param <P> the column property type
   * @param type the {@link ColumnProperty} type
   * @return the property, or the default value if the property is not defined
   */
  <P extends ColumnProperty> P getColumnProperty(ColumnProperty.Type<P> type);

  /**
   * Set the value of this column in the row value.
   * 
   * @param rowValue the value of the row
   * @param cellValue the new value of the cell
   */
  void setCellValue(RowType rowValue, ColType cellValue);
}
