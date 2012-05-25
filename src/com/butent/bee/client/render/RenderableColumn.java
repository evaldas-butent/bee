package com.butent.bee.client.render;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.client.grid.column.DataColumn;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class RenderableColumn extends DataColumn<String> implements HasCellRenderer {

  private AbstractCellRenderer renderer;

  public RenderableColumn(Cell<String> cell, int index, IsColumn dataColumn,
      AbstractCellRenderer renderer) {
    super(cell, index, dataColumn);
    this.renderer = renderer;
  }

  public RenderableColumn(int index, IsColumn dataColumn, AbstractCellRenderer renderer) {
    this(new RenderableCell(), index, dataColumn, renderer);
  }

  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public String getValue(IsRow row) {
    return renderer.render(row);
  }

  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }
}
