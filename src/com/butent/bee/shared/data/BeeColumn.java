package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.Defaults.DefaultExpression;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Extends {@code TableColumn} class, handles core column object's requirements like serialization,
 * id and parameters management.
 */

public class BeeColumn extends TableColumn implements BeeSerializable, Transformable,
    HasExtendedInfo {

  /**
   * Contains a list of parameters for column serialization.
   */

  private enum Serial {
    ID, LABEL, VALUE_TYPE, PRECISION, SCALE, ISNULL, READ_ONLY, LEVEL, DEFAULTS
  }

  public static final int NO_NULLS = 0;
  public static final int NULLABLE = 1;
  public static final int NULLABLE_UNKNOWN = 2;

  public static BeeColumn restore(String s) {
    BeeColumn c = new BeeColumn();
    c.deserialize(s);
    return c;
  }

  private int index = BeeConst.UNDEF;

  private String schema = null;
  private String catalog = null;
  private String table = null;

  private String clazz = null;
  private int sqlType = 0;
  private String typeName = null;

  private int displaySize = BeeConst.UNDEF;

  private int nullable = NULLABLE_UNKNOWN;

  private boolean signed = false;
  private boolean autoIncrement = false;
  private boolean caseSensitive = false;

  private boolean currency = false;
  private boolean searchable = false;

  private boolean readOnly = false;
  private boolean sqlWritable = false;
  private boolean definitelyWritable = false;

  private int level = 0;
  private Pair<DefaultExpression, Object> defaults = null;

  public BeeColumn() {
    super(ValueType.TEXT);
  }

  public BeeColumn(String id) {
    this(ValueType.TEXT, id, id);
  }

  public BeeColumn(ValueType type, String id) {
    this(type, id, id);
  }

  public BeeColumn(ValueType type, String id, boolean nillable) {
    this(type, id, id);
    setNullable(nillable ? NULLABLE : NO_NULLS);
  }

  public BeeColumn(ValueType type, String label, String id) {
    super(type, label, id);
  }

  public BeeColumn clone() {
    BeeColumn result = new BeeColumn();

    result.setId(getId());
    result.setType(getType());
    result.setLabel(getLabel());
    result.setPattern(getPattern());
    if (getProperties() != null) {
      result.setProperties(getProperties());
    }

    result.setIndex(getIndex());
    result.setSchema(getSchema());
    result.setCatalog(getCatalog());
    result.setTable(getTable());
    result.setClazz(getClazz());
    result.setSqlType(getSqlType());
    result.setTypeName(getTypeName());
    result.setDisplaySize(getDisplaySize());
    result.setPrecision(getPrecision());
    result.setScale(getScale());
    result.setNullable(getNullable());
    result.setSigned(isSigned());
    result.setAutoIncrement(isAutoIncrement());
    result.setCaseSensitive(isCaseSensitive());
    result.setCurrency(isCurrency());
    result.setSearchable(isSearchable());
    result.setReadOnly(isReadOnly());
    result.setSqlWritable(isSqlWritable());
    result.setDefinitelyWritable(isDefinitelyWritable());

    return result;
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case ID:
          setId(value);
          break;
        case LABEL:
          setLabel(value);
          break;
        case VALUE_TYPE:
          setType(ValueType.getByTypeCode(value));
          break;
        case PRECISION:
          setPrecision(BeeUtils.toInt(value));
          break;
        case SCALE:
          setScale(BeeUtils.toInt(value));
          break;
        case ISNULL:
          setNullable(BeeUtils.toInt(value));
          break;
        case READ_ONLY:
          setReadOnly(Codec.unpack(value));
          break;
        case LEVEL:
          setLevel(BeeUtils.toInt(value));
          break;
        case DEFAULTS:
          String[] def = Codec.beeDeserializeCollection(value);

          if (ArrayUtils.length(def) == 2) {
            setDefaults(Pair.create(NameUtils.getConstant(DefaultExpression.class, def[0]),
                (Object) def[1]));
          }
          break;
      }
    }
  }

  public String getCatalog() {
    return catalog;
  }

  public String getClazz() {
    return clazz;
  }

  public Pair<DefaultExpression, Object> getDefaults() {
    return defaults;
  }

  public int getDisplaySize() {
    return displaySize;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<Property> lst = getInfo();

    PropertyUtils.addProperties(lst,
        "Index", valueAsString(getIndex()),
        "Schema", getSchema(),
        "Catalog", getCatalog(),
        "Table", getTable(),
        "Class", getClazz(),
        "Sql Type", getSqlType(),
        "Type Name", getTypeName(),
        "Display Size", valueAsString(getDisplaySize()),
        "Signed", valueAsString(isSigned()),
        "Auto Increment", valueAsString(isAutoIncrement()),
        "Case Sensitive", isCaseSensitive(),
        "Currency", valueAsString(isCurrency()),
        "Searchable", isSearchable(),
        "Sql Writable", isSqlWritable(),
        "Definitely Writable", isDefinitelyWritable(),
        "Pattern", getPattern());

    if (getProperties() != null) {
      lst.addAll(getProperties().getInfo());
    }

    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.appendChildrenToExtended(result, getId(), lst);

    return result;
  }

  public int getIndex() {
    return index;
  }

  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Id", getId(),
        "Label", getLabel(),
        "Type", getType(),
        "Precision", valueAsString(getPrecision()),
        "Scale", valueAsString(getScale()),
        "Nullable", nullableAsString(),
        "Read Only", isReadOnly(),
        "Level", getLevel(),
        "Defaults", getDefaults());
  }

  public int getLevel() {
    return level;
  }

  public int getNullable() {
    return nullable;
  }

  public String getSchema() {
    return schema;
  }

  public int getSqlType() {
    return sqlType;
  }

  public String getTable() {
    return table;
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public boolean isCurrency() {
    return currency;
  }

  public boolean isDefinitelyWritable() {
    return definitelyWritable;
  }

  public boolean isForeign() {
    return getLevel() > 0;
  }

  public boolean isNullable() {
    return getNullable() == NULLABLE;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isSearchable() {
    return searchable;
  }

  public boolean isSigned() {
    return signed;
  }

  public boolean isSqlWritable() {
    return sqlWritable;
  }
  
  public boolean isWritable() {
    return !isReadOnly() && !isForeign();
  }

  public String nullableAsString() {
    switch (getNullable()) {
      case NULLABLE:
        return "nullable";
      case NO_NULLS:
        return "no nulls";
      case NULLABLE_UNKNOWN:
        return "nullable unkown";
      default:
        return BeeConst.UNKNOWN;
    }
  }

  public String serialize() {
    Assert.state(validState());

    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case ID:
          arr[i++] = getId();
          break;
        case LABEL:
          arr[i++] = getLabel();
          break;
        case VALUE_TYPE:
          arr[i++] = (getType() == null) ? null : getType().getTypeCode();
          break;
        case PRECISION:
          arr[i++] = getPrecision();
          break;
        case SCALE:
          arr[i++] = getScale();
          break;
        case ISNULL:
          arr[i++] = getNullable();
          break;
        case READ_ONLY:
          arr[i++] = Codec.pack(isReadOnly());
          break;
        case LEVEL:
          arr[i++] = getLevel();
          break;
        case DEFAULTS:
          arr[i++] = (getDefaults() == null) ? null
              : new Object[] {getDefaults().getA(), getDefaults().getB()};
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public void setCurrency(boolean currency) {
    this.currency = currency;
  }

  public void setDefaults(Pair<DefaultExpression, Object> defaults) {
    this.defaults = defaults;
  }

  public void setDefinitelyWritable(boolean definitelyWritable) {
    this.definitelyWritable = definitelyWritable;
  }

  public void setDisplaySize(int displaySize) {
    this.displaySize = displaySize;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setNullable(int nullable) {
    this.nullable = nullable;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  public void setSigned(boolean signed) {
    this.signed = signed;
  }

  public void setSqlType(int sqlType) {
    this.sqlType = sqlType;
  }

  public void setSqlWritable(boolean sqlWritable) {
    this.sqlWritable = sqlWritable;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    if (validState()) {
      return BeeUtils.transformCollection(getInfo(), BeeConst.DEFAULT_LIST_SEPARATOR);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String transform() {
    return toString();
  }

  private boolean validState() {
    return !BeeUtils.isEmpty(getId());
  }

  private String valueAsString(boolean v) {
    return v ? Boolean.toString(v) : BeeConst.STRING_EMPTY;
  }

  private String valueAsString(int v) {
    if (BeeConst.isUndef(v)) {
      return BeeUtils.concat(1, v, BeeConst.UNKNOWN);
    } else {
      return Integer.toString(v);
    }
  }
}
