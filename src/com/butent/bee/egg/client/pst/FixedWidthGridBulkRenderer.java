package com.butent.bee.egg.client.pst;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import java.util.Iterator;

/**
 * Helper class to bulk load {@link FixedWidthGrid} tables.
 * 
 * @param <RowType> the data type of the row values
 */
public class FixedWidthGridBulkRenderer<RowType> extends
    SelectionGridBulkRenderer<RowType> {
  /**
   * Constructor. Takes in the number of columns in the table to allow efficient
   * creation of the header row.
   * 
   * @param grid {@link FixedWidthGrid} to be be bulk rendered
   * @param tableDef the table definition that should be used during rendering
   */
  public FixedWidthGridBulkRenderer(FixedWidthGrid grid,
      TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
  }

  /**
   * Constructor. Takes in the number of columns in the table to allow efficient
   * creation of the header row.
   * 
   * @param grid {@link FixedWidthGrid} to be be bulk rendered
   * @param sourceTableDef the external source of the table definition
   */
  public FixedWidthGridBulkRenderer(FixedWidthGrid grid,
      HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
  }

  /**
   * Gets the new ghost element from the table.
   * 
   * @param table the table
   * @return the new ghost row
   */
  protected native Element getBulkLoadedGhostRow(HTMLTable table)
  /*-{
    return table.@com.butent.bee.egg.client.pst.HTMLTable::getBodyElement()(table).rows[0];
  }-*/;

  @Override
  protected void renderRows(Iterator<RowType> iterator,
      final RenderingOptions options) {
    FixedWidthGrid table = (FixedWidthGrid) super.getTable();
    options.headerRow = DOM.toString(table.getGhostRow());
    super.renderRows(iterator, options);
  }

  @Override
  protected void renderRows(String rawHTMLTable) {
    super.renderRows(rawHTMLTable);

    // Update the ghost row variable after the num columns has been set
    Element newGhostRow = getBulkLoadedGhostRow(getTable());
    FixedWidthGrid grid = (FixedWidthGrid) getTable();
    grid.setGhostRow(newGhostRow);
    grid.updateGhostRow();
  }
}
