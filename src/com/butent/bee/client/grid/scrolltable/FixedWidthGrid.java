package com.butent.bee.client.grid.scrolltable;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.scrolltable.GridUtils.IdealColumnWidthInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

import java.util.HashMap;
import java.util.Map;

/**
 * Extends {@code GridTable} class, enables using grids with fixed width and manage their attributes
 * and events.
 */

public class FixedWidthGrid extends GridTable {
  public class FixedWidthGridCellFormatter extends CellFormatter {
    @Override
    protected Element getRawElement(int row, int column) {
      return super.getRawElement(row + 1, column);
    }
  }

  public class FixedWidthGridColumnFormatter extends ColumnFormatter {
    @Override
    public void setWidth(int column, String width) {
      Assert.unsupported("setWidth is not supported");
    }
  }

  public class FixedWidthGridRowFormatter extends RowFormatter {
    @Override
    protected Element getRawElement(int row) {
      return super.getRawElement(row + 1);
    }
  }

  private int inputColumnWidth = 30;
  private int defaultColumnWidth;
  private Map<Integer, Integer> colWidths = new HashMap<Integer, Integer>();

  private Element ghostRow = null;

  private int[] idealWidths;
  private IdealColumnWidthInfo idealColumnWidthInfo;

  public FixedWidthGrid(int defColWidth) {
    super();
    setDefaultColumnWidth(defColWidth);
    setClearText(BeeConst.HTML_NBSP);

    Element tableElem = getElement();
    StyleUtils.fixedTableLayout(tableElem);
    tableElem.getStyle().setWidth(0, Unit.PX);

    setRowFormatter(new FixedWidthGridRowFormatter());
    setCellFormatter(new FixedWidthGridCellFormatter());
    setColumnFormatter(new FixedWidthGridColumnFormatter());

    ghostRow = GridUtils.createGhostRow();
    DOM.insertChild(getBodyElement(), ghostRow, 0);

    sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEDOWN | Event.ONCLICK);
  }

  @Override
  public void clear() {
    super.clear();
    clearIdealWidths();
  }

  public int getColumnWidth(int column) {
    Integer colWidth = colWidths.get(new Integer(column));
    if (colWidth == null) {
      return getDefaultColumnWidth();
    }
    return colWidth;
  }

  public int getDefaultColumnWidth() {
    return defaultColumnWidth;
  }

  @Override
  public int getDOMCellCount(int row) {
    return super.getDOMCellCount(row + 1);
  }

  @Override
  public int getDOMRowCount() {
    return super.getDOMRowCount() - 1;
  }

  public Element getGhostRow() {
    return ghostRow;
  }

  public int getIdealColumnWidth(int column) {
    maybeRecalculateIdealColumnWidths();
    if (idealWidths.length > column) {
      return idealWidths[column];
    }
    return -1;
  }

  public int getInputColumnWidth() {
    return inputColumnWidth;
  }

  @Override
  public boolean remove(Widget widget) {
    if (super.remove(widget)) {
      clearIdealWidths();
      return true;
    }
    return false;
  }

  @Override
  public void removeRow(int row) {
    super.removeRow(row);
    clearIdealWidths();
  }

  @Override
  public void resizeColumns(int columns) {
    super.resizeColumns(columns);
    updateGhostRow();
    clearIdealWidths();
  }

  @Override
  public void resizeRows(int rows) {
    super.resizeRows(rows);
    clearIdealWidths();
  }

  @Override
  public void setCellPadding(int padding) {
    super.setCellPadding(padding);
    resetColumnWidhts();
  }

  @Override
  public void setCellSpacing(int spacing) {
    super.setCellSpacing(spacing);
    resetColumnWidhts();
  }

  public void setColumnWidth(int column, int width) {
    Assert.nonNegative(column, "Cannot access a column with a negative index: " + column);
    Assert.isPositive(width, "column width must be positive");

    colWidths.put(new Integer(column), new Integer(width));
    if (column >= numColumns) {
      return;
    }
    setColumnWidthImpl(column, width);
  }

  public void setDefaultColumnWidth(int defaultColumnWidth) {
    this.defaultColumnWidth = defaultColumnWidth;
  }

  public void setGhostRow(Element ghostRow) {
    this.ghostRow = ghostRow;
  }

  @Override
  public void setHTML(int row, int column, String html) {
    super.setHTML(row, column, html);
    clearIdealWidths();
  }

  public void setInputColumnWidth(int inputColumnWidth) {
    this.inputColumnWidth = inputColumnWidth;
  }

  @Override
  public void setText(int row, int column, String text) {
    super.setText(row, column, text);
    clearIdealWidths();
  }

  @Override
  public void setWidget(int row, int column, Widget widget) {
    super.setWidget(row, column, widget);
    clearIdealWidths();
  }

  public void updateGhostRow() {
    int numGhosts = getGhostColumnCount();
    if (numColumns > numGhosts) {
      for (int i = numGhosts; i < numColumns; i++) {
        Element td = GridUtils.createGhostCell(null);
        DOM.appendChild(ghostRow, td);
        setColumnWidth(i, getColumnWidth(i));
      }
    } else if (numColumns < numGhosts) {
      int cellsToRemove = numGhosts - numColumns;
      for (int i = 0; i < cellsToRemove; i++) {
        Element td = getGhostCellElement(numColumns);
        DOM.removeChild(ghostRow, td);
      }
    }
  }

  protected FixedWidthGridCellFormatter getFixedWidthGridCellFormatter() {
    return (FixedWidthGridCellFormatter) getCellFormatter();
  }

  protected FixedWidthGridRowFormatter getFixedWidthGridRowFormatter() {
    return (FixedWidthGridRowFormatter) getRowFormatter();
  }

  protected int getGhostColumnCount() {
    return super.getDOMCellCount(0);
  }

  @Override
  protected int getRowIndex(Element rowElem) {
    int rowIndex = super.getRowIndex(rowElem);
    if (rowIndex < 0) {
      return rowIndex;
    }
    return rowIndex - 1;
  }

  @Override
  protected boolean internalClearCell(Element td, boolean clearInnerHTML) {
    clearIdealWidths();
    return super.internalClearCell(td, clearInnerHTML);
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    clearIdealWidths();
  }

  protected void recalculateIdealColumnWidths() {
    int columnCount = getColumnCount();
    if (!isAttached() || getRowCount() == 0 || columnCount < 1) {
      idealWidths = new int[0];
      return;
    }

    recalculateIdealColumnWidthsSetup();
    recalculateIdealColumnWidthsImpl();
    recalculateIdealColumnWidthsTeardown();
  }

  void clearIdealWidths() {
    idealWidths = null;
  }

  boolean isIdealColumnWidthsCalculated() {
    return idealWidths != null;
  }

  void recalculateIdealColumnWidthsImpl() {
    idealWidths = GridUtils.recalculateIdealColumnWidths(idealColumnWidthInfo);
  }

  void recalculateIdealColumnWidthsSetup() {
    int offset = 0;
    clearColumnWidhts();
    idealColumnWidthInfo = GridUtils.recalculateIdealColumnWidthsSetup(this,
        getColumnCount(), offset);
  }

  void recalculateIdealColumnWidthsTeardown() {
    GridUtils.recalculateIdealColumnWidthsTeardown(idealColumnWidthInfo);
    idealColumnWidthInfo = null;
    resetColumnWidhts();
  }

  private void clearColumnWidhts() {
    for (int column : colWidths.keySet()) {
      if (column < numColumns) {
        setColumnWidthImpl(column, 0);
      }
    }
  }

  private Element getGhostCellElement(int column) {
    return GridUtils.getGhostCell(ghostRow, column);
  }

  private void maybeRecalculateIdealColumnWidths() {
    if (idealWidths == null) {
      recalculateIdealColumnWidths();
    }
  }

  private void resetColumnWidhts() {
    for (Map.Entry<Integer, Integer> entry : colWidths.entrySet()) {
      setColumnWidth(entry.getKey(), entry.getValue());
    }
  }

  private void setColumnWidthImpl(int column, int width) {
    if (width > 0) {
      GridUtils.setColumnWidth(ghostRow, column, width);
    } else {
      GridUtils.clearColumnWidth(ghostRow, column);
    }
  }
}
