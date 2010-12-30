package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.IsFrom;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlBuilder;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("hiding")
class BeeTable implements HasExtFields, HasStates {

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
      return isExtended() ? getExtTable(getName()) : BeeTable.this.getName();
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

    BeeField setExtended(boolean extended) {
      this.extended = extended;
      return this;
    }

    private BeeField setCustom() {
      this.custom = true;
      return this;
    }
  }

  public class BeeForeignKey {
    private boolean custom = false;
    private boolean extended = false;
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
      return isExtended() ? getExtTable(getKeyField()) : BeeTable.this.getName();
    }

    public boolean isCustom() {
      return custom;
    }

    public boolean isExtended() {
      return extended;
    }

    BeeForeignKey setExtended(boolean extended) {
      this.extended = extended;
      return this;
    }

    private BeeForeignKey setCustom() {
      this.custom = true;
      return this;
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
      return isExtended() ? getExtTable(getKeyFields()[0]) : BeeTable.this.getName();
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

    BeeKey setExtended(boolean extended) {
      this.extended = extended;
      return this;
    }

    private BeeKey setCustom() {
      this.custom = true;
      return this;
    }
  }

  public class BeeState {
    private static final String USER_MODE = "USER";
    private static final String ROLE_MODE = "ROLE";

    private boolean custom = false;
    private final String name;
    private final int id;
    private final String mode;
    private final boolean unchecked;
    private Boolean active = null;

    private BeeState(String name, int id, String mode, boolean unchecked) {
      Assert.notEmpty(name);
      Assert.betweenInclusive(id, 1, 64);

      this.id = id;
      this.name = name;
      this.mode = mode;
      this.unchecked = unchecked;
    }

    public int getId() {
      return id;
    }

    public String getMode() {
      return mode;
    }

    public String getName() {
      return name;
    }

    public String getTable() {
      return BeeTable.this.getName();
    }

    public boolean isActive() {
      return !BeeUtils.isEmpty(active);
    }

    public boolean isCustom() {
      return custom;
    }

    public boolean isInitialized() {
      return active != null;
    }

    public boolean isUnchecked() {
      return unchecked;
    }

    public boolean supportsRoles() {
      return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, ROLE_MODE);
    }

    public boolean supportsUsers() {
      return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, USER_MODE);
    }

    void initialize(boolean active) {
      this.active = active;
    }

    private void setCustom() {
      this.custom = true;
    }
  }

  private class ExtSingleTable implements HasExtFields {

    private final String extIdName = getName() + getIdName();
    private final String extLockName = getName() + getLockName();

    @Override
    public SqlCreate createExtTable(SqlCreate query, BeeField field) {
      SqlCreate sc = null;

      if (BeeUtils.isEmpty(query)) {
        sc = new SqlCreate(getExtTable(field.getName()), false)
            .addLong(extIdName, Keywords.NOT_NULL)
            .addLong(extLockName, Keywords.NOT_NULL);

        addForeignKey(extIdName, getName(), Keywords.CASCADE)
          .setExtended(true).setCustom();
      } else {
        sc = query;
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keywords.NOT_NULL : null);

      return sc;
    }

    @Override
    public String getExtTable(String fldName) {
      Assert.notEmpty(fldName);
      return getName() + "_EXT";
    }

    @Override
    public IsQuery insertExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
      SqlInsert si = null;

      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(getExtTable(field.getName()))
            .addConstant(extLockName, System.currentTimeMillis())
            .addConstant(extIdName, rootId);
      } else {
        si = (SqlInsert) query;
      }
      si.addConstant(field.getName(), newValue);

      return si;
    }

    @Override
    public String joinExtField(HasFrom<?> query, String tblAlias, BeeField field) {
      String extAlias = null;

      String tblName = getName();
      String alias = BeeUtils.ifString(tblAlias, tblName);
      String extTable = getExtTable(field.getName());

      for (IsFrom from : query.getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && BeeUtils.same((String) src, extTable)) {
          SqlBuilder builder = SqlBuilderFactory.getBuilder();
          String strFrom = from.getSqlString(builder, false);
          String strAlias = SqlUtils.field(alias, getIdName()).getSqlString(builder, false);

          if (strFrom.contains(strAlias)) {
            extAlias = BeeUtils.ifString(from.getAlias(), extTable);
            break;
          }
        }
      }
      if (BeeUtils.isEmpty(extAlias)) {
        if (BeeUtils.same(alias, tblName)) {
          extAlias = extTable;
        } else {
          extAlias = SqlUtils.uniqueName();
        }
        query.addFromLeft(extTable, extAlias,
            SqlUtils.join(alias, getIdName(), extAlias, extIdName));
      }
      return extAlias;
    }

    @Override
    public IsQuery updateExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
      SqlUpdate su = null;

      if (BeeUtils.isEmpty(query)) {
        su = new SqlUpdate(getExtTable(field.getName()))
            .addConstant(extLockName, System.currentTimeMillis())
            .setWhere(SqlUtils.equal(getExtTable(field.getName()), extIdName, rootId));
      } else {
        su = (SqlUpdate) query;
      }
      su.addConstant(field.getName(), newValue);

      return su;
    }
  }

  private enum KeyTypes {
    PRIMARY, UNIQUE, INDEX
  }

  private class StateSingleTable implements HasStates {

    @Override
    public void checkState(SqlSelect query, String tblAlias, BeeState state, int user, int... roles) {
      String stateAlias = joinState(query, tblAlias, state);
      IsCondition wh = state.isUnchecked() ? SqlUtils.sqlFalse() : null;

      if (!BeeUtils.isEmpty(stateAlias)) {
        long bitOn = 1;
        int bitCount = 64;

        if (state.supportsUsers()) {
          int pos = (user - 1);
          String colName = "State" + state.getId() + "User" + (int) Math.floor(pos / bitCount);
          pos = pos % bitCount;
          long userMask = bitOn << pos;

          if (state.isUnchecked()) {
            wh = SqlUtils.and(SqlUtils.isNotNull(stateAlias, colName),
                SqlUtils.notEqual(SqlUtils.bitAnd(stateAlias, colName, userMask), 0));
          } else {
            wh = SqlUtils.or(SqlUtils.isNull(stateAlias, colName),
                SqlUtils.equal(SqlUtils.bitAnd(stateAlias, colName, userMask), 0));
          }
        }
        if (state.supportsRoles()) {
          Map<String, Long> roleMasks = new HashMap<String, Long>();

          for (int role : roles) {
            int pos = (role - 1);
            String colName = "State" + state.getId() + "Role" + (int) Math.floor(pos / bitCount);
            pos = pos % bitCount;
            long mask = 0;

            if (roleMasks.containsKey(colName)) {
              mask = roleMasks.get(colName);
            }
            roleMasks.put(colName, mask | (bitOn << pos));
          }
          for (Entry<String, Long> entry : roleMasks.entrySet()) {
            if (state.isUnchecked()) {
              wh = SqlUtils.or(wh,
                  SqlUtils.and(SqlUtils.isNotNull(stateAlias, entry.getKey()),
                      SqlUtils.notEqual(
                          SqlUtils.bitAnd(stateAlias, entry.getKey(), entry.getValue()), 0)));
            } else {
              wh = SqlUtils.and(wh,
                  SqlUtils.or(SqlUtils.isNull(stateAlias, entry.getKey()),
                      SqlUtils.equal(
                          SqlUtils.bitAnd(stateAlias, entry.getKey(), entry.getValue()), 0)));
            }
          }
        }
      }
      query.setWhere(SqlUtils.and(query.getWhere(), wh));
    }

    @Override
    public String getStateTable(String stateName) {
      Assert.notEmpty(stateName);
      return getName() + "_STATE";
    }

    @Override
    public String joinState(HasFrom<?> query, String tblAlias, BeeState state) {
      String stateAlias = null;

      if (state.isActive()) {
        String tblName = getName();
        String alias = BeeUtils.ifString(tblAlias, tblName);
        String stateTable = getStateTable(state.getName());

        for (IsFrom from : query.getFrom()) {
          Object src = from.getSource();

          if (src instanceof String && BeeUtils.same((String) src, stateTable)) {
            SqlBuilder builder = SqlBuilderFactory.getBuilder();
            String strFrom = from.getSqlString(builder, false);
            String strAlias = SqlUtils.field(alias, getIdName()).getSqlString(builder, false);

            if (strFrom.contains(strAlias)) {
              stateAlias = BeeUtils.ifString(from.getAlias(), stateTable);
              break;
            }
          }
        }
        if (BeeUtils.isEmpty(stateAlias)) {
          if (BeeUtils.same(alias, tblName)) {
            stateAlias = stateTable;
          } else {
            stateAlias = SqlUtils.uniqueName();
          }
          query.addFromLeft(stateTable, stateAlias,
              SqlUtils.joinUsing(alias, stateAlias, getIdName()));
        }
      }
      return stateAlias;
    }
  }

  private static final String DEFAULT_ID_FIELD = "ID";
  private static final String DEFAULT_LOCK_FIELD = "Version";

  private static final String PRIMARY_KEY_PREFIX = "PK_";
  private static final String UNIQUE_KEY_PREFIX = "UK_";
  private static final String INDEX_KEY_PREFIX = "IK_";
  private static final String FOREIGN_KEY_PREFIX = "FK_";

  private final String name;
  private final String idName;
  private final String lockName;

  private Map<String, BeeField> fields = new LinkedHashMap<String, BeeField>();
  private Map<String, BeeForeignKey> foreignKeys = new LinkedHashMap<String, BeeForeignKey>();
  private Map<String, BeeKey> keys = new LinkedHashMap<String, BeeKey>();
  private Map<String, BeeState> states = new LinkedHashMap<String, BeeState>();

  private final HasExtFields extSource;
  private HasStates stateSource;

  private boolean active = false;
  private boolean custom = false;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);

    this.extSource = new ExtSingleTable();
    this.stateSource = new StateSingleTable();

    BeeKey key = new BeeKey(KeyTypes.PRIMARY, getIdName());
    keys.put(key.getName(), key);
  }

  @Override
  public void checkState(SqlSelect query, String tblAlias, BeeState state, int user, int... roles) {
    stateSource.checkState(query, tblAlias, state, user, roles);
  }

  @Override
  public SqlCreate createExtTable(SqlCreate query, BeeField field) {
    return extSource.createExtTable(query, field);
  }

  @Override
  public String getExtTable(String fldName) {
    return extSource.getExtTable(fldName);
  }

  public BeeField getField(String fldName) {
    return fields.get(fldName);
  }

  public Collection<BeeField> getFields() {
    return Collections.unmodifiableCollection(fields.values());
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    return Collections.unmodifiableCollection(foreignKeys.values());
  }

  public String getIdName() {
    return idName;
  }

  public Collection<BeeKey> getKeys() {
    return Collections.unmodifiableCollection(keys.values());
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

  public BeeState getState(String stateName) {
    return states.get(stateName);
  }

  public Collection<BeeState> getStates() {
    return Collections.unmodifiableCollection(states.values());
  }

  @Override
  public String getStateTable(String stateName) {
    return stateSource.getStateTable(stateName);
  }

  public boolean hasField(String fldName) {
    return !BeeUtils.isEmpty(getField(fldName));
  }

  public boolean hasFields() {
    return !BeeUtils.isEmpty(getFields());
  }

  public boolean hasState(String stateName) {
    return !BeeUtils.isEmpty(getState(stateName));
  }

  public boolean hasStates() {
    return !BeeUtils.isEmpty(getStates());
  }

  @Override
  public IsQuery insertExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
    return extSource.insertExtField(query, rootId, field, newValue);
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

  @Override
  public String joinExtField(HasFrom<?> query, String tblAlias, BeeField field) {
    return extSource.joinExtField(query, tblAlias, field);
  }

  @Override
  public String joinState(HasFrom<?> query, String tblAlias, BeeState state) {
    return stateSource.joinState(query, tblAlias, state);
  }

  @Override
  public IsQuery updateExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
    return extSource.updateExtField(query, rootId, field, newValue);
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

    return field;
  }

  BeeForeignKey addForeignKey(String keyField, String refTable, Keywords action) {
    BeeForeignKey fKey = new BeeForeignKey(keyField, refTable, action);
    foreignKeys.put(fKey.getName(), fKey);
    return fKey;
  }

  BeeKey addKey(boolean unique, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, keyFields);
    String keyName = key.getName();

    Assert.state(!keys.containsKey(keyName), "Dublicate key name: " + getName() + " " + keyName);
    keys.put(keyName, key);

    return key;
  }

  BeeState addState(String name, int id, String mode, boolean unchecked) {
    BeeState state = new BeeState(name, id, mode, unchecked);
    String stateName = state.getName();
    int stateId = state.getId();

    for (BeeState st : getStates()) {
      Assert.state(st.getId() != stateId, "Dublicate state ID: " + getName() + " " + stateId);
    }
    Assert.state(!hasState(stateName), "Dublicate state name: " + getName() + " " + stateName);
    states.put(stateName, state);

    return state;
  }

  int applyChanges(BeeTable extension) {
    dropCustom();
    int cnt = 0;

    for (BeeField fld : extension.getFields()) {
      String relation = fld.getRelation();
      boolean cascade = fld.isCascade();

      addField(fld.getName()
          , fld.getType()
          , fld.getPrecision()
          , fld.getScale()
          , fld.isNotNull()
          , fld.isUnique()
          , relation, cascade)
        .setExtended(fld.isExtended())
        .setCustom();
      cnt++;
    }
    for (BeeForeignKey fKey : extension.getForeignKeys()) {
      addForeignKey(fKey.getKeyField(), fKey.getRefTable(), fKey.getAction())
        .setExtended(fKey.isExtended())
        .setCustom();
      cnt++;
    }
    for (BeeKey key : extension.getKeys()) {
      if (!key.isPrimary()) {
        addKey(key.isUnique(), key.getKeyFields())
          .setExtended(key.isExtended())
          .setCustom();
        cnt++;
      }
    }
    for (BeeState state : extension.getStates()) {
      addState(state.getName(), state.getId(), state.getMode(), state.isUnchecked())
        .setCustom();
      cnt++;
    }
    return cnt;
  }

  void setCustom() {
    this.custom = true;
  }

  private void dropCustom() {
    for (BeeField field : new ArrayList<BeeField>(getFields())) {
      if (field.isCustom()) {
        fields.remove(field.getName());
      }
    }
    for (BeeKey key : new ArrayList<BeeKey>(getKeys())) {
      if (key.isCustom()) {
        keys.remove(key.getName());
      }
    }
    for (BeeForeignKey fKey : new ArrayList<BeeForeignKey>(getForeignKeys())) {
      if (fKey.isCustom()) {
        foreignKeys.remove(fKey.getName());
      }
    }
    for (BeeState state : new ArrayList<BeeState>(getStates())) {
      if (state.isCustom()) {
        states.remove(state.getName());
      }
    }
  }
}
