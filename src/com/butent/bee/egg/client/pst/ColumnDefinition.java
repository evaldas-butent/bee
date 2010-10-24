package com.butent.bee.egg.client.pst;

public interface ColumnDefinition<RowType, ColType> {
  CellEditor<ColType> getCellEditor();

  CellRenderer<RowType, ColType> getCellRenderer();

  ColType getCellValue(RowType rowValue);

  <P extends ColumnProperty> P getColumnProperty(ColumnProperty.Type<P> type);

  void setCellValue(RowType rowValue, ColType cellValue);
}
