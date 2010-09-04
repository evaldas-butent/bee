package com.butent.bee.egg.shared;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.shared.lang.StringUtils;

import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;

public class BeeColumn implements Transformable, BeeSerializable {
  public static final char SERIALIZATION_SEPARATOR = ',';

  public static final int NO_NULLS = 0;
  public static final int NULLABLE = 1;
  public static final int NULLABLE_UNKNOWN = 2;

  private int idx = BeeConst.INDEX_UNKNOWN;
  private String name = null;

  private String schema = null;
  private String catalog = null;
  private String table = null;

  private String clazz = null;
  private int type = 0;
  private String typeName = null;

  private String label = null;
  private int displaySize = BeeConst.SIZE_UNKNOWN;

  private int precision = BeeConst.SIZE_UNKNOWN;
  private int scale = BeeConst.SIZE_UNKNOWN;

  private int nullable = NULLABLE_UNKNOWN;

  private boolean signed = false;
  private boolean autoIncrement = false;
  private boolean caseSensitive = false;
  private boolean currency = false;

  private boolean definitelyWritable = false;
  private boolean readOnly = false;
  private boolean searchable = false;
  private boolean writable = false;

  public BeeColumn() {
    super();
  }

  public BeeColumn(String name) {
    this();
    setName(name);
  }

  public int getIdx() {
    return idx;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getDisplaySize() {
    return displaySize;
  }

  public void setDisplaySize(int displaySize) {
    this.displaySize = displaySize;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public int getNullable() {
    return nullable;
  }

  public void setNullable(int nullable) {
    this.nullable = nullable;
  }

  public boolean isSigned() {
    return signed;
  }

  public void setSigned(boolean signed) {
    this.signed = signed;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public boolean isCurrency() {
    return currency;
  }

  public void setCurrency(boolean currency) {
    this.currency = currency;
  }

  public boolean isDefinitelyWritable() {
    return definitelyWritable;
  }

  public void setDefinitelyWritable(boolean definitelyWritable) {
    this.definitelyWritable = definitelyWritable;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isSearchable() {
    return searchable;
  }

  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  public boolean isWritable() {
    return writable;
  }

  public void setWritable(boolean writable) {
    this.writable = writable;
  }

  public String nullableAsString() {
    switch (getNullable()) {
    case (NULLABLE):
      return "nullable";
    case (NO_NULLS):
      return "no nulls";
    case (NULLABLE_UNKNOWN):
      return "nullable unkown";
    default:
      return BeeConst.UNKNOWN;
    }
  }

  private String valueAsString(int v) {
    if (v == BeeConst.INDEX_UNKNOWN || v == BeeConst.SIZE_UNKNOWN
        || v == BeeConst.TIME_UNKNOWN)
      return BeeUtils.concat(1, v, BeeConst.UNKNOWN);
    else
      return Integer.toString(v);
  }

  private String valueAsString(boolean v) {
    return v ? Boolean.toString(v) : BeeConst.STRING_EMPTY;
  }

  public List<StringProp> getColumnInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "index", valueAsString(getIdx()), "name",
        getName(), "schema", getSchema(), "catalog", getCatalog(), "table",
        getTable(), "class", getClazz(), "type", getType(), "type name",
        getTypeName(), "label", getLabel(), "display size",
        valueAsString(getDisplaySize()), "precision",
        valueAsString(getPrecision()), "scale", valueAsString(getScale()),
        "nullable", BeeUtils.concat(1, getNullable(), nullableAsString()),
        "signed", valueAsString(isSigned()), "auto increment",
        valueAsString(isAutoIncrement()), "case sensitive", isCaseSensitive(),
        "currency", valueAsString(isCurrency()), "searchable", isSearchable(),
        "read only", isReadOnly(), "writable", isWritable(),
        "definitely writable", isDefinitelyWritable());

    return lst;
  }

  @Override
  public String toString() {
    if (validState())
      return BeeUtils.transformCollection(getColumnInfo(),
          BeeConst.DEFAULT_LIST_SEPARATOR);
    else
      return BeeConst.STRING_EMPTY;
  }

  public String transform() {
    return toString();
  }

  private boolean validState() {
    return !BeeUtils.isEmpty(getName());
  }

  public String serialize() {
    Assert.state(validState());

    return getName() + SERIALIZATION_SEPARATOR + getType()
        + SERIALIZATION_SEPARATOR + getPrecision() + SERIALIZATION_SEPARATOR
        + getScale() + SERIALIZATION_SEPARATOR + getNullable();
  }

  public void deserialize(String s) {
    Assert.notEmpty(s);

    String[] arr = StringUtils.split(s, SERIALIZATION_SEPARATOR);
    Assert.arrayLength(arr, 5);

    int i = 0;

    setName(arr[i++]);
    setType(BeeUtils.toInt(arr[i++]));
    setPrecision(BeeUtils.toInt(arr[i++]));
    setScale(BeeUtils.toInt(arr[i++]));
    setNullable(BeeUtils.toInt(arr[i++]));
  }

}
