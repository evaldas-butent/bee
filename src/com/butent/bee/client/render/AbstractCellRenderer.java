package com.butent.bee.client.render;

import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

public abstract class AbstractCellRenderer extends AbstractRenderer<IsRow> implements HasValueType {

  private final CellSource cellSource;

  public AbstractCellRenderer(CellSource cellSource) {
    this.cellSource = cellSource;
  }

  @Override
  public ValueType getValueType() {
    return (cellSource == null) ? null : cellSource.getValueType();
  }
  
  protected CellSource getCellSource() {
    return cellSource;
  }

  protected Integer getInteger(IsRow row) {
    if (row == null || cellSource == null) {
      return null;
    } else {
      return cellSource.getInteger(row);
    }
  }
  
  protected String getString(IsRow row) {
    if (row == null || cellSource == null) {
      return null;
    } else {
      return cellSource.getString(row);
    }
  }
  
  protected Value getValue(IsRow row) {
    if (row == null || cellSource == null) {
      return null;
    } else {
      return cellSource.getValue(row);
    }
  }
  
  protected Value parse(String value, boolean parseDates) {
    if (value == null || value.isEmpty() || cellSource == null) {
      return null;
    } else {
      return Value.parseValue(getValueType(), value, parseDates);
    }
  }
}
