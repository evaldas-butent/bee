package com.butent.bee.egg.server.datasource.query;

public interface ColumnLookup {
  boolean containsColumn(AbstractColumn column);
  int getColumnIndex(AbstractColumn column);
}
