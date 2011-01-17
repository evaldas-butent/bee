package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class BeeRowSet extends AbstractData implements BeeSerializable {

  public class BeeRow implements BeeSerializable {

    private int id = 0;
    private int mode = 0;
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
      Assert.arrayLength(arr, 4);

      id = BeeUtils.toInt(arr[0]);
      mode = BeeUtils.toInt(arr[1]);

      if (!BeeUtils.isEmpty(arr[2])) {
        setData(Codec.beeDeserialize(arr[2]));
      }
      if (!BeeUtils.isEmpty(arr[3])) {
        String[] shArr = Codec.beeDeserialize(arr[3]);

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

    public double getDouble(int col) {
      return BeeUtils.toDouble(getValue(col));
    }

    public double getDouble(String colName) {
      return getDouble(getColumnIndex(colName));
    }

    public float getFloat(int col) {
      return BeeUtils.toFloat(getValue(col));
    }

    public float getFloat(String colName) {
      return getFloat(getColumnIndex(colName));
    }

    public int getId() {
      return id;
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

    public BigDecimal getNumber(int col) {
      return new BigDecimal(getValue(col));
    }

    public BigDecimal getNumber(String colName) {
      return getNumber(getColumnIndex(colName));
    }

    public Object getOriginal(int col) {
      if (getValue(col) == null) {
        return null;
      }
      switch (getColumn(col).getType()) {
        case 2: // java.sql.Types.NUMERIC // TODO Kaip su Oracle, PgSql?
        case 3: // java.sql.Types.DECIMAL
          return getNumber(col);
        case 4: // java.sql.Types.INTEGER
          return getInt(col);
        case -5: // java.sql.Types.BIGINT
          return getLong(col);
        case 6: // java.sql.Types.FLOAT
        case 7: // java.sql.Types.REAL
        case 100: // oracle.sql.Types.BINARY_FLOAT
          return getFloat(col);
        case 8: // java.sql.Types.DOUBLE
        case 101: // oracle.sql.Types.BINARY_DOUBLE
          return getDouble(col);
        case -7: // java.sql.Types.BIT
        case 16: // java.sql.Types.BOOLEAN
          return getBoolean(col);
        default:
          return getString(col);
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

    public boolean markedForDelete() {
      return mode < 0;
    }

    public boolean markedForInsert() {
      return mode > 0;
    }

    public void markForDelete() {
      mode = -1;
    }

    public void markForInsert() {
      mode = 1;
    }

    public void reset() {
      setShadow(null);
      mode = 0;
    }

    @Override
    public String serialize() {
      StringBuilder sb = new StringBuilder();

      sb.append(Codec.beeSerialize(id));
      sb.append(Codec.beeSerialize(mode));
      sb.append(Codec.beeSerialize((Object) data));
      sb.append(Codec.beeSerialize(shadow));

      return sb.toString();
    }

    public void setData(String[] row) {
      Assert.arrayLength(row, getColumnCount());
      data = row;
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

            if (BeeUtils.isEmpty(shadow) && !markedForInsert()) {
              markForDelete(); // TODO: dummy
            }
          }
        }
        data[col] = value;
      }
    }

    public void setValue(String colName, String value) {
      setValue(getColumnIndex(colName), value);
    }

    private void setShadow(Map<Integer, String> shadow) {
      this.shadow = shadow;
    }
  }

  private enum SerializationMembers {
    COUNTER, VIEW, COLUMNS, ROWS
  }

  private static Logger logger = Logger.getLogger(BeeRowSet.class.getName());

  public static BeeRowSet restore(String s) {
    BeeRowSet rs = new BeeRowSet();
    rs.deserialize(s);
    return rs;
  }

  private int counter = 0;
  private String viewName;

  private List<BeeRow> rows = new ArrayList<BeeRow>();

  public BeeRowSet(BeeColumn... columns) {
    setColumns(columns);
  }

  private BeeRowSet() {
  }

  public BeeRow addRow(String[] data) {
    BeeRow row = new BeeRow(data);
    addRow(row);
    return row;
  }

  public void commit(BeeRowSet update) {
    if (!BeeUtils.isEmpty(update)) {
      for (BeeRow upd : update.getRows()) {
        BeeRow dst = findRow(upd.getId());

        if (!BeeUtils.isEmpty(dst)) {
          if (dst.markedForDelete()) {
            removeRow(dst);
          } else {
            dst.setData(upd.getData());
            dst.reset();
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

        case VIEW:
          setViewName(value);
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

  public BeeRowSet getChanges() {
    BeeRowSet update = new BeeRowSet(getColumns());
    update.setViewName(getViewName());

    if (!isEmpty()) {
      for (BeeRow row : getRows()) {
        if (!BeeUtils.isEmpty(row.getShadow()) || row.markedForDelete() || row.markedForInsert()) {
          update.addRow(row);
        }
      }
    }
    if (update.isEmpty()) {
      return null;
    }
    return update;
  }

  public BeeColumn getColumn(int col) {
    Assert.betweenExclusive(col, 0, getColumnCount());
    return getColumns()[col];
  }

  public BeeColumn getColumn(String colName) {
    return getColumn(getColumnIndex(colName));
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

  public BeeRow getRow(int row) {
    Assert.betweenExclusive(row, 0, getRowCount());
    return rows.get(row);
  }

  public List<BeeRow> getRows() {
    return rows;
  }

  @Override
  public String getValue(int row, int col) {
    return getRow(row).getValue(col);
  }

  public String getViewName() {
    return viewName;
  }

  public boolean isEmpty() {
    return getRowCount() <= 0;
  }

  public void rollback() {
    if (!isEmpty()) {
      for (BeeRow row : new ArrayList<BeeRow>(getRows())) {
        if (row.markedForInsert()) {
          removeRow(row);
        } else {
          if (!BeeUtils.isEmpty(row.getShadow())) {
            for (Entry<Integer, String> shadow : row.getShadow().entrySet()) {
              row.setValue(shadow.getKey(), shadow.getValue());
            }
          }
          row.reset();
        }
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

        case VIEW:
          sb.append(Codec.beeSerialize(getViewName()));
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

  @Override
  public void setValue(int row, int col, String value) {
    BeeRow r = getRow(row);
    r.setValue(col, value);
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  private void addRow(BeeRow row) {
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
}
