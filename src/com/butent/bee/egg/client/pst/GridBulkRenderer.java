package com.butent.bee.egg.client.pst;

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
  public GridBulkRenderer(Grid grid, TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
    init(grid);
  }

  /**
   * Construct a new {@link GridBulkRenderer}.
   * 
   * @param grid {@link Grid} to be be bulk rendered
   * @param sourceTableDef the external source of the table definition
   */
  public GridBulkRenderer(Grid grid, HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
    init(grid);
  }

  @Override
  protected void renderRows(String rawHTMLTable) {
    super.renderRows(rawHTMLTable);
    setGridDimensions((Grid) getTable());
  }

  /**
   * Short term hack to set protected row and columns.
   */
  native void setGridDimensions(Grid table) /*-{
    var numRows =  table.@com.butent.bee.egg.client.pst.HTMLTable::getDOMRowCount()();
    table.@com.butent.bee.egg.client.pst.Grid::numRows = numRows;
    var cellCount = 0;
    if (numRows > 0) {
      cellCount =
        table.@com.butent.bee.egg.client.pst.HTMLTable::getDOMCellCount(I)(0);
    }
    table.@com.butent.bee.egg.client.pst.Grid::numColumns = cellCount;
  }-*/;

  private void init(Grid grid) {
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
