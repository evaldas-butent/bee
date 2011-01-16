package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.server.datasource.query.QuerySort;
import com.butent.bee.egg.shared.data.SortInfo;
import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.SortOrder;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.ColumnLookup;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.Comparator;
import java.util.List;

class TableRowComparator implements Comparator<IsRow> {
  private AbstractColumn[] sortColumns;
  private SortOrder[] sortColumnOrder;

  private Comparator<Value> valueComparator;

  private ColumnLookup columnLookup;

  public TableRowComparator(QuerySort sort, ColumnLookup lookup) {
    valueComparator = Value.getComparator();
    columnLookup = lookup;
    List<SortInfo> columns = sort.getSortColumns();
    sortColumns = new AbstractColumn[columns.size()];
    sortColumnOrder = new SortOrder[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      SortInfo columnSort = columns.get(i);
      sortColumns[i] = columnSort.getColumn();
      sortColumnOrder[i] = columnSort.getOrder();
    }
  }

  public int compare(IsRow r1, IsRow r2) {
    for (int i = 0; i < sortColumns.length; i++) {
      AbstractColumn col = sortColumns[i];
      int cc = valueComparator.compare(col.getValue(columnLookup, r1),
          col.getValue(columnLookup, r2));
      if (cc != 0) {
        return (sortColumnOrder[i] == SortOrder.ASCENDING) ? cc : -cc;
      }
    }
    return 0;
  }
}
