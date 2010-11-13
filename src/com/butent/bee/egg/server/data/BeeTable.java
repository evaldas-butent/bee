package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BeeTable {

  public class BeeForeignKey {
    private final String name;
    private final String keyField;
    private final String refTable;
    private final String refField;
    private final Keywords action;

    private BeeForeignKey(String keyField,
        String refTable, String refField, Keywords action) {
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);
      Assert.notEmpty(refField);

      this.name = "FK_" + refTable + "_" + refField;
      this.keyField = keyField;
      this.refTable = refTable;
      this.refField = refField;
      this.action = action;
    }

    public Keywords getAction() {
      return action;
    }

    public String getKeyField() {
      return keyField;
    }

    public String getName() {
      return name;
    }

    public String getRefField() {
      return refField;
    }

    public String getRefTable() {
      return refTable;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }
  }

  public class BeeKey {
    private final String name;
    private final String[] keyFields;
    private final boolean unique;

    private BeeKey(String name, boolean unique, String... keyFields) {
      Assert.notEmpty(name);

      this.name = name;
      this.unique = unique;

      List<String> flds = new ArrayList<String>();

      if (!BeeUtils.isEmpty(keyFields)) {
        for (String fld : keyFields) {
          if (!BeeUtils.isEmpty(fld)) {
            flds.add(fld);
          }
        }
      }
      if (BeeUtils.isEmpty(flds)) {
        this.keyFields = new String[]{name};
      } else {
        this.keyFields = flds.toArray(new String[0]);
      }
    }

    public String[] getKeyFields() {
      return keyFields;
    }

    public String getName() {
      return name;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }

    public boolean isUnique() {
      return unique;
    }
  }

  public class BeeStructure {
    private final String name;
    private final DataTypes type;
    private final int precision;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;

    private BeeStructure(String name, DataTypes type,
        int precision, int scale, boolean notNull, boolean unique) {
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;
      this.notNull = notNull;
      this.unique = unique;
    }

    public String getName() {
      return name;
    }

    public int getPrecision() {
      return precision;
    }

    public int getScale() {
      return scale;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }

    public DataTypes getType() {
      return type;
    }

    public boolean isNotNull() {
      return notNull;
    }

    public boolean isUnique() {
      return unique;
    }
  }

  public static final String DEFAULT_ID_FIELD = "ID";
  public static final String DEFAULT_LOCK_FIELD = "Version";

  private final String name;
  private final String idName;
  private final String lockName;

  private Map<String, BeeStructure> fields = new LinkedHashMap<String, BeeStructure>();
  private List<BeeKey> keys = new ArrayList<BeeKey>();
  private List<BeeForeignKey> foreignKeys = new ArrayList<BeeForeignKey>();

  public BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);
  }

  public BeeTable addField(String name, DataTypes type,
      int precision, int scale, boolean notNull, boolean unique) {
    fields.put(name,
        new BeeStructure(name, type, precision, scale, notNull, unique));
    return this;
  }

  public BeeTable addForeignKey(String keyField,
      String refTable, String refField, Keywords action) {
    foreignKeys.add(new BeeForeignKey(keyField, refTable, refField, action));
    return this;
  }

  public BeeTable addKey(String keyName, String... keyFields) {
    keys.add(new BeeKey(keyName, false, keyFields));
    return this;
  }

  public BeeTable addUniqueKey(String keyName, String... keyFields) {
    keys.add(new BeeKey(keyName, true, keyFields));
    return this;
  }

  public BeeStructure getField(String field) {
    return fields.get(field);
  }

  public Collection<BeeStructure> getFields() {
    return fields.values();
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    return foreignKeys;
  }

  public String getIdName() {
    return idName;
  }

  public Collection<BeeKey> getKeys() {
    return keys;
  }

  public String getLockName() {
    return lockName;
  }

  public String getName() {
    return name;
  }
}
