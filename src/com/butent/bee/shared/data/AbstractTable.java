package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Sequence;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Is an abstract class for table classes, contains full range of table operating methods like
 * {@code addRow} or {@code sort}.
 */

public abstract class AbstractTable<RowType extends IsRow, ColType extends IsColumn> implements
    IsTable<RowType, ColType> {

  protected class IndexOrdering implements Comparator<Integer> {
    private RowOrdering<RowType> rowOrdering;

    private IndexOrdering(RowOrdering<RowType> rowOrdering) {
      this.rowOrdering = rowOrdering;
    }

    public int compare(Integer idx1, Integer idx2) {
      return rowOrdering.compare(getRow(idx1), getRow(idx2));
    }
  }

  protected class RowIdOrdering implements Comparator<RowType> {
    private final boolean ascending;

    RowIdOrdering() {
      this(true);
    }

    RowIdOrdering(boolean ascending) {
      this.ascending = ascending;
    }

    public int compare(RowType row1, RowType row2) {
      if (row1 == row2) {
        return BeeConst.COMPARE_EQUAL;
      }
      if (row1 == null) {
        return ascending ? BeeConst.COMPARE_LESS : BeeConst.COMPARE_MORE;
      }
      if (row2 == null) {
        return ascending ? BeeConst.COMPARE_MORE : BeeConst.COMPARE_LESS;
      }

      if (ascending) {
        return Longs.compare(row1.getId(), row2.getId());
      } else {
        return Longs.compare(row2.getId(), row1.getId());
      }
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
    int lastIndex = BeeConst.UNDEF;
    for (ColType column : columnsToAdd) {
      lastIndex = addColumn(column);
    }
    return lastIndex;
  }

  public int addRow() {
    return addRow(fillRow(createRow()));
  }

  public int addRow(RowType row) {
    getRows().add(row);
    return getNumberOfRows() - 1;
  }

  public int addRows(Collection<RowType> rowsToAdd) {
    Assert.notNull(rowsToAdd);
    int lastIndex = BeeConst.UNDEF;
    for (RowType row : rowsToAdd) {
      lastIndex = addRow(row);
    }
    return lastIndex;
  }

  public int addRows(int rowCount) {
    Assert.isPositive(rowCount);
    int lastIndex = BeeConst.UNDEF;
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

  public abstract void clearRows();

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

  public boolean containsRow(long rowId) {
    return getRowById(rowId) != null;
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
    return DataUtils.getColumnIndex(columnId, getColumns());
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

  public String getColumnProperty(int colIndex, String name) {
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

  public JustDate getDate(int rowIndex, int colIndex) {
    return getRow(rowIndex).getDate(colIndex);
  }

  public DateTime getDateTime(int rowIndex, int colIndex) {
    return getRow(rowIndex).getDateTime(colIndex);
  }

  public BigDecimal getDecimal(int rowIndex, int colIndex) {
    return getRow(rowIndex).getDecimal(colIndex);
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
        if (!filter.isMatch(getColumns(), row)) {
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

  public Integer getInteger(int rowIndex, int colIndex) {
    return getRow(rowIndex).getInteger(colIndex);
  }

  public Long getLong(int rowIndex, int colIndex) {
    return getRow(rowIndex).getLong(colIndex);
  }

  public int getNumberOfColumns() {
    return columns.size();
  }

  public abstract int getNumberOfRows();

  public CustomProperties getProperties(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getProperties();
  }

  public String getProperty(int rowIndex, int colIndex, String name) {
    return getCell(rowIndex, colIndex).getProperty(name);
  }

  public abstract RowType getRow(int rowIndex);

  public RowType getRowById(long rowId) {
    for (int i = 0; i < getNumberOfRows(); i++) {
      if (getRow(i).getId() == rowId) {
        return getRow(i);
      }
    }
    return null;
  }

  public int getRowIndex(long rowId) {
    for (int i = 0; i < getNumberOfRows(); i++) {
      if (getRow(i).getId() == rowId) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  public CustomProperties getRowProperties(int rowIndex) {
    return getRow(rowIndex).getProperties();
  }

  public String getRowProperty(int rowIndex, String name) {
    return getRow(rowIndex).getProperty(name);
  }

  public abstract Sequence<RowType> getRows();

  public int[] getSortedRows(int... colIndexes) {
    Assert.notNull(colIndexes);
    Assert.parameterCount(colIndexes.length, 1);

    List<Pair<Integer, Boolean>> sortInfo = Lists.newArrayList();
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo.add(Pair.of(colIndexes[i], true));
    }
    return getSortedRows(sortInfo);
  }

  public int[] getSortedRows(List<Pair<Integer, Boolean>> sortInfo) {
    Assert.notNull(sortInfo);
    Assert.isTrue(sortInfo.size() >= 1);
    int rowCount = getNumberOfRows();
    if (rowCount <= 0) {
      return new int[0];
    }
    if (rowCount == 1) {
      return new int[] {0};
    }

    List<Integer> rowIndexes = Lists.newArrayListWithCapacity(rowCount);
    for (int i = 0; i < rowCount; i++) {
      rowIndexes.add(i);
    }

    Collections.sort(rowIndexes, new IndexOrdering(new RowOrdering<RowType>(getColumns(),
        sortInfo)));
    return Ints.toArray(rowIndexes);
  }

  public String getString(int rowIndex, int colIndex) {
    return getRow(rowIndex).getString(colIndex);
  }

  public CustomProperties getTableProperties() {
    return properties;
  }

  public String getTableProperty(String key) {
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

  public boolean removeRowById(long rowId) {
    int rowIndex = getRowIndex(rowId);
    if (BeeConst.isUndef(rowIndex)) {
      return false;
    } else {
      removeRow(rowIndex);
      return true;
    }
  }

  public void removeRows(int rowIndex, int rowCount) {
    assertRowIndex(rowIndex);
    Assert.betweenInclusive(rowCount, 1, getNumberOfRows() - rowIndex);

    for (int i = 0; i < rowCount; i++) {
      removeRow(rowIndex);
    }
  }

  public void setCell(int rowIndex, int colIndex, IsCell cell) {
    getRow(rowIndex).setCell(colIndex, cell);
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

  public void setColumnProperty(int colIndex, String name, String value) {
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

  public void setProperty(int rowIndex, int colIndex, String name, String value) {
    getCell(rowIndex, colIndex).setProperty(name, value);
  }

  public void setRowProperties(int rowIndex, CustomProperties properties) {
    getRow(rowIndex).setProperties(properties);
  }

  public void setRowProperty(int rowIndex, String name, String value) {
    getRow(rowIndex).setProperty(name, value);
  }

  public void setRows(Collection<RowType> rows) {
    clearRows();
    addRows(rows);
  }

  public void setTableProperties(CustomProperties properties) {
    this.properties = properties;
  }

  public void setTableProperty(String propertyKey, String propertyValue) {
    Assert.notEmpty(propertyKey);
    Assert.notNull(propertyValue);
    if (properties == null) {
      properties = CustomProperties.create();
    }
    properties.put(propertyKey, propertyValue);
  }

  public void setValue(int rowIndex, int colIndex, BigDecimal value) {
    setValue(rowIndex, colIndex, new DecimalValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Boolean value) {
    setValue(rowIndex, colIndex, BooleanValue.getInstance(value));
  }

  public void setValue(int rowIndex, int colIndex, DateTime value) {
    setValue(rowIndex, colIndex, new DateTimeValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Double value) {
    setValue(rowIndex, colIndex, new NumberValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Integer value) {
    setValue(rowIndex, colIndex, new IntegerValue(value));
  }

  public void setValue(int rowIndex, int colIndex, JustDate value) {
    setValue(rowIndex, colIndex, new DateValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Long value) {
    setValue(rowIndex, colIndex, new LongValue(value));
  }

  public void setValue(int rowIndex, int colIndex, String value) {
    setValue(rowIndex, colIndex, new TextValue(value));
  }

  public void setValue(int rowIndex, int colIndex, Value value) {
    getRow(rowIndex).setValue(colIndex, value);
  }

  public void sort(int... colIndexes) {
    Assert.notNull(colIndexes);
    Assert.parameterCount(colIndexes.length, 1);

    List<Pair<Integer, Boolean>> sortInfo = Lists.newArrayList();
    for (int i = 0; i < colIndexes.length; i++) {
      sortInfo.add(Pair.of(colIndexes[i], true));
    }
    sort(sortInfo);
  }

  public abstract void sort(List<Pair<Integer, Boolean>> sortInfo);

  public abstract void sortByRowId(boolean ascending);

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

  public boolean updateRow(RowType row) {
    if (row == null) {
      return false;
    }

    int rowIndex = getRowIndex(row.getId());
    if (BeeConst.isUndef(rowIndex)) {
      return false;
    } else {
      getRows().set(rowIndex, row);
      return true;
    }
  }

  protected void assertColumnIndex(int colIndex) {
    Assert.isIndex(columns, colIndex);
  }

  protected abstract void assertRowIndex(int rowIndex);

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
