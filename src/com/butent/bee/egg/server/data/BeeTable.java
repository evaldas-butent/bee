package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("hiding")
public abstract class BeeTable implements HasExtFields {

  public class BeeField {
    private boolean custom = false;
    private boolean extended = false;
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

    public boolean isCustom() {
      return custom;
    }

    public boolean isExtended() {
      return extended;
    }

    public boolean isNotNull() {
      return notNull;
    }

    public boolean isUnique() {
      return unique;
    }

    protected BeeField setCustom() {
      this.custom = true;
      return this;
    }

    protected BeeField setExtended() {
      this.extended = true;
      return this;
    }
  }

  public class BeeForeignKey {
    private final String name;
    private final String keyField;
    private final String refTable;
    private final Keywords action;

    private BeeForeignKey(String keyField, String refTable, Keywords action) {
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);

      this.name = FOREIGN_KEY_PREFIX + Codec.crc32(getTable() + keyField);
      this.keyField = keyField;
      this.refTable = refTable;
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

    public String getRefTable() {
      return refTable;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }
  }

  public class BeeKey {
    private boolean custom = false;
    private boolean extended = false;
    private final String name;
    private final KeyTypes keyType;
    private final String[] keyFields;

    private BeeKey(KeyTypes keyType, String... keyFields) {
      Assert.notEmpty(keyFields);
      String[] flds = new String[keyFields.length];
      String keyName = getTable();

      for (int i = 0; i < keyFields.length; i++) {
        String fld = keyFields[i];
        Assert.notEmpty(fld);
        flds[i] = fld.trim();
        keyName += flds[i];
      }
      keyName = Codec.crc32(keyName);

      switch (keyType) {
        case PRIMARY:
          keyName = PRIMARY_KEY_PREFIX + keyName;
          break;
        case UNIQUE:
          keyName = UNIQUE_KEY_PREFIX + keyName;
          break;
        case INDEX:
          keyName = INDEX_KEY_PREFIX + keyName;
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

    public boolean isCustom() {
      return custom;
    }

    public boolean isExtended() {
      return extended;
    }

    public boolean isPrimary() {
      return keyType.equals(KeyTypes.PRIMARY);
    }

    public boolean isUnique() {
      return keyType.equals(KeyTypes.UNIQUE);
    }

    protected BeeKey setCustom() {
      this.custom = true;
      return this;
    }

    protected BeeKey setExtended() {
      this.extended = true;
      return this;
    }
  }

  public class BeeState {
    private boolean custom = false;
    private final String name;
    private final int id;
    private final boolean userMode;
    private final boolean roleMode;
    private final boolean forced;

    private BeeState(String name, int id, boolean userMode, boolean roleMode, boolean forced) {
      Assert.notEmpty(name);
      Assert.betweenInclusive(id, 1, 64);

      this.id = id;
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

    public boolean isCustom() {
      return custom;
    }

    public boolean isForced() {
      return forced;
    }

    private void setCustom() {
      this.custom = true;
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

  private static final String STATE_TABLE_SUFFIX = "_STATE";

  private final String name;
  private final String idName;
  private final String lockName;

  private Map<String, BeeField> fields = new LinkedHashMap<String, BeeField>();
  private List<BeeKey> keys = new ArrayList<BeeKey>();
  private List<BeeForeignKey> foreignKeys = new ArrayList<BeeForeignKey>();
  private Map<String, BeeState> states = new LinkedHashMap<String, BeeState>();

  private BeeTable stateTable;

  private boolean active = false;
  private boolean custom = false;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);

    keys.add(new BeeKey(KeyTypes.PRIMARY, getIdName()));
  }

  public BeeField getField(String fldName) {
    BeeField field = fields.get(fldName);

    if (BeeUtils.isEmpty(field)) {
      field = getExtField(fldName);
    }
    return field;
  }

  public Collection<BeeField> getFields() {
    List<BeeField> fldList = new ArrayList<BeeField>();
    fldList.addAll(fields.values());
    fldList.addAll(getExtFields());
    return fldList;
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    List<BeeForeignKey> fKeyList = new ArrayList<BeeForeignKey>();
    fKeyList.addAll(foreignKeys);
    fKeyList.addAll(getExtForeignKeys());
    return fKeyList;
  }

  public String getIdName() {
    return idName;
  }

  public Collection<BeeKey> getKeys() {
    List<BeeKey> keyList = new ArrayList<BeeKey>();
    keyList.addAll(keys);
    keyList.addAll(getExtKeys());
    return keyList;
  }

  public String getLockName() {
    return lockName;
  }

  public Collection<BeeField> getMainFields() {
    List<BeeField> flds = new ArrayList<BeeField>();

    for (BeeField field : getFields()) {
      if (field.isUnique()) {
        flds.add(field);
      }
    }
    return flds;
  }

  public String getName() {
    return name;
  }

  public Collection<BeeState> getStates() {
    return Collections.unmodifiableCollection(states.values());
  }

  public boolean hasField(String fldName) {
    return !BeeUtils.isEmpty(getField(fldName));
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

  public boolean isActive() {
    return active;
  }

  public boolean isCustom() {
    return custom;
  }

  public boolean isEmpty() {
    return !hasFields();
  }

  protected boolean dropField(BeeField field) {
    String fldName = field.getName();
    boolean ok = fields.containsKey(fldName);

    if (ok) {
      for (BeeForeignKey fKey : getForeignKeys()) {
        if (BeeUtils.same(fKey.getKeyField(), fldName)) {
          foreignKeys.remove(fKey);
          break;
        }
      }
      fields.remove(fldName);
    } else {
      ok = removeExtField(field);
    }
    return ok;
  }

  protected boolean dropKey(BeeKey key) {
    boolean ok = keys.remove(key);

    if (!ok) {
      ok = removeExtKey(key);
    }
    return ok;
  }

  void activate() {
    this.active = true;
  }

  BeeField addField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    BeeField field = new BeeField(name, type, precision, scale, notNull, unique, relation, cascade);
    String fieldName = field.getName();

    Assert.state(!hasField(fieldName), "Dublicate field name: " + getName() + " " + fieldName);
    fields.put(fieldName, field);

    if (!BeeUtils.isEmpty(relation)) {
      addForeignKey(name, relation,
          cascade ? (notNull ? Keywords.CASCADE : Keywords.SET_NULL) : null);
    }

    return field;
  }

  BeeForeignKey addForeignKey(String keyField, String refTable, Keywords action) {
    BeeForeignKey key = new BeeForeignKey(keyField, refTable, action);
    foreignKeys.add(key);
    return key;
  }

  BeeKey addKey(boolean unique, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, keyFields);
    keys.add(key);
    return key;
  }

  BeeState addState(String name, int id, boolean userMode, boolean roleMode, boolean forced) {
    BeeState state = new BeeState(name, id, userMode, roleMode, forced);
    String stateName = state.getName();
    int stateId = state.getId();

    for (BeeState st : getStates()) {
      Assert.state(st.getId() != stateId, "Dublicate state ID: " + getName() + " " + stateId);
    }
    Assert.state(!hasState(stateName), "Dublicate state name: " + getName() + " " + stateName);
    states.put(stateName, state);

    BeeTable stateTbl = createStateTable();

    if (state.hasUserMode()) {
      stateTbl.addField("State" + stateId + "UserMask",
          DataTypes.LONG, 0, 0, false, false, null, false);
    }
    if (state.hasRoleMode()) {
      stateTbl.addField("State" + stateId + "RoleMask",
          DataTypes.LONG, 0, 0, false, false, null, false);
    }
    return state;
  }

  int applyChanges(BeeTable extension) {
    dropCustom();
    int cnt = 0;

    for (BeeField fld : extension.getFields()) {
      BeeField f;
      String fName = fld.getName();
      DataTypes type = fld.getType();
      int prec = fld.getPrecision();
      int scale = fld.getScale();
      boolean notNull = fld.isNotNull();
      boolean unique = fld.isUnique();
      String relation = fld.getRelation();
      boolean cascade = fld.isCascade();

      if (fld.isExtended()) {
        f = addExtField(fName, type, prec, scale, notNull, unique, relation, cascade);
      } else {
        f = addField(fName, type, prec, scale, notNull, unique, relation, cascade);
      }
      f.setCustom();
      cnt++;
    }
    for (BeeKey key : extension.getKeys()) {
      if (!key.isPrimary()) {
        BeeKey k;
        boolean unique = key.isUnique();
        String[] keyFlds = key.getKeyFields();

        if (key.isExtended()) {
          k = addExtKey(unique, keyFlds);
        } else {
          k = addKey(unique, keyFlds);
        }
        k.setCustom();
        cnt++;
      }
    }
    for (BeeState state : extension.getStates()) {
      addState(state.getName(), state.getId(),
          state.hasUserMode(), state.hasRoleMode(), state.isForced())
        .setCustom();
      cnt++;
    }
    return cnt;
  }

  BeeTable getStateTable() {
    return stateTable;
  }

  void setCustom() {
    this.custom = true;
  }

  private BeeTable createStateTable() {
    if (BeeUtils.isEmpty(stateTable)) {
      String tblName = getName();

      stateTable = new BeeExtTable(
          tblName + STATE_TABLE_SUFFIX,
          tblName + getIdName(),
          tblName + getLockName());
      stateTable.addField("StateMask", DataTypes.LONG, 0, 0, true, false, null, false);
      stateTable.addForeignKey(stateTable.getIdName(), tblName, Keywords.CASCADE);
    }
    return stateTable;
  }

  private void dropCustom() {
    for (BeeField fld : getFields()) {
      if (fld.isCustom()) {
        dropField(fld);
      }
    }
    for (BeeKey key : getKeys()) {
      if (key.isCustom()) {
        dropKey(key);
      }
    }
    for (BeeState state : getStates()) {
      if (state.isCustom()) {
        dropState(state);
      }
    }
  }

  private void dropState(BeeState state) {
    states.remove(state.getName());
  }
}
