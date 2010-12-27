package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsFrom;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
      return isExtended() ? getExtName() : BeeTable.this.getName();
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
      return isExtended() ? getExtName() : BeeTable.this.getName();
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
      return isExtended() ? getExtName() : BeeTable.this.getName();
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

  private class ExtSingleTable implements HasExtFields {

    private static final String EXT_TABLE_SUFFIX = "_EXT";

    private final String extIdName = getName() + getIdName();
    private final String extLockName = getName() + getLockName();

    @Override
    public SqlCreate createExtTable(SqlCreate query, BeeField field) {
      SqlCreate sc = null;

      if (BeeUtils.isEmpty(query)) {
        sc = new SqlCreate(getExtName(), false)
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
    public String getExtName() {
      return getName() + EXT_TABLE_SUFFIX;
    }

    @Override
    public IsQuery insertExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
      SqlInsert si = null;

      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(getExtName())
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
      String idField = getIdName();
      String alias = BeeUtils.ifString(tblAlias, tblName);
      String extName = getExtName();

      for (IsFrom from : query.getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && BeeUtils.same((String) src, extName)) {
          if (from.getSqlString(SqlBuilderFactory.getBuilder(), false).contains(alias)) {
            extAlias = BeeUtils.ifString(from.getAlias(), extName);
            break;
          }
        }
      }
      if (BeeUtils.isEmpty(extAlias)) {
        if (BeeUtils.same(alias, tblName)) {
          extAlias = extName;
        } else {
          extAlias = SqlUtils.uniqueName();
        }
        query.addFromLeft(extName, extAlias, SqlUtils.join(alias, idField, extAlias, extIdName));
      }
      return extAlias;
    }

    @Override
    public IsQuery updateExtField(IsQuery query, long rootId, BeeField field, Object newValue) {
      SqlUpdate su = null;

      if (BeeUtils.isEmpty(query)) {
        su = new SqlUpdate(getExtName())
            .addConstant(extLockName, System.currentTimeMillis())
            .setWhere(SqlUtils.equal(getExtName(), extIdName, rootId));
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

    private static final String STATE_TABLE_SUFFIX = "_STATE";

    private BeeTable stateTable;

    private BeeTable createStateTable() {
      if (BeeUtils.isEmpty(stateTable)) {
        String tblName = getName();

        stateTable = new BeeTable(
            tblName + STATE_TABLE_SUFFIX,
            tblName + getIdName(),
            tblName + getLockName());
        stateTable.addField("StateMask", DataTypes.LONG, 0, 0, true, false, null, false);
        stateTable.addForeignKey(stateTable.getIdName(), tblName, Keywords.CASCADE);
      }
      return stateTable;
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
  private HasStates stateSource = new StateSingleTable();

  private boolean active = false;
  private boolean custom = false;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);

    this.extSource = new ExtSingleTable();

    BeeKey key = new BeeKey(KeyTypes.PRIMARY, getIdName());
    keys.put(key.getName(), key);
  }

  @Override
  public SqlCreate createExtTable(SqlCreate query, BeeField field) {
    return extSource.createExtTable(query, field);
  }

  @Override
  public String getExtName() {
    return extSource.getExtName();
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

  BeeState addState(String name, int id, boolean userMode, boolean roleMode, boolean forced) {
    BeeState state = new BeeState(name, id, userMode, roleMode, forced);
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
      addState(state.getName(), state.getId(),
          state.hasUserMode(), state.hasRoleMode(), state.isForced())
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
