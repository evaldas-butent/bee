package com.butent.bee.server.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsFrom;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.Defaults.DefaultExpression;
import com.butent.bee.shared.data.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements database table management - contains parameters for table and it's fields, keys,
 * extensions and methods for operating with the table.
 */

@SuppressWarnings("hiding")
public class BeeTable implements BeeObject, HasExtFields, HasStates, HasTranslations,
    HasExtendedInfo {

  public class BeeField {
    private final String name;
    private final SqlDataType type;
    private final int precision;
    private final int scale;
    private final boolean notNull;
    private final boolean unique;
    private final DefaultExpression defExpr;
    private final Object defValue;
    private final String relation;
    private final SqlKeyword cascade;
    private boolean extended = false;
    private boolean translatable = false;

    private BeeField(String name, SqlDataType type, int precision, int scale,
        boolean notNull, boolean unique, DefaultExpression defExpr, String defValue,
        String relation, SqlKeyword cascade) {
      Assert.notEmpty(name);
      Assert.notNull(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;
      this.notNull = notNull;
      this.unique = unique;
      this.defExpr = defExpr;
      this.relation = relation;
      this.cascade = cascade;

      switch (type) {
        case DATE:
          JustDate date = JustDate.parse(defValue);

          if (date != null) {
            this.defValue = date.getDays();
          } else {
            this.defValue = null;
          }
          break;

        case DATETIME:
          DateTime time = DateTime.parse(defValue);

          if (time != null) {
            this.defValue = time.getTime();
          } else {
            this.defValue = null;
          }
          break;

        default:
          this.defValue = type.parse(defValue);
          break;
      }
    }

    public SqlKeyword getCascade() {
      return cascade;
    }

    public Pair<DefaultExpression, Object> getDefaults() {
      Pair<DefaultExpression, Object> defaults = null;

      if (defExpr != null || defValue != null) {
        defaults = Pair.create(defExpr, defValue);
      }
      return defaults;
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

    public SqlDataType getType() {
      return type;
    }

    public boolean hasEditableRelation() {
      return BeeUtils.allNotEmpty(isUnique(), getRelation()) && BeeUtils.isEmpty(getCascade());
    }

    public boolean isExtended() {
      return extended;
    }

    public boolean isNotNull() {
      return notNull;
    }

    public boolean isTranslatable() {
      return translatable;
    }

    public boolean isUnique() {
      return unique;
    }

    BeeField setExtended(boolean extended) {
      this.extended = extended;
      return this;
    }

    BeeField setTranslatable(boolean translatable) {
      this.translatable = translatable;
      return this;
    }
  }

  public class BeeForeignKey {
    private final String tblName;
    private final String name;
    private final String keyField;
    private final String refTable;
    private final SqlKeyword cascade;

    private BeeForeignKey(String tblName, String keyField, String refTable, SqlKeyword cascade) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(keyField);
      Assert.notEmpty(refTable);

      this.tblName = tblName;
      this.name = FOREIGN_KEY_PREFIX + Codec.crc32(tblName + keyField + refTable + cascade);
      this.keyField = keyField;
      this.refTable = refTable;
      this.cascade = cascade;
    }

    public SqlKeyword getCascade() {
      return cascade;
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
  }

  public class BeeKey {
    private final String tblName;
    private final String name;
    private final KeyTypes keyType;
    private final String[] keyFields;

    private BeeKey(KeyTypes keyType, String tblName, String... keyFields) {
      Assert.notEmpty(tblName);
      Assert.notEmpty(keyFields);

      this.tblName = tblName;
      String keyName = getTable();

      for (String fld : keyFields) {
        keyName += fld;
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
      }
      this.name = keyName;
      this.keyType = keyType;
      this.keyFields = keyFields;
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

    public boolean isPrimary() {
      return keyType.equals(KeyTypes.PRIMARY);
    }

    public boolean isUnique() {
      return keyType.equals(KeyTypes.UNIQUE);
    }
  }

  public class BeeTrigger {
    private final String tblName;
    private final String name;
    private final Object content;
    private final String timing;
    private final String event;
    private final String scope;

    private BeeTrigger(String tblName, Object content, String timing, String event, String scope) {
      this.tblName = tblName;
      this.name =
          TRIGGER_PREFIX
              + Codec.crc32(tblName + BeeUtils.transformCollection((Collection<?>) content)
                  + timing + event + scope);
      this.content = content;
      this.timing = timing;
      this.event = event;
      this.scope = scope;
    }

    public Object getContent() {
      return content;
    }

    public String getEvent() {
      return event;
    }

    public String getName() {
      return name;
    }

    public BeeTable getOwner() {
      return BeeTable.this;
    }

    public String getScope() {
      return scope;
    }

    public String getTable() {
      return tblName;
    }

    public String getTiming() {
      return timing;
    }
  }

  private class ExtSingleTable implements HasExtFields {

    private final String extIdName = getIdName();
    private final String extVersionName = getVersionName();

    @Override
    public SqlCreate createExtTable(SqlCreate query, BeeField field) {
      Assert.state(hasField(field) && field.isExtended());
      SqlCreate sc = query;

      if (BeeUtils.isEmpty(query)) {
        String tblName = field.getTable();

        sc = new SqlCreate(tblName, false)
            .addLong(extIdName, true)
            .addLong(extVersionName, true);

        addKey(true, tblName, extIdName);
        addForeignKey(tblName, extIdName, getName(), SqlKeyword.DELETE);
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
          field.isNotNull());
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
            .addConstant(extVersionName, System.currentTimeMillis())
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

          if (from.getSqlString(builder)
              .endsWith(SqlUtils.join(alias, getIdName(), tmpAlias, extIdName)
                  .getSqlString(builder))) {

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
            .addConstant(extVersionName, System.currentTimeMillis())
            .setWhere(SqlUtils.equal(tblName, extIdName, rootId));
      }
      su.addConstant(field.getName(), newValue);
      return su;
    }
  }

  /**
   * Contains available database key types.
   */

  private enum KeyTypes {
    PRIMARY, UNIQUE, INDEX
  }

  private class StateSingleTable<T extends Number> implements HasStates {

    private final String stateIdName = getIdName();
    private final String stateVersionName = getVersionName();
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
              .addLong(stateIdName, true)
              .addLong(stateVersionName, true);

          addKey(true, tblName, stateIdName);
          addForeignKey(tblName, stateIdName, getName(), SqlKeyword.DELETE);
        }
        for (String col : stateFields.get(state)) {
          if (bitCount <= Integer.SIZE) {
            sc.addInteger(col, false);
          } else {
            sc.addLong(col, false);
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
          .addConstant(stateVersionName, System.currentTimeMillis())
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

            if (from.getSqlString(builder)
                .endsWith(SqlUtils.join(alias, getIdName(), tmpAlias, stateIdName)
                    .getSqlString(builder))) {

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
          if (fld.matches(BeeUtils.concat("_", state.getName(), "[0-9]+", "[0-9]+"))) {
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
          .addConstant(stateVersionName, System.currentTimeMillis())
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
        return BeeUtils.concat("_", state.getName(), to, from); // User field
      } else {
        return BeeUtils.concat("_", state.getName(), from, to); // Role field
      }
    }

    private boolean isStateActive(BeeState state) {
      return stateFields.containsKey(state);
    }
  }

  private class TranslationSingleTable implements HasTranslations {

    private final String translationIdName = getIdName();
    private final String translationVersionName = getVersionName();
    private final String translationLocaleName = "Locale";

    @Override
    public SqlCreate createTranslationTable(SqlCreate query, BeeField field) {
      Assert.state(hasField(field) && field.isTranslatable());
      SqlCreate sc = query;
      String tblName = getTranslationTable(field);

      if (BeeUtils.isEmpty(sc)) {
        sc = new SqlCreate(tblName, false)
            .addLong(translationIdName, true)
            .addLong(translationVersionName, true)
            .addString(translationLocaleName, 2, true);

        addKey(true, tblName, translationIdName, translationLocaleName);
        addForeignKey(tblName, translationIdName, getName(), SqlKeyword.DELETE);
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(), false);

      if (field.isUnique()) {
        addKey(true, tblName, field.getName(), translationLocaleName);
      }
      return sc;
    }

    @Override
    public String getTranslationField(BeeField field, String locale) {
      Assert.state(hasField(field) && field.isTranslatable());
      Assert.notEmpty(locale);
      return field.getName();
    }

    @Override
    public String getTranslationTable(BeeField field) {
      Assert.state(hasField(field) && field.isTranslatable());
      return getName() + "_TRAN";
    }

    @Override
    public SqlInsert insertTranslationField(SqlInsert query, long rootId, BeeField field,
        String locale, Object newValue) {
      Assert.state(hasField(field) && field.isTranslatable());
      Assert.notEmpty(locale);
      SqlInsert si = query;

      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(getTranslationTable(field))
            .addConstant(translationLocaleName, locale)
            .addConstant(translationVersionName, System.currentTimeMillis())
            .addConstant(translationIdName, rootId);
      }
      si.addConstant(getTranslationField(field, locale), newValue);
      return si;
    }

    @Override
    public String joinTranslationField(HasFrom<?> query, String tblAlias, BeeField field,
        String locale) {
      Assert.state(hasField(field) && field.isTranslatable());
      Assert.notEmpty(locale);
      String tranAlias = null;

      String tblName = getName();
      String alias = BeeUtils.ifString(tblAlias, tblName);
      String tranTable = getTranslationTable(field);

      for (IsFrom from : query.getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && BeeUtils.same((String) src, tranTable)) {
          String tmpAlias = BeeUtils.ifString(from.getAlias(), tranTable);
          SqlBuilder builder = SqlBuilderFactory.getBuilder();

          if (from.getSqlString(builder)
              .endsWith(SqlUtils.and(
                  SqlUtils.join(alias, getIdName(), tmpAlias, translationIdName),
                  SqlUtils.equal(tmpAlias, translationLocaleName, locale))
                  .getSqlString(builder))) {

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
            SqlUtils.and(
                SqlUtils.join(alias, getIdName(), tranAlias, translationIdName),
                SqlUtils.equal(tranAlias, translationLocaleName, locale)));
      }
      return tranAlias;
    }

    @Override
    public SqlUpdate updateTranslationField(SqlUpdate query, long rootId, BeeField field,
        String locale, Object newValue) {
      Assert.state(hasField(field) && field.isTranslatable());
      Assert.notEmpty(locale);
      SqlUpdate su = query;

      if (BeeUtils.isEmpty(query)) {
        String tblName = getTranslationTable(field);

        su = new SqlUpdate(tblName)
            .addConstant(translationVersionName, System.currentTimeMillis())
            .setWhere(SqlUtils.and(
                SqlUtils.equal(tblName, translationIdName, rootId),
                SqlUtils.equal(tblName, translationLocaleName, locale)));
      }
      su.addConstant(getTranslationField(field, locale), newValue);
      return su;
    }
  }

  private static final String PRIMARY_KEY_PREFIX = "PK_";
  private static final String UNIQUE_KEY_PREFIX = "UK_";
  private static final String INDEX_KEY_PREFIX = "IK_";
  private static final String FOREIGN_KEY_PREFIX = "FK_";
  private static final String TRIGGER_PREFIX = "TR_";

  private final String moduleName;
  private final String name;
  private final String idName;
  private final String versionName;

  private final Map<String, BeeField> fields = Maps.newLinkedHashMap();
  private final Map<String, BeeForeignKey> foreignKeys = Maps.newLinkedHashMap();
  private final Map<String, BeeKey> keys = Maps.newLinkedHashMap();
  private final Map<String, BeeTrigger> triggers = Maps.newLinkedHashMap();
  private final Set<BeeState> states = Sets.newHashSet();

  private final HasExtFields extSource;
  private final HasStates stateSource;
  private final HasTranslations translationSource;

  private boolean active = false;

  BeeTable(String moduleName, String name, String idName, String versionName) {
    Assert.notEmpty(name);
    Assert.notEmpty(idName);
    Assert.notEmpty(versionName);
    Assert.state(!BeeUtils.same(idName, versionName));

    this.moduleName = moduleName;
    this.name = name;
    this.idName = idName;
    this.versionName = versionName;

    this.extSource = new ExtSingleTable();
    this.stateSource = new StateSingleTable<Long>(Long.SIZE);
    this.translationSource = new TranslationSingleTable();

    BeeKey key = new BeeKey(KeyTypes.PRIMARY, getName(), getIdName());
    keys.put(BeeUtils.normalize(key.getName()), key);
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

  public Map<String, Pair<DefaultExpression, Object>> getDefaults() {
    Map<String, Pair<DefaultExpression, Object>> defaults = null;

    for (BeeField field : getFields()) {
      Pair<DefaultExpression, Object> pair = field.getDefaults();

      if (pair != null) {
        if (defaults == null) {
          defaults = Maps.newHashMap();
        }
        defaults.put(field.getName(), pair);
      }
    }
    return defaults;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    PropertyUtils.addProperties(info, false, "Module Name", getModuleName(), "Name", getName(),
        "Id Name", getIdName(), "Version Name", getVersionName(), "active", isActive());

    info.add(new ExtendedProperty("Fields", BeeUtils.toString(fields.size())));
    int i = 0;
    for (Map.Entry<String, BeeField> entry : fields.entrySet()) {
      String key = BeeUtils.concat(1, "Field", ++i, entry.getKey());
      BeeField field = entry.getValue();

      PropertyUtils.addChildren(info, key, "Name", field.getName(), "Type", field.getType(),
          "Precision", field.getPrecision(), "Scale", field.getScale(),
          "Not Null", field.isNotNull(), "Unique", field.isUnique(),
          "Defaults", field.getDefaults(),
          "Relation", field.getRelation(), "Cascade", field.getCascade(),
          "Extended", field.isExtended(), "Translatable", field.isTranslatable());
    }

    info.add(new ExtendedProperty("Foreign Keys", BeeUtils.toString(foreignKeys.size())));
    i = 0;
    for (Map.Entry<String, BeeForeignKey> entry : foreignKeys.entrySet()) {
      String key = BeeUtils.concat(1, "Foreign Key", ++i, entry.getKey());
      BeeForeignKey fk = entry.getValue();

      PropertyUtils.addChildren(info, key, "Table", fk.getTable(), "Name", fk.getName(),
          "Key Field", fk.getKeyField(), "Ref Table", fk.getRefTable(), "Cascade", fk.getCascade());
    }

    info.add(new ExtendedProperty("Keys", BeeUtils.toString(keys.size())));
    i = 0;
    for (Map.Entry<String, BeeKey> entry : keys.entrySet()) {
      String key = BeeUtils.concat(1, "Key", ++i, entry.getKey());
      BeeKey bk = entry.getValue();

      PropertyUtils.addChildren(info, key, "Table", bk.getTable(), "Name", bk.getName(),
          "Type", bk.keyType);
      String[] keyFields = bk.getKeyFields();
      int cnt = ArrayUtils.length(keyFields);
      for (int k = 0; k < cnt; k++) {
        info.add(new ExtendedProperty(key, BeeUtils.concat(1, "Key Field",
            BeeUtils.progress(k + 1, cnt)), keyFields[k]));
      }
    }

    info.add(new ExtendedProperty("States", BeeUtils.toString(states.size())));
    i = 0;
    for (BeeState state : states) {
      String key = BeeUtils.concat(1, "State", ++i);
      PropertyUtils.addChildren(info, key, "Name", state.getName(),
          "UserMode", state.supportsUsers(),
          "RoleMode", state.supportsRoles(),
          "Checked", state.isChecked());
    }

    info.add(new ExtendedProperty("Triggers", BeeUtils.toString(triggers.size())));
    i = 0;
    for (Map.Entry<String, BeeTrigger> entry : triggers.entrySet()) {
      String key = BeeUtils.concat(1, "Trigger", ++i, entry.getKey());
      BeeTrigger trigger = entry.getValue();

      PropertyUtils.addChildren(info, key, "Table", trigger.getTable(), "Name", trigger.getName(),
          "Timing", trigger.getTiming(), "Event", trigger.getEvent(), "Scope", trigger.getScope(),
          "Content", BeeUtils.transformCollection((Collection<?>) trigger.getContent()));
    }

    return info;
  }

  @Override
  public String getExtTable(BeeField field) {
    return extSource.getExtTable(field);
  }

  public BeeField getField(String fldName) {
    Assert.state(hasField(fldName), BeeUtils.concat(1, "Unknown field name:", getName(), fldName));
    return fields.get(BeeUtils.normalize(fldName));
  }

  public int getFieldCount() {
    return fields.size();
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

  public Collection<BeeField> getMainFields() {
    Collection<BeeField> flds = Lists.newArrayList();

    for (BeeField field : getFields()) {
      if (field.isUnique() || field.isNotNull()) {
        flds.add(field);
      }
    }
    return flds;
  }

  @Override
  public String getModuleName() {
    return moduleName;
  }

  @Override
  public String getName() {
    return name;
  }

  public Collection<BeeState> getStates() {
    return ImmutableSet.copyOf(states);
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

  public Collection<BeeTrigger> getTriggers() {
    return ImmutableList.copyOf(triggers.values());
  }

  public String getVersionName() {
    return versionName;
  }

  public boolean hasField(BeeField field) {
    if (BeeUtils.isEmpty(field) || !hasField(field.getName())) {
      return false;
    }
    return getField(field.getName()) == field;
  }

  public boolean hasField(String fldName) {
    return !BeeUtils.isEmpty(fldName) && fields.containsKey(BeeUtils.normalize(fldName));
  }

  public boolean hasState(BeeState state) {
    return states.contains(state);
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

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getFieldCount());
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
  public SqlUpdate updateTranslationField(SqlUpdate query, long rootId, BeeField field,
      String locale, Object newValue) {
    return translationSource.updateTranslationField(query, rootId, field, locale, newValue);
  }

  @Override
  public SqlSelect verifyState(SqlSelect query, String tblAlias, BeeState state, long... bits) {
    return stateSource.verifyState(query, tblAlias, state, bits);
  }

  BeeField addField(String name, SqlDataType type, int precision, int scale,
      boolean notNull, boolean unique, DefaultExpression defExpr, String defValue,
      String relation, SqlKeyword cascade) {

    BeeField field = new BeeField(name, type, precision, scale, notNull, unique, defExpr, defValue,
        relation, cascade);
    String fieldName = field.getName();

    Assert.state(!hasField(fieldName)
        && !BeeUtils.inListSame(fieldName, getIdName(), getVersionName()),
        BeeUtils.concat(1, "Dublicate field name:", getName(), fieldName));
    fields.put(BeeUtils.normalize(fieldName), field);

    return field;
  }

  BeeForeignKey addForeignKey(String tblName, String keyField, String refTable, SqlKeyword cascade) {
    BeeForeignKey fKey = new BeeForeignKey(tblName, keyField, refTable, cascade);
    foreignKeys.put(BeeUtils.normalize(fKey.getName()), fKey);
    return fKey;
  }

  BeeKey addKey(boolean unique, String tblName, String... keyFields) {
    BeeKey key = new BeeKey(unique ? KeyTypes.UNIQUE : KeyTypes.INDEX, tblName, keyFields);
    keys.put(BeeUtils.normalize(key.getName()), key);
    return key;
  }

  BeeState addState(BeeState state) {
    states.add(state);
    return state;
  }

  BeeTrigger addTrigger(String tblName, Object content, String timing, String event, String scope) {
    BeeTrigger trigger = new BeeTrigger(tblName, content, timing, event, scope);
    triggers.put(BeeUtils.normalize(trigger.getName()), trigger);
    return trigger;
  }

  void setActive(boolean active) {
    this.active = active;
  }
}
