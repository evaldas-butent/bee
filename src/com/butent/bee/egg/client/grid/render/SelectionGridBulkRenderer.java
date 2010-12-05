package com.butent.bee.egg.client.grid.render;

import com.butent.bee.egg.client.grid.RowView;
import com.butent.bee.egg.client.grid.ColumnDefinition;
import com.butent.bee.egg.client.grid.HasTableDefinition;
import com.butent.bee.egg.client.grid.SelectionGrid;
import com.butent.bee.egg.client.grid.TableDefinition;
import com.butent.bee.egg.client.grid.SelectionGrid.SelectionPolicy;

public class SelectionGridBulkRenderer<RowType> extends GridBulkRenderer<RowType> {

  protected static class SelectionBulkCellView<RowType> extends BulkCellView<RowType> {
    private TableBulkRenderer<RowType> bulkRenderer = null;

    public SelectionBulkCellView(TableBulkRenderer<RowType> bulkRenderer) {
      super(bulkRenderer);
      this.bulkRenderer = bulkRenderer;
    }

    @Override
    protected <ColType> void renderRowValue(RowType rowValue, 
        ColumnDefinition<RowType, ColType> columnDef) {
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

  public SelectionGridBulkRenderer(SelectionGrid grid, TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
  }

  public SelectionGridBulkRenderer(SelectionGrid grid, HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
  }

  @Override
  protected RowView<RowType> createRowView(final RenderingOptions options) {
    BulkCellView<RowType> cellView = new SelectionBulkCellView<RowType>(this);
    return new BulkRowView<RowType>(cellView, this, options);
  }
}
