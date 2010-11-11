package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BeeTable {

  public class BeeStructure {
    private final String name;
    private final DataTypes type;
    private final int precission;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;

    private BeeStructure(String name, DataTypes type,
        int precission, int scale, boolean notNull, boolean unique) {

      this.name = name;
      this.type = type;
      this.precission = precission;
      this.scale = scale;
      this.notNull = notNull;
      this.unique = unique;
    }

    public String getName() {
      return name;
    }

    public int getPrecission() {
      return precission;
    }

    public int getScale() {
      return scale;
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

  private Map<String, BeeStructure> fields = new LinkedHashMap<String, BeeTable.BeeStructure>();
  private List<?> keys;
  private List<?> foreignKeys;

  public BeeTable(String name, String idName, String lockName) {
    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);
  }

  public BeeTable addField(String name, DataTypes type,
      int precission, int scale, boolean notNull, boolean unique) {

    fields.put(name,
        new BeeStructure(name, type, precission, scale, notNull, unique));
    return this;
  }

  public Collection<BeeStructure> getFields() {
    return fields.values();
  }

  public String getIdName() {
    return idName;
  }

  public String getLockName() {
    return lockName;
  }

  public String getName() {
    return name;
  }
}
