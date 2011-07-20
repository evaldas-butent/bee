package com.butent.bee.client.grid;

import com.google.gwt.user.client.Element;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;

/**
 * Extends {@code HtmlTable} class, enables to use flexible tables that create cells on demand.
 */

public class FlexTable extends HtmlTable {

  public class FlexCellFormatter extends CellFormatter {
    public int getColSpan(int row, int column) {
      return DomUtils.getColSpan(getElement(row, column));
    }

    public int getRowSpan(int row, int column) {
      return DomUtils.getRowSpan(getElement(row, column));
    }

    public void setColSpan(int row, int column, int colSpan) {
      DomUtils.setColSpan(ensureElement(row, column), colSpan);
    }

    public void setRowSpan(int row, int column, int rowSpan) {
      DomUtils.setRowSpan(ensureElement(row, column), rowSpan);
    }
  }

  private static native void addCells(Element table, int row, int num)/*-{
    var rowElem = table.rows[row];
    for ( var i = 0; i < num; i++) {
      var cell = $doc.createElement("td");
      rowElem.appendChild(cell);
    }
  }-*/;

  public FlexTable() {
    super();
    setCellFormatter(new FlexCellFormatter());
    setRowFormatter(new RowFormatter());
    setColumnFormatter(new ColumnFormatter());

    setStyleName("bee-FlexTable");
  }

  public void addCell(int row) {
    insertCell(row, getCellCount(row));
  }

  @Override
  public int getCellCount(int row) {
    checkRowBounds(row);
    return getDOMCellCount(getBodyElement(), row);
  }

  public int getColumnIndex(int row, int column) {
    checkCellBounds(row, column);
    return getRawColumnIndex(row, column);
  }

  public FlexCellFormatter getFlexCellFormatter() {
    return (FlexCellFormatter) getCellFormatter();
  }

  @Override
  public String getIdPrefix() {
    return "flex";
  }

  @Override
  public int getRowCount() {
    return getDOMRowCount();
  }

  @Override
  public Element insertCell(int beforeRow, int beforeColumn) {
    return super.insertCell(beforeRow, beforeColumn);
  }

  @Override
  public int insertRow(int beforeRow) {
    return super.insertRow(beforeRow);
  }

  public void removeAllRows() {
    int numRows = getRowCount();
    for (int i = 0; i < numRows; i++) {
      removeRow(0);
    }
  }

  @Override
  public void removeCell(int row, int col) {
    super.removeCell(row, col);
  }

  public void removeCells(int row, int column, int num) {
    for (int i = 0; i < num; i++) {
      removeCell(row, column);
    }
  }

  @Override
  public void removeRow(int row) {
    super.removeRow(row);
  }

  protected void addCells(int row, int num) {
    addCells(getBodyElement(), row, num);
  }

  @Override
  protected void prepareCell(int row, int column) {
    prepareRow(row);
    Assert.nonNegative(column, "Cannot create a column with a negative index: " + column);

    int cellCount = getCellCount(row);
    int required = column + 1 - cellCount;
    if (required > 0) {
      addCells(row, required);
    }
  }

  @Override
  protected void prepareRow(int row) {
    Assert.nonNegative(row, "Cannot create a row with a negative index: " + row);

    int rowCount = getRowCount();
    for (int i = rowCount; i <= row; i++) {
      insertRow(i);
    }
  }

  private int getRawColumnIndex(int row, int column) {
    FlexCellFormatter formatter = getFlexCellFormatter();
    int columnIndex = 0;
    for (int curCell = 0; curCell < column; curCell++) {
      columnIndex += formatter.getColSpan(row, curCell);
    }

    int numCells = 0;
    for (int curRow = 0; curRow < row; curRow++) {
      numCells = getCellCount(curRow);
      for (int curCell = 0; curCell < numCells; curCell++) {
        if ((curRow + formatter.getRowSpan(curRow, curCell) - 1) >= row) {
          if (getRawColumnIndex(curRow, curCell) <= columnIndex) {
            columnIndex += formatter.getColSpan(curRow, curCell);
          }
        }
      }
    }
    return columnIndex;
  }
}
