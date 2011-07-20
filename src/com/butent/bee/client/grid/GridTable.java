package com.butent.bee.client.grid;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

/**
 * Extends {@code HtmlTable} class, enables to use grid tables and make such operations with them as
 * adding/removing rows or setting formatters to it.
 */

public class GridTable extends HtmlTable {

  private static native void addRows(Element table, int rows, int columns) /*-{
    var td = $doc.createElement("td");
    td.innerHTML = "&nbsp;";
    var row = $doc.createElement("tr");
    for ( var cellNum = 0; cellNum < columns; cellNum++) {
      var cell = td.cloneNode(true);
      row.appendChild(cell);
    }
    table.appendChild(row);
    for ( var rowNum = 1; rowNum < rows; rowNum++) {
      table.appendChild(row.cloneNode(true));
    }
  }-*/;

  protected int numColumns;
  protected int numRows;

  public GridTable() {
    super();

    setCellFormatter(new CellFormatter());
    setRowFormatter(new RowFormatter());
    setColumnFormatter(new ColumnFormatter());

    setClearText(BeeConst.HTML_NBSP);
  }

  public GridTable(int rows, int columns) {
    this();
    resize(rows, columns);
  }

  @Override
  public int getCellCount(int row) {
    return numColumns;
  }

  public int getColumnCount() {
    return numColumns;
  }

  @Override
  public String getIdPrefix() {
    return "grid";
  }

  public int getNumColumns() {
    return numColumns;
  }

  public int getNumRows() {
    return numRows;
  }

  @Override
  public int getRowCount() {
    return numRows;
  }

  @Override
  public int insertRow(int beforeRow) {
    int index = super.insertRow(beforeRow);
    numRows++;

    for (int i = 0; i < numColumns; i++) {
      insertCell(index, i);
    }

    return index;
  }

  @Override
  public void removeRow(int row) {
    super.removeRow(row);
    numRows--;
  }

  public void resize(int rows, int columns) {
    resizeColumns(columns);
    resizeRows(rows);
  }

  public void resizeColumns(int columns) {
    if (numColumns == columns) {
      return;
    }
    Assert.nonNegative(columns, "Cannot set number of columns to " + columns);

    if (numColumns > columns) {
      for (int i = 0; i < numRows; i++) {
        for (int j = numColumns - 1; j >= columns; j--) {
          removeCell(i, j);
        }
      }
    } else {
      for (int i = 0; i < numRows; i++) {
        for (int j = numColumns; j < columns; j++) {
          insertCell(i, j);
        }
      }
    }
    numColumns = columns;

    getColumnFormatter().resizeColumnGroup(columns, false);
  }

  public void resizeRows(int rows) {
    if (numRows == rows) {
      return;
    }
    Assert.nonNegative(rows, "Cannot set number of rows to " + rows);

    if (numRows < rows) {
      addRows(getBodyElement(), rows - numRows, numColumns);
      numRows = rows;
    } else {
      while (numRows > rows) {
        removeRow(numRows - 1);
      }
    }
  }

  public void setNumColumns(int numColumns) {
    this.numColumns = numColumns;
  }

  public void setNumRows(int numRows) {
    this.numRows = numRows;
  }

  @Override
  protected Element createCell() {
    Element td = super.createCell();
    DOM.setInnerHTML(td, BeeConst.HTML_NBSP);
    return td;
  }

  @Override
  protected Element createRow() {
    Element tr = super.createRow();
    for (int i = 0; i < numColumns; i++) {
      tr.appendChild(createCell());
    }
    return tr;
  }

  @Override
  protected void prepareCell(int row, int column) {
    prepareRow(row);
    Assert.nonNegative(column, "Cannot access a column with a negative index: " + column);
    Assert.isTrue(column < numColumns, "Column index: " + column
        + ", Column size: " + numColumns);
  }

  @Override
  protected void prepareColumn(int column) {
    super.prepareColumn(column);
    Assert.isTrue(column < numColumns, "Column index: " + column
        + ", Column size: " + numColumns);
  }

  @Override
  protected void prepareRow(int row) {
    Assert.nonNegative(row, "Cannot access a row with a negative index: " + row);
    Assert.isTrue(row < numRows, "Row index: " + row + ", Row size: " + numRows);
  }
}
