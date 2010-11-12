package com.butent.bee.egg.client.grid.render;

import com.butent.bee.egg.client.grid.BeeGrid;
import com.butent.bee.egg.client.grid.FixedWidthGrid;
import com.butent.bee.egg.client.grid.HasTableDefinition;
import com.butent.bee.egg.client.grid.SelectionGrid;
import com.butent.bee.egg.client.grid.TableDefinition;
import com.butent.bee.egg.shared.Assert;

public class GridBulkRenderer<RowType> extends TableBulkRenderer<RowType> {
  public GridBulkRenderer(BeeGrid grid, TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
    init(grid);
  }

  public GridBulkRenderer(BeeGrid grid, HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
    init(grid);
  }

  @Override
  protected void renderRows(String rawHTMLTable) {
    super.renderRows(rawHTMLTable);
    setGridDimensions((BeeGrid) getTable());
  }

  void setGridDimensions(BeeGrid table) {
    int numRows = table.getDOMRowCount();
    table.setNumRows(numRows);

    int cellCount = 0;
    if (numRows > 0) {
      cellCount = table.getDOMCellCount(0);
    }
    table.setNumColumns(cellCount);
  }

  private void init(BeeGrid grid) {
    if (grid instanceof FixedWidthGrid
        && (!(this instanceof FixedWidthGridBulkRenderer))) {
      Assert.unsupported("Must use a FixedWidthGridBulkLoader to bulk load a fixed grid");
    }
    if (grid instanceof SelectionGrid
        && (!(this instanceof SelectionGridBulkRenderer))) {
      Assert.unsupported("Must use a SelectionGridBulkLoader to bulk load a selection grid");
    }
  }
}
