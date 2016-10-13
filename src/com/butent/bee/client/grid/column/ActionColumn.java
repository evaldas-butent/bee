package com.butent.bee.client.grid.column;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.grid.cell.ActionCell;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;

public class ActionColumn extends AbstractColumn<String> implements HasCellRenderer {

  private AbstractCellRenderer renderer;

  public ActionColumn(AbstractCellRenderer renderer) {
    this(new ActionCell(), renderer);
  }

  public ActionColumn(ActionCell cell, AbstractCellRenderer renderer) {
    super(cell);
    this.renderer = renderer;
  }

  @Override
  public ColType getColType() {
    return ColType.ACTION;
  }

  @Override
  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public String getString(CellContext context) {
    return getValue(context.getRow());
  }

  @Override
  public String getStyleSuffix() {
    return "action";
  }

  @Override
  public String getValue(IsRow row) {
    if (row == null) {
      return null;
    } else if (getRenderer() != null) {
      return getRenderer().render(row);
    } else {
      return null;
    }
  }

  @Override
  public ValueType getValueType() {
    return null;
  }

  @Override
  public String render(CellContext context) {
    return getCell().render(context, getString(context));
  }

  @Override
  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }
}
