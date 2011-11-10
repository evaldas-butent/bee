package com.butent.bee.client.grid.scrolltable.model;

import com.butent.bee.client.grid.scrolltable.model.TableModelHelper.Request;
import com.butent.bee.client.grid.scrolltable.model.TableModelHelper.Response;

/**
 * Requires all extending classes to have {@code requestRows} method and row count management
 * implementation.
 */

public abstract class TableModel {

  /**
   * Determines necessary methods for callback implementing classes.
   */

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
