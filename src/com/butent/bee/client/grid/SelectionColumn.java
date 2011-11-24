package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

public class SelectionColumn extends AbstractColumn<Boolean> {
  
  private final CellGrid grid;
  
  public SelectionColumn(CellGrid grid) {
    this(grid, new SimpleBooleanCell());
  }
  
  private SelectionColumn(CellGrid grid, Cell<Boolean> cell) {
    super(cell);
    this.grid = grid;
    setHorizontalAlignment(ALIGN_CENTER);
  }

  @Override
  public ColType getColType() {
    return ColType.SELECTION;
  }

  @Override
  public String getString(Context context, IsRow row) {
    Boolean value = getValue(row);
    return (value == null) ? null : BeeUtils.toString(value);
  }

  @Override
  public Boolean getValue(IsRow row) {
    if (row != null && getGrid() != null) {
      return getGrid().isRowSelected(row.getId());
    } else {
      return null;
    }
  }

  @Override
  public ValueType getValueType() {
    return ValueType.BOOLEAN;
  }
  
  public void update(Element cellElement, boolean value) {
    ((SimpleBooleanCell) getCell()).update(cellElement, value);
  }

  private CellGrid getGrid() {
    return grid;
  }
}
