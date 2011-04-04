package com.butent.bee.client.grid.model;

import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;

public abstract class TableModel {

  public static interface Callback {
    void onFailure(Throwable caught);
    void onRowsReady(Request request, Response response);
  }

  public static final int ALL_ROWS = -1;
  public static final int UNKNOWN_ROW_COUNT = -1;

  private int rowCount = UNKNOWN_ROW_COUNT;

  public int getRowCount() {
    return rowCount;
  }

  public abstract void requestRows(Request request, Callback callback);

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }
}
