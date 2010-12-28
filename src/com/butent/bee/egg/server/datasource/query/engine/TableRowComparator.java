package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.query.AbstractColumn;
import com.butent.bee.egg.server.datasource.query.ColumnLookup;
import com.butent.bee.egg.server.datasource.query.ColumnSort;
import com.butent.bee.egg.server.datasource.query.QuerySort;
import com.butent.bee.egg.server.datasource.query.SortOrder;
import com.ibm.icu.util.ULocale;

import java.util.Comparator;
import java.util.List;

class TableRowComparator implements Comparator<TableRow> {
  private AbstractColumn[] sortColumns;
  private SortOrder[] sortColumnOrder;

  private Comparator<Value> valueComparator;

  private ColumnLookup columnLookup;

  public TableRowComparator(QuerySort sort, ULocale locale, ColumnLookup lookup) {
    valueComparator = Value.getLocalizedComparator(locale);
    columnLookup = lookup;
    List<ColumnSort> columns = sort.getSortColumns();
    sortColumns = new AbstractColumn[columns.size()];
    sortColumnOrder = new SortOrder[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      ColumnSort columnSort = columns.get(i);
      sortColumns[i] = columnSort.getColumn();
      sortColumnOrder[i] = columnSort.getOrder();
    }
  }

  public int compare(TableRow r1, TableRow r2) {
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
