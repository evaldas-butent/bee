package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public final class AnalysisValue implements BeeSerializable {

  public static AnalysisValue actual(long columnId, long rowId, double value) {
    AnalysisValue av = new AnalysisValue(columnId, rowId);
    av.setActualValue(BeeUtils.toString(value));
    return av;
  }

  public static AnalysisValue budget(long columnId, long rowId, double value) {
    AnalysisValue av = new AnalysisValue(columnId, rowId);
    av.setBudgetValue(BeeUtils.toString(value));
    return av;
  }

  public static AnalysisValue of(long columnId, long rowId, Double actual, Double budget) {
    AnalysisValue av = new AnalysisValue(columnId, rowId);

    if (BeeUtils.isDouble(actual)) {
      av.setActualValue(BeeUtils.toString(actual));
    }
    if (BeeUtils.isDouble(budget)) {
      av.setBudgetValue(BeeUtils.toString(budget));
    }

    return av;
  }

  public static AnalysisValue restore(String s) {
    AnalysisValue av = new AnalysisValue();
    av.deserialize(s);
    return av;
  }

  private enum Serial {
    COLUMN_ID, ROW_ID,
    COLUMN_PARENT_VALUE_INDEX, COLUMN_SPLIT_TYPE_INDEX, COLUMN_SPLIT_VALUE_INDEX,
    ROW_PARENT_VALUE_INDEX, ROW_SPLIT_TYPE_INDEX, ROW_SPLIT_VALUE_INDEX,
    ACTUAL_VALUE, BUDGET_VALUE
  }

  private long columnId;
  private long rowId;

  private Integer columnParentValueIndex;
  private Integer columnSplitTypeIndex;
  private Integer columnSplitValueIndex;

  private Integer rowParentValueIndex;
  private Integer rowSplitTypeIndex;
  private Integer rowSplitValueIndex;

  private String actualValue;
  private String budgetValue;

  private AnalysisValue() {
  }

  private AnalysisValue(long columnId, long rowId) {
    this.columnId = columnId;
    this.rowId = rowId;
  }

  public long getColumnId() {
    return columnId;
  }

  public long getRowId() {
    return rowId;
  }

  public Integer getColumnParentValueIndex() {
    return columnParentValueIndex;
  }

  public Integer getColumnSplitTypeIndex() {
    return columnSplitTypeIndex;
  }

  public Integer getColumnSplitValueIndex() {
    return columnSplitValueIndex;
  }

  public Integer getRowParentValueIndex() {
    return rowParentValueIndex;
  }

  public Integer getRowSplitTypeIndex() {
    return rowSplitTypeIndex;
  }

  public Integer getRowSplitValueIndex() {
    return rowSplitValueIndex;
  }

  public String getActualValue() {
    return actualValue;
  }

  public String getBudgetValue() {
    return budgetValue;
  }

  private void setColumnId(long columnId) {
    this.columnId = columnId;
  }

  private void setRowId(long rowId) {
    this.rowId = rowId;
  }

  public void setColumnParentValueIndex(Integer columnParentValueIndex) {
    this.columnParentValueIndex = columnParentValueIndex;
  }

  public void setColumnSplitTypeIndex(Integer columnSplitTypeIndex) {
    this.columnSplitTypeIndex = columnSplitTypeIndex;
  }

  public void setColumnSplitValueIndex(Integer columnSplitValueIndex) {
    this.columnSplitValueIndex = columnSplitValueIndex;
  }

  public void setRowParentValueIndex(Integer rowParentValueIndex) {
    this.rowParentValueIndex = rowParentValueIndex;
  }

  private void setActualValue(String actualValue) {
    this.actualValue = actualValue;
  }

  private void setBudgetValue(String budgetValue) {
    this.budgetValue = budgetValue;
  }

  public void setRowSplitTypeIndex(Integer rowSplitTypeIndex) {
    this.rowSplitTypeIndex = rowSplitTypeIndex;
  }

  public void setRowSplitValueIndex(Integer rowSplitValueIndex) {
    this.rowSplitValueIndex = rowSplitValueIndex;
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

          case COLUMN_PARENT_VALUE_INDEX:
            setColumnParentValueIndex(BeeUtils.toIntOrNull(v));
            break;
          case COLUMN_SPLIT_TYPE_INDEX:
            setColumnSplitTypeIndex(BeeUtils.toIntOrNull(v));
            break;
          case COLUMN_SPLIT_VALUE_INDEX:
            setColumnSplitValueIndex(BeeUtils.toIntOrNull(v));
            break;

          case ROW_PARENT_VALUE_INDEX:
            setRowParentValueIndex(BeeUtils.toIntOrNull(v));
            break;
          case ROW_SPLIT_TYPE_INDEX:
            setRowSplitTypeIndex(BeeUtils.toIntOrNull(v));
            break;
          case ROW_SPLIT_VALUE_INDEX:
            setRowSplitValueIndex(BeeUtils.toIntOrNull(v));
            break;

          case ACTUAL_VALUE:
            setActualValue(v);
            break;
          case BUDGET_VALUE:
            setBudgetValue(v);
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

        case COLUMN_PARENT_VALUE_INDEX:
          arr[i++] = getColumnParentValueIndex();
          break;
        case COLUMN_SPLIT_TYPE_INDEX:
          arr[i++] = getColumnSplitTypeIndex();
          break;
        case COLUMN_SPLIT_VALUE_INDEX:
          arr[i++] = getColumnSplitValueIndex();
          break;

        case ROW_PARENT_VALUE_INDEX:
          arr[i++] = getRowParentValueIndex();
          break;
        case ROW_SPLIT_TYPE_INDEX:
          arr[i++] = getRowSplitTypeIndex();
          break;
        case ROW_SPLIT_VALUE_INDEX:
          arr[i++] = getRowSplitValueIndex();
          break;

        case ACTUAL_VALUE:
          arr[i++] = getActualValue();
          break;
        case BUDGET_VALUE:
          arr[i++] = getBudgetValue();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }
}
