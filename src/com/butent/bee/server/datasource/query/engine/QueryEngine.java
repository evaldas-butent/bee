package com.butent.bee.server.datasource.query.engine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.datasource.query.GenericColumnLookup;
import com.butent.bee.server.datasource.query.Query;
import com.butent.bee.server.datasource.query.QueryFormat;
import com.butent.bee.server.datasource.query.QueryGroup;
import com.butent.bee.server.datasource.query.QueryLabels;
import com.butent.bee.server.datasource.query.QueryPivot;
import com.butent.bee.server.datasource.query.QuerySelection;
import com.butent.bee.server.datasource.util.ValueFormatter;
import com.butent.bee.shared.data.DataWarning;
import com.butent.bee.shared.data.IsCell;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.Reasons;
import com.butent.bee.shared.data.TableCell;
import com.butent.bee.shared.data.TableRow;
import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.ColumnLookup;
import com.butent.bee.shared.data.column.DataTableColumnLookup;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.shared.data.column.SimpleColumn;
import com.butent.bee.shared.data.filter.RowFilter;
import com.butent.bee.shared.data.sort.SortQuery;
import com.butent.bee.shared.data.sort.TableRowComparator;
import com.butent.bee.shared.data.value.Value;

import com.ibm.icu.util.ULocale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public final class QueryEngine {

  public static <R extends IsRow, C extends IsColumn> IsTable<R, C> executeQuery(Query query,
      IsTable<R, C> table, ULocale locale) {
    ColumnIndices columnIndices = new ColumnIndices();
    List<C> columns = table.getColumns();
    for (int i = 0; i < columns.size(); i++) {
      columnIndices.put(new SimpleColumn(columns.get(i).getId()), i);
    }

    TreeMap<List<Value>, ColumnLookup> columnLookups =
        new TreeMap<List<Value>, ColumnLookup>(GroupingComparators.VALUE_LIST_COMPARATOR);

    table = performFilter(table, query);
    table = performGroupingAndPivoting(table, query, columnIndices, columnLookups);
    table = performSort(table, query);
    table = performSkipping(table, query);
    table = performPagination(table, query);

    AtomicReference<ColumnIndices> columnIndicesReference =
        new AtomicReference<ColumnIndices>(columnIndices);
    table = performSelection(table, query, columnIndicesReference, columnLookups);
    columnIndices = columnIndicesReference.get();

    table = performLabels(table, query, columnIndices);
    table = performFormatting(table, query, columnIndices, locale);

    return table;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> createDataTable(
      List<String> groupByColumnIds, SortedSet<ColumnTitle> columnTitles,
      IsTable<R, C> original, List<ScalarFunctionColumnTitle> scalarFunctionColumnTitles) {
    IsTable<R, C> result = original.create();
    for (String groupById : groupByColumnIds) {
      result.addColumn(original.getColumn(groupById));
    }
    for (ColumnTitle colTitle : columnTitles) {
      result.addColumn(colTitle.createColumnDescription(original));
    }

    for (ScalarFunctionColumnTitle scalarFunctionColumnTitle : scalarFunctionColumnTitles) {
      result.addColumn(scalarFunctionColumnTitle.createColumnDescription(original));
    }
    return result;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performFilter(
      IsTable<R, C> table, Query query) {
    if (!query.hasFilter()) {
      return table;
    }

    List<R> newRowList = Lists.newArrayList();
    RowFilter filter = query.getFilter();
    for (R inputRow : table.getRows()) {
      if (filter.isMatch(table, inputRow)) {
        newRowList.add(inputRow);
      }
    }
    table.setRows(newRowList);
    return table;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performFormatting(
      IsTable<R, C> table, Query query, ColumnIndices columnIndices, ULocale locale) {
    if (!query.hasUserFormatOptions()) {
      return table;
    }

    QueryFormat queryFormat = query.getUserFormatOptions();
    List<C> columns = table.getColumns();
    Map<Integer, ValueFormatter> indexToFormatter = Maps.newHashMap();
    for (AbstractColumn col : queryFormat.getColumns()) {
      String pattern = queryFormat.getPattern(col);
      List<Integer> indices = columnIndices.getColumnIndices(col);
      boolean allSucceeded = true;
      for (int i : indices) {
        IsColumn c = columns.get(i);
        ValueFormatter f = ValueFormatter.createFromPattern(c.getType(), pattern, locale);
        if (f == null) {
          allSucceeded = false;
        } else {
          indexToFormatter.put(i, f);
          table.getColumn(i).setPattern(pattern);
        }
      }
      if (!allSucceeded) {
        DataWarning warning = new DataWarning(Reasons.ILLEGAL_FORMATTING_PATTERNS,
            "Illegal formatting pattern: " + pattern + " requested on column: " + col.getId());
        table.addWarning(warning);
      }
    }

    for (IsRow row : table.getRows()) {
      for (int col : indexToFormatter.keySet()) {
        IsCell cell = row.getCell(col);
        Value value = cell.getValue();
        ValueFormatter formatter = indexToFormatter.get(col);
        String formattedValue = formatter.format(value);
        cell.setFormattedValue(formattedValue);
      }
    }
    return table;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performGroupingAndPivoting(
      IsTable<R, C> table, Query query, ColumnIndices columnIndices, TreeMap<List<Value>,
      ColumnLookup> columnLookups) {
    if (!queryHasAggregation(query) || (table.getNumberOfRows() == 0)) {
      return table;
    }
    QueryGroup group = query.getGroup();
    QueryPivot pivot = query.getPivot();
    QuerySelection selection = query.getSelection();

    List<String> groupByIds = Lists.newArrayList();
    if (group != null) {
      groupByIds = group.getColumnIds();
    }

    List<String> pivotByIds = Lists.newArrayList();
    if (pivot != null) {
      pivotByIds = pivot.getColumnIds();
    }

    List<String> groupAndPivotIds = Lists.newArrayList(groupByIds);
    groupAndPivotIds.addAll(pivotByIds);

    List<AggregationColumn> tmpColumnAggregations = selection.getAggregationColumns();
    List<ScalarFunctionColumn> selectedScalarFunctionColumns = selection.getScalarFunctionColumns();

    List<AggregationColumn> columnAggregations =
        Lists.newArrayListWithExpectedSize(tmpColumnAggregations.size());
    for (AggregationColumn aggCol : tmpColumnAggregations) {
      if (!columnAggregations.contains(aggCol)) {
        columnAggregations.add(aggCol);
      }
    }

    List<String> aggregationIds = Lists.newArrayList();
    for (AggregationColumn col : columnAggregations) {
      aggregationIds.add(col.getAggregatedColumn().getId());
    }

    List<ScalarFunctionColumn> groupAndPivotScalarFunctionColumns = Lists.newArrayList();
    if (group != null) {
      groupAndPivotScalarFunctionColumns.addAll(group.getScalarFunctionColumns());
    }
    if (pivot != null) {
      groupAndPivotScalarFunctionColumns.addAll(pivot.getScalarFunctionColumns());
    }

    List<C> newColumns = Lists.newArrayList();
    newColumns.addAll(table.getColumns());

    for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
      newColumns.add(table.createColumn(column.getValueType(table),
          ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, column), column.getId()));
    }

    IsTable<R, C> tempTable = table.create();
    tempTable.addColumns(newColumns);

    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    for (R sourceRow : table.getRows()) {
      R newRow = table.createRow(tempTable.getNumberOfRows() + 1);
      for (IsCell sourceCell : sourceRow.getCells()) {
        newRow.addCell(sourceCell);
      }
      for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
        newRow.addCell(new TableCell(column.getValue(lookup, sourceRow)));
      }
      tempTable.addRow(newRow);
    }
    table = tempTable;

    TableAggregator aggregator = new TableAggregator(groupAndPivotIds,
        Sets.newHashSet(aggregationIds), table);
    Set<AggregationPath> paths = aggregator.getPathsToLeaves();

    SortedSet<RowTitle> rowTitles =
        Sets.newTreeSet(GroupingComparators.ROW_TITLE_COMPARATOR);
    SortedSet<ColumnTitle> columnTitles = Sets.newTreeSet(
        GroupingComparators.getColumnTitleDynamicComparator(columnAggregations));

    TreeSet<List<Value>> pivotValuesSet =
        Sets.newTreeSet(GroupingComparators.VALUE_LIST_COMPARATOR);
    MetaTable metaTable = new MetaTable();
    for (AggregationColumn columnAggregation : columnAggregations) {
      for (AggregationPath path : paths) {
        List<Value> originalValues = path.getValues();

        List<Value> rowValues = originalValues.subList(0, groupByIds.size());
        RowTitle rowTitle = new RowTitle(rowValues);
        rowTitles.add(rowTitle);

        List<Value> columnValues = originalValues.subList(groupByIds.size(), originalValues.size());
        pivotValuesSet.add(columnValues);

        ColumnTitle columnTitle = new ColumnTitle(columnValues,
            columnAggregation, (columnAggregations.size() > 1));
        columnTitles.add(columnTitle);
        metaTable.put(rowTitle, columnTitle, new TableCell(aggregator.getAggregationValue(path,
            columnAggregation.getAggregatedColumn().getId(),
            columnAggregation.getAggregationType())));
      }
    }

    List<ScalarFunctionColumnTitle> scalarFunctionColumnTitles = Lists.newArrayList();
    for (ScalarFunctionColumn scalarFunctionColumn : selectedScalarFunctionColumns) {
      if (scalarFunctionColumn.getAllAggregationColumns().size() != 0) {
        for (List<Value> columnValues : pivotValuesSet) {
          scalarFunctionColumnTitles.add(new ScalarFunctionColumnTitle(columnValues,
              scalarFunctionColumn));
        }
      }
    }

    IsTable<R, C> result = createDataTable(groupByIds, columnTitles, table,
        scalarFunctionColumnTitles);
    List<C> cols = result.getColumns();

    columnIndices.clear();
    int columnIndex = 0;
    if (group != null) {
      List<Value> empytListOfValues = Lists.newArrayList();
      columnLookups.put(empytListOfValues, new GenericColumnLookup());
      for (AbstractColumn column : group.getColumns()) {
        columnIndices.put(column, columnIndex);
        if (!(column instanceof ScalarFunctionColumn)) {
          ((GenericColumnLookup) columnLookups.get(empytListOfValues)).put(column, columnIndex);
          for (List<Value> columnValues : pivotValuesSet) {
            if (!columnLookups.containsKey(columnValues)) {
              columnLookups.put(columnValues, new GenericColumnLookup());
            }
            ((GenericColumnLookup) columnLookups.get(columnValues)).put(column, columnIndex);
          }
        }
        columnIndex++;
      }
    }

    for (ColumnTitle title : columnTitles) {
      columnIndices.put(title.aggregation, columnIndex);
      List<Value> values = title.getValues();
      if (!columnLookups.containsKey(values)) {
        columnLookups.put(values, new GenericColumnLookup());
      }
      ((GenericColumnLookup) columnLookups.get(values)).put(title.aggregation, columnIndex);
      columnIndex++;
    }

    for (RowTitle rowTitle : rowTitles) {
      IsRow curRow = new TableRow(result.getNumberOfRows() + 1);
      for (Value v : rowTitle.values) {
        curRow.addCell(new TableCell(v));
      }
      Map<ColumnTitle, TableCell> rowData = metaTable.getRow(rowTitle);
      int i = 0;
      for (ColumnTitle colTitle : columnTitles) {
        TableCell cell = rowData.get(colTitle);
        curRow.addCell((cell != null) ? cell : new TableCell(
            Value.getNullValueFromValueType(cols.get(i + rowTitle.values.size()).getType())));
        i++;
      }
      for (ScalarFunctionColumnTitle columnTitle : scalarFunctionColumnTitles) {
        curRow.addCell(new TableCell(columnTitle.scalarFunctionColumn
            .getValue(columnLookups.get(columnTitle.getValues()), curRow)));
      }
      result.addRow(curRow);
    }

    for (ScalarFunctionColumnTitle scalarFunctionColumnTitle : scalarFunctionColumnTitles) {
      columnIndices.put(scalarFunctionColumnTitle.scalarFunctionColumn, columnIndex);
      List<Value> values = scalarFunctionColumnTitle.getValues();
      if (!columnLookups.containsKey(values)) {
        columnLookups.put(values, new GenericColumnLookup());
      }
      ((GenericColumnLookup) columnLookups.get(values)).put(
          scalarFunctionColumnTitle.scalarFunctionColumn, columnIndex);
      columnIndex++;
    }

    return result;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performLabels(
      IsTable<R, C> table, Query query, ColumnIndices columnIndices) {
    if (!query.hasLabels()) {
      return table;
    }

    QueryLabels labels = query.getLabels();

    List<C> columns = table.getColumns();

    for (AbstractColumn column : labels.getColumns()) {
      String label = labels.getLabel(column);
      List<Integer> indices = columnIndices.getColumnIndices(column);
      if (indices.size() == 1) {
        columns.get(indices.get(0)).setLabel(label);
      } else {
        String columnId = column.getId();
        for (int i : indices) {
          IsColumn col = columns.get(i);
          String colDescId = col.getId();
          String specificLabel =
              colDescId.substring(0, colDescId.length() - columnId.length()) + label;
          columns.get(i).setLabel(specificLabel);
        }
      }
    }
    return table;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performPagination(
      IsTable<R, C> table, Query query) {
    int rowOffset = query.getRowOffset();
    int rowLimit = query.getRowLimit();

    if (((rowLimit == -1) || (table.getRows().length() <= rowLimit)) && (rowOffset == 0)) {
      return table;
    }
    int numRows = table.getNumberOfRows();
    int fromIndex = Math.max(0, rowOffset);
    int toIndex = (rowLimit == -1) ? numRows : Math.min(numRows, rowOffset + rowLimit);

    List<R> relevantRows = table.getRows().getList().subList(fromIndex, toIndex);
    IsTable<R, C> newTable = table.create();
    newTable.addColumns(table.getColumns());
    newTable.addRows(relevantRows);

    if (toIndex < numRows) {
      DataWarning warning = new DataWarning(Reasons.DATA_TRUNCATED,
          "Data has been truncated due to user request (LIMIT in query)");
      newTable.addWarning(warning);
    }
    return newTable;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performSelection(
      IsTable<R, C> table, Query query, AtomicReference<ColumnIndices> columnIndicesReference,
      Map<List<Value>, ColumnLookup> columnLookups) {
    if (!query.hasSelection()) {
      return table;
    }

    ColumnIndices columnIndices = columnIndicesReference.get();

    List<AbstractColumn> selectedColumns = query.getSelection().getColumns();
    List<Integer> selectedIndices = Lists.newArrayList();

    List<C> oldColumns = table.getColumns();
    List<C> newColumns = Lists.newArrayList();
    ColumnIndices newColumnIndices = new ColumnIndices();
    int currIndex = 0;
    for (AbstractColumn col : selectedColumns) {
      List<Integer> colIndices = columnIndices.getColumnIndices(col);
      selectedIndices.addAll(colIndices);
      if (colIndices.size() == 0) {
        newColumns.add(table.createColumn(col.getValueType(table), 
            ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, col), col.getId()));
        newColumnIndices.put(col, currIndex++);
      } else {
        for (int colIndex : colIndices) {
          newColumns.add(oldColumns.get(colIndex));
          newColumnIndices.put(col, currIndex++);
        }
      }
    }
    columnIndices = newColumnIndices;
    columnIndicesReference.set(columnIndices);

    IsTable<R, C> result = table.create();
    result.addColumns(newColumns);

    for (IsRow sourceRow : table.getRows()) {
      IsRow newRow = new TableRow(result.getNumberOfRows() + 1);
      for (AbstractColumn col : selectedColumns) {
        boolean wasFound = false;
        Set<List<Value>> pivotValuesSet = columnLookups.keySet();
        for (List<Value> values : pivotValuesSet) {
          if (columnLookups.get(values).containsColumn(col)
              && ((col.getAllAggregationColumns().size() != 0) || !wasFound)) {
            wasFound = true;
            newRow.addCell(sourceRow.getCell(columnLookups.get(values).getColumnIndex(col)));
          }
        }
        if (!wasFound) {
          DataTableColumnLookup lookup = new DataTableColumnLookup(table);
          newRow.addCell(col.getCell(lookup, sourceRow));
        }
      }
      result.addRow(newRow);
    }
    return result;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performSkipping(
      IsTable<R, C> table, Query query) {
    int rowSkipping = query.getRowSkipping();

    if (rowSkipping <= 1) {
      return table;
    }

    int numRows = table.getNumberOfRows();
    List<R> relevantRows = new ArrayList<R>();
    for (int rowIndex = 0; rowIndex < numRows; rowIndex += rowSkipping) {
      relevantRows.add(table.getRows().get(rowIndex));
    }

    IsTable<R, C> newTable = table.create();
    newTable.addColumns(table.getColumns());
    newTable.addRows(relevantRows);

    return newTable;
  }

  private static <R extends IsRow, C extends IsColumn> IsTable<R, C> performSort(
      IsTable<R, C> table, Query query) {
    if (!query.hasSort()) {
      return table;
    }
    SortQuery sortBy = query.getSort();
    DataTableColumnLookup columnLookup = new DataTableColumnLookup(table);
    TableRowComparator comparator = new TableRowComparator(sortBy, columnLookup);
    Collections.sort(table.getRows().getList(), comparator);
    return table;
  }

  private static boolean queryHasAggregation(Query query) {
    return (query.hasSelection() && !query.getSelection().getAggregationColumns().isEmpty());
  }

  private QueryEngine() {
  }
}
