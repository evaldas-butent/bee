package com.butent.bee.client.render;

import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

public abstract class AbstractCellRenderer extends AbstractRenderer<IsRow> {

  private final int dataIndex;
  private final IsColumn dataColumn;
  
  private final ValueType dataType;

  public AbstractCellRenderer() {
    this(ValueType.TEXT);
  }

  public AbstractCellRenderer(int dataIndex, IsColumn dataColumn) {
    this(dataIndex, dataColumn, (dataColumn == null) ? null : dataColumn.getType());
  }
  
  public AbstractCellRenderer(int dataIndex, ValueType dataType) {
    this(dataIndex, null, dataType);
  }

  public AbstractCellRenderer(ValueType dataType) {
    this(BeeConst.UNDEF, null, dataType);
  }
  
  private AbstractCellRenderer(int dataIndex, IsColumn dataColumn, ValueType dataType) {
    super();
    this.dataIndex = dataIndex;
    this.dataColumn = dataColumn;
    this.dataType = dataType;
  }

  public IsColumn getDataColumn() {
    return dataColumn;
  }

  public int getDataIndex() {
    return dataIndex;
  }
  
  public ValueType getDataType() {
    return dataType;
  }

  protected Integer getInteger(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return row.getInteger(dataIndex);
    }
  }
  
  protected String getString(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return row.getString(dataIndex);
    }
  }
  
  protected Value getValue(IsRow row) {
    if (row == null || row.isNull(dataIndex)) {
      return null;
    } else {
      return row.getValue(dataIndex, dataType);
    }
  }

  protected Value parse(String value) {
    return parse(value, true);
  }
  
  protected Value parse(String value, boolean parseDates) {
    if (value == null || value.isEmpty()) {
      return null;
    } else {
      return Value.parseValue(dataType, value, parseDates);
    }
  }
}
