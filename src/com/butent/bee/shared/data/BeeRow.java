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
import java.util.logging.Logger;

/**
 * Extends {@code StringRow} class, handles core row object's requirements like serialization, id
 * and value management.
 */

public class BeeRow extends StringRow implements BeeSerializable {

  /**
   * Contains a list of parameters for row serialization.
   */

  private enum SerializationMembers {
    ID, VERSION, VALUES, NEWID, SHADOW
  }

  private static Logger logger = Logger.getLogger(BeeRow.class.getName());

  static BeeRow restore(String s, int cellCount) {
    BeeRow row = new BeeRow(0, new String[cellCount]);
    row.deserialize(s);
    return row;
  }

  private long version = 0;
  private long newId = 0;
  private Map<Integer, String> shadow = null;

  BeeRow(long id, String[] row) {
    super(id, new StringArray(row));
  }

  public void deserialize(String s) {
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case ID:
          setId(BeeUtils.toLong(value));
          break;

        case VERSION:
          setVersion(BeeUtils.toLong(value));
          break;

        case VALUES:
          setValues(new StringArray(Codec.beeDeserialize(value)));
          break;

        case NEWID:
          setNewId(BeeUtils.toLong(value));
          break;

        case SHADOW:
          if (!BeeUtils.isEmpty(value)) {
            String[] shArr = Codec.beeDeserialize(value);

            if (ArrayUtils.length(shArr) > 1) {
              Map<Integer, String> shMap = new HashMap<Integer, String>(shArr.length / 2);

              for (int j = 0; j < shArr.length; j += 2) {
                shMap.put(BeeUtils.toInt(shArr[j]), shArr[j + 1]);
              }
              setShadow(shMap);
            }
          }
          break;

        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  public BigDecimal getDecimal(int col) {
    return new BigDecimal(getString(col));
  }

  public long getNewId() {
    return newId;
  }

  public Object getOriginal(int col, int sqlType) {
    if (isNull(col)) {
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

  public long getVersion() {
    return version;
  }

  public boolean isMarkedForDelete() {
    return newId < 0;
  }

  public boolean isMarkedForInsert() {
    return getId() < 0;
  }

  public void reset() {
    setShadow(null);
    newId = 0;
  }

  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case ID:
          arr[i++] = getId();
          break;

        case VERSION:
          arr[i++] = getVersion();
          break;

        case VALUES:
          arr[i++] = getValueArray();
          break;

        case NEWID:
          arr[i++] = getNewId();
          break;

        case SHADOW:
          arr[i++] = getShadow();
          break;

        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  public void setNewId(long newId) {
    this.newId = newId;
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
            setNewId(-1); // TODO: dummy for Delete
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
