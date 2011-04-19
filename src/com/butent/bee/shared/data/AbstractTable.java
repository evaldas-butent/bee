package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
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
import java.util.Set;

public abstract class AbstractTable<RowType extends IsRow, ColType extends IsColumn> implements
    IsTable<RowType, ColType> {

  protected class IndexOrdering implements Comparator<Integer> {
    private RowOrdering rowOrdering;

    private IndexOrdering(RowOrdering rowOrdering) {
      this.rowOrdering = rowOrdering;
    }

    public int compare(Integer idx1, Integer idx2) {
      return rowOrdering.compare(getRow(idx1), getRow(idx2));
    }
  }

  protected class RowOrdering implements Comparator<RowType> {
    private List<SortInfo> orderBy = Lists.newArrayList();
    private List<ValueType> types = Lists.newArrayList();

    RowOrdering(SortInfo... sortInfo) {
      Assert.notNull(sortInfo);
      Assert.parameterCount(sortInfo.length, 1);

      for (int i = 0; i < sortInfo.length; i++) {
        int index = sortInfo[i].getIndex();
        assertColumnIndex(index);
        if (!containsIndex(index)) {
          orderBy.add(sortInfo[i]);
          types.add(getColumnType(index));
        }
      }
    }

    public int compare(RowType row1, RowType row2) {
      if (row1 == row2) {
        return BeeConst.COMPARE_EQUAL;
      }
      if (row1 == null) {
        return orderBy.get(0).isAscending() ? BeeConst.COMPARE_LESS : BeeConst.COMPARE_MORE;
      }
      if (row2 == null) {
        return orderBy.get(0).isAscending() ? BeeConst.COMPARE_MORE : BeeConst.COMPARE_LESS;
      }
      
      int z;
      for (int i = 0; i < orderBy.size(); i++) {
        int index = orderBy.get(i).getIndex();
        switch (types.get(i)) {
          case BOOLEAN:
            z = BeeUtils.compare(row1.getBoolean(index), row2.getBoolean(index));
            break;
          case DATE:
            z = BeeUtils.compare(row1.getDate(index), row2.getDate(index));
            break;
          case DATETIME:
            z = BeeUtils.compare(row1.getDateTime(index), row2.getDateTime(index));
            break;
          case NUMBER:
            z = BeeUtils.compare(row1.getDouble(index), row2.getDouble(index));
            break;
          default:
            z = BeeUtils.compare(row1.getString(index), row2.getString(index));
        }
        if (z != BeeConst.COMPARE_EQUAL) {
          return orderBy.get(i).isAscending() ? z : -z;
        }
      }
      return BeeConst.COMPARE_EQUAL;
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

  private List<ColType> columns = Lists.newArrayList();

  private CustomProperties properties = null;
  private List<DataWarning> warnings = Lists.newArrayList();

  public AbstractTable() {
  }
  
  protected AbstractTable(ColType... columns) {
    Assert.notNull(columns);
    for (ColType column : columns) {
      addColumn(column);
    }
  }

  protected AbstractTable(String... columnLabels) {
    Assert.notNull(columnLabels);
    for (String label : columnLabels) {
      addColumn(ValueType.TEXT, label);
    }
  }
  
  public int addColumn(ColType column) {
    assertNewColumnId(column.getId());
    columns.add(column);

    if (getNumberOfRows() > 0) {
      Value nullValue = Value.getNullValueFromValueType(column.getType());
      for (RowType row : getRows()) {
        row.addCell(new TableCell(nullValue));
      }
    }
    return columns.size() - 1;
  }

  public int addColumn(ValueType type) {
    return addColumn(type, DataUtils.defaultColumnLabel(getNumberOfColumns()));
  }

  public int addColumn(ValueType type, String label) {
    return addColumn(type, label, DataUtils.defaultColumnId(getNumberOfColumns()));
  }

  public int addColumn(ValueType type, String label, String id) {
    return addColumn(createColumn(type, label, id));
  }

  public int addColumns(Collection<ColType> columnsToAdd) {
    Assert.hasLength(columnsToAdd);
    int lastIndex = BeeConst.INDEX_UNKNOWN;
    for (ColType column : columnsToAdd) {
      lastIndex = addColumn(column);
    }
    return lastIndex;
  }

  public int addRow() {
    return addRow(fillRow(createRow()));
  }

  public int addRow(Object... cells) {
    Assert.notNull(cells);
    Assert.parameterCount(cells.length, 1, getNumberOfColumns());
    RowType row = createRow();

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

  public int addRow(RowType row) {
    getRows().add(row);
    return getNumberOfRows() - 1;
  }
  
  public int addRows(Collection<RowType> rowsToAdd) {
    Assert.hasLength(rowsToAdd);
    int lastIndex = BeeConst.INDEX_UNKNOWN;
    for (RowType row : rowsToAdd) {
      lastIndex = addRow(row);
    }
    return lastIndex;
  }

  public int addRows(int rowCount) {
    Assert.isPositive(rowCount);
    int lastIndex = BeeConst.INDEX_UNKNOWN;
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
  public abstract IsTable<RowType, ColType> clone();
  
  public boolean containsAllColumnIds(Collection<String> colIds) {
    for (String id : colIds) {
      if (!containsColumn(id)) {
        return false;
      }
    }
    return true;
  }

  public boolean containsColumn(String columnId) {
    return getColumnIndex(columnId) >= 0;
  }

  public abstract IsTable<RowType, ColType> create();

  public abstract ColType createColumn(ValueType type, String label, String id);

  public abstract RowType createRow(long id);

  public IsData fromJson(String data) {
    // TODO Auto-generated method stub
    return null;
  }

  public IsData fromJson(String data, double version) {
    // TODO Auto-generated method stub
    return null;
  }

  public Boolean getBoolean(int rowIndex, int colIndex) {
    return getRow(rowIndex).getBoolean(colIndex);
  }

  public IsCell getCell(int rowIndex, int colIndex) {
    return getRow(rowIndex).getCell(colIndex);
  }

  public ColType getColumn(int colIndex) {
    assertColumnIndex(colIndex);
    return columns.get(colIndex);
  }

  public ColType getColumn(String columnId) {
    return getColumn(getColumnIndex(columnId));
  }

  public List<IsCell> getColumnCells(int colIndex) {
    assertColumnIndex(colIndex);
    List<IsCell> colCells = Lists.newArrayListWithCapacity(getNumberOfRows());

    for (RowType row : getRows()) {
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
    if (!BeeUtils.isEmpty(columnId)) {
      for (int i = 0; i < getNumberOfColumns(); i++) {
        if (BeeUtils.same(getColumnId(i), columnId)) {
          return i;
        }
      }
      for (int i = 0; i < getNumberOfColumns(); i++) {
        if (BeeUtils.same(getColumnLabel(i), columnId)) {
          return i;
        }
      }
    }
    return BeeConst.INDEX_UNKNOWN;
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

  public List<ColType> getColumns() {
    return columns;
  }

  public ValueType getColumnType(int colIndex) {
    return getColumn(colIndex).getType();
  }

  public List<Value> getColumnValues(int colIndex) {
    assertColumnIndex(colIndex);
    List<Value> values = Lists.newArrayListWithCapacity(getNumberOfRows());

    for (RowType row : getRows()) {
      values.add(row.getCell(colIndex).getValue());
    }
    return values;
  }

  public List<Value> getDistinctValues(int colIndex) {
    assertColumnIndex(colIndex);
    Set<Value> values = Sets.newTreeSet();
    for (RowType row : getRows()) {
      values.add(row.getCell(colIndex).getValue());
    }
    return Lists.newArrayList(values);
  }

  public List<Value> getDistinctValues(String columnId) {
    return getDistinctValues(getColumnIndex(columnId));
  }

  public Double getDouble(int rowIndex, int colIndex) {
    return getRow(rowIndex).getDouble(colIndex);
  }

  public int[] getFilteredRows(RowFilter... filters) {
    Assert.notNull(filters);
    Assert.parameterCount(filters.length, 1);
    List<Integer> match = Lists.newArrayList();
    boolean ok;

    for (int i = 0; i < getNumberOfRows(); i++) {
      RowType row = getRow(i);
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

  public abstract int getNumberOfRows();

  public CustomProperties getProperties(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getProperties();
  }

  public Object getProperty(int rowIndex, int colIndex, String name) {
    return getCell(rowIndex, colIndex).getProperty(name);
  }

  public abstract RowType getRow(int rowIndex);

  public CustomProperties getRowProperties(int rowIndex) {
    return getRow(rowIndex).getProperties();
  }

  public Object getRowProperty(int rowIndex, String name) {
    return getRow(rowIndex).getProperty(name);
  }

  public int[] getSortedRows(int... colIndexes) {
    Assert.notNull(colIndexes);
    SortInfo[] sortInfo = new SortInfo[colIndexes.length];
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo[i] = new SortInfo(colIndexes[i]);
    }
    return getSortedRows(sortInfo);
  }

  public int[] getSortedRows(SortInfo... sortInfo) {
    Assert.notNull(sortInfo);
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

  public String getString(int rowIndex, int colIndex) {
    return getRow(rowIndex).getString(colIndex);
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

  public void insertColumn(int colIndex, ColType column) {
    assertColumnIndex(colIndex);
    assertNewColumnId(column.getId());
    
    columns.add(colIndex, column);
    if (getNumberOfRows() > 0) {
      Value nullValue = Value.getNullValueFromValueType(column.getType());
      for (RowType row : getRows()) {
        row.insertCell(colIndex, new TableCell(nullValue));
      }
    }
  }

  public void insertColumn(int colIndex, ValueType type) {
    insertColumn(colIndex, type, DataUtils.defaultColumnLabel(colIndex));
  }

  public void insertColumn(int colIndex, ValueType type, String label) {
    insertColumn(colIndex, type, label, DataUtils.defaultColumnId(colIndex));
  }

  public void insertColumn(int colIndex, ValueType type, String label, String id) {
    insertColumn(colIndex, createColumn(type, label, id));
  }

  public void insertRows(int rowIndex, Collection<RowType> rowsToAdd) {
    assertRowIndex(rowIndex);
    int idx = rowIndex;
    for (RowType row : rowsToAdd) {
      insertRow(idx++, row);
    }
  }

  public void insertRows(int rowIndex, int rowCount) {
    assertRowIndex(rowIndex);
    Assert.isPositive(rowCount);

    for (int i = 0; i < rowCount; i++) {
      insertRow(rowIndex + i, fillRow(createRow()));
    }
  }

  public void removeColumn(int colIndex) {
    assertColumnIndex(colIndex);
    columns.remove(colIndex);

    for (RowType row : getRows()) {
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

  public abstract void removeRow(int rowIndex);

  public void removeRows(int rowIndex, int rowCount) {
    assertRowIndex(rowIndex);
    Assert.betweenInclusive(rowCount, 1, getNumberOfRows() - rowIndex);

    for (int i = 0; i < rowCount; i++) {
      removeRow(rowIndex);
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

  public void setColumns(List<ColType> columns) {
    this.columns = columns;
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

  public void setRows(Collection<RowType> rows) {
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
    getRow(rowIndex).setValue(colIndex, value);
  }

  public void sort(int... colIndexes) {
    Assert.notNull(colIndexes);
    SortInfo[] sortInfo = new SortInfo[colIndexes.length];
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo[i] = new SortInfo(colIndexes[i]);
    }
    sort(sortInfo);
  }

  public abstract void sort(SortInfo... sortInfo);

  public String toJson() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int rowIndex = 0; rowIndex < getNumberOfRows(); rowIndex++) {
      RowType row = getRow(rowIndex);
      for (int cellIndex = 0; cellIndex < row.getCells().size(); cellIndex++) {
        IsCell tableCell = row.getCells().get(cellIndex);
        sb.append(tableCell.toString());
        if (cellIndex < row.getCells().size() - 1) {
          sb.append(",");
        }
      }
      if (rowIndex < getNumberOfRows() - 1) {
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  protected void assertColumnIndex(int colIndex) {
    Assert.isIndex(columns, colIndex);
  }

  protected abstract void assertRowIndex(int rowIndex);

  protected abstract void clearRows();

  protected void cloneColumns(IsTable<RowType, ColType> cloneTable) {
    cloneTable.setColumns(getColumns());
  }
  
  protected void cloneTableDescription(IsTable<RowType, ColType> cloneTable) {
    cloneColumns(cloneTable);
    cloneProperties(cloneTable);
    cloneWarnings(cloneTable);
  }
  
  protected abstract void insertRow(int rowIndex, RowType row);

  private void assertNewColumnId(String columnId) {
    Assert.notEmpty(columnId);
    Assert.isFalse(containsColumn(columnId));
  }

  private void cloneProperties(IsTable<RowType, ColType> cloneTable) {
    if (getTableProperties() != null) {
      cloneTable.setTableProperties(getTableProperties().clone());
    }
  }

  private void cloneWarnings(IsTable<RowType, ColType> cloneTable) {
    for (DataWarning warning : getWarnings()) {
      cloneTable.getWarnings().add(warning);
    }
  }

  private IsCell createCell(int colIndex) {
    return new TableCell(Value.getNullValueFromValueType(columns.get(colIndex).getType()));
  }
  
  private RowType createRow() {
    return createRow(getNumberOfRows() + 1);
  }

  private RowType fillRow(RowType row) {
    for (int i = row.getNumberOfCells(); i < columns.size(); i++) {
      row.addCell(createCell(i));
    }
    return row;
  }
}
