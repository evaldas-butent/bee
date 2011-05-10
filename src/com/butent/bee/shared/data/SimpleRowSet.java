package com.butent.bee.shared.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Enables storing data in hash map type structure.
 */

public class SimpleRowSet implements Iterable<Map<String, String>>, BeeSerializable {

  /**
   * Contains a list of items for serialization.
   */

  private enum SerializationMembers {
    COLUMNS, ROWS
  }

  public static SimpleRowSet restore(String s) {
    SimpleRowSet rs = new SimpleRowSet();
    rs.deserialize(s);
    return rs;
  }

  private class RowSetIterator implements Iterator<Map<String, String>> {
    private int index = -1;

    public boolean hasNext() {
      return index < (getNumberOfRows() - 1);
    }

    public Map<String, String> next() {
      if (index >= getNumberOfRows()) {
        Assert.untouchable();
      }
      return getRow(++index);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private BiMap<String, Integer> columns;
  private List<String[]> rows;

  public SimpleRowSet(String[] cols) {
    Assert.notEmpty(cols);

    columns = HashBiMap.create(cols.length);
    rows = Lists.newArrayList();

    for (int i = 0; i < cols.length; i++) {
      columns.put(cols[i], i);
    }
  }

  private SimpleRowSet() {
  }

  public void addRow(String[] row) {
    Assert.lengthEquals(row, getNumberOfColumns());
    rows.add(row);
  }

  @Override
  public void deserialize(String s) {
    Assert.isTrue(getNumberOfColumns() == 0);

    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case COLUMNS:
          String[] colData = Codec.beeDeserialize(value);
          columns = HashBiMap.create(colData.length / 2);

          for (int j = 0; j < colData.length; j += 2) {
            columns.put(colData[j], BeeUtils.toInt(colData[j + 1]));
          }
          break;

        case ROWS:
          rows = Lists.newArrayList();

          if (!BeeUtils.isEmpty(value)) {
            String[] rowData = Codec.beeDeserialize(value);

            for (String r : rowData) {
              rows.add(Codec.beeDeserialize(r));
            }
          }
          break;

        default:
          Assert.untouchable("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  public boolean getBoolean(int rowIndex, int colIndex) {
    return BeeUtils.toBoolean(getValue(rowIndex, colIndex));
  }

  public boolean getBoolean(int rowIndex, String colName) {
    return BeeUtils.toBoolean(getValue(rowIndex, colName));
  }

  public boolean[] getBooleanColumn(int index) {
    Assert.contains(columns.inverse(), index);
    boolean[] col = new boolean[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = BeeUtils.toBoolean(rows.get(i)[index]);
    }
    return col;
  }

  public String[] getColumn(int index) {
    Assert.contains(columns.inverse(), index);
    String[] col = new String[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = rows.get(i)[index];
    }
    return col;
  }

  public int getColumnIndex(String colName) {
    Assert.contains(columns, colName);
    return columns.get(colName);
  }

  public String getColumnName(int colIndex) {
    Assert.contains(columns.inverse(), colIndex);
    return columns.inverse().get(colIndex);
  }

  public String[] getColumnNames() {
    String[] fields = new String[getNumberOfColumns()];

    for (int i = 0; i < getNumberOfColumns(); i++) {
      fields[i] = getColumnName(i);
    }
    return fields;
  }

  public JustDate getDate(int rowIndex, int colIndex) {
    return new JustDate(getInt(rowIndex, colIndex));
  }

  public JustDate getDate(int rowIndex, String colName) {
    return new JustDate(getInt(rowIndex, colName));
  }

  public JustDate[] getDateColumn(int index) {
    Assert.contains(columns.inverse(), index);
    JustDate[] col = new JustDate[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = new JustDate(BeeUtils.toInt(rows.get(i)[index]));
    }
    return col;
  }

  public DateTime getDateTime(int rowIndex, int colIndex) {
    return new DateTime(getLong(rowIndex, colIndex));
  }

  public DateTime getDateTime(int rowIndex, String colName) {
    return new DateTime(getLong(rowIndex, colName));
  }

  public DateTime[] getDateTimeColumn(int index) {
    Assert.contains(columns.inverse(), index);
    DateTime[] col = new DateTime[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = new DateTime(BeeUtils.toLong(rows.get(i)[index]));
    }
    return col;
  }

  public BigDecimal getDecimal(int rowIndex, int colIndex) {
    return new BigDecimal(getValue(rowIndex, colIndex));
  }

  public BigDecimal getDecimal(int rowIndex, String colName) {
    return new BigDecimal(getValue(rowIndex, colName));
  }

  public BigDecimal[] getDecimalColumn(int index) {
    Assert.contains(columns.inverse(), index);
    BigDecimal[] col = new BigDecimal[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = new BigDecimal(rows.get(i)[index]);
    }
    return col;
  }

  public double getDouble(int rowIndex, int colIndex) {
    return BeeUtils.toDouble(getValue(rowIndex, colIndex));
  }

  public double getDouble(int rowIndex, String colName) {
    return BeeUtils.toDouble(getValue(rowIndex, colName));
  }

  public double[] getDoubleColumn(int index) {
    Assert.contains(columns.inverse(), index);
    double[] col = new double[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = BeeUtils.toDouble(rows.get(i)[index]);
    }
    return col;
  }

  public int getInt(int rowIndex, int colIndex) {
    return BeeUtils.toInt(getValue(rowIndex, colIndex));
  }

  public int getInt(int rowIndex, String colName) {
    return BeeUtils.toInt(getValue(rowIndex, colName));
  }

  public int[] getIntColumn(int index) {
    Assert.contains(columns.inverse(), index);
    int[] col = new int[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = BeeUtils.toInt(rows.get(i)[index]);
    }
    return col;
  }

  public long getLong(int rowIndex, int colIndex) {
    return BeeUtils.toLong(getValue(rowIndex, colIndex));
  }

  public long getLong(int rowIndex, String colName) {
    return BeeUtils.toLong(getValue(rowIndex, colName));
  }

  public long[] getLongColumn(int index) {
    Assert.contains(columns.inverse(), index);
    long[] col = new long[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = BeeUtils.toLong(rows.get(i)[index]);
    }
    return col;
  }

  public int getNumberOfColumns() {
    return columns.size();
  }

  public int getNumberOfRows() {
    return rows.size();
  }

  public Map<String, String> getRow(int index) {
    String[] cells = getValues(index);
    Map<String, String> row = Maps.newHashMapWithExpectedSize(getNumberOfColumns());

    for (String col : columns.keySet()) {
      row.put(col, cells[columns.get(col)]);
    }
    return row;
  }

  public List<String[]> getRows() {
    return ImmutableList.copyOf(rows);
  }

  public String getValue(int rowIndex, int colIndex) {
    String[] cells = getValues(rowIndex);
    Assert.isIndex(cells, colIndex);
    return cells[colIndex];
  }

  public String getValue(int rowIndex, String colName) {
    return getValue(rowIndex, getColumnIndex(colName));
  }

  public String[] getValues(int index) {
    Assert.isIndex(rows, index);
    return rows.get(index);
  }

  @Override
  public Iterator<Map<String, String>> iterator() {
    return new RowSetIterator();
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case COLUMNS:
          arr[i++] = columns;
          break;

        case ROWS:
          arr[i++] = rows;
          break;

        default:
          Assert.untouchable("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }
}
