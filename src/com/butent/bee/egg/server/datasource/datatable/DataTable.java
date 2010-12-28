package com.butent.bee.egg.server.datasource.datatable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import com.butent.bee.egg.server.datasource.base.TypeMismatchException;
import com.butent.bee.egg.server.datasource.base.Warning;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.shared.Assert;
import com.ibm.icu.util.ULocale;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTable {

  public static DataTable createSingleCellTable(String str) {
    DataTable dataTable = new DataTable();
    ColumnDescription colDesc = new ColumnDescription("SingleCellTable", ValueType.TEXT, "");
    dataTable.addColumn(colDesc);
    TableRow row = new TableRow();
    row.addCell(new TableCell(str));

    try {
      dataTable.addRow(row);
    } catch (TypeMismatchException e) {
      Assert.untouchable();
    }

    return dataTable;
  }
  private List<ColumnDescription> columns;

  private Map<String, Integer> columnIndexById;

  private List<TableRow> rows;

  private Map<String, String> customProperties = null;
  private List<Warning> warnings;

  private ULocale localeForUserMessages = null;

  public DataTable() {
    columns = Lists.newArrayList();
    columnIndexById = Maps.newHashMap();
    rows = Lists.newArrayList();
    warnings = Lists.newArrayList();
  }

  public void addColumn(ColumnDescription columnDescription) {
    String columnId = columnDescription.getId();
    if (columnIndexById.containsKey(columnId)) {
      throw new RuntimeException("Column Id [" + columnId + "] already in table description");
    }

    columnIndexById.put(columnId, columns.size());
    columns.add(columnDescription);
    for (TableRow row : rows) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(columnDescription.getType())));
    }
  }

  public void addColumns(Collection<ColumnDescription> columnsToAdd) {
    for (ColumnDescription column : columnsToAdd) {
      addColumn(column);
    }
  }

  public void addRow(TableRow row) throws TypeMismatchException {
    List<TableCell> cells = row.getCells();
    if (cells.size() > columns.size()) {
      throw new TypeMismatchException("Row has too many cells. Should be at most of size: " +
          columns.size());
    }
    for (int i = 0; i < cells.size(); i++) {
      if (cells.get(i).getType() != columns.get(i).getType()) {
        throw new TypeMismatchException("Cell type does not match column type, at index: " + i +
            ". Should be of type: " + columns.get(i).getType().toString());
      }
    }
    for (int i = cells.size(); i < columns.size(); i++) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(columns.get(i).getType())));
    }

    rows.add(row);
  }

  public void addRowFromValues(Object... values) throws TypeMismatchException {
    Iterator<ColumnDescription> columnIt = columns.listIterator();
    int i = 0;
    TableRow row = new TableRow();

    while (i < values.length && columnIt.hasNext()) {
      ColumnDescription colDesc = columnIt.next();
      row.addCell(colDesc.getType().createValue(values[i]));
      i++;
    }
    addRow(row);
  }

  public void addRows(Collection<TableRow> rowsToAdd) throws TypeMismatchException {
    for (TableRow row : rowsToAdd) {
      addRow(row);
    }
  }

  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  @Override
  public DataTable clone() {
    DataTable result = new DataTable();

    for (ColumnDescription column : columns) {
      result.addColumn(column.clone());
    }
    try {
      for (TableRow row : rows) {
        result.addRow(row.clone());
      }
    } catch (TypeMismatchException e) {
      Assert.untouchable();
    }
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    result.warnings = Lists.newArrayList();
    for (Warning warning : warnings) {
      result.warnings.add(warning);
    }
    result.setLocaleForUserMessages(localeForUserMessages);

    return result;
  }

  public boolean containsAllColumnIds(Collection<String> colIds) {
    for (String id : colIds) {
      if (!containsColumn(id)) {
        return false;
      }
    }
    return true;
  }

  public boolean containsColumn(String columnId) {
    return columnIndexById.containsKey(columnId);
  }

  public TableCell getCell(int rowIndex, int colIndex) {
    return getRow(rowIndex).getCell(colIndex);
  }

  public List<TableCell> getColumnCells(int columnIndex) {
    List<TableCell> colCells = Lists.newArrayListWithCapacity(getNumberOfRows());

    for (TableRow row : getRows()) {
      colCells.add(row.getCell(columnIndex));
    }
    return colCells;
  }

  public List<TableCell> getColumnCells(String columnId) {
    return getColumnCells(getColumnIndex(columnId));
  }

  public ColumnDescription getColumnDescription(int colIndex) {
    return columns.get(colIndex);
  }

  public ColumnDescription getColumnDescription(String columnId) {
    return columns.get(getColumnIndex(columnId));
  }

  public List<ColumnDescription> getColumnDescriptions() {
    return ImmutableList.copyOf(columns);
  }

  public List<TableCell> getColumnDistinctCellsSorted(int columnIndex,
      Comparator<TableCell> comparator) {
    Set<TableCell> colCells = Sets.newTreeSet(comparator);
    for (TableCell cell : getColumnCells(columnIndex)) {
      colCells.add(cell);
    }

    return Ordering.from(comparator).sortedCopy(colCells);
  }

  public int getColumnIndex(String columnId) {
    return columnIndexById.get(columnId);
  }

  public Map<String, String> getCustomProperties() {
    if (customProperties == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(customProperties);
  }

  public String getCustomProperty(String key) {
    if (customProperties == null) {
      return null;
    }
    if (key == null) {
      throw new RuntimeException("Null keys are not allowed.");
    }
    return customProperties.get(key);
  }

  public ULocale getLocaleForUserMessages() {
    return localeForUserMessages;
  }

  public int getNumberOfColumns() {
    return columns.size();
  }

  public int getNumberOfRows() {
    return rows.size();
  }

  public TableRow getRow(int rowIndex) {
    return rows.get(rowIndex);
  }

  public List<TableRow> getRows() {
    return rows;
  }

  public Value getValue(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getValue();
  }

  public List<Warning> getWarnings() {
    return ImmutableList.copyOf(warnings);
  }

  public void setCustomProperty(String propertyKey, String propertyValue) {
    if (customProperties == null) {
      customProperties = Maps.newHashMap();
    }
    if ((propertyKey == null) || (propertyValue == null)) {
      throw new RuntimeException("Null keys/values are not allowed.");
    }
    customProperties.put(propertyKey, propertyValue);
  }

  public void setLocaleForUserMessages(ULocale localeForUserMessges) {
    this.localeForUserMessages = localeForUserMessges;
  }

  public void setRows(Collection<TableRow> rows) throws TypeMismatchException {
    this.rows.clear();
    addRows(rows);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      TableRow tableRow = rows.get(rowIndex);
      for (int cellIndex = 0; cellIndex < tableRow.getCells().size(); cellIndex++) {
        TableCell tableCell = tableRow.getCells().get(cellIndex);
        sb.append(tableCell.toString());
        if (cellIndex < tableRow.getCells().size() - 1) {
          sb.append(",");
        }
      }
      if (rowIndex < rows.size() - 1) {
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  List<Value> getColumnDistinctValues(int columnIndex) {
    Set<Value> values = Sets.newTreeSet();
    for (TableRow row : getRows()) {
      values.add(row.getCell(columnIndex).getValue());
    }
    return Lists.newArrayList(values);
  }
  
  List<Value> getColumnDistinctValues(String columnId) {
    return getColumnDistinctValues(getColumnIndex(columnId));
  }
}
