package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.egg.server.datasource.base.ReasonType;
import com.butent.bee.egg.server.datasource.base.TypeMismatchException;
import com.butent.bee.egg.server.datasource.base.Warning;
import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableCell;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.ValueFormatter;
import com.butent.bee.egg.server.datasource.query.AbstractColumn;
import com.butent.bee.egg.server.datasource.query.AggregationColumn;
import com.butent.bee.egg.server.datasource.query.ColumnLookup;
import com.butent.bee.egg.server.datasource.query.DataTableColumnLookup;
import com.butent.bee.egg.server.datasource.query.GenericColumnLookup;
import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.server.datasource.query.QueryFilter;
import com.butent.bee.egg.server.datasource.query.QueryFormat;
import com.butent.bee.egg.server.datasource.query.QueryGroup;
import com.butent.bee.egg.server.datasource.query.QueryLabels;
import com.butent.bee.egg.server.datasource.query.QueryPivot;
import com.butent.bee.egg.server.datasource.query.QuerySelection;
import com.butent.bee.egg.server.datasource.query.QuerySort;
import com.butent.bee.egg.server.datasource.query.ScalarFunctionColumn;
import com.butent.bee.egg.server.datasource.query.SimpleColumn;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.Value;

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

  public static DataTable executeQuery(Query query, DataTable table, ULocale locale) {
    ColumnIndices columnIndices = new ColumnIndices();
    List<ColumnDescription> columnsDescription = table.getColumnDescriptions();
    for (int i = 0; i < columnsDescription.size(); i++) {
      columnIndices.put(new SimpleColumn(columnsDescription.get(i).getId()), i);
    }

    TreeMap<List<Value>, ColumnLookup> columnLookups =
        new TreeMap<List<Value>, ColumnLookup>(GroupingComparators.VALUE_LIST_COMPARATOR);
    try {
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
    } catch (TypeMismatchException e) {
      Assert.untouchable();
    }
    return table;
  }

  private static DataTable createDataTable(
      List<String> groupByColumnIds, SortedSet<ColumnTitle> columnTitles,
      DataTable original, List<ScalarFunctionColumnTitle> scalarFunctionColumnTitles) {
    DataTable result = new DataTable();
    for (String groupById : groupByColumnIds) {
      result.addColumn(original.getColumnDescription(groupById));
    }
    for (ColumnTitle colTitle : columnTitles) {
      result.addColumn(colTitle.createColumnDescription(original));
    }

    for (ScalarFunctionColumnTitle scalarFunctionColumnTitle : scalarFunctionColumnTitles) {
      result.addColumn(scalarFunctionColumnTitle.createColumnDescription(original));
    }
    return result;
  }

  private static DataTable performFilter(DataTable table, Query query)
      throws TypeMismatchException {
    if (!query.hasFilter()) {
      return table;
    }

    List<TableRow> newRowList = Lists.newArrayList();
    QueryFilter filter = query.getFilter();
    for (TableRow inputRow : table.getRows()) {
      if (filter.isMatch(table, inputRow)) {
        newRowList.add(inputRow);
      }
    }
    table.setRows(newRowList);
    return table;
  }

  private static DataTable performFormatting(DataTable table, Query query,
      ColumnIndices columnIndices, ULocale locale) {
    if (!query.hasUserFormatOptions()) {
      return table;
    }

    QueryFormat queryFormat = query.getUserFormatOptions();
    List<ColumnDescription> columnDescriptions = table.getColumnDescriptions();
    Map<Integer, ValueFormatter> indexToFormatter = Maps.newHashMap();
    for (AbstractColumn col : queryFormat.getColumns()) {
      String pattern = queryFormat.getPattern(col);
      List<Integer> indices = columnIndices.getColumnIndices(col);
      boolean allSucceeded = true;
      for (int i : indices) {
        ColumnDescription colDesc = columnDescriptions.get(i);
        ValueFormatter f = ValueFormatter.createFromPattern(colDesc.getType(), pattern, locale);
        if (f == null) {
          allSucceeded = false;
        } else {
          indexToFormatter.put(i, f);
          table.getColumnDescription(i).setPattern(pattern);
        }
      }
      if (!allSucceeded) {
        Warning warning = new Warning(ReasonType.ILLEGAL_FORMATTING_PATTERNS,
            "Illegal formatting pattern: " + pattern + " requested on column: " + col.getId());
        table.addWarning(warning);
      }
    }

    for (TableRow row : table.getRows()) {
      for (int col : indexToFormatter.keySet()) {
        TableCell cell = row.getCell(col);
        Value value = cell.getValue();
        ValueFormatter formatter = indexToFormatter.get(col);
        String formattedValue = formatter.format(value);
        cell.setFormattedValue(formattedValue);
      }
    }
    return table;
  }

  private static DataTable performGroupingAndPivoting(DataTable table, Query query,
      ColumnIndices columnIndices, TreeMap<List<Value>, ColumnLookup> columnLookups)
      throws TypeMismatchException {
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

    List<ColumnDescription> newColumnDescriptions = Lists.newArrayList();
    newColumnDescriptions.addAll(table.getColumnDescriptions());

    for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
      newColumnDescriptions.add(new ColumnDescription(column.getId(),
          column.getValueType(table),
          ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, column)));
    }

    DataTable tempTable = new DataTable();
    tempTable.addColumns(newColumnDescriptions);

    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    for (TableRow sourceRow : table.getRows()) {
      TableRow newRow = new TableRow();
      for (TableCell sourceCell : sourceRow.getCells()) {
        newRow.addCell(sourceCell);
      }
      for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
        newRow.addCell(new TableCell(column.getValue(lookup, sourceRow)));
      }
      try {
        tempTable.addRow(newRow);
      } catch (TypeMismatchException e) {
        Assert.untouchable();
      }
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

    DataTable result = createDataTable(groupByIds, columnTitles, table, scalarFunctionColumnTitles);
    List<ColumnDescription> colDescs = result.getColumnDescriptions();

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
      TableRow curRow = new TableRow();
      for (Value v : rowTitle.values) {
        curRow.addCell(new TableCell(v));
      }
      Map<ColumnTitle, TableCell> rowData = metaTable.getRow(rowTitle);
      int i = 0;
      for (ColumnTitle colTitle : columnTitles) {
        TableCell cell = rowData.get(colTitle);
        curRow.addCell((cell != null) ? cell : new TableCell(
            Value.getNullValueFromValueType(colDescs.get(i + rowTitle.values.size()).getType())));
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

  private static DataTable performLabels(DataTable table, Query query,
      ColumnIndices columnIndices) {

    if (!query.hasLabels()) {
      return table;
    }

    QueryLabels labels = query.getLabels();

    List<ColumnDescription> columnDescriptions = table.getColumnDescriptions();

    for (AbstractColumn column : labels.getColumns()) {
      String label = labels.getLabel(column);
      List<Integer> indices = columnIndices.getColumnIndices(column);
      if (indices.size() == 1) {
        columnDescriptions.get(indices.get(0)).setLabel(label);
      } else {
        String columnId = column.getId();
        for (int i : indices) {
          ColumnDescription colDesc = columnDescriptions.get(i);
          String colDescId = colDesc.getId();
          String specificLabel =
              colDescId.substring(0, colDescId.length() - columnId.length()) + label;
          columnDescriptions.get(i).setLabel(specificLabel);
        }
      }
    }
    return table;
  }

  private static DataTable performPagination(DataTable table, Query query)
      throws TypeMismatchException {
    int rowOffset = query.getRowOffset();
    int rowLimit = query.getRowLimit();

    if (((rowLimit == -1) || (table.getRows().size() <= rowLimit)) && (rowOffset == 0)) {
      return table;
    }
    int numRows = table.getNumberOfRows();
    int fromIndex = Math.max(0, rowOffset);
    int toIndex = (rowLimit == -1) ? numRows : Math.min(numRows, rowOffset + rowLimit);

    List<TableRow> relevantRows = table.getRows().subList(fromIndex, toIndex);
    DataTable newTable = new DataTable();
    newTable.addColumns(table.getColumnDescriptions());
    newTable.addRows(relevantRows);

    if (toIndex < numRows) {
      Warning warning = new Warning(ReasonType.DATA_TRUNCATED, "Data has been truncated due to user"
          + "request (LIMIT in query)");
      newTable.addWarning(warning);
    }
    return newTable;
  }

  private static DataTable performSelection(DataTable table, Query query,
      AtomicReference<ColumnIndices> columnIndicesReference,
      Map<List<Value>, ColumnLookup> columnLookups) throws TypeMismatchException {
    if (!query.hasSelection()) {
      return table;
    }

    ColumnIndices columnIndices = columnIndicesReference.get();

    List<AbstractColumn> selectedColumns = query.getSelection().getColumns();
    List<Integer> selectedIndices = Lists.newArrayList();

    List<ColumnDescription> oldColumnDescriptions = table.getColumnDescriptions();
    List<ColumnDescription> newColumnDescriptions = Lists.newArrayList();
    ColumnIndices newColumnIndices = new ColumnIndices();
    int currIndex = 0;
    for (AbstractColumn col : selectedColumns) {
      List<Integer> colIndices = columnIndices.getColumnIndices(col);
      selectedIndices.addAll(colIndices);
      if (colIndices.size() == 0) {
        newColumnDescriptions.add(new ColumnDescription(col.getId(),
            col.getValueType(table),
            ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, col)));
        newColumnIndices.put(col, currIndex++);
      } else {
        for (int colIndex : colIndices) {
          newColumnDescriptions.add(oldColumnDescriptions.get(colIndex));
          newColumnIndices.put(col, currIndex++);
        }
      }
    }
    columnIndices = newColumnIndices;
    columnIndicesReference.set(columnIndices);

    DataTable result = new DataTable();
    result.addColumns(newColumnDescriptions);

    for (TableRow sourceRow : table.getRows()) {
      TableRow newRow = new TableRow();
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

  private static DataTable performSkipping(DataTable table, Query query)
      throws TypeMismatchException {
    int rowSkipping = query.getRowSkipping();
    
    if (rowSkipping <= 1) {
      return table;
    }

    int numRows = table.getNumberOfRows();
    List<TableRow> relevantRows = new ArrayList<TableRow>();
    for (int rowIndex = 0; rowIndex < numRows; rowIndex += rowSkipping) {
      relevantRows.add(table.getRows().get(rowIndex));
    }
    
    DataTable newTable = new DataTable();
    newTable.addColumns(table.getColumnDescriptions());
    newTable.addRows(relevantRows);
    
    return newTable;
  }

  private static DataTable performSort(DataTable table, Query query) {
    if (!query.hasSort()) {
      return table;
    }
    QuerySort sortBy = query.getSort();
    DataTableColumnLookup columnLookup = new DataTableColumnLookup(table);
    TableRowComparator comparator = new TableRowComparator(sortBy, columnLookup);
    Collections.sort(table.getRows(), comparator);
    return table;
  }

  private static boolean queryHasAggregation(Query query) {
    return (query.hasSelection()
        && !query.getSelection().getAggregationColumns().isEmpty());
  }

  private QueryEngine() {
  }
}
