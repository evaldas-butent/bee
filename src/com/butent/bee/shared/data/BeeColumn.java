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

  public static BeeColumn restore(String s) {
    BeeColumn c = new BeeColumn();
    c.deserialize(s);
    return c;
  }

  private int index = BeeConst.UNDEF;

  private boolean nullable = false;
  private boolean readOnly = false;

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
    setNullable(nillable);
  }

  public BeeColumn(ValueType type, String label, String id) {
    super(type, label, id);
  }

  @Override
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
    result.setPrecision(getPrecision());
    result.setScale(getScale());
    result.setNullable(isNullable());
    result.setReadOnly(isReadOnly());

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
          setNullable(Codec.unpack(value));
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
            setDefaults(Pair.of(NameUtils.getEnumByName(DefaultExpression.class, def[0]),
                (Object) def[1]));
          }
          break;
      }
    }
  }

  public Pair<DefaultExpression, Object> getDefaults() {
    return defaults;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<Property> lst = getInfo();

    PropertyUtils.addProperties(lst,
        "Index", valueAsString(getIndex()),
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

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Id", getId(),
        "Label", getLabel(),
        "Type", getType(),
        "Precision", getPrecision(),
        "Scale", getScale(),
        "Nullable", isNullable(),
        "Read Only", isReadOnly(),
        "Level", getLevel(),
        "Defaults", getDefaults());
  }

  public int getLevel() {
    return level;
  }

  public boolean hasDefaults() {
    if (getDefaults() == null) {
      return false;
    } else {
      return getDefaults().getA() != null || getDefaults().getB() != null;
    }
  }

  public boolean isForeign() {
    return getLevel() > 0;
  }

  public boolean isNullable() {
    return nullable;
  }

  public boolean isReadOnly() {
    return readOnly;
  }
  
  public boolean isText() {
    return ValueType.TEXT.equals(getType()) && getPrecision() <= 0;
  }

  public boolean isWritable() {
    return !isReadOnly() && !isForeign();
  }

  public String serialize() {
    Assert.state(validState());

    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
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
          arr[i++] = Codec.pack(isNullable());
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

  public void setDefaults(Pair<DefaultExpression, Object> defaults) {
    this.defaults = defaults;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
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

  private String valueAsString(int v) {
    if (BeeConst.isUndef(v)) {
      return BeeUtils.concat(1, v, BeeConst.UNKNOWN);
    } else {
      return Integer.toString(v);
    }
  }
}
