package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public final class AnalysisValue implements BeeSerializable {

  public static AnalysisValue of(long columnId, long rowId, double value) {
    AnalysisValue av = new AnalysisValue();

    av.setColumnId(columnId);
    av.setRowId(rowId);
    av.setValue(BeeUtils.toString(value));

    return av;
  }

  public static AnalysisValue restore(String s) {
    AnalysisValue av = new AnalysisValue();
    av.deserialize(s);
    return av;
  }

  private enum Serial {
    COLUMN_ID, ROW_ID,
    COLUMN_SPLIT_TYPE_INDEX, COLUMN_SPLIT_VALUE_INDEX,
    ROW_SPLIT_TYPE_INDEX, ROW_SPLIT_VALUE_INDEX,
    VALUE
  }

  private long columnId;
  private long rowId;

  private Integer columnSplitTypeIndex;
  private Integer columnSplitValueIndex;

  private Integer rowSplitTypeIndex;
  private Integer rowSplitValueIndex;

  private String value;

  private AnalysisValue() {
  }

  public long getColumnId() {
    return columnId;
  }

  public long getRowId() {
    return rowId;
  }

  public Integer getColumnSplitTypeIndex() {
    return columnSplitTypeIndex;
  }

  public Integer getColumnSplitValueIndex() {
    return columnSplitValueIndex;
  }

  public Integer getRowSplitTypeIndex() {
    return rowSplitTypeIndex;
  }

  public Integer getRowSplitValueIndex() {
    return rowSplitValueIndex;
  }

  public String getValue() {
    return value;
  }

  private void setColumnId(long columnId) {
    this.columnId = columnId;
  }

  private void setRowId(long rowId) {
    this.rowId = rowId;
  }

  public void setColumnSplitTypeIndex(Integer columnSplitTypeIndex) {
    this.columnSplitTypeIndex = columnSplitTypeIndex;
  }

  public void setColumnSplitValueIndex(Integer columnSplitValueIndex) {
    this.columnSplitValueIndex = columnSplitValueIndex;
  }

  public void setRowSplitTypeIndex(Integer rowSplitTypeIndex) {
    this.rowSplitTypeIndex = rowSplitTypeIndex;
  }

  public void setRowSplitValueIndex(Integer rowSplitValueIndex) {
    this.rowSplitValueIndex = rowSplitValueIndex;
  }

  private void setValue(String value) {
    this.value = value;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, Serial.values().length);

    for (int i = 0; i < arr.length; i++) {
      String v = arr[i];

      if (!BeeUtils.isEmpty(v)) {
        switch (Serial.values()[i]) {
          case COLUMN_ID:
            setColumnId(BeeUtils.toLong(v));
            break;
          case ROW_ID:
            setRowId(BeeUtils.toLong(v));
            break;

          case COLUMN_SPLIT_TYPE_INDEX:
            setColumnSplitTypeIndex(BeeUtils.toIntOrNull(v));
            break;
          case COLUMN_SPLIT_VALUE_INDEX:
            setColumnSplitValueIndex(BeeUtils.toIntOrNull(v));
            break;

          case ROW_SPLIT_TYPE_INDEX:
            setRowSplitTypeIndex(BeeUtils.toIntOrNull(v));
            break;
          case ROW_SPLIT_VALUE_INDEX:
            setRowSplitValueIndex(BeeUtils.toIntOrNull(v));
            break;

          case VALUE:
            setValue(v);
            break;
        }
      }
    }
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[Serial.values().length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case COLUMN_ID:
          arr[i++] = getColumnId();
          break;
        case ROW_ID:
          arr[i++] = getRowId();
          break;

        case COLUMN_SPLIT_TYPE_INDEX:
          arr[i++] = getColumnSplitTypeIndex();
          break;
        case COLUMN_SPLIT_VALUE_INDEX:
          arr[i++] = getColumnSplitValueIndex();
          break;

        case ROW_SPLIT_TYPE_INDEX:
          arr[i++] = getRowSplitTypeIndex();
          break;
        case ROW_SPLIT_VALUE_INDEX:
          arr[i++] = getRowSplitValueIndex();
          break;

        case VALUE:
          arr[i++] = getValue();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }
}
