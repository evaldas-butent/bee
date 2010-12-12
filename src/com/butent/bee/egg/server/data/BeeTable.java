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

  public class BeeField {
    private final String name;
    private final DataTypes type;
    private final int precision;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;
    private final String relation;
    private final boolean cascade;

    private BeeField(String name, DataTypes type, int precision, int scale,
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
    private final KeyTypes keyType;
    private final String[] keyFields;

    private BeeKey(KeyTypes keyType, String... keyFields) {
      Assert.notEmpty(keyFields);
      String[] flds = new String[keyFields.length];

      for (int i = 0; i < keyFields.length; i++) {
        String fld = keyFields[i];
        Assert.notEmpty(fld);
        flds[i] = fld.trim();
      }
      String keyName = null;

      switch (keyType) {
        case PRIMARY:
          keyName = PRIMARY_KEY_PREFIX + getTable();
          break;
        case UNIQUE:
          keyName = UNIQUE_KEY_PREFIX + BeeUtils.concat("_", getTable(), keyCounter++);
          break;
        case INDEX:
          keyName = INDEX_KEY_PREFIX + BeeUtils.concat("_", getTable(), keyCounter++);
          break;
        default:
          Assert.untouchable();
          break;
      }
      this.name = keyName;
      this.keyType = keyType;
      this.keyFields = flds;
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
      return keyType.equals(KeyTypes.PRIMARY);
    }

    public boolean isUnique() {
      return keyType.equals(KeyTypes.UNIQUE);
    }
  }

  public class BeeState {
    private final int id;
    private final String name;
    private final boolean userMode;
    private final boolean roleMode;
    private final boolean forced;

    private BeeState(String name, boolean userMode, boolean roleMode, boolean forced) {
      Assert.notEmpty(name);

      this.id = stateCounter++;
      this.name = name;
      this.userMode = userMode;
      this.roleMode = roleMode;
      this.forced = forced;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }

    public boolean hasRoleMode() {
      return roleMode;
    }

    public boolean hasUserMode() {
      return userMode;
    }

    public boolean isForced() {
      return forced;
    }
  }

  private enum KeyTypes {
    PRIMARY, UNIQUE, INDEX
  }

  private static final String DEFAULT_ID_FIELD = "ID";
  private static final String DEFAULT_LOCK_FIELD = "Version";

  private static final String PRIMARY_KEY_PREFIX = "PK_";
  private static final String UNIQUE_KEY_PREFIX = "UK_";
  private static final String INDEX_KEY_PREFIX = "IK_";
  private static final String FOREIGN_KEY_PREFIX = "FK_";

  private static final String EXT_TABLE_SUFFIX = "_EXT";
  private static final String STATE_TABLE_SUFFIX = "_STATE";

  private String name;
  private String idName;
  private String lockName;
  private int keyCounter = 0;
  private int foreignKeyCounter = 0;
  private int stateCounter = 0;

  private Map<String, BeeField> fields = new LinkedHashMap<String, BeeField>();
  private List<BeeKey> keys = new ArrayList<BeeKey>();
  private List<BeeForeignKey> foreignKeys = new ArrayList<BeeForeignKey>();
  private Map<String, BeeState> states = new LinkedHashMap<String, BeeState>();
  private boolean custom = false;
  private BeeTable extTable;
  private BeeTable stateTable;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);
  }

  public Collection<BeeField> getAllFields() {
    List<BeeField> flds = new ArrayList<BeeTable.BeeField>();
    flds.addAll(getFields());
    flds.addAll(getExtFields());
    return flds;
  }

  public Collection<BeeField> getExtFields() {
    if (!BeeUtils.isEmpty(getExtTable())) {
      return getExtTable().getFields();
    }
    return new ArrayList<BeeField>();
  }

  public Collection<BeeKey> getExtKeys() {
    if (!BeeUtils.isEmpty(getExtTable())) {
      return getExtTable().getKeys();
    }
    return new ArrayList<BeeKey>();
  }

  public BeeField getField(String field) {
    return fields.get(field);
  }

  public Collection<BeeField> getFields() {
    return Collections.unmodifiableCollection(fields.values());
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    List<BeeForeignKey> fKeys = new ArrayList<BeeForeignKey>();
    fKeys.addAll(foreignKeys);

    if (hasExtFields()) {
      fKeys.addAll(getExtTable().getForeignKeys());
    }
    if (hasStates()) {
      fKeys.addAll(getStateTable().getForeignKeys());
    }
    return fKeys;
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

  public Collection<BeeState> getStates() {
    return Collections.unmodifiableCollection(states.values());
  }

  public boolean hasExtFields() {
    return !BeeUtils.isEmpty(getExtFields());
  }

  public boolean hasField(String field) {
    return fields.containsKey(field);
  }

  public boolean hasFields() {
    return !BeeUtils.isEmpty(getFields());
  }

  public boolean hasState(String state) {
    return states.containsKey(state);
  }

  public boolean hasStates() {
    return !BeeUtils.isEmpty(getStates());
  }

  public boolean isCustom() {
    return custom;
  }

  public boolean isEmpty() {
    return !hasFields() && !hasExtFields();
  }

  BeeTable addExtField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    Assert.state(!hasField(name), "Dublicate field name: " + getName() + " " + name);
    createExtTable();
    extTable.addField(name, type, precision, scale, notNull, unique, relation, cascade);
    return this;
  }

  BeeTable addExtKey(boolean unique, String... keyFields) {
    createExtTable();
    extTable.addKey(unique, keyFields);
    return this;
  }

  BeeTable addField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    BeeField field = new BeeField(name, type, precision, scale, notNull, unique, relation, cascade);
    String fieldName = field.getName();

    Assert.state(!hasField(fieldName), "Dublicate field name: " + getName() + " " + fieldName);
    fields.put(fieldName, field);
    return this;
  }

  BeeTable addForeignKey(String keyField, String refTable, String refField, Keywords action) {
    BeeForeignKey key = new BeeForeignKey(keyField, refTable, refField, action);
    foreignKeys.add(key);
    return this;
  }

  BeeTable addKey(boolean unique, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, keyFields);
    keys.add(key);
    return this;
  }

  BeeTable addPrimaryKey(String... keyFields) {
    BeeKey key = new BeeKey(KeyTypes.PRIMARY, keyFields);
    keys.add(key);
    return this;
  }

  BeeTable addState(String name, boolean userMode, boolean roleMode, boolean forced) {
    BeeState state = new BeeState(name, userMode, roleMode, forced);
    String stateName = state.getName();

    Assert.state(!hasState(stateName), "Dublicate state name: " + getName() + " " + stateName);
    states.put(stateName, state);

    createStateTable();
    int stateId = state.getId();

    if (state.hasUserMode()) {
      stateTable.addField("State" + stateId + "UserMask",
          DataTypes.LONG, 0, 0, false, false, null, false);
    }
    if (state.hasRoleMode()) {
      stateTable.addField("State" + stateId + "RoleMask",
          DataTypes.LONG, 0, 0, false, false, null, false);
    }
    return this;
  }

  BeeTable getExtTable() {
    return extTable;
  }

  BeeTable getStateTable() {
    return stateTable;
  }

  void makeCustom() {
    this.custom = true;
  }

  private void createExtTable() {
    if (BeeUtils.isEmpty(extTable)) {
      extTable = new BeeTable(
          getName() + EXT_TABLE_SUFFIX,
          getName() + getIdName(),
          getName() + getLockName());
      extTable.addForeignKey(extTable.getIdName(), getName(), getIdName(), Keywords.CASCADE);
    }
  }

  private void createStateTable() {
    if (BeeUtils.isEmpty(stateTable)) {
      stateTable = new BeeTable(
          getName() + STATE_TABLE_SUFFIX,
          getName() + getIdName(),
          getName() + getLockName());
      stateTable.addField("StateMask", DataTypes.LONG, 0, 0, true, false, null, false)
        .addForeignKey(stateTable.getIdName(), getName(), getIdName(), Keywords.CASCADE);
    }
  }
}
