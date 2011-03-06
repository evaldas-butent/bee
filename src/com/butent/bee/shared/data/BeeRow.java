package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.StringArray;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BeeRow extends StringRow implements BeeSerializable {
  public static BeeRow restore(String s) {
    BeeRow row = new BeeRow(0, 0);
    row.deserialize(s);
    return row;
  }
  
  private long version = 0;
  private int mode = 0;
  private Map<Integer, String> shadow = null;

  BeeRow(long id, int size) {
    super(id, new StringArray(new String[size]));
  }

  BeeRow(long id, String[] row) {
    super(id, new StringArray(row));
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.arrayLength(arr, 5);
    int p = 0;

    setId(BeeUtils.toLong(arr[p++]));
    setVersion(BeeUtils.toLong(arr[p++]));
    mode = BeeUtils.toInt(arr[p++]);

    if (!BeeUtils.isEmpty(arr[p])) {
      setValues(new StringArray(Codec.beeDeserialize(arr[p])));
    }
    p++;
    if (!BeeUtils.isEmpty(arr[p])) {
      String[] shArr = Codec.beeDeserialize(arr[p]);

      if (ArrayUtils.length(shArr) > 1) {
        Map<Integer, String> shMap = new HashMap<Integer, String>();

        for (int i = 0; i < shArr.length; i += 2) {
          shMap.put(BeeUtils.toInt(shArr[i]), shArr[i + 1]);
        }
        setShadow(shMap);
      }
    }
  }
  
  @Override
  public Boolean getBoolean(int col) {
    return BeeUtils.toBoolean(getString(col));
  }

  @Override
  public JustDate getDate(int col) {
    return new JustDate(getInt(col));
  }

  @Override
  public DateTime getDateTime(int col) {
    return new DateTime(getLong(col));
  }

  public BigDecimal getDecimal(int col) {
    return new BigDecimal(getString(col));
  }

  @Override
  public Double getDouble(int col) {
    return BeeUtils.toDouble(getString(col));
  }

  public float getFloat(int col) {
    return BeeUtils.toFloat(getString(col));
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

  @Override
  public String getString(int index) {
    return BeeUtils.ifString(super.getString(index), BeeConst.STRING_EMPTY);
  }

  public long getVersion() {
    return version;
  }

  public boolean isMarkedForDelete() {
    return mode < 0;
  }

  public boolean isMarkedForInsert() {
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

    sb.append(Codec.beeSerialize(getId()));
    sb.append(Codec.beeSerialize(getVersion()));
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

          if (BeeUtils.isEmpty(shadow) && !isMarkedForInsert()) {
            markForDelete(); // TODO: dummy
          }
        }
      }
      super.setValue(col, value);
    }
  }

  public void setVersion(long version) {
    this.version = version;
  }

  private void setShadow(Map<Integer, String> shadow) {
    this.shadow = shadow;
  }
}
