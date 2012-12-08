package com.butent.bee.shared.data;

import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.math.BigDecimal;

public class CellSource extends AbstractRenderer<IsRow> implements HasPrecision, HasScale,
    HasValueType {

  private enum SourceType {
    COLUMN, PROPERTY, ID, VERSION;
  }

  public static CellSource forColumn(IsColumn column, int index) {
    Assert.notNull(column);
    Assert.nonNegative(index);

    CellSource source = new CellSource(SourceType.COLUMN, column.getId(), index, column.getType());
    source.setPrecision(column.getPrecision());
    source.setScale(column.getScale());

    return source;
  }

  public static CellSource forProperty(String name, ValueType valueType) {
    Assert.notEmpty(name);
    Assert.notNull(valueType);

    return new CellSource(SourceType.PROPERTY, name, null, valueType);
  }

  public static CellSource forRowId(String name) {
    return new CellSource(SourceType.ID, name, DataUtils.ID_INDEX, DataUtils.ID_TYPE);
  }

  public static CellSource forRowVersion(String name) {
    return new CellSource(SourceType.VERSION, name, DataUtils.VERSION_INDEX, DataUtils.VERSION_TYPE);
  }

  private final SourceType sourceType;

  private final String name;
  private final Integer index;

  private final ValueType valueType;

  private int precision = BeeConst.UNDEF;
  private int scale = BeeConst.UNDEF;

  private CellSource(SourceType sourceType, String name, Integer index, ValueType valueType) {
    super();

    this.sourceType = sourceType;
    this.name = name;
    this.index = index;
    this.valueType = valueType;
  }

  public void clear(IsRow row) {
    Assert.notNull(row);

    switch (sourceType) {
      case COLUMN:
        row.clearCell(index);
        break;

      case PROPERTY:
        row.clearProperty(name);
        break;

      case ID:
        row.setId(DataUtils.NEW_ROW_ID);
        break;

      case VERSION:
        row.setVersion(DataUtils.NEW_ROW_VERSION);
        break;
    }
  }

  public Boolean getBoolean(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getBoolean(index);
        case PROPERTY:
          return BeeUtils.toBooleanOrNull(row.getProperty(name));
        case ID:
          return row.getId() != DataUtils.NEW_ROW_ID;
        case VERSION:
          return row.getVersion() != DataUtils.NEW_ROW_VERSION;
      }
    }
    return null;
  }

  public JustDate getDate(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getDate(index);
        case PROPERTY:
          return TimeUtils.toDateOrNull(row.getProperty(name));
        case ID:
          return null;
        case VERSION:
          return new DateTime(row.getVersion()).getDate();
      }
    }
    return null;
  }

  public DateTime getDateTime(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getDateTime(index);
        case PROPERTY:
          return TimeUtils.toDateTimeOrNull(row.getProperty(name));
        case ID:
          return null;
        case VERSION:
          return new DateTime(row.getVersion());
      }
    }
    return null;
  }

  public BigDecimal getDecimal(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getDecimal(index);
        case PROPERTY:
          return BeeUtils.toDecimalOrNull(row.getProperty(name));
        case ID:
          return BigDecimal.valueOf(row.getId());
        case VERSION:
          return BigDecimal.valueOf(row.getVersion());
      }
    }
    return null;
  }

  public Double getDouble(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getDouble(index);
        case PROPERTY:
          return BeeUtils.toDoubleOrNull(row.getProperty(name));
        case ID:
          return (double) row.getId();
        case VERSION:
          return (double) row.getVersion();
      }
    }
    return null;
  }

  public Integer getIndex() {
    return index;
  }

  public Integer getInteger(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getInteger(index);
        case PROPERTY:
          return BeeUtils.toIntOrNull(row.getProperty(name));
        case ID:
          return BeeUtils.isInt(row.getId()) ? (int) row.getId() : null;
        case VERSION:
          return BeeUtils.isInt(row.getVersion()) ? (int) row.getVersion() : null;
      }
    }
    return null;
  }

  public Long getLong(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getLong(index);
        case PROPERTY:
          return BeeUtils.toLongOrNull(row.getProperty(name));
        case ID:
          return row.getId();
        case VERSION:
          return row.getVersion();
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }

  @Override
  public int getPrecision() {
    return precision;
  }

  @Override
  public int getScale() {
    return scale;
  }

  public String getString(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getString(index);
        case PROPERTY:
          return row.getProperty(name);
        case ID:
          return BeeUtils.toString(row.getId());
        case VERSION:
          return BeeUtils.toString(row.getVersion());
      }
    }
    return null;
  }

  public Value getValue(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.getValue(index);
        case PROPERTY:
          String s = row.getProperty(name);
          return BeeUtils.isEmpty(s) ? null : Value.parseValue(valueType, s, false);
        case ID:
          return new LongValue(row.getId());
        case VERSION:
          return new LongValue(row.getVersion());
      }
    }
    return null;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  public boolean hasColumn() {
    return SourceType.COLUMN.equals(sourceType);
  }

  public boolean isEmpty(IsRow row) {
    if (row != null) {
      switch (sourceType) {
        case COLUMN:
          return row.isNull(index);
        case PROPERTY:
          return BeeUtils.isEmpty(row.getProperty(name));
        case ID:
          return row.getId() == DataUtils.NEW_ROW_ID;
        case VERSION:
          return row.getVersion() == DataUtils.NEW_ROW_VERSION;
      }
    }
    return true;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;

    } else if (sourceType == SourceType.ID) {
      return BeeUtils.toString(row.getId());

    } else if (sourceType == SourceType.VERSION) {
      return new DateTime(row.getVersion()).toString();

    } else if (isEmpty(row)) {
      return null;

    } else {

      switch (valueType) {
        case BOOLEAN:
          Boolean bool = getBoolean(row);
          return (bool == null) ? null : BeeUtils.toString(bool);

        case DATE:
          JustDate date = getDate(row);
          return (date == null) ? null : date.toString();

        case DATETIME:
          DateTime dateTime = getDateTime(row);
          return (dateTime == null) ? null : dateTime.toCompactString();

        case DECIMAL:
          BigDecimal decimal = getDecimal(row);
          return (decimal == null) ? null : decimal.toString();

        case INTEGER:
          Integer integer = getInteger(row);
          return (integer == null) ? null : integer.toString();

        case LONG:
          Long longValue = getLong(row);
          return (longValue == null) ? null : longValue.toString();

        case NUMBER:
          Double doubleValue = getDouble(row);
          return (doubleValue == null) ? null : BeeUtils.toString(doubleValue);

        case TEXT:
          return getString(row);

        case TIMEOFDAY:
          return getString(row);
      }

      return null;
    }
  }

  public void set(IsRow row, Integer value) {
    Assert.notNull(row);

    switch (sourceType) {
      case COLUMN:
        row.setValue(index, value);
        break;

      case PROPERTY:
        row.setProperty(name, (value == null) ? null : value.toString());
        break;

      case ID:
        row.setId((value == null) ? DataUtils.NEW_ROW_ID : value.longValue());
        break;

      case VERSION:
        row.setVersion((value == null) ? DataUtils.NEW_ROW_VERSION : value.longValue());
        break;
    }
  }

  public void set(IsRow row, String value) {
    Assert.notNull(row);

    switch (sourceType) {
      case COLUMN:
        row.setValue(index, value);
        break;

      case PROPERTY:
        row.setProperty(name, value);
        break;

      case ID:
        row.setId(BeeUtils.toLong(value));
        break;

      case VERSION:
        row.setVersion(BeeUtils.toLong(value));
        break;
    }
  }

  @Override
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  @Override
  public void setScale(int scale) {
    this.scale = scale;
  }
}
