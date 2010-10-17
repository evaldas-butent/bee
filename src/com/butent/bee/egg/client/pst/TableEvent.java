package com.butent.bee.egg.client.pst;

/**
 * Handler interface for all Table events.
 */
public interface TableEvent {
  /**
   * Information about the cell that is being highlighted.
   */
  public static class Cell {
    private int cellIndex;
    private int rowIndex;

    /**
     * Construct a new Cell.
     * 
     * @param rowIndex the index of the highlighted row
     * @param cellIndex the index of the highlighted cell
     */
    public Cell(int rowIndex, int cellIndex) {
      this.cellIndex = cellIndex;
      this.rowIndex = rowIndex;
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
  }
  
  /**
   * Information about the row that is being highlighted.
   */
  public static class Row implements Comparable<Row> {
    private int rowIndex;

    /**
     * Construct a new Row.
     * 
     * @param rowIndex the index of the highlighted row
     */
    public Row(int rowIndex) {
      this.rowIndex = rowIndex;
    }

    public int compareTo(Row o) {
      if (o == null) {
        return 1;
      } else {
        return rowIndex - o.getRowIndex();
      }
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Row) {
        return compareTo((Row) o) == 0;
      }
      return false;
    }

    /**
     * @return the row index
     */
    public int getRowIndex() {
      return rowIndex;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }
}
