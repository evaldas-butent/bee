package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BeeRowSet implements BeeSerializable {

  public class BeeRow implements BeeSerializable {
    private String[] data;

    private BeeRow() {
    }

    private BeeRow(String[] row) {
      setData(row);
    }

    @Override
    public void deserialize(String s) {
      setData(Codec.beeDeserialize(s));
    }

    public boolean getBoolean(int col) {
      return BeeUtils.toBoolean(getValue(col));
    }

    public boolean getBoolean(String colName) {
      return getBoolean(getColumnIndex(colName));
    }

    public float getFloat(int col) {
      return BeeUtils.toFloat(getValue(col));
    }

    public float getFloat(String colName) {
      return getFloat(getColumnIndex(colName));
    }

    public int getInt(int col) {
      return BeeUtils.toInt(getValue(col));
    }

    public int getInt(String colName) {
      return getInt(getColumnIndex(colName));
    }

    public long getLong(int col) {
      return BeeUtils.toLong(getValue(col));
    }

    public long getLong(String colName) {
      return getLong(getColumnIndex(colName));
    }

    public String getString(int col) {
      return BeeUtils.ifString(getValue(col), BeeConst.STRING_EMPTY);
    }

    public String getString(String colName) {
      return getString(getColumnIndex(colName));
    }

    public String getValue(int col) {
      Assert.betweenExclusive(col, 0, getColumnCount());

      return data[col];
    }

    @Override
    public String serialize() {
      return Codec.beeSerialize((Object) data);
    }

    private void setData(String[] row) {
      Assert.arrayLength(row, getColumnCount());
      data = row;
    }
  }

  private enum SerializationMembers {
    COLUMNS, ROWS
  }

  private static Logger logger = Logger.getLogger(BeeRowSet.class.getName());

  public static BeeRowSet restore(String s) {
    BeeRowSet rs = new BeeRowSet();
    rs.deserialize(s);
    return rs;
  }

  private BeeColumn[] columns;
  private List<BeeRow> rows;

  public BeeRowSet(BeeColumn[] columns) {
    setColumns(columns);
  }

  private BeeRowSet() {
  }

  public void addRow(String[] row) {
    addRow(new BeeRow(row));
  }

  @Override
  public void deserialize(String s) {
    Assert.isNull(columns);

    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);

    Assert.arrayLength(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case COLUMNS:
          String[] cArr = Codec.beeDeserialize(value);
          BeeColumn[] cols = new BeeColumn[cArr.length];

          for (int j = 0; j < cArr.length; j++) {
            cols[j] = BeeColumn.restore(cArr[j]);
          }
          setColumns(cols);
          break;

        case ROWS:
          if (!BeeUtils.isEmpty(value)) {
            String[] data = Codec.beeDeserialize(value);

            for (String r : data) {
              BeeRow row = new BeeRow();
              row.deserialize(r);
              addRow(row);
            }
          }
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  public int getColumnCount() {
    return columns.length;
  }

  public BeeColumn[] getColumns() {
    return columns;
  }

  public BeeRow getRow(int row) {
    Assert.betweenExclusive(row, 0, getRowCount());

    return rows.get(row);
  }

  public int getRowCount() {
    int cnt = 0;

    if (!BeeUtils.isEmpty(rows)) {
      cnt = rows.size();
    }
    return cnt;
  }

  public List<BeeRow> getRows() {
    return rows;
  }

  public boolean isEmpty() {
    return getRowCount() <= 0;
  }

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();

    for (SerializationMembers member : SerializationMembers.values()) {
      switch (member) {
        case COLUMNS:
          sb.append(Codec.beeSerialize((Object) columns));
          break;
        case ROWS:
          sb.append(Codec.beeSerialize(rows));
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return sb.toString();
  }

  private void addRow(BeeRow row) {
    if (BeeUtils.isEmpty(rows)) {
      rows = new ArrayList<BeeRow>();
    }
    rows.add(row);
  }

  private int getColumnIndex(String colName) {
    for (int i = 0; i < getColumnCount(); i++) {
      if (BeeUtils.same(colName, columns[i].getName())) {
        return i;
      }
    }
    return -1;
  }

  private void setColumns(BeeColumn[] columns) {
    Assert.notEmpty(columns);
    this.columns = columns;
  }
}
