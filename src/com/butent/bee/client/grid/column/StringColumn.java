package com.butent.bee.client.grid.column;

import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.cell.TextCell;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

public class StringColumn extends DataColumn<String> {

  public StringColumn(CellSource cellSource) {
    this(new TextCell(), cellSource);
  }

  public StringColumn(AbstractCell<String> cell, CellSource cellSource) {
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
