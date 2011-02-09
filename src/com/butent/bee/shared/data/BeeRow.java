package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.StringArray;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BeeRow extends StringRow implements BeeSerializable {
  public static BeeRow restore(String s) {
    BeeRow row = new BeeRow();
    row.deserialize(s);
    return row;
  }

  private int id = 0;
  private int mode = 0;
  private Map<Integer, String> shadow = null;
  
  private BeeRow() {
    super(null);
  }

  BeeRow(int size, int id) {
    super(new StringArray(new String[size]));
    this.id = id;
  }

  BeeRow(String[] row, int id) {
    super(new StringArray(row));
    this.id = id;
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.arrayLength(arr, 4);

    id = BeeUtils.toInt(arr[0]);
    mode = BeeUtils.toInt(arr[1]);

    if (!BeeUtils.isEmpty(arr[2])) {
      setValues(new StringArray(Codec.beeDeserialize(arr[2])));
    }
    if (!BeeUtils.isEmpty(arr[3])) {
      String[] shArr = Codec.beeDeserialize(arr[3]);

      if (ArrayUtils.length(shArr) > 1) {
        Map<Integer, String> shMap = new HashMap<Integer, String>();

        for (int i = 0; i < shArr.length; i += 2) {
          shMap.put(BeeUtils.toInt(shArr[i]), shArr[i + 1]);
        }
        setShadow(shMap);
      }
    }
  }

  public BigDecimal getDecimal(int col) {
    return new BigDecimal(getString(col));
  }

  public double getDouble(int col) {
    return BeeUtils.toDouble(getString(col));
  }

  public float getFloat(int col) {
    return BeeUtils.toFloat(getString(col));
  }

  public int getId() {
    return id;
  }

  public int getInt(int col) {
    return BeeUtils.toInt(getString(col));
  }

  public long getLong(int col) {
    return BeeUtils.toLong(getString(col));
  }

  public Object getOriginal(int col, int sqlType) {
    if (getValue(col) == null) {
      return null;
    }
    switch (sqlType) {
      case 2: // java.sql.Types.NUMERIC // TODO Kaip su Oracle, PgSql?
      case 3: // java.sql.Types.DECIMAL
        return getDecimal(col);
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

  public Map<Integer, String> getShadow() {
    return shadow;
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

  public String serialize() {
    StringBuilder sb = new StringBuilder();

    sb.append(Codec.beeSerialize(id));
    sb.append(Codec.beeSerialize(mode));
    sb.append(Codec.beeSerialize((Object) getValues().getArray()));
    sb.append(Codec.beeSerialize(shadow));

    return sb.toString();
  }

  @Override
  public void setValue(int col, String value) {
    String oldValue = getString(col);

    if (!BeeUtils.equalsTrim(value, oldValue)) {
      if (shadow == null) {
        shadow = new HashMap<Integer, String>();
      }
      if (!shadow.containsKey(col)) {
        shadow.put(col, oldValue);
      } else {
        if (BeeUtils.equalsTrim(shadow.get(col), value)) {
          shadow.remove(col);

          if (BeeUtils.isEmpty(shadow) && !markedForInsert()) {
            markForDelete(); // TODO: dummy
          }
        }
      }
      super.setValue(col, value);
    }
  }

  private void setShadow(Map<Integer, String> shadow) {
    this.shadow = shadow;
  }
}
