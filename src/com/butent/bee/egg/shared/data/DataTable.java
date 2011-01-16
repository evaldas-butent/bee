package com.butent.bee.egg.shared.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.filter.RowFilter;
import com.butent.bee.egg.shared.data.value.BooleanValue;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.TextValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTable implements IsTable {

  private List<IsColumn> columns;
  private Map<String, Integer> columnIndexById;

  private List<IsRow> rows;

  private CustomProperties properties = null;
  private List<DataWarning> warnings;

  public DataTable() {
    columns = Lists.newArrayList();
    columnIndexById = Maps.newHashMap();
    rows = Lists.newArrayList();
    warnings = Lists.newArrayList();
  }

  public int addColumn(IsColumn column) {
    String columnId = column.getId();
    Assert.notEmpty(columnId);
    Assert.isFalse(columnIndexById.containsKey(columnId),
        "Column Id [" + columnId + "] already in table description");

    columnIndexById.put(columnId, columns.size());
    columns.add(column);
    for (IsRow row : rows) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(column.getType())));
    }
    return columns.size() - 1;
  }

  public int addColumn(ValueType type) {
    return addColumn(type, BeeUtils.concat(1, "Column", columns.size()));
  }

  public int addColumn(ValueType type, String label) {
    return addColumn(type, label, "col" + BeeUtils.toLeadingZeroes(columns.size(), 3));
  }

  public int addColumn(ValueType type, String label, String id) {
    return addColumn(new TableColumn(id, type, label));
  }

  public int addColumns(Collection<IsColumn> columnsToAdd) {
    Assert.hasLength(columnsToAdd);
    for (IsColumn column : columnsToAdd) {
      addColumn(column);
    }
    return columns.size() - 1;
  }
  
  public int addRow() {
    IsRow row = new TableRow();
    for (int i = 0; i < columns.size(); i++) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(columns.get(i).getType())));
    }
    rows.add(row);
    return rows.size() - 1;
  }

  public int addRow(IsRow row) throws TypeMismatchException {
    List<IsCell> cells = row.getCells();
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
    return rows.size() - 1;
  }

  public int addRow(Object... cells) throws TypeMismatchException {
    Assert.parameterCount(cells.length, 1, columns.size());
    IsRow row = new TableRow();

    for (int i = 0; i < columns.size(); i++) {
      ValueType type = columns.get(i).getType();
      
      if (i >= cells.length) {
        row.addCell(new TableCell(Value.getNullValueFromValueType(type)));
      } else if (cells[i] instanceof IsCell) {
        row.addCell((IsCell) cells[i]);
      } else if (cells[i] instanceof Value) {
        if (((Value) cells[i]).getType().equals(type)) {
          row.addCell((Value) cells[i]);
        } else {
          row.addCell(type.createValue(((Value) cells[i]).getObjectValue()));
        }
      } else {
        row.addCell(type.createValue(cells[i]));
      }
    }
    return addRow(row);
  }
  
  public int addRows(Collection<IsRow> rowsToAdd) throws TypeMismatchException {
    Assert.hasLength(rowsToAdd);
    for (IsRow row : rowsToAdd) {
      addRow(row);
    }
    return rows.size() - 1;
  }

  public int addRows(int rowCount) {
    Assert.isPositive(rowCount);
    for (int i = 0; i < rowCount; i++) {
      addRow();
    }
    return rows.size() - 1;
  }

  public void addWarning(DataWarning warning) {
    warnings.add(warning);
  }

  public void clearCell(int rowIndex, int colIndex) {
    // TODO Auto-generated method stub
  }

  public void clearValue(int rowIndex, int colIndex) {
    IsCell cell = getCell(rowIndex, colIndex);
    cell.clearValue();
    cell.clearFormattedValue();
    cell.clearProperties();
  }

  @Override
  public IsTable clone() {
    DataTable result = new DataTable();

    for (IsColumn column : columns) {
      result.addColumn(column.clone());
    }
    try {
      for (IsRow row : rows) {
        result.addRow(row.clone());
      }
    } catch (TypeMismatchException ex) {
      Assert.untouchable();
    }
    if (properties != null) {
      result.properties = properties.clone();
    }
    result.warnings = Lists.newArrayList();
    for (DataWarning warning : warnings) {
      result.warnings.add(warning);
    }

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

  public void fromJson(String data, double version) {
    // TODO Auto-generated method stub
  }

  public IsCell getCell(int rowIndex, int colIndex) {
    assertCellIndex(rowIndex, colIndex);
    return getRow(rowIndex).getCell(colIndex);
  }

  public IsColumn getColumn(int colIndex) {
    assertColumnIndex(colIndex);
    return columns.get(colIndex);
  }

  public IsColumn getColumn(String columnId) {
    return getColumn(getColumnIndex(columnId));
  }

  public List<IsCell> getColumnCells(int colIndex) {
    assertColumnIndex(colIndex);
    List<IsCell> colCells = Lists.newArrayListWithCapacity(getNumberOfRows());

    for (IsRow row : getRows()) {
      colCells.add(row.getCell(colIndex));
    }
    return colCells;
  }

  public List<IsCell> getColumnCells(String columnId) {
    return getColumnCells(getColumnIndex(columnId));
  }

  public String getColumnId(int colIndex) {
    return getColumn(colIndex).getId();
  }

  public int getColumnIndex(String columnId) {
    return columnIndexById.get(columnId);
  }

  public String getColumnLabel(int colIndex) {
    return getColumn(colIndex).getLabel();
  }

  public String getColumnPattern(int colIndex) {
    return getColumn(colIndex).getPattern();
  }

  public CustomProperties getColumnProperties(int colIndex) {
    return getColumn(colIndex).getProperties();
  }

  public Object getColumnProperty(int colIndex, String name) {
    return getColumn(colIndex).getProperty(name);
  }

  public Range getColumnRange(int colIndex) {
    assertColumnIndex(colIndex);
    // TODO Auto-generated method stub
    return null;
  }

  public List<IsColumn> getColumns() {
    return ImmutableList.copyOf(columns);
  }

  public ValueType getColumnType(int colIndex) {
    return getColumn(colIndex).getType();
  }
  
  public List<Value> getColumnValues(int colIndex) {
    assertColumnIndex(colIndex);
    List<Value> values = Lists.newArrayListWithCapacity(getNumberOfRows());

    for (IsRow row : getRows()) {
      values.add(row.getCell(colIndex).getValue());
    }
    return values;
  }

  public List<Value> getDistinctValues(int colIndex) {
    assertColumnIndex(colIndex);
    Set<Value> values = Sets.newTreeSet();
    for (IsRow row : getRows()) {
      values.add(row.getCell(colIndex).getValue());
    }
    return Lists.newArrayList(values);
  }

  public List<Value> getDistinctValues(String columnId) {
    return getDistinctValues(getColumnIndex(columnId));
  }

  public int[] getFilteredRows(RowFilter... filters) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFormattedValue(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getFormattedValue();
  }

  public int getNumberOfColumns() {
    return columns.size();
  }

  public int getNumberOfRows() {
    return rows.size();
  }

  public CustomProperties getProperties(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getProperties();
  }

  public Object getProperty(int rowIndex, int colIndex, String name) {
    return getCell(rowIndex, colIndex).getProperty(name);
  }

  public IsRow getRow(int rowIndex) {
    assertRowIndex(rowIndex);
    return rows.get(rowIndex);
  }

  public CustomProperties getRowProperties(int rowIndex) {
    return getRow(rowIndex).getProperties();
  }

  public Object getRowProperty(int rowIndex, String name) {
    return getRow(rowIndex).getProperty(name);
  }

  public List<IsRow> getRows() {
    return rows;
  }
  
  public int[] getSortedRows(int... colIndexes) {
    Assert.parameterCount(colIndexes.length, 1);
    // TODO Auto-generated method stub
    return null;
  }

  public int[] getSortedRows(SortInfo... sortColumns) {
    Assert.parameterCount(sortColumns.length, 1);
    // TODO Auto-generated method stub
    return null;
  }
  
  public CustomProperties getTableProperties() {
    return properties;
  }

  public Object getTableProperty(String key) {
    Assert.notEmpty(key);
    if (properties == null) {
      return null;
    }
    return properties.get(key);
  }

  public Value getValue(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getValue();
  }

  public List<DataWarning> getWarnings() {
    return ImmutableList.copyOf(warnings);
  }

  public void insertColumn(int colIndex, IsColumn column) {
    // TODO Auto-generated method stub
  }

  public void insertColumn(int colIndex, String type) {
    // TODO Auto-generated method stub
  }

  public void insertColumn(int colIndex, String type, String label) {
    // TODO Auto-generated method stub
  }

  public void insertColumn(int colIndex, String type, String label, String id) {
    // TODO Auto-generated method stub
  }

  public void insertRows(int rowIndex, Collection<IsRow> rowsToAdd) throws TypeMismatchException {
    // TODO Auto-generated method stub
  }

  public void insertRows(int rowIndex, int rowCount) {
    // TODO Auto-generated method stub
  }

  public void removeColumn(int colIndex) {
    // TODO Auto-generated method stub
  }

  public void removeColumns(int colIndex, int colCount) {
    // TODO Auto-generated method stub
  }

  public void removeRow(int rowIndex) {
    // TODO Auto-generated method stub
  }

  public void removeRows(int rowIndex, int rowCount) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, boolean value) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, boolean value, String formattedValue) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, boolean value, String formattedValue,
      CustomProperties properties) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, double value) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, double value, String formattedValue) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, double value, String formattedValue,
      CustomProperties properties) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, IsCell cell) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, String value) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, String value, String formattedValue) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, String value, String formattedValue,
      CustomProperties properties) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, Value value) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, Value value, String formattedValue) {
    // TODO Auto-generated method stub
  }

  public void setCell(int rowIndex, int colIndex, Value value, String formattedValue,
      CustomProperties properties) {
    // TODO Auto-generated method stub
  }

  public void setColumnLabel(int colIndex, String label) {
    getColumn(colIndex).setLabel(label);
  }

  public void setColumnProperties(int colIndex, CustomProperties properties) {
    getColumn(colIndex).setProperties(properties);
  }

  public void setColumnProperty(int colIndex, String name, Object value) {
    getColumn(colIndex).setProperty(name, value);
  }

  public void setFormattedValue(int rowIndex, int colIndex, String formattedValue) {
    getCell(rowIndex, colIndex).setFormattedValue(formattedValue);
  }

  public void setProperties(int rowIndex, int colIndex, CustomProperties properties) {
    getCell(rowIndex, colIndex).setProperties(properties);
  }

  public void setProperty(int rowIndex, int colIndex, String name, Object value) {
    getCell(rowIndex, colIndex).setProperty(name, value);
  }

  public void setRowProperties(int rowIndex, CustomProperties properties) {
    getRow(rowIndex).setProperties(properties);
  }

  public void setRowProperty(int rowIndex, String name, Object value) {
    getRow(rowIndex).setProperty(name, value);
  }

  public void setRows(Collection<IsRow> rows) throws TypeMismatchException {
    this.rows.clear();
    addRows(rows);
  }

  public void setTableProperties(CustomProperties properties) {
    this.properties = properties;
  }

  public void setTableProperty(String propertyKey, Object propertyValue) {
    Assert.notEmpty(propertyKey);
    Assert.notNull(propertyValue);
    if (properties == null) {
      properties = CustomProperties.create();
    }
    properties.put(propertyKey, propertyValue);
  }

  public void setValue(int rowIndex, int colIndex, boolean value) {
    setValue(rowIndex, colIndex, BooleanValue.getInstance(value));
  }

  public void setValue(int rowIndex, int colIndex, double value) {
    setValue(rowIndex, colIndex, new NumberValue(value));
  }

  public void setValue(int rowIndex, int colIndex, String value) {
    setValue(rowIndex, colIndex, new TextValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Value value) {
    IsCell cell = getCell(rowIndex, colIndex);
    cell.setValue(value);
    cell.clearFormattedValue();
    cell.clearProperties();
  }

  public void sort(int... colIndexes) {
    // TODO Auto-generated method stub
  }

  public void sort(SortInfo... sortColumns) {
    // TODO Auto-generated method stub
  }

  public String toJson() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      IsRow tableRow = rows.get(rowIndex);
      for (int cellIndex = 0; cellIndex < tableRow.getCells().size(); cellIndex++) {
        IsCell tableCell = tableRow.getCells().get(cellIndex);
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

  private void assertCellIndex(int rowIndex, int colIndex) {
    assertRowIndex(rowIndex);
    assertColumnIndex(colIndex);
  }

  private void assertColumnIndex(int colIndex) {
    Assert.isIndex(columns, colIndex);
  }

  private void assertRowIndex(int rowIndex) {
    Assert.isIndex(rows, rowIndex);
  }
  
}
