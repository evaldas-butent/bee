package com.butent.bee.server.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
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
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("hiding")
class BeeTable implements HasExtFields, HasStates, HasTranslations {

  public class BeeField {
    private boolean custom = false;
    private boolean extended = false;
    private final String name;
    private final DataType type;
    private final int precision;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;
    private final String relation;
    private final boolean cascade;

    private BeeField(String name, DataType type, int precision, int scale,
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

    public BeeTable getOwner() {
      return BeeTable.this;
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
      return isExtended() ? getExtTable(this) : getOwner().getName();
    }

    public DataType getType() {
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
    private final Keyword action;

    private BeeForeignKey(String tblName, String keyField, String refTable, Keyword action) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);

      this.tblName = tblName;
      this.name = FOREIGN_KEY_PREFIX + Codec.crc32(getTable() + keyField);
      this.keyField = keyField;
      this.refTable = refTable;
      this.action = action;
    }

    public Keyword getAction() {
      return action;
    }

    public String getKeyField() {
      return keyField;
    }

    public String getName() {
      return name;
    }

    public BeeTable getOwner() {
      return BeeTable.this;
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

    public BeeTable getOwner() {
      return BeeTable.this;
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

  private enum KeyTypes {
    PRIMARY, UNIQUE, INDEX
  }

  private class ExtSingleTable implements HasExtFields {

    private final String extIdName = getName() + getIdName();
    private final String extLockName = getName() + getLockName();

    @Override
    public SqlCreate createExtTable(SqlCreate query, BeeField field) {
      Assert.state(hasField(field) && field.isExtended());
      SqlCreate sc = query;

      if (BeeUtils.isEmpty(query)) {
        String tblName = field.getTable();

        sc = new SqlCreate(tblName, false)
            .addLong(extIdName, Keyword.NOT_NULL)
            .addLong(extLockName, Keyword.NOT_NULL);

        addKey(true, tblName, extIdName).setCustom();
        addForeignKey(tblName, extIdName, getName(), Keyword.CASCADE).setCustom();
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keyword.NOT_NULL : null);
      return sc;
    }

    @Override
    public String getExtTable(BeeField field) {
      Assert.state(hasField(field) && field.isExtended());
      return getName() + "_EXT";
    }

    @Override
    public SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue) {
      Assert.state(hasField(field) && field.isExtended());
      SqlInsert si = query;

      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(getExtTable(field))
            .addConstant(extLockName, System.currentTimeMillis())
            .addConstant(extIdName, rootId);
      }
      si.addConstant(field.getName(), newValue);
      return si;
    }

    @Override
    public String joinExtField(HasFrom<?> query, String tblAlias, BeeField field) {
      Assert.state(hasField(field) && field.isExtended());
      String extAlias = null;

      String tblName = getName();
      String alias = BeeUtils.ifString(tblAlias, tblName);
      String extTable = field.getTable();

      for (IsFrom from : query.getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && BeeUtils.same((String) src, extTable)) {
          String tmpAlias = BeeUtils.ifString(from.getAlias(), extTable);
          SqlBuilder builder = SqlBuilderFactory.getBuilder();

          if (from.getSqlString(builder, false)
              .endsWith(SqlUtils.join(alias, getIdName(), tmpAlias, extIdName)
                  .getSqlString(builder, false))) {

            extAlias = tmpAlias;
            break;
          }
        }
      }
      if (BeeUtils.isEmpty(extAlias)) {
        if (BeeUtils.isEmpty(tblAlias)) {
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
      Assert.state(hasField(field) && field.isExtended());
      SqlUpdate su = query;

      if (BeeUtils.isEmpty(query)) {
        String tblName = getExtTable(field);

        su = new SqlUpdate(tblName)
            .addConstant(extLockName, System.currentTimeMillis())
            .setWhere(SqlUtils.equal(tblName, extIdName, rootId));
      }
      su.addConstant(field.getName(), newValue);
      return su;
    }
  }

  private class StateSingleTable<T extends Number> implements HasStates {

    private final String stateIdName = getName() + getIdName();
    private final String stateLockName = getName() + getLockName();
    private final int bitCount;
    private final Multimap<BeeState, String> stateFields = HashMultimap.create();

    public StateSingleTable(int size) {
      Assert.isPositive(size);
      bitCount = size;
    }

    @Override
    public IsCondition checkState(String stateAlias, BeeState state, long... bits) {
      Assert.state(hasState(state));
      IsCondition wh = null;
      Map<Long, Boolean> bitMap = Maps.newHashMap();

      for (long bit : bits) {
        bitMap.put(bit, true);
      }
      Map<String, T> bitMasks = getMasks(state, bitMap);

      for (String fld : bitMasks.keySet()) {
        T mask = bitMasks.get(fld);

        if (state.isChecked()) {
          wh = SqlUtils.and(wh,
                  SqlUtils.or(SqlUtils.isNull(stateAlias, fld),
                      SqlUtils.notEqual(SqlUtils.bitAnd(stateAlias, fld, mask), mask)));
        } else {
          wh = SqlUtils.or(wh,
                  SqlUtils.and(SqlUtils.notNull(stateAlias, fld),
                      SqlUtils.notEqual(SqlUtils.bitAnd(stateAlias, fld, mask), 0)));
        }
      }
      if (BeeUtils.isEmpty(wh)) {
        wh = state.isChecked() ? SqlUtils.sqlTrue() : SqlUtils.sqlFalse();
      }
      return wh;
    }

    @Override
    public SqlCreate createStateTable(SqlCreate query, BeeState state) {
      Assert.state(hasState(state));
      SqlCreate sc = query;

      if (isStateActive(state)) {
        if (BeeUtils.isEmpty(sc)) {
          String tblName = getStateTable(state);

          sc = new SqlCreate(tblName, false)
              .addLong(stateIdName, Keyword.NOT_NULL)
              .addLong(stateLockName, Keyword.NOT_NULL);

          addKey(true, tblName, stateIdName).setCustom();
          addForeignKey(tblName, stateIdName, getName(), Keyword.CASCADE).setCustom();
        }
        for (String col : stateFields.get(state)) {
          if (bitCount <= Integer.SIZE) {
            sc.addInt(col);
          } else {
            sc.addLong(col);
          }
        }
      }
      return sc;
    }

    @Override
    public String getStateTable(BeeState state) {
      Assert.state(hasState(state));
      return getName() + "_STATE";
    }

    @Override
    public SqlInsert insertState(long id, BeeState state, Map<Long, Boolean> bits) {
      Assert.state(hasState(state));
      Map<String, T> bitMasks = getMasks(state, bits);

      if (BeeUtils.isEmpty(bitMasks)) {
        return null;
      }
      SqlInsert si = new SqlInsert(getStateTable(state))
          .addConstant(stateLockName, System.currentTimeMillis())
          .addConstant(stateIdName, id);

      for (String bitFld : bitMasks.keySet()) {
        si.addConstant(bitFld, bitMasks.get(bitFld));
      }
      return si;
    }

    @Override
    public String joinState(HasFrom<?> query, String tblAlias, BeeState state) {
      Assert.state(hasState(state));
      String stateAlias = null;

      if (isStateActive(state)) {
        String tblName = getName();
        String alias = BeeUtils.ifString(tblAlias, tblName);
        String stateTable = getStateTable(state);

        for (IsFrom from : query.getFrom()) {
          Object src = from.getSource();

          if (src instanceof String && BeeUtils.same((String) src, stateTable)) {
            String tmpAlias = BeeUtils.ifString(from.getAlias(), stateTable);
            SqlBuilder builder = SqlBuilderFactory.getBuilder();

            if (from.getSqlString(builder, false)
                .endsWith(SqlUtils.join(alias, getIdName(), tmpAlias, stateIdName)
                    .getSqlString(builder, false))) {

              stateAlias = tmpAlias;
              break;
            }
          }
        }
        if (BeeUtils.isEmpty(stateAlias)) {
          if (BeeUtils.isEmpty(tblAlias)) {
            stateAlias = stateTable;
          } else {
            stateAlias = SqlUtils.uniqueName();
          }
          query.addFromLeft(stateTable, stateAlias,
              SqlUtils.join(alias, getIdName(), stateAlias, stateIdName));
        }
      }
      return stateAlias;
    }

    @Override
    public void setStateActive(BeeState state, String... flds) {
      Assert.state(hasState(state));
      Set<String> fldList = Sets.newLinkedHashSet();

      if (!BeeUtils.isEmpty(flds)) {
        for (String fld : flds) {
          if (fld.matches(state.getName() + "\\d+_\\d+")) {
            fldList.add(fld);
          }
        }
      }
      stateFields.replaceValues(state, fldList);
    }

    @Override
    public SqlUpdate updateState(long id, BeeState state, Map<Long, Boolean> bits) {
      Assert.state(hasState(state));
      Map<String, T> bitMasks = getMasks(state, bits);

      if (BeeUtils.isEmpty(bitMasks)) {
        return null;
      }
      String tblName = getStateTable(state);

      SqlUpdate su = new SqlUpdate(tblName)
          .addConstant(stateLockName, System.currentTimeMillis())
          .setWhere(SqlUtils.equal(tblName, stateIdName, id));

      for (String bitFld : bitMasks.keySet()) {
        su.addConstant(bitFld, bitMasks.get(bitFld));
      }
      return su;
    }

    @Override
    public boolean updateStateActive(BeeState state, long... bits) {
      Assert.state(hasState(state));
      Set<String> flds = Sets.newLinkedHashSet();

      for (long bit : bits) {
        if ((bit < 0 && !state.supportsUsers())
            || (bit > 0 && !state.supportsRoles())
            || BeeUtils.isEmpty(bit)) {
          continue;
        }
        flds.add(getStateField(state, bit));
      }
      return stateFields.putAll(state, flds);
    }

    @Override
    public SqlSelect verifyState(SqlSelect query, String tblAlias, BeeState state, long... bits) {
      Assert.state(hasState(state));
      String stateAlias = joinState(query, tblAlias, state);
      IsCondition wh = null;

      if (!BeeUtils.isEmpty(stateAlias)) {
        wh = checkState(stateAlias, state, bits);

      } else if (!state.isChecked()) {
        wh = SqlUtils.sqlFalse();
      }
      return query.setWhere(SqlUtils.and(query.getWhere(), wh));
    }

    @SuppressWarnings("unchecked")
    private Map<String, T> getMasks(BeeState state, Map<Long, Boolean> bits) {
      Map<String, T> bitMasks = Maps.newHashMap();

      for (long bit : bits.keySet()) {
        if ((bit < 0 && !state.supportsUsers())
            || (bit > 0 && !state.supportsRoles())
            || BeeUtils.isEmpty(bit)) {
          continue;
        }
        String fld = getStateField(state, bit);

        if (isStateActive(state) && stateFields.containsEntry(state, fld)) {
          long pos = (Math.abs(bit) - 1) % bitCount;
          Long mask = 0L;

          if (bitMasks.containsKey(fld)) {
            mask = bitMasks.get(fld).longValue();
          }
          if (bits.get(bit)) {
            mask = mask | (1L << pos);
          }
          bitMasks.put(fld, (T) mask);
        }
      }
      return bitMasks;
    }

    private String getStateField(BeeState state, long bit) {
      long from = ((long) Math.floor((Math.abs(bit) - 1) / bitCount)) * bitCount + 1;
      long to = from + bitCount - 1;

      if (bit < 0) {
        return state.getName() + to + "_" + from; // User field
      } else {
        return state.getName() + from + "_" + to; // Role field
      }
    }

    private boolean isStateActive(BeeState state) {
      return stateFields.containsKey(state);
    }
  }

  private class TranslationSingleTable implements HasTranslations {

    private final String translationIdName = getName() + getIdName();
    private final String translationLockName = getName() + getLockName();
    private final String translationLocaleName = "Locale";
    private final Set<BeeField> translationFields = Sets.newHashSet();

    @Override
    public SqlCreate createTranslationTable(SqlCreate query, BeeField field) {
      Assert.state(hasField(field));
      SqlCreate sc = query;

      if (isTranslationActive(field)) {
        if (BeeUtils.isEmpty(sc)) {
          String tblName = getTranslationTable(field);

          sc = new SqlCreate(tblName, false)
              .addLong(translationIdName, Keyword.NOT_NULL)
              .addLong(translationLockName, Keyword.NOT_NULL)
              .addString(translationLocaleName, 2, Keyword.NOT_NULL);

          addKey(true, tblName, translationIdName, translationLocaleName).setCustom();
          addForeignKey(tblName, translationIdName, getName(), Keyword.CASCADE).setCustom();
        }
        sc.addField(field.getName(), field.getType(), field.getPrecision(),
            field.getScale(), field.isNotNull() ? Keyword.NOT_NULL : null);
      }
      return sc;
    }

    @Override
    public String getTranslationField(BeeField field, String locale) {
      Assert.state(hasField(field));
      Assert.notEmpty(locale);
      return field.getName();
    }

    @Override
    public String getTranslationTable(BeeField field) {
      Assert.state(hasField(field));
      return getName() + "_TRAN";
    }

    @Override
    public SqlInsert insertTranslationField(SqlInsert query, long rootId, BeeField field,
        String locale, Object newValue) {
      Assert.state(hasField(field));
      Assert.notEmpty(locale);
      SqlInsert si = query;

      if (isTranslationActive(field)) {
        if (BeeUtils.isEmpty(query)) {
          si = new SqlInsert(getTranslationTable(field))
              .addConstant(translationLocaleName, locale)
              .addConstant(translationLockName, System.currentTimeMillis())
              .addConstant(translationIdName, rootId);
        }
        si.addConstant(getTranslationField(field, locale), newValue);
      }
      return si;
    }

    @Override
    public String joinTranslationField(HasFrom<?> query, String tblAlias, BeeField field,
        String locale) {
      Assert.state(hasField(field));
      Assert.notEmpty(locale);
      String tranAlias = null;

      if (isTranslationActive(field)) {
        String tblName = getName();
        String alias = BeeUtils.ifString(tblAlias, tblName);
        String tranTable = getTranslationTable(field);

        for (IsFrom from : query.getFrom()) {
          Object src = from.getSource();

          if (src instanceof String && BeeUtils.same((String) src, tranTable)) {
            String tmpAlias = BeeUtils.ifString(from.getAlias(), tranTable);
            SqlBuilder builder = SqlBuilderFactory.getBuilder();

            if (from.getSqlString(builder, false)
                .endsWith(SqlUtils.and(
                    SqlUtils.join(alias, getIdName(), tmpAlias, translationIdName),
                    SqlUtils.equal(tmpAlias, translationLocaleName, locale))
                    .getSqlString(builder, false))) {

              tranAlias = tmpAlias;
              break;
            }
          }
        }
        if (BeeUtils.isEmpty(tranAlias)) {
          if (BeeUtils.isEmpty(tblAlias)) {
            tranAlias = tranTable;
          } else {
            tranAlias = SqlUtils.uniqueName();
          }
          query.addFromLeft(tranTable, tranAlias,
              SqlUtils.and(SqlUtils.join(alias, getIdName(), tranAlias, translationIdName),
                  SqlUtils.equal(tranAlias, translationLocaleName, locale)));
        }
      }
      return tranAlias;
    }

    @Override
    public void setTranslationActive(BeeField field, String... flds) {
      Assert.state(hasField(field));

      if (ArrayUtils.contains(field.getName(), flds)) {
        translationFields.add(field);
      } else {
        translationFields.remove(field);
      }
    }

    @Override
    public boolean updateTranslationActive(BeeField field, String locale) {
      Assert.state(hasField(field));
      Assert.notEmpty(locale);

      return translationFields.add(field);
    }

    @Override
    public SqlUpdate updateTranslationField(SqlUpdate query, long rootId, BeeField field,
        String locale, Object newValue) {
      Assert.state(hasField(field));
      Assert.notEmpty(locale);
      SqlUpdate su = query;

      if (isTranslationActive(field)) {
        if (BeeUtils.isEmpty(query)) {
          String tblName = getTranslationTable(field);

          su = new SqlUpdate(tblName)
              .addConstant(translationLockName, System.currentTimeMillis())
              .setWhere(SqlUtils.and(
                  SqlUtils.equal(tblName, translationIdName, rootId),
                  SqlUtils.equal(tblName, translationLocaleName, locale)));
        }
        su.addConstant(getTranslationField(field, locale), newValue);
      }
      return su;
    }

    private boolean isTranslationActive(BeeField field) {
      return translationFields.contains(field);
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

  private final Map<String, BeeField> fields = Maps.newLinkedHashMap();
  private final Map<String, BeeForeignKey> foreignKeys = Maps.newLinkedHashMap();
  private final Map<String, BeeKey> keys = Maps.newLinkedHashMap();
  private final Map<BeeState, Boolean> states = Maps.newLinkedHashMap();

  private final HasExtFields extSource;
  private final HasStates stateSource;
  private final HasTranslations translationSource;

  private boolean active = false;
  private boolean custom = false;

  BeeTable(String name, String idName, String lockName) {
    Assert.notEmpty(name);

    this.name = name;
    this.idName = BeeUtils.ifString(idName, DEFAULT_ID_FIELD);
    this.lockName = BeeUtils.ifString(lockName, DEFAULT_LOCK_FIELD);

    this.extSource = new ExtSingleTable();
    this.stateSource = new StateSingleTable<Long>(Long.SIZE);
    this.translationSource = new TranslationSingleTable();

    BeeKey key = new BeeKey(KeyTypes.PRIMARY, getName(), getIdName());
    keys.put(key.getName(), key);
  }

  @Override
  public IsCondition checkState(String stateAlias, BeeState state, long... bits) {
    return stateSource.checkState(stateAlias, state, bits);
  }

  @Override
  public SqlCreate createExtTable(SqlCreate query, BeeField field) {
    return extSource.createExtTable(query, field);
  }

  @Override
  public SqlCreate createStateTable(SqlCreate query, BeeState state) {
    return stateSource.createStateTable(query, state);
  }

  @Override
  public SqlCreate createTranslationTable(SqlCreate query, BeeField field) {
    return translationSource.createTranslationTable(query, field);
  }

  @Override
  public String getExtTable(BeeField field) {
    return extSource.getExtTable(field);
  }

  public BeeField getField(String fldName) {
    Assert.state(hasField(fldName), BeeUtils.concat(1, "Unknown field name:", getName(), fldName));
    return fields.get(fldName);
  }

  public Collection<BeeField> getFields() {
    return ImmutableList.copyOf(fields.values());
  }

  public Collection<BeeForeignKey> getForeignKeys() {
    return ImmutableList.copyOf(foreignKeys.values());
  }

  public String getIdName() {
    return idName;
  }

  public Collection<BeeKey> getKeys() {
    return ImmutableList.copyOf(keys.values());
  }

  public String getLockName() {
    return lockName;
  }

  public Collection<BeeField> getMainFields() {
    Collection<BeeField> flds = Lists.newArrayList();

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
    return ImmutableList.copyOf(states.keySet());
  }

  @Override
  public String getStateTable(BeeState state) {
    return stateSource.getStateTable(state);
  }

  @Override
  public String getTranslationField(BeeField field, String locale) {
    return translationSource.getTranslationField(field, locale);
  }

  @Override
  public String getTranslationTable(BeeField field) {
    return translationSource.getTranslationTable(field);
  }

  public boolean hasField(String fldName) {
    return fields.containsKey(fldName);
  }

  public boolean hasField(BeeField field) {
    if (BeeUtils.isEmpty(field)) {
      return false;
    }
    return fields.get(field.getName()) == field;
  }

  public boolean hasState(BeeState state) {
    return states.containsKey(state);
  }

  @Override
  public SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue) {
    return extSource.insertExtField(query, rootId, field, newValue);
  }

  @Override
  public SqlInsert insertState(long id, BeeState state, Map<Long, Boolean> bits) {
    return stateSource.insertState(id, state, bits);
  }

  @Override
  public SqlInsert insertTranslationField(SqlInsert query, long rootId, BeeField field,
      String locale, Object newValue) {
    return translationSource.insertTranslationField(query, rootId, field, locale, newValue);
  }

  public boolean isActive() {
    return active;
  }

  public boolean isCustom() {
    return custom;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getFields());
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
  public String joinTranslationField(HasFrom<?> query, String tblAlias, BeeField field,
      String locale) {
    return translationSource.joinTranslationField(query, tblAlias, field, locale);
  }

  @Override
  public void setStateActive(BeeState state, String... flds) {
    stateSource.setStateActive(state, flds);
  }

  @Override
  public void setTranslationActive(BeeField field, String... flds) {
    translationSource.setTranslationActive(field, flds);
  }

  @Override
  public SqlUpdate updateExtField(SqlUpdate query, long rootId, BeeField field, Object newValue) {
    return extSource.updateExtField(query, rootId, field, newValue);
  }

  @Override
  public SqlUpdate updateState(long id, BeeState state, Map<Long, Boolean> bits) {
    return stateSource.updateState(id, state, bits);
  }

  @Override
  public boolean updateStateActive(BeeState state, long... bits) {
    return stateSource.updateStateActive(state, bits);
  }

  @Override
  public boolean updateTranslationActive(BeeField field, String locale) {
    return translationSource.updateTranslationActive(field, locale);
  }

  @Override
  public SqlUpdate updateTranslationField(SqlUpdate query, long rootId, BeeField field,
      String locale, Object newValue) {
    return translationSource.updateTranslationField(query, rootId, field, locale, newValue);
  }

  @Override
  public SqlSelect verifyState(SqlSelect query, String tblAlias, BeeState state, long... bits) {
    return stateSource.verifyState(query, tblAlias, state, bits);
  }

  BeeField addField(String name, DataType type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    BeeField field = new BeeField(name, type, precision, scale, notNull, unique, relation, cascade);
    String fieldName = field.getName();

    Assert.state(!hasField(fieldName),
        BeeUtils.concat(1, "Dublicate field name:", getName(), fieldName));
    fields.put(fieldName, field);

    return field;
  }

  BeeForeignKey addForeignKey(String tblName, String keyField, String refTable, Keyword action) {
    BeeForeignKey fKey = new BeeForeignKey(tblName, keyField, refTable, action);
    foreignKeys.put(fKey.getName(), fKey);
    return fKey;
  }

  BeeKey addKey(boolean unique, String tblName, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, tblName, keyFields);
    keys.put(key.getName(), key);
    return key;
  }

  BeeState addState(BeeState state) {
    return addState(state, false);
  }

  int applyChanges(BeeTable extension) {
    dropCustom();
    int cnt = 0;

    for (BeeField fld : extension.getFields()) {
      addField(fld.getName()
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
      addState(state, true);
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

  private BeeState addState(BeeState state, boolean custom) {
    Assert.state(!hasState(state),
        BeeUtils.concat(1, "Dublicate state:", getName(), state.getName()));
    states.put(state, custom);
    return state;
  }

  private void dropCustom() {
    for (BeeField field : Lists.newArrayList(getFields())) {
      if (field.isCustom()) {
        fields.remove(field.getName());
      }
    }
    for (BeeKey key : Lists.newArrayList(getKeys())) {
      if (key.isCustom()) {
        keys.remove(key.getName());
      }
    }
    for (BeeForeignKey fKey : Lists.newArrayList(getForeignKeys())) {
      if (fKey.isCustom()) {
        foreignKeys.remove(fKey.getName());
      }
    }
    for (BeeForeignKey fKey : Lists.newArrayList(getForeignKeys())) {
      if (fKey.isCustom()) {
        foreignKeys.remove(fKey.getName());
      }
    }

    for (BeeState state : Lists.newArrayList(getStates())) {
      if (states.get(state)) { // isCustom
        states.remove(state);
      }
    }
  }
}
