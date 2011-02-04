package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.shared.sql.BeeConstants.Keywords;
import com.butent.bee.shared.sql.HasFrom;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.IsFrom;
import com.butent.bee.shared.sql.SqlBuilder;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlCreate;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUpdate;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("hiding")
class BeeTable implements HasExtFields, HasStates {

  public class BeeField {
    private boolean custom = false;
    private boolean extended = false;
    private final String tblName;
    private final String name;
    private final DataTypes type;
    private final int precision;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;
    private final String relation;
    private final boolean cascade;

    private BeeField(String tblName, String name, DataTypes type, int precision, int scale,
        boolean notNull, boolean unique, String relation, boolean cascade) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.tblName = tblName;
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

    public String getOwner() {
      return BeeTable.this.getName();
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
      return tblName;
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
    private final String tblName;
    private final String name;
    private final String keyField;
    private final String refTable;
    private final Keywords action;

    private BeeForeignKey(String tblName, String keyField, String refTable, Keywords action) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);

      this.tblName = tblName;
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

    public String getOwner() {
      return BeeTable.this.getName();
    }

    public String getRefTable() {
      return refTable;
    }

    public String getTable() {
      return tblName;
    }

    public boolean isCustom() {
      return custom;
    }

    private BeeForeignKey setCustom() {
      this.custom = true;
      return this;
    }
  }

  public class BeeKey {
    private boolean custom = false;
    private final String tblName;
    private final String name;
    private final KeyTypes keyType;
    private final String[] keyFields;

    private BeeKey(KeyTypes keyType, String tblName, String... keyFields) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(keyFields);

      this.tblName = tblName;

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

    public String getOwner() {
      return BeeTable.this.getName();
    }

    public String getTable() {
      return tblName;
    }

    public boolean isCustom() {
      return custom;
    }

    public boolean isPrimary() {
      return keyType.equals(KeyTypes.PRIMARY);
    }

    public boolean isUnique() {
      return keyType.equals(KeyTypes.UNIQUE);
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
    private final boolean checked;
    private Boolean active = false;

    private BeeState(String name, int id, String mode, boolean checked) {
      Assert.notEmpty(name);
      Assert.betweenInclusive(id, 1, 64);

      this.id = id;
      this.name = name;
      this.mode = mode;
      this.checked = checked;
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

    public String getOwner() {
      return BeeTable.this.getName();
    }

    public String getTable() {
      return getStateTable(getName());
    }

    public boolean isActive() {
      return active;
    }

    public boolean isChecked() {
      return checked;
    }

    public boolean isCustom() {
      return custom;
    }

    public boolean supportsRoles() {
      return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, ROLE_MODE);
    }

    public boolean supportsUsers() {
      return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, USER_MODE);
    }

    void setActive(boolean active) {
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
        String tblName = getExtTable(field.getName());

        sc = new SqlCreate(tblName, false)
            .addLong(extIdName, Keywords.NOT_NULL)
            .addLong(extLockName, Keywords.NOT_NULL);

        addKey(true, tblName, extIdName).setCustom();
        addForeignKey(tblName, extIdName, getName(), Keywords.CASCADE).setCustom();
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
    public SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue) {
      SqlInsert si = null;

      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(getExtTable(field.getName()))
            .addConstant(extLockName, System.currentTimeMillis())
            .addConstant(extIdName, rootId);
      } else {
        si = query;
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
    public SqlUpdate updateExtField(SqlUpdate query, long rootId, BeeField field, Object newValue) {
      SqlUpdate su = null;

      if (BeeUtils.isEmpty(query)) {
        String tblName = getExtTable(field.getName());

        su = new SqlUpdate(tblName)
            .addConstant(extLockName, System.currentTimeMillis())
            .setWhere(SqlUtils.equal(tblName, extIdName, rootId));
      } else {
        su = query;
      }
      su.addConstant(field.getName(), newValue);

      return su;
    }
  }

  private class StateSingleTable implements HasStates {

    private static final int BIT_COUNT = 64;

    @Override
    public IsCondition checkState(String stateAlias, BeeState state, boolean mdRole, int... bits) {
      IsCondition wh = null;
      Map<Integer, Boolean> bitMap = new HashMap<Integer, Boolean>();
      for (int bit : bits) {
        bitMap.put(bit, true);
      }
      Map<String, Long> bitMasks = getMasks(getStateField(state.getName()), mdRole, bitMap);

      for (String fld : bitMasks.keySet()) {
        long mask = bitMasks.get(fld);

        if (state.isChecked()) {
          wh = SqlUtils.and(wh,
              SqlUtils.or(SqlUtils.isNull(stateAlias, fld),
                  SqlUtils.notEqual(SqlUtils.bitAnd(stateAlias, fld, mask), mask)));
        } else {
          wh = SqlUtils.or(wh,
              SqlUtils.and(SqlUtils.isNotNull(stateAlias, fld),
                  SqlUtils.notEqual(SqlUtils.bitAnd(stateAlias, fld, mask), 0)));
        }
      }
      return wh;
    }

    @Override
    public SqlCreate createStateTable(SqlCreate query, BeeState state,
        Collection<Integer> users, Collection<Integer> roles) {
      SqlCreate sc = null;

      if (BeeUtils.isEmpty(query)) {
        String tblName = getStateTable(state.getName());

        sc = new SqlCreate(tblName, false)
            .addLong(getIdName(), Keywords.NOT_NULL);

        addKey(true, tblName, getIdName()).setCustom();
        addForeignKey(tblName, getIdName(), getName(), Keywords.CASCADE).setCustom();
      } else {
        sc = query;
      }
      Set<String> cols = new HashSet<String>();
      String stateField = getStateField(state.getName());

      if (state.supportsUsers()) {
        for (int user : users) {
          cols.add(stateField + "User" + (int) Math.floor((user - 1) / BIT_COUNT));
        }
      }
      if (state.supportsRoles()) {
        for (int role : roles) {
          cols.add(stateField + "Role" + (int) Math.floor((role - 1) / BIT_COUNT));
        }
      }
      for (String col : cols) {
        sc.addLong(col);
      }
      return sc;
    }

    @Override
    public String getStateField(String stateName) {
      Assert.state(hasState(stateName));
      return "State" + getState(stateName).getId();
    }

    @Override
    public String getStateTable(String stateName) {
      Assert.state(hasState(stateName));
      return getName() + "_STATE";
    }

    @Override
    public SqlInsert insertState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits) {
      if (!(mdRole && state.supportsRoles() || state.supportsUsers())) {
        return null;
      }
      Map<String, Long> bitMasks = getMasks(getStateField(state.getName()), mdRole, bits);

      String stateTable = getStateTable(state.getName());

      SqlInsert si = new SqlInsert(stateTable)
        .addConstant(getIdName(), id);

      for (Entry<String, Long> entry : bitMasks.entrySet()) {
        si.addConstant(entry.getKey(), entry.getValue());
      }
      return si;
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

    @Override
    public SqlUpdate updateState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits) {
      if (!(mdRole && state.supportsRoles() || state.supportsUsers())) {
        return null;
      }
      Map<String, Long> bitMasks = getMasks(getStateField(state.getName()), mdRole, bits);

      String stateTable = getStateTable(state.getName());

      SqlUpdate su = new SqlUpdate(stateTable)
        .setWhere(SqlUtils.equal(stateTable, getIdName(), id));

      for (Entry<String, Long> entry : bitMasks.entrySet()) {
        su.addConstant(entry.getKey(), entry.getValue());
      }
      return su;
    }

    @Override
    public void verifyState(SqlSelect query, String tblAlias, BeeState state,
        int user, int... roles) {
      String stateAlias = joinState(query, tblAlias, state);
      IsCondition wh = null;

      if (!BeeUtils.isEmpty(stateAlias)) {
        if (state.supportsUsers()) {
          wh = checkState(stateAlias, state, false, user);
        }
        if (state.supportsRoles()) {
          IsCondition roleWh = checkState(stateAlias, state, true, roles);

          if (BeeUtils.isEmpty(wh)) {
            wh = roleWh;
          } else {
            wh = SqlUtils.or(wh, roleWh);
          }
        }
      } else if (!state.isChecked()) {
        wh = SqlUtils.sqlFalse();
      }
      query.setWhere(SqlUtils.and(query.getWhere(), wh));
    }

    private Map<String, Long> getMasks(String stateFld, boolean mdRole, Map<Integer, Boolean> bits) {
      Map<String, Long> bitMasks = new HashMap<String, Long>();

      for (int bit : bits.keySet()) {
        long bitOn = 1;
        int pos = (bit - 1);
        String colName = stateFld + (mdRole ? "Role" : "User") + (int) Math.floor(pos / BIT_COUNT);
        pos = pos % BIT_COUNT;
        long mask = 0;

        if (bitMasks.containsKey(colName)) {
          mask = bitMasks.get(colName);
        }
        if (bits.get(bit)) {
          mask = mask | (bitOn << pos);
        }
        bitMasks.put(colName, mask);
      }
      return bitMasks;
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

    BeeKey key = new BeeKey(KeyTypes.PRIMARY, getName(), getIdName());
    keys.put(key.getName(), key);
  }

  @Override
  public IsCondition checkState(String stateAlias, BeeState state, boolean mdRole, int... bits) {
    return stateSource.checkState(stateAlias, state, mdRole, bits);
  }

  @Override
  public SqlCreate createExtTable(SqlCreate query, BeeField field) {
    return extSource.createExtTable(query, field);
  }

  @Override
  public SqlCreate createStateTable(SqlCreate query, BeeState state,
      Collection<Integer> users, Collection<Integer> roles) {
    return stateSource.createStateTable(query, state, users, roles);
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

  @Override
  public String getStateField(String stateName) {
    return stateSource.getStateField(stateName);
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
  public SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue) {
    return extSource.insertExtField(query, rootId, field, newValue);
  }

  @Override
  public SqlInsert insertState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits) {
    return stateSource.insertState(id, state, mdRole, bits);
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
  public SqlUpdate updateExtField(SqlUpdate query, long rootId, BeeField field, Object newValue) {
    return extSource.updateExtField(query, rootId, field, newValue);
  }

  @Override
  public SqlUpdate updateState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits) {
    return stateSource.updateState(id, state, mdRole, bits);
  }

  @Override
  public void verifyState(SqlSelect query, String tblAlias, BeeState state, int user, int... roles) {
    stateSource.verifyState(query, tblAlias, state, user, roles);
  }

  BeeField addField(String tblName, String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    BeeField field = new BeeField(tblName,
        name, type, precision, scale, notNull, unique, relation, cascade);
    String fieldName = field.getName();

    Assert.state(!hasField(fieldName), "Dublicate field name: " + getName() + " " + fieldName);
    fields.put(fieldName, field);

    return field;
  }

  BeeForeignKey addForeignKey(String tblName, String keyField, String refTable, Keywords action) {
    BeeForeignKey fKey = new BeeForeignKey(tblName, keyField, refTable, action);
    foreignKeys.put(fKey.getName(), fKey);
    return fKey;
  }

  BeeKey addKey(boolean unique, String tblName, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, tblName, keyFields);
    keys.put(key.getName(), key);
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
      addField(fld.getTable()
          , fld.getName()
          , fld.getType()
          , fld.getPrecision()
          , fld.getScale()
          , fld.isNotNull()
          , fld.isUnique()
          , fld.getRelation()
          , fld.isCascade())
        .setExtended(fld.isExtended())
        .setCustom();
      cnt++;
    }
    for (BeeForeignKey fKey : extension.getForeignKeys()) {
      addForeignKey(fKey.getTable(), fKey.getKeyField(), fKey.getRefTable(), fKey.getAction())
        .setCustom();
      cnt++;
    }
    for (BeeKey key : extension.getKeys()) {
      if (!key.isPrimary()) {
        addKey(key.isUnique(), key.getTable(), key.getKeyFields())
          .setCustom();
        cnt++;
      }
    }
    for (BeeState state : extension.getStates()) {
      addState(state.getName(), state.getId(), state.getMode(), state.isChecked())
        .setCustom();
      cnt++;
    }
    return cnt;
  }

  void setActive(boolean active) {
    this.active = active;
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
