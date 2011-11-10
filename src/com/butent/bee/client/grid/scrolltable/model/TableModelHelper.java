package com.butent.bee.client.grid.scrolltable.model;

import com.butent.bee.shared.data.IsRow;

import java.util.Iterator;

/**
 * Implements grid data requests and responses, is a final class.
 */

public final class TableModelHelper {

  /**
   * Implements grid data requests by specifying starting row and number of rows.
   */

  public static class Request {
    private int startRow;
    private int numRows;

    public Request() {
      this(0, 0);
    }

    public Request(int startRow, int numRows) {
      this.startRow = startRow;
      this.numRows = numRows;
    }

    public int getNumRows() {
      return numRows;
    }

    public int getStartRow() {
      return startRow;
    }
  }

  /**
   * Specifies how response classes should be implemented in extending classes.
   */

  public abstract static class Response {
    public abstract Iterator<IsRow> getRowValues();
  }

}
