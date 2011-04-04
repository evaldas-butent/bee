package com.butent.bee.client.grid.render;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.client.grid.FixedWidthGrid;
import com.butent.bee.client.grid.HasTableDefinition;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.grid.TableDefinition;
import com.butent.bee.shared.data.IsRow;

import java.util.Iterator;

public class FixedWidthGridBulkRenderer extends GridBulkRenderer {
  public FixedWidthGridBulkRenderer(FixedWidthGrid grid, TableDefinition tableDef) {
    super(grid, tableDef);
  }

  public FixedWidthGridBulkRenderer(FixedWidthGrid grid, HasTableDefinition sourceTableDef) {
    super(grid, sourceTableDef);
  }

  protected Element getBulkLoadedGhostRow(HtmlTable table) {
    return table.getRow(0);
  }

  @Override
  protected void renderRows(Iterator<IsRow> iterator, final RenderingOptions options) {
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
