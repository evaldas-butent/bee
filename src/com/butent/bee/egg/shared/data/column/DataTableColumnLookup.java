package com.butent.bee.egg.shared.data.column;

import com.butent.bee.egg.shared.data.IsTable;

public class DataTableColumnLookup implements ColumnLookup {
  private IsTable table;

  public DataTableColumnLookup(IsTable table) {
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
