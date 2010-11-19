package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class BeeRowSet extends AbstractData implements BeeSerializable {

  public class BeeRow implements BeeSerializable {

    private int id = 0;
    private String[] data;
    private Map<Integer, String> shadow;

    private BeeRow() {
    }

    private BeeRow(String[] row) {
      id = counter++;
      setData(row);
    }

    @Override
    public void deserialize(String s) {
      String[] arr = Codec.beeDeserialize(s);
      Assert.arrayLength(arr, 3);

      id = BeeUtils.toInt(arr[0]);

      if (!BeeUtils.isEmpty(arr[1])) {
        setData(Codec.beeDeserialize(arr[1]));
      }
      if (!BeeUtils.isEmpty(arr[2])) {
        String[] shArr = Codec.beeDeserialize(arr[2]);

        if (BeeUtils.arrayLength(shArr) > 1) {
          Map<Integer, String> shMap = new HashMap<Integer, String>();

          for (int i = 0; i < shArr.length; i += 2) {
            shMap.put(BeeUtils.toInt(shArr[i]), shArr[i + 1]);
          }
          setShadow(shMap);
        }
      }
    }

    public boolean getBoolean(int col) {
      return BeeUtils.toBoolean(getValue(col));
    }

    public boolean getBoolean(String colName) {
      return getBoolean(getColumnIndex(colName));
    }

    public String[] getData() {
      return data;
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

    public Object getOriginal(int col) {
      int type = getColumn(col).getType();

      switch (type) {
        case 4: // java.sql.Types.INTEGER
          return getInt(col);
        case 6: // java.sql.Types.FLOAT
        case 7: // java.sql.Types.REAL
        case 8: // java.sql.Types.DOUBLE
          return getFloat(col);
        case 16: // java.sql.Types.BOOLEAN
          return getBoolean(col);
        case -5: // java.sql.Types.BIGINT
          return getLong(col);
        default:
          return getValue(col);
      }
    }

    public Object getOriginal(String colName) {
      return getOriginal(getColumnIndex(colName));
    }

    public Map<Integer, String> getShadow() {
      return shadow;
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

    public String getValue(String colName) {
      return getValue(getColumnIndex(colName));
    }

    @Override
    public String serialize() {
      StringBuilder sb = new StringBuilder();

      sb.append(Codec.beeSerialize(id));
      sb.append(Codec.beeSerialize((Object) data));
      sb.append(Codec.beeSerialize(shadow));

      return sb.toString();
    }

    public void setValue(int col, String value) {
      Assert.betweenExclusive(col, 0, getColumnCount());

      String oldValue = data[col];

      if (oldValue != value) {
        if (BeeUtils.isEmpty(shadow)) {
          shadow = new HashMap<Integer, String>();
        }
        if (!shadow.containsKey(col)) {
          shadow.put(col, oldValue);
        } else {
          if (shadow.get(col) == value) {
            shadow.remove(col);
          }
        }
        data[col] = value;
      }
    }

    public void setValue(String colName, String value) {
      setValue(getColumnIndex(colName), value);
    }

    private int getId() {
      return id;
    }

    private void setData(String[] row) {
      Assert.arrayLength(row, getColumnCount());
      data = row;
      setShadow(null);
    }

    private void setShadow(Map<Integer, String> shadow) {
      this.shadow = shadow;
    }
  }

  private enum SerializationMembers {
    COUNTER, SOURCE, ID_INDEX, LOCK_INDEX, COLUMNS, ROWS
  }

  private static Logger logger = Logger.getLogger(BeeRowSet.class.getName());

  public static BeeRowSet restore(String s) {
    BeeRowSet rs = new BeeRowSet();
    rs.deserialize(s);
    return rs;
  }

  private int counter = 0;

  private String source;
  private int idIndex;
  private int lockIndex;

  private List<BeeRow> rows;

  public BeeRowSet(BeeColumn[] columns) {
    setColumns(columns);
  }

  private BeeRowSet() {
  }

  public void addRow(String[] row) {
    addRow(new BeeRow(row));
  }

  public void commit(BeeRowSet rs) {
    if (!BeeUtils.isEmpty(rs)) {
      for (BeeRow upd : rs.getRows()) {
        BeeRow dst = findRow(upd.getId());

        if (!BeeUtils.isEmpty(dst)) {
          Object delete = upd.getShadow().get(getIdIndex());

          if (!BeeUtils.isEmpty(delete)) {
            removeRow(dst);
          } else {
            dst.setData(upd.getData());
          }
        }
      }
    }
  }

  @Override
  public void deserialize(String s) {
    Assert.isNull(getColumns());

    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);

    Assert.arrayLength(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case COUNTER:
          counter = BeeUtils.toInt(value);
          break;

        case SOURCE:
          source = value;
          break;

        case ID_INDEX:
          idIndex = BeeUtils.toInt(value);
          break;

        case LOCK_INDEX:
          lockIndex = BeeUtils.toInt(value);
          break;

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

  public BeeColumn getColumn(int col) {
    Assert.betweenExclusive(col, 0, getColumnCount());
    return getColumns()[col];
  }

  public String getColumnName(int col) {
    return getColumn(col).getName();
  }

  public String[][] getData() {
    int rCnt = getRowCount();
    String[][] data = new String[rCnt][getColumnCount()];

    for (int i = 0; i < rCnt; i++) {
      BeeRow row = getRow(i);
      data[i] = row.getData();
    }
    return data;
  }

  public int getIdIndex() {
    return idIndex;
  }

  public String getIdName() {
    return getColumnName(idIndex);
  }

  public int getLockIndex() {
    return lockIndex;
  }

  public String getLockName() {
    return getColumnName(lockIndex);
  }

  public BeeRow getRow(int row) {
    Assert.betweenExclusive(row, 0, getRowCount());
    return rows.get(row);
  }

  public List<BeeRow> getRows() {
    return rows;
  }

  public String getSource() {
    return source;
  }

  @Override
  public String getValue(int row, int col) {
    return getRow(row).getValue(col);
  }

  public boolean isEmpty() {
    return getRowCount() <= 0;
  }

  public BeeRowSet prepareUpdate() {
    BeeRowSet update = new BeeRowSet(getColumns());
    update.setSource(getSource());
    update.setIdIndex(getIdIndex());
    update.setLockIndex(getLockIndex());

    for (BeeRow row : getRows()) {
      if (!BeeUtils.isEmpty(row.getShadow())) {
        update.addRow(row);
      }
    }
    if (update.isEmpty()) {
      return null;
    }
    return update;
  }

  public void rollback() {
    for (BeeRow row : getRows()) {
      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Entry<Integer, String> shadow : row.getShadow().entrySet()) {
          row.setValue(shadow.getKey(), shadow.getValue()); // TODO Conflict
        }
        row.setShadow(null);
      }
    }
  }

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();

    for (SerializationMembers member : SerializationMembers.values()) {
      switch (member) {
        case COUNTER:
          sb.append(Codec.beeSerialize(counter));
          break;
        case SOURCE:
          sb.append(Codec.beeSerialize(source));
          break;
        case ID_INDEX:
          sb.append(Codec.beeSerialize(idIndex));
          break;
        case LOCK_INDEX:
          sb.append(Codec.beeSerialize(lockIndex));
          break;
        case COLUMNS:
          sb.append(Codec.beeSerialize((Object) getColumns()));
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

  public void setIdField(String fieldName) {
    setIdIndex(getColumnIndex(fieldName));
  }

  public void setLockField(String fieldName) {
    setLockIndex(getColumnIndex(fieldName));
  }

  public void setSource(String src) {
    source = src;
  }

  @Override
  public void setValue(int row, int col, String value) {
    BeeRow r = getRow(row);
    r.setValue(col, value);
  }

  private void addRow(BeeRow row) {
    if (BeeUtils.isEmpty(rows)) {
      rows = new ArrayList<BeeRow>();
    }
    rows.add(row);
    setRowCount(rows.size());
  }

  private BeeRow findRow(int id) {
    for (BeeRow row : getRows()) {
      if (row.getId() == id) {
        return row;
      }
    }
    return null;
  }

  private int getColumnIndex(String colName) {
    for (int i = 0; i < getColumnCount(); i++) {
      if (BeeUtils.same(colName, getColumnName(i))) {
        return i;
      }
    }
    return -1;
  }

  private void removeRow(BeeRow row) {
    rows.remove(row);
    setRowCount(rows.size());
  }

  private void setIdIndex(int index) {
    idIndex = index;
  }

  private void setLockIndex(int index) {
    lockIndex = index;
  }
}
