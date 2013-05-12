package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

public class StringColumn extends DataColumn<String> {

  public StringColumn(CellSource cellSource) {
    this(new TextCell(), cellSource);
  }

  public StringColumn(Cell<String> cell, CellSource cellSource) {
    super(cell, cellSource);
  }

  @Override
  public String getStyleSuffix() {
    return "string";
  }

  @Override
  public String getValue(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return getCellSource().getString(row);
    }
  }
}
