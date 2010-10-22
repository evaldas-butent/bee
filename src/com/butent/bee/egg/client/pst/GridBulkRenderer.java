package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.grid.BeeGrid;

/**
 * Allows bulk rendering of {@link Grid}s.
 * <p>
 * Must use the {@link Grid} in the overrides package.
 * </p>
 * 
 * @param <RowType> the data type of the row values
 */
public class GridBulkRenderer<RowType> extends TableBulkRenderer<RowType> {
  /**
   * Construct a new {@link GridBulkRenderer}.
   * 
   * @param grid {@link Grid} to be be bulk rendered
   * @param tableDef the table definition that should be used during rendering
   */
  public GridBulkRenderer(BeeGrid grid, TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
    init(grid);
  }

  /**
   * Construct a new {@link GridBulkRenderer}.
   * 
   * @param grid {@link Grid} to be be bulk rendered
   * @param sourceTableDef the external source of the table definition
   */
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
      throw new UnsupportedOperationException(
          "Must use a FixedWidthGridBulkLoader to bulk load a fixed grid");
    }
    if (grid instanceof SelectionGrid
        && (!(this instanceof SelectionGridBulkRenderer))) {
      throw new UnsupportedOperationException(
          "Must use a SelectionGridBulkLoader to bulk load a selection grid");
    }
  }
}
