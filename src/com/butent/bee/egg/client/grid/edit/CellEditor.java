package com.butent.bee.egg.client.grid.edit;

import com.butent.bee.egg.client.grid.HtmlTable;

public interface CellEditor<ColType> {
  public static interface Callback<ColType> {
    void onComplete(CellEditInfo cellEditInfo, ColType cellValue);
    void onCancel(CellEditInfo cellEditInfo);
  }

  public static class CellEditInfo {
    private int cellIndex;
    private int rowIndex;

    private HtmlTable table;

    public CellEditInfo(HtmlTable table, int rowIndex, int cellIndex) {
      this.table = table;
      this.rowIndex = rowIndex;
      this.cellIndex = cellIndex;
    }

    public int getCellIndex() {
      return cellIndex;
    }

    public int getRowIndex() {
      return rowIndex;
    }

    public HtmlTable getTable() {
      return table;
    }
  }

  void editCell(CellEditInfo cellEditInfo, ColType cellValue, Callback<ColType> callback);
}
