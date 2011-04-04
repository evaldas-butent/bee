package com.butent.bee.client.grid.model;

import com.butent.bee.shared.data.IsRow;

import java.util.Iterator;

public final class TableModelHelper {

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

  public abstract static class Response {
    public abstract Iterator<IsRow> getRowValues();
  }

}
