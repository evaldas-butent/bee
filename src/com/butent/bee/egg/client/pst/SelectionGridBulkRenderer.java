package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.SelectionGrid.SelectionPolicy;
import com.butent.bee.egg.client.pst.TableDefinition.AbstractRowView;

/**
 * Allows bulk rendering of {@link SelectionGrid}s.
 * 
 * @param <RowType> the data type of the row values
 */
public class SelectionGridBulkRenderer<RowType> extends
    GridBulkRenderer<RowType> {
  /**
   * A customized {@link SelectionGridBulkRenderer.BulkCellView} used by the
   * {@link SelectionGridBulkRenderer}.
   * 
   * @param <RowType> the data type of the row values
   */
  protected static class SelectionBulkCellView<RowType> extends
      BulkCellView<RowType> {
    private TableBulkRenderer<RowType> bulkRenderer = null;

    /**
     * Construct a new {@link SelectionGridBulkRenderer.SelectionBulkCellView}.
     * 
     * @param bulkRenderer the renderer
     */
    public SelectionBulkCellView(TableBulkRenderer<RowType> bulkRenderer) {
      super(bulkRenderer);
      this.bulkRenderer = bulkRenderer;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void renderRowValue(RowType rowValue, ColumnDefinition columnDef) {
      // Add the input column
      if (getCellIndex() == 0) {
        SelectionPolicy selectionPolicy = ((SelectionGrid) bulkRenderer.getTable()).getSelectionPolicy();
        if (selectionPolicy.hasInputColumn()) {
          getStringBuffer().append("<td align='CENTER'>");
          getStringBuffer().append(
              ((SelectionGrid) bulkRenderer.getTable()).getInputHtml(selectionPolicy));
          getStringBuffer().append("</td>");
        }
      }

      // Render the actual cell
      super.renderRowValue(rowValue, columnDef);
    }
  }

  /**
   * Construct a new {@link SelectionGridBulkRenderer}.
   * 
   * @param grid {@link SelectionGrid} to be be bulk rendered
   * @param tableDef the table definition that should be used during rendering
   */
  public SelectionGridBulkRenderer(SelectionGrid grid,
      TableDefinition<RowType> tableDef) {
    super(grid, tableDef);
  }

  /**
   * Construct a new {@link SelectionGridBulkRenderer}.
   * 
   * @param grid {@link SelectionGrid} to be be bulk rendered
   * @param sourceTableDef the external source of the table definition
   */
  public SelectionGridBulkRenderer(SelectionGrid grid,
      HasTableDefinition<RowType> sourceTableDef) {
    super(grid, sourceTableDef);
  }

  @Override
  protected AbstractRowView<RowType> createRowView(
      final RenderingOptions options) {
    BulkCellView<RowType> cellView = new SelectionBulkCellView<RowType>(this);
    return new BulkRowView<RowType>(cellView, this, options);
  }
}
