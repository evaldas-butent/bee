package com.butent.bee.client.render;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.client.grid.column.DataColumn;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class RenderableColumn extends DataColumn<String> {

  private final AbstractCellRenderer renderer;

  public RenderableColumn(int index, IsColumn dataColumn, AbstractCellRenderer renderer) {
    this(new RenderableCell(), index, dataColumn, renderer);
  }

  public RenderableColumn(Cell<String> cell, int index, IsColumn dataColumn,
      AbstractCellRenderer renderer) {
    super(cell, index, dataColumn);
    this.renderer = renderer;
  }

  @Override
  public String getValue(IsRow row) {
    return renderer.render(row);
  }
}
