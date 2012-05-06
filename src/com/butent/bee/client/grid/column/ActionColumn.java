package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.cell.ActionCell;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

public class ActionColumn extends AbstractColumn<String> implements HasCellRenderer {

  private final int dataIndex;
  private AbstractCellRenderer renderer;

  public ActionColumn(int dataIndex, AbstractCellRenderer renderer) {
    this(new ActionCell(), dataIndex, renderer);
  }

  public ActionColumn(Cell<String> cell, int dataIndex, AbstractCellRenderer renderer) {
    super(cell);
    this.dataIndex = dataIndex;
    this.renderer = renderer;
  }

  @Override
  public ColType getColType() {
    return ColType.ACTION;
  }

  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public String getString(Context context, IsRow row) {
    return getValue(row);
  }

  @Override
  public String getValue(IsRow row) {
    if (row == null) {
      return null;
    } else if (getRenderer() != null) {
      return getRenderer().render(row);
    } else if (getDataIndex() >= 0) {
      return row.getString(getDataIndex());
    } else {
      return null;
    }
  }

  @Override
  public ValueType getValueType() {
    return null;
  }

  @Override
  public void render(Context context, IsRow rowValue, SafeHtmlBuilder sb) {
    String value = getString(context, rowValue);
    if (BeeUtils.isEmpty(value)) {
      return;
    }
    getCell().render(context, getValue(rowValue), sb);
  }

  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }

  private int getDataIndex() {
    return dataIndex;
  }
}
