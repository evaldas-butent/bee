package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Ordering;

import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.query.AggregationColumn;

import java.util.Comparator;
import java.util.List;

class GroupingComparators {

  private static class ColumnTitleDynamicComparator implements Comparator<ColumnTitle> {
    private Comparator<AggregationColumn> aggregationsComparator;

    public ColumnTitleDynamicComparator(List<AggregationColumn> aggregations) {
      aggregationsComparator = Ordering.explicit(aggregations);
    }

    public int compare(ColumnTitle col1, ColumnTitle col2) {
      int listCompare = VALUE_LIST_COMPARATOR.compare(col1.getValues(), col2.getValues());
      if (listCompare != 0) {
        return listCompare;
      }
      return aggregationsComparator.compare(col1.aggregation, col2.aggregation);
    }
  }

  public static final Comparator<List<Value>> VALUE_LIST_COMPARATOR =
      new Comparator<List<Value>>() {
        public int compare(List<Value> l1, List<Value> l2) {
          int i;
          int localCompare;
          for (i = 0; i < Math.min(l1.size(), l2.size()); i++) {
            localCompare = l1.get(i).compareTo(l2.get(i));
            if (localCompare != 0) {
              return localCompare;
            }
          }

          if (i < l1.size()) {
            localCompare = 1;
          } else if (i < l2.size()) {
            localCompare = -1;
          } else {
            localCompare = 0;
          }
          return localCompare;
        }
      };

  public static final Comparator<RowTitle> ROW_TITLE_COMPARATOR =
      new Comparator<RowTitle>() {
        public int compare(RowTitle col1, RowTitle col2) {
          return VALUE_LIST_COMPARATOR.compare(col1.values, col2.values);
        }
      };

  public static Comparator<ColumnTitle> getColumnTitleDynamicComparator(
      List<AggregationColumn> columnAggregations) {
    return new ColumnTitleDynamicComparator(columnAggregations);
  }

  private GroupingComparators() {
  }
}
