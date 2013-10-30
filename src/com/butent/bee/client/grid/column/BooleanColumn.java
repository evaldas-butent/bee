package com.butent.bee.client.grid.column;

import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.cell.BooleanCell;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

public class BooleanColumn extends DataColumn<Boolean> {

  public BooleanColumn(CellSource cellSource) {
    this(new BooleanCell(), cellSource);
  }

  public BooleanColumn(AbstractCell<Boolean> cell, CellSource cellSource) {
    super(cell, cellSource);
    setTextAlign(TextAlign.CENTER);
  }

  @Override
  public String getStyleSuffix() {
    return "boolean";
  }
  
  @Override
  public Boolean getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return getCellSource().getBoolean(row);
  }
}
