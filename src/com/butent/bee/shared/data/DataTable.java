package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.RowFilter;
import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTable implements IsTable {

  private class IndexOrdering implements Comparator<Integer> {
    private RowOrdering rowOrdering;

    private IndexOrdering(RowOrdering rowOrdering) {
      this.rowOrdering = rowOrdering;
    }

    public int compare(Integer idx1, Integer idx2) {
      return rowOrdering.compare(getRow(idx1), getRow(idx2));
    }
  }

  private class RowOrdering implements Comparator<IsRow> {
    private List<SortInfo> orderBy = Lists.newArrayList();
    private Comparator<Value> comparator = Value.getComparator();

    private RowOrdering(SortInfo... sortInfo) {
      Assert.parameterCount(sortInfo.length, 1);

      for (int i = 0; i < sortInfo.length; i++) {
        int index = sortInfo[i].getIndex();
        assertColumnIndex(index);
        if (!containsIndex(index)) {
          orderBy.add(sortInfo[i]);
        }
      }
    }

    public int compare(IsRow row1, IsRow row2) {
      for (int i = 0; i < orderBy.size(); i++) {
        int col = orderBy.get(i).getIndex();
        int z = comparator.compare(row1.getCell(col).getValue(), row2.getCell(col).getValue());
        if (z != 0) {
          return orderBy.get(i).isAscending() ? z : -z;
        }
      }
      return 0;
    }

    private boolean containsIndex(int index) {
      boolean found = false;
      for (SortInfo info : orderBy) {
        if (info.getIndex() == index) {
          found = true;
          break;
        }
      }
      return found;
    }
  }

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
    assertNewColumnId(column.getId());
    return addColumnImpl(column);
  }

  public int addColumn(ValueType type) {
    return addColumn(type, DataUtils.defaultColumnLabel(getNumberOfColumns()));
  }

  public int addColumn(ValueType type, String label) {
    return addColumn(type, label, DataUtils.defaultColumnId(getNumberOfColumns()));
  }

  public int addColumn(ValueType type, String label, String id) {
    return addColumn(new TableColumn(id, type, label));
  }

  public int addColumns(Collection<IsColumn> columnsToAdd) {
    Assert.hasLength(columnsToAdd);
    int lastIndex = -1;
    for (IsColumn column : columnsToAdd) {
      lastIndex = addColumn(column);
    }
    return lastIndex;
  }

  public int addRow() {
    return addRowImpl(createRow());
  }

  public int addRow(IsRow row) {
    return addRowImpl(cloneRow(row));
  }

  public int addRow(Object... cells) {
    Assert.parameterCount(cells.length, 1, getNumberOfColumns());
    IsRow row = new TableRow();

    for (int i = 0; i < getNumberOfColumns(); i++) {
      ValueType type = getColumnType(i);

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

  public int addRows(Collection<IsRow> rowsToAdd) {
    Assert.hasLength(rowsToAdd);
    int lastIndex = -1;
    for (IsRow row : rowsToAdd) {
      lastIndex = addRow(row);
    }
    return lastIndex;
  }

  public int addRows(int rowCount) {
    Assert.isPositive(rowCount);
    int lastIndex = -1;
    for (int i = 0; i < rowCount; i++) {
      lastIndex = addRow();
    }
    return lastIndex;
  }

  public void addWarning(DataWarning warning) {
    warnings.add(warning);
  }

  public void clearCell(int rowIndex, int colIndex) {
    getRow(rowIndex).clearCell(colIndex);
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

    for (IsColumn column : getColumns()) {
      result.addColumn(column.clone());
    }
    for (IsRow row : getRows()) {
      result.addRow(row.clone());
    }

    if (properties != null) {
      result.properties = properties.clone();
    }
    result.warnings = Lists.newArrayList();
    for (DataWarning warning : getWarnings()) {
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

  public IsData fromJson(String data) {
    // TODO Auto-generated method stub
    return null;
  }

  public IsData fromJson(String data, double version) {
    // TODO Auto-generated method stub
    return null;
  }

  public IsCell getCell(int rowIndex, int colIndex) {
    return getRow(rowIndex).getCell(colIndex);
  }

  public IsColumn getColumn(int colIndex) {
    assertColumnIndex(colIndex);
    return getColumnImpl(colIndex);
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
    ValueType type = getColumnType(colIndex);
    Value min = Value.getNullValueFromValueType(type);
    Value max = Value.getNullValueFromValueType(type);
    Value value;
    int cnt = 0;

    for (int i = 0; i < getNumberOfRows(); i++) {
      value = getValue(i, colIndex);
      if (value == null || value.isNull()) {
        continue;
      }
      if (cnt == 0) {
        min = value;
        max = value;
        cnt++;
        break;
      }

      if (value.compareTo(min) < 0) {
        min = value;
      } else if (value.compareTo(max) > 0) {
        max = value;
      }
      cnt++;
    }

    return new Range(min, max);
  }

  public List<IsColumn> getColumns() {
    return columns;
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
    Assert.parameterCount(filters.length, 1);
    List<Integer> match = Lists.newArrayList();
    boolean ok;

    for (int i = 0; i < getNumberOfRows(); i++) {
      IsRow row = getRow(i);
      ok = true;
      for (RowFilter filter : filters) {
        if (!filter.isMatch(this, row)) {
          ok = false;
          break;
        }
      }
      if (ok) {
        match.add(i);
      }
    }
    return Ints.toArray(match);
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
    return getRowImpl(rowIndex);
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
    SortInfo[] sortInfo = new SortInfo[colIndexes.length];
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo[i] = new SortInfo(colIndexes[i]);
    }
    return getSortedRows(sortInfo);
  }

  public int[] getSortedRows(SortInfo... sortInfo) {
    Assert.parameterCount(sortInfo.length, 1);
    int rowCount = getNumberOfRows();
    if (rowCount <= 0) {
      return new int[0];
    }
    if (rowCount == 1) {
      return new int[]{0};
    }

    List<Integer> rowIndexes = Lists.newArrayListWithCapacity(rowCount);
    for (int i = 0; i < rowCount; i++) {
      rowIndexes.add(i);
    }

    Collections.sort(rowIndexes, new IndexOrdering(new RowOrdering(sortInfo)));
    return Ints.toArray(rowIndexes);
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
    return warnings;
  }

  public void insertColumn(int colIndex, IsColumn column) {
    assertColumnIndex(colIndex);
    assertNewColumnId(column.getId());
    insertColumnImpl(colIndex, column);
  }

  public void insertColumn(int colIndex, ValueType type) {
    insertColumn(colIndex, type, DataUtils.defaultColumnLabel(colIndex));
  }

  public void insertColumn(int colIndex, ValueType type, String label) {
    insertColumn(colIndex, type, label, DataUtils.defaultColumnId(colIndex));
  }

  public void insertColumn(int colIndex, ValueType type, String label, String id) {
    insertColumn(colIndex, new TableColumn(id, type, label));
  }

  public void insertRows(int rowIndex, Collection<IsRow> rowsToAdd) {
    assertRowIndex(rowIndex);
    int idx = rowIndex;
    for (IsRow row : rowsToAdd) {
      insertRowImpl(idx++, row);
    }
  }

  public void insertRows(int rowIndex, int rowCount) {
    assertRowIndex(rowIndex);
    Assert.isPositive(rowCount);

    for (int i = 0; i < rowCount; i++) {
      insertRowImpl(rowIndex + i, createRow());
    }
  }

  public void removeColumn(int colIndex) {
    assertColumnIndex(colIndex);
    removeColumnImpl(colIndex);
  }

  public void removeColumnImpl(int colIndex) {
    columnIndexById.remove(columns.get(colIndex).getId());
    for (Map.Entry<String, Integer> entry : columnIndexById.entrySet()) {
      int idx = entry.getValue();
      if (idx > colIndex) {
        entry.setValue(idx - 1);
      }
    }
    columns.remove(colIndex);

    for (IsRow row : rows) {
      row.removeCell(colIndex);
    }
  }

  public void removeColumns(int colIndex, int colCount) {
    assertColumnIndex(colIndex);
    Assert.betweenInclusive(colCount, 1, getNumberOfColumns() - colIndex);

    for (int i = 0; i < colCount; i++) {
      removeColumn(colIndex);
    }
  }

  public void removeRow(int rowIndex) {
    assertRowIndex(rowIndex);
    removeRowImpl(rowIndex);
  }

  public void removeRows(int rowIndex, int rowCount) {
    assertRowIndex(rowIndex);
    Assert.betweenInclusive(rowCount, 1, getNumberOfRows() - rowIndex);

    for (int i = 0; i < rowCount; i++) {
      removeRowImpl(rowIndex);
    }
  }

  public void setCell(int rowIndex, int colIndex, boolean value) {
    setCell(rowIndex, colIndex, BooleanValue.getInstance(value));
  }

  public void setCell(int rowIndex, int colIndex, boolean value, String formattedValue) {
    setCell(rowIndex, colIndex, BooleanValue.getInstance(value), formattedValue);
  }

  public void setCell(int rowIndex, int colIndex, boolean value, String formattedValue,
      CustomProperties properties) {
    setCell(rowIndex, colIndex, BooleanValue.getInstance(value), formattedValue, properties);
  }

  public void setCell(int rowIndex, int colIndex, double value) {
    setCell(rowIndex, colIndex, new NumberValue(value));
  }

  public void setCell(int rowIndex, int colIndex, double value, String formattedValue) {
    setCell(rowIndex, colIndex, new NumberValue(value), formattedValue);
  }

  public void setCell(int rowIndex, int colIndex, double value, String formattedValue,
      CustomProperties properties) {
    setCell(rowIndex, colIndex, new NumberValue(value), formattedValue, properties);
  }

  public void setCell(int rowIndex, int colIndex, IsCell cell) {
    getRow(rowIndex).setCell(colIndex, cell);
  }

  public void setCell(int rowIndex, int colIndex, String value) {
    setCell(rowIndex, colIndex, new TextValue(value));
  }

  public void setCell(int rowIndex, int colIndex, String value, String formattedValue) {
    setCell(rowIndex, colIndex, new TextValue(value), formattedValue);
  }

  public void setCell(int rowIndex, int colIndex, String value, String formattedValue,
      CustomProperties properties) {
    setCell(rowIndex, colIndex, new TextValue(value), formattedValue, properties);
  }

  public void setCell(int rowIndex, int colIndex, Value value) {
    setCell(rowIndex, colIndex, new TableCell(value));
  }

  public void setCell(int rowIndex, int colIndex, Value value, String formattedValue) {
    setCell(rowIndex, colIndex, new TableCell(value, formattedValue));
  }

  public void setCell(int rowIndex, int colIndex, Value value, String formattedValue,
      CustomProperties properties) {
    setCell(rowIndex, colIndex, new TableCell(value, formattedValue, properties));
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

  public void setRows(Collection<IsRow> rows) {
    clearRows();
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
    SortInfo[] sortInfo = new SortInfo[colIndexes.length];
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo[i] = new SortInfo(colIndexes[i]);
    }
    sort(sortInfo);
  }

  public void sort(SortInfo... sortInfo) {
    if (getNumberOfRows() > 1) {
      Collections.sort(rows, new RowOrdering(sortInfo));
    }
  }

  public String toJson() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int rowIndex = 0; rowIndex < getNumberOfRows(); rowIndex++) {
      IsRow tableRow = getRowImpl(rowIndex);
      for (int cellIndex = 0; cellIndex < tableRow.getCells().size(); cellIndex++) {
        IsCell tableCell = tableRow.getCells().get(cellIndex);
        sb.append(tableCell.toString());
        if (cellIndex < tableRow.getCells().size() - 1) {
          sb.append(",");
        }
      }
      if (rowIndex < getNumberOfRows() - 1) {
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  private int addColumnImpl(IsColumn column) {
    columnIndexById.put(column.getId(), columns.size());
    columns.add(column);

    Value nullValue = Value.getNullValueFromValueType(column.getType());
    for (IsRow row : rows) {
      row.addCell(new TableCell(nullValue));
    }

    return columns.size() - 1;
  }

  private int addRowImpl(IsRow row) {
    rows.add(row);
    return rows.size() - 1;
  }

  private void assertColumnIndex(int colIndex) {
    Assert.isIndex(columns, colIndex);
  }

  private void assertNewColumnId(String columnId) {
    Assert.notEmpty(columnId);
    Assert.isFalse(columnIndexById.containsKey(columnId),
        "Column Id [" + columnId + "] already in table description");
  }

  private void assertRowIndex(int rowIndex) {
    Assert.isIndex(rows, rowIndex);
  }

  private void clearRows() {
    rows.clear();
  }

  private IsRow cloneRow(IsRow source) {
    List<IsCell> cells = source.getCells();
    Assert.isTrue(cells.size() <= columns.size(),
        "Row has too many cells. Should be at most of size: " + columns.size());
    IsRow row = new TableRow();

    for (int i = 0; i < cells.size(); i++) {
      Assert.isTrue(cells.get(i).getType().equals(columns.get(i).getType()),
          BeeUtils.concat(1, "Cell type", cells.get(i).getType(), "does not match column type",
              columns.get(i).getType(), "at index:", i));
    }
    for (int i = cells.size(); i < columns.size(); i++) {
      row.addCell(createCell(i));
    }

    CustomProperties props = source.getProperties();
    if (props != null) {
      row.setProperties(props);
    }
    return row;
  }

  private IsCell createCell(int colIndex) {
    return new TableCell(Value.getNullValueFromValueType(columns.get(colIndex).getType()));
  }

  private IsRow createRow() {
    IsRow row = new TableRow();
    for (int i = 0; i < columns.size(); i++) {
      row.addCell(createCell(i));
    }
    return row;
  }

  private IsColumn getColumnImpl(int colIndex) {
    return columns.get(colIndex);
  }

  private IsRow getRowImpl(int rowIndex) {
    return rows.get(rowIndex);
  }

  private void insertColumnImpl(int colIndex, IsColumn column) {
    for (Map.Entry<String, Integer> entry : columnIndexById.entrySet()) {
      int idx = entry.getValue();
      if (idx >= colIndex) {
        entry.setValue(idx + 1);
      }
    }
    columnIndexById.put(column.getId(), colIndex);
    columns.add(colIndex, column);

    Value nullValue = Value.getNullValueFromValueType(column.getType());
    for (IsRow row : rows) {
      row.insertCell(colIndex, new TableCell(nullValue));
    }
  }

  private void insertRowImpl(int rowIndex, IsRow row) {
    rows.add(rowIndex, row);
  }

  private void removeRowImpl(int rowIndex) {
    rows.remove(rowIndex);
  }
}
