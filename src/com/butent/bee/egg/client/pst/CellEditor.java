package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.grid.BeeHtmlTable;

/**
 * Cell editors provide a mechanism to edit cells.
 * 
 * @param <ColType> the data type of the column
 */
public interface CellEditor<ColType> {
  /**
   * Callback for {@link CellEditor}. The callback will be used when the user
   * finishes editing the cell.
   * 
   * @param <ColType> the data type of the column
   */
  public static interface Callback<ColType> {
    /**
     * Use this callback to return a new row value to the table.
     * 
     * @param cellEditInfo information about the source of the edit request
     * @param cellValue the new value to associated with the cell
     */
    void onComplete(CellEditInfo cellEditInfo, ColType cellValue);

    /**
     * Use this callback to cancel the edit request.
     * 
     * @param cellEditInfo information about the source of the edit request
     */
    void onCancel(CellEditInfo cellEditInfo);
  }

  /**
   * The information about the cell to edit.
   */
  public static class CellEditInfo {
    /**
     * The cell index.
     */
    private int cellIndex;

    /**
     * The row index.
     */
    private int rowIndex;

    /**
     * The table that triggered the editor.
     */
    private BeeHtmlTable table;

    /**
     * Construct a new {@link CellEditInfo}.
     * 
     * @param table the table that opened the editor
     * @param rowIndex the row index
     * @param cellIndex the cell index
     */
    public CellEditInfo(BeeHtmlTable table, int rowIndex, int cellIndex) {
      this.table = table;
      this.rowIndex = rowIndex;
      this.cellIndex = cellIndex;
    }

    /**
     * @return the cell index
     */
    public int getCellIndex() {
      return cellIndex;
    }

    /**
     * @return the row index
     */
    public int getRowIndex() {
      return rowIndex;
    }

    /**
     * @return the table that opened the editor
     */
    public BeeHtmlTable getTable() {
      return table;
    }
  }

  /**
   * Handle a request to edit a cell.
   * 
   * @param cellEditInfo information about the source of the edit request
   * @param cellValue the value in the cell to edit
   * @param callback callback used when editing is complete
   */
  void editCell(CellEditInfo cellEditInfo, ColType cellValue,
      Callback<ColType> callback);
}
