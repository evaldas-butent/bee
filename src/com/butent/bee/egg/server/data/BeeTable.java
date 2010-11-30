package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("hiding")
public class BeeTable {

  public class BeeForeignKey {
    private final String name;
    private final String keyField;
    private final String refTable;
    private final String refField;
    private final Keywords action;

    private BeeForeignKey(String keyField, String refTable, String refField, Keywords action) {
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);
      Assert.notEmpty(refField);

      this.name = FOREIGN_KEY_PREFIX + BeeUtils.concat("_", getTable(), foreignKeyCounter++);
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
    private final boolean primary;
    private final boolean unique;

    private BeeKey(String name, boolean primary, boolean unique, String... keyFields) {
      Assert.notEmpty(name);

      this.name = name;
      this.primary = primary;
      this.unique = primary || unique;

      List<String> flds = new ArrayList<String>();

      for (String fld : keyFields) {
        if (!BeeUtils.isEmpty(fld)) {
          flds.add(fld);
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

    public boolean isPrimary() {
      return primary;
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
    private final String relation;
    private final boolean cascade;

    private BeeStructure(String name, DataTypes type, int precision, int scale,
        boolean notNull, boolean unique, String relation, boolean cascade) {
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;
      this.notNull = notNull;
      this.unique = unique;
      this.relation = relation;
      this.cascade = cascade;
    }

    public String getName() {
      return name;
    }

    public int getPrecision() {
      return precision;
    }

    public String getRelation() {
      return relation;
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

    public boolean isCascade() {
      return cascade;
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

  public static final String PRIMARY_KEY_PREFIX = "PK_";
  public static final String FOREIGN_KEY_PREFIX = "FK_";
  public static final String EXT_TABLE_SUFFIX = "_EXT";

  private final String name;
  private final String idName;
  private final String lockName;
  private int foreignKeyCounter = 0;

  private Map<String, BeeStructure> fields = new LinkedHashMap<String, BeeStructure>();
  private List<BeeKey> keys = new ArrayList<BeeKey>();
  private List<BeeForeignKey> foreignKeys = new ArrayList<BeeForeignKey>();
  private BeeTable extTable;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);
  }

  public BeeTable getExtTable() {
    return extTable;
  }

  public BeeStructure getField(String field) {
    return fields.get(field);
  }

  public Collection<BeeStructure> getFields() {
    return Collections.unmodifiableCollection(fields.values());
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    return Collections.unmodifiableCollection(foreignKeys);
  }

  public String getIdName() {
    return idName;
  }

  public Collection<BeeKey> getKeys() {
    return Collections.unmodifiableCollection(keys);
  }

  public String getLockName() {
    return lockName;
  }

  public String getName() {
    return name;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(fields);
  }

  public boolean isField(String field) {
    return fields.containsKey(field);
  }

  BeeTable addField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    Assert.state(!isField(name), "Dublicate field name: " + getName() + " " + name);
    fields.put(name,
        new BeeStructure(name, type, precision, scale, notNull, unique, relation, cascade));
    return this;
  }

  BeeTable addForeignKey(String keyField, String refTable, String refField, Keywords action) {
    foreignKeys.add(new BeeForeignKey(keyField, refTable, refField, action));
    return this;
  }

  void addKey(BeeKey key) {
    String keyName = key.getName();

    for (BeeKey k : keys) {
      Assert.state(!BeeUtils.same(k.getName(), keyName),
          "Dublicate key name: " + getName() + " " + keyName);
    }
    keys.add(key);
  }

  BeeTable addKey(String keyName, String... keyFields) {
    addKey(new BeeKey(keyName, false, false, keyFields));
    return this;
  }

  BeeTable addPrimaryKey(String keyField) {
    Assert.notEmpty(keyField);
    addKey(new BeeKey(PRIMARY_KEY_PREFIX + getName(), true, false, keyField));
    return this;
  }

  BeeTable addUniqueKey(String keyName, String... keyFields) {
    addKey(new BeeKey(keyName, false, true, keyFields));
    return this;
  }

  void setExtTable(BeeTable extTable) {
    this.extTable = extTable;
  }
}
