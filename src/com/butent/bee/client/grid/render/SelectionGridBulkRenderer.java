package com.butent.bee.client.grid.render;

import com.butent.bee.client.grid.ColumnDefinition;
import com.butent.bee.client.grid.HasTableDefinition;
import com.butent.bee.client.grid.RowView;
import com.butent.bee.client.grid.SelectionGrid;
import com.butent.bee.client.grid.TableDefinition;
import com.butent.bee.client.grid.SelectionGrid.SelectionPolicy;
import com.butent.bee.shared.data.IsRow;

public class SelectionGridBulkRenderer extends GridBulkRenderer {

  protected static class SelectionBulkCellView extends BulkCellView {
    private TableBulkRenderer bulkRenderer = null;

    public SelectionBulkCellView(TableBulkRenderer bulkRenderer) {
      super(bulkRenderer);
      this.bulkRenderer = bulkRenderer;
    }

    @Override
    protected void renderRowValue(IsRow rowValue, ColumnDefinition columnDef) {
      if (getCellIndex() == 0) {
        SelectionPolicy selectionPolicy = 
          ((SelectionGrid) bulkRenderer.getTable()).getSelectionPolicy();
        if (selectionPolicy.hasInputColumn()) {
          getStringBuffer().append("<td align='CENTER'>");
          getStringBuffer().append(
              ((SelectionGrid) bulkRenderer.getTable()).getInputHtml(selectionPolicy));
          getStringBuffer().append("</td>");
        }
      }

      super.renderRowValue(rowValue, columnDef);
    }
  }

  public SelectionGridBulkRenderer(SelectionGrid grid, TableDefinition tableDef) {
    super(grid, tableDef);
  }

  public SelectionGridBulkRenderer(SelectionGrid grid, HasTableDefinition sourceTableDef) {
    super(grid, sourceTableDef);
  }

  @Override
  protected RowView createRowView(final RenderingOptions options) {
    BulkCellView cellView = new SelectionBulkCellView(this);
    return new BulkRowView(cellView, this, options);
  }
}
