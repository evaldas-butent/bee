package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BeeTable {

  public class BeeKey {
    private final String name;
    private final String[] columns;
    private final boolean unique;

    private BeeKey(String name, boolean unique, String... columns) {
      Assert.notEmpty(name);

      this.name = name;
      this.unique = unique;

      List<String> cols = new ArrayList<String>();

      if (!BeeUtils.isEmpty(columns)) {
        for (String col : columns) {
          if (!BeeUtils.isEmpty(col)) {
            cols.add(col);
          }
        }
      }
      if (BeeUtils.isEmpty(cols)) {
        this.columns = new String[]{name};
      } else {
        this.columns = cols.toArray(new String[0]);
      }
    }

    public String[] getColumns() {
      return columns;
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
  private List<?> foreignKeys;

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

  public BeeTable addKey(String keyName, String... keyColumns) {
    keys.add(new BeeKey(keyName, false, keyColumns));
    return this;
  }

  public BeeTable addUniqueKey(String keyName, String... keyColumns) {
    keys.add(new BeeKey(keyName, true, keyColumns));
    return this;
  }

  public Collection<BeeStructure> getFields() {
    return fields.values();
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
