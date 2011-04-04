package com.butent.bee.client.grid.render;

import com.butent.bee.client.grid.FixedWidthGrid;
import com.butent.bee.client.grid.GridTable;
import com.butent.bee.client.grid.HasTableDefinition;
import com.butent.bee.client.grid.TableDefinition;
import com.butent.bee.shared.Assert;

public class GridBulkRenderer extends TableBulkRenderer {
  public GridBulkRenderer(GridTable grid, TableDefinition tableDef) {
    super(grid, tableDef);
    init(grid);
  }

  public GridBulkRenderer(GridTable grid, HasTableDefinition sourceTableDef) {
    super(grid, sourceTableDef);
    init(grid);
  }

  @Override
  protected void renderRows(String rawHTMLTable) {
    super.renderRows(rawHTMLTable);
    setGridDimensions((GridTable) getTable());
  }

  void setGridDimensions(GridTable table) {
    int numRows = table.getDOMRowCount();
    table.setNumRows(numRows);

    int cellCount = 0;
    if (numRows > 0) {
      cellCount = table.getDOMCellCount(0);
    }
    table.setNumColumns(cellCount);
  }

  private void init(GridTable grid) {
    if (grid instanceof FixedWidthGrid && (!(this instanceof FixedWidthGridBulkRenderer))) {
      Assert.unsupported("Must use a FixedWidthGridBulkLoader to bulk load a fixed grid");
    }
  }
}
