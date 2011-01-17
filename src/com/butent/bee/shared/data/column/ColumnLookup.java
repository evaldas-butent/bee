package com.butent.bee.shared.data.column;

public interface ColumnLookup {
  boolean containsColumn(AbstractColumn column);
  int getColumnIndex(AbstractColumn column);
}
