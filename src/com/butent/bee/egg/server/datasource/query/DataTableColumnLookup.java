package com.butent.bee.egg.server.datasource.query;

import com.butent.bee.egg.server.datasource.datatable.DataTable;

public class DataTableColumnLookup implements ColumnLookup {
  private DataTable table;

  public DataTableColumnLookup(DataTable table) {
    this.table = table;
  }

  @Override
  public boolean containsColumn(AbstractColumn column) {
    return table.containsColumn(column.getId());
  }

  @Override
  public int getColumnIndex(AbstractColumn column) {
    return table.getColumnIndex(column.getId());
  }
}
