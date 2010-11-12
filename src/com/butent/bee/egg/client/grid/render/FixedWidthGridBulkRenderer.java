package com.butent.bee.egg.client.grid.render;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.client.grid.BeeHtmlTable;
import com.butent.bee.egg.client.grid.FixedWidthGrid;
import com.butent.bee.egg.client.grid.HasTableDefinition;
import com.butent.bee.egg.client.grid.TableDefinition;

import java.util.Iterator;

public class FixedWidthGridBulkRenderer<RowType> extends SelectionGridBulkRenderer<RowType> {
  public FixedWidthGridBulkRenderer(FixedWidthGrid grid, TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
  }

  public FixedWidthGridBulkRenderer(FixedWidthGrid grid, HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
  }

  protected Element getBulkLoadedGhostRow(BeeHtmlTable table) {
    return table.getRow(0);
  }

  @Override
  protected void renderRows(Iterator<RowType> iterator, final RenderingOptions options) {
    FixedWidthGrid table = (FixedWidthGrid) super.getTable();
    options.headerRow = DOM.toString(table.getGhostRow());
    super.renderRows(iterator, options);
  }

  @Override
  protected void renderRows(String rawHTMLTable) {
    super.renderRows(rawHTMLTable);

    Element newGhostRow = getBulkLoadedGhostRow(getTable());
    FixedWidthGrid grid = (FixedWidthGrid) getTable();
    grid.setGhostRow(newGhostRow);
    grid.updateGhostRow();
  }
}
