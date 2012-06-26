package com.butent.bee.shared.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
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

  private enum Serial {
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
  private Map<Integer, Map<String, Integer>> indexes = null;

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

    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case COLUMNS:
          String[] colData = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(colData)) {
            columns = HashBiMap.create(colData.length / 2);

            for (int j = 0; j < colData.length; j += 2) {
              columns.put(colData[j], BeeUtils.toInt(colData[j + 1]));
            }
          }
          break;

        case ROWS:
          rows = Lists.newArrayList();
          String[] rowData = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(rowData)) {
            for (String r : rowData) {
              rows.add(Codec.beeDeserializeCollection(r));
            }
          }
          break;
      }
    }
  }

  public Boolean getBoolean(int rowIndex, int colIndex) {
    return BeeUtils.toBooleanOrNull(getValue(rowIndex, colIndex));
  }

  public Boolean getBoolean(int rowIndex, String colName) {
    return getBoolean(rowIndex, getColumnIndex(colName));
  }

  public Boolean[] getBooleanColumn(int index) {
    Boolean[] col = new Boolean[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getBoolean(i, index);
    }
    return col;
  }

  public Boolean[] getBooleanColumn(String colName) {
    return getBooleanColumn(getColumnIndex(colName));
  }

  public String[] getColumn(int index) {
    String[] col = new String[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getValue(i, index);
    }
    return col;
  }

  public String[] getColumn(String colName) {
    return getColumn(getColumnIndex(colName));
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
    return TimeUtils.toDateOrNull(BeeUtils.transform(getLong(rowIndex, colIndex)
        / TimeUtils.MILLIS_PER_DAY));
  }

  public JustDate getDate(int rowIndex, String colName) {
    return getDate(rowIndex, getColumnIndex(colName));
  }

  public JustDate[] getDateColumn(int index) {
    JustDate[] col = new JustDate[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getDate(i, index);
    }
    return col;
  }

  public JustDate[] getDateColumn(String colName) {
    return getDateColumn(getColumnIndex(colName));
  }

  public DateTime getDateTime(int rowIndex, int colIndex) {
    return TimeUtils.toDateTimeOrNull(getValue(rowIndex, colIndex));
  }

  public DateTime getDateTime(int rowIndex, String colName) {
    return getDateTime(rowIndex, getColumnIndex(colName));
  }

  public DateTime[] getDateTimeColumn(int index) {
    DateTime[] col = new DateTime[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getDateTime(i, index);
    }
    return col;
  }

  public DateTime[] getDateTimeColumn(String colName) {
    return getDateTimeColumn(getColumnIndex(colName));
  }

  public BigDecimal getDecimal(int rowIndex, int colIndex) {
    return BeeUtils.toDecimalOrNull(getValue(rowIndex, colIndex));
  }

  public BigDecimal getDecimal(int rowIndex, String colName) {
    return getDecimal(rowIndex, getColumnIndex(colName));
  }

  public BigDecimal[] getDecimalColumn(int index) {
    BigDecimal[] col = new BigDecimal[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getDecimal(i, index);
    }
    return col;
  }

  public BigDecimal[] getDecimalColumn(String colName) {
    return getDecimalColumn(getColumnIndex(colName));
  }

  public Double getDouble(int rowIndex, int colIndex) {
    return BeeUtils.toDoubleOrNull(getValue(rowIndex, colIndex));
  }

  public Double getDouble(int rowIndex, String colName) {
    return getDouble(rowIndex, getColumnIndex(colName));
  }

  public Double[] getDoubleColumn(int index) {
    Double[] col = new Double[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getDouble(i, index);
    }
    return col;
  }

  public Double[] getDoubleColumn(String colName) {
    return getDoubleColumn(getColumnIndex(colName));
  }

  public Integer getInt(int rowIndex, int colIndex) {
    return BeeUtils.toIntOrNull(getValue(rowIndex, colIndex));
  }

  public Integer getInt(int rowIndex, String colName) {
    return getInt(rowIndex, getColumnIndex(colName));
  }

  public Integer[] getIntColumn(int index) {
    Integer[] col = new Integer[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getInt(i, index);
    }
    return col;
  }

  public Integer[] getIntColumn(String colName) {
    return getIntColumn(getColumnIndex(colName));
  }

  public Long getLong(int rowIndex, int colIndex) {
    return BeeUtils.toLongOrNull(getValue(rowIndex, colIndex));
  }

  public Long getLong(int rowIndex, String colName) {
    return getLong(rowIndex, getColumnIndex(colName));
  }

  public Long[] getLongColumn(int index) {
    Long[] col = new Long[getNumberOfRows()];

    for (int i = 0; i < getNumberOfRows(); i++) {
      col[i] = getLong(i, index);
    }
    return col;
  }

  public Long[] getLongColumn(String colName) {
    return getLongColumn(getColumnIndex(colName));
  }

  public int getNumberOfColumns() {
    return columns.size();
  }

  public int getNumberOfRows() {
    return rows.size();
  }

  public Map<String, String> getRow(int index) {
    String[] cells = getValues(index);

    if (cells == null) {
      return null;
    }
    Map<String, String> row = Maps.newHashMapWithExpectedSize(getNumberOfColumns());

    for (String col : columns.keySet()) {
      row.put(col, cells[columns.get(col)]);
    }
    return row;
  }

  public List<String[]> getRows() {
    return rows;
  }

  public String getValue(int rowIndex, int colIndex) {
    String[] cells = getValues(rowIndex);

    if (cells == null) {
      return null;
    }
    Assert.isIndex(cells, colIndex);
    return cells[colIndex];
  }

  public String getValue(int rowIndex, String colName) {
    return getValue(rowIndex, getColumnIndex(colName));
  }

  public String getValueByKey(String keyName, String keyValue, String colName) {
    return getValue(getIndex(keyName, keyValue), getColumnIndex(colName));
  }

  public String[] getValues(int index) {
    if (BeeUtils.isIndex(rows, index)) {
      return rows.get(index);
    }
    return null;
  }

  @Override
  public Iterator<Map<String, String>> iterator() {
    return new RowSetIterator();
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case COLUMNS:
          arr[i++] = columns;
          break;

        case ROWS:
          arr[i++] = rows;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  private int getIndex(String indexName, String indexValue) {
    int colIndex = getColumnIndex(indexName);

    if (indexes == null) {
      indexes = Maps.newHashMap();
    }
    if (!indexes.containsKey(colIndex)) {
      Map<String, Integer> index = Maps.newHashMapWithExpectedSize(getNumberOfRows());

      for (int i = 0; i < getNumberOfRows(); i++) {
        index.put(getValue(i, colIndex), i);
      }
      indexes.put(colIndex, index);
    }
    Integer idx = indexes.get(colIndex).get(indexValue);

    if (idx == null) {
      idx = BeeConst.UNDEF;
    }
    return idx;
  }
}
