package com.butent.bee.egg.shared.data.column;

public interface ColumnLookup {
  boolean containsColumn(AbstractColumn column);
  int getColumnIndex(AbstractColumn column);
}
