package com.butent.bee.egg.client.pst;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.grid.BeeFlexTable;
import com.butent.bee.egg.client.pst.FixedWidthTable.IdealColumnWidthInfo;
import com.butent.bee.egg.shared.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedWidthFlexTable extends BeeFlexTable {
  public class FixedWidthFlexCellFormatter extends BeeFlexCellFormatter {
    @Override
    public void setColSpan(int row, int column, int colSpan) {
      colSpan = Math.max(1, colSpan);
      int colSpanDelta = colSpan - getColSpan(row, column);
      super.setColSpan(row, column, colSpan);

      int rowSpan = getRowSpan(row, column);
      for (int i = row; i < row + rowSpan; i++) {
        setNumColumnsPerRow(i, getNumColumnsPerRow(i) + colSpanDelta);
      }
    }

    @Override
    public void setRowSpan(int row, int column, int rowSpan) {
      rowSpan = Math.max(1, rowSpan);
      int curRowSpan = getRowSpan(row, column);
      super.setRowSpan(row, column, rowSpan);

      int colSpan = getColSpan(row, column);
      if (rowSpan > curRowSpan) {
        for (int i = row + curRowSpan; i < row + rowSpan; i++) {
          setNumColumnsPerRow(i, getNumColumnsPerRow(i) + colSpan);
        }
      } else if (rowSpan < curRowSpan) {
        for (int i = row + rowSpan; i < row + curRowSpan; i++) {
          setNumColumnsPerRow(i, getNumColumnsPerRow(i) - colSpan);
        }
      }
    }

    @Override
    public void setWidth(int row, int column, String width) {
      Assert.unsupported("setWidth is not supported");
    }

    @Override
    protected Element getRawElement(int row, int column) {
      return super.getRawElement(row + 1, column);
    }
  }

  public class FixedWidthFlexColumnFormatter extends BeeColumnFormatter {
    @Override
    public void setWidth(int column, String width) {
      Assert.unsupported("setWidth is not supported");
    }
  }

  public class FixedWidthFlexRowFormatter extends BeeRowFormatter {
    @Override
    protected Element getRawElement(int row) {
      return super.getRawElement(row + 1);
    }
  }

  public static final int DEFAULT_COLUMN_WIDTH = 80;

  private Map<Integer, Integer> colWidths = new HashMap<Integer, Integer>();

  private List<Integer> columnsPerRow = new ArrayList<Integer>();

  private Map<Integer, Integer> columnCountMap = new HashMap<Integer, Integer>();

  private int maxRawColumnCount = 0;

  private Element ghostRow;

  private int[] idealWidths;
  private IdealColumnWidthInfo idealColumnWidthInfo;

  public FixedWidthFlexTable() {
    super();
    Element tableElem = getElement();
    BeeKeeper.getStyle().fixedTableLayout(tableElem);
    BeeKeeper.getStyle().zeroWidth(tableElem);

    setCellFormatter(new FixedWidthFlexCellFormatter());
    setColumnFormatter(new FixedWidthFlexColumnFormatter());
    setRowFormatter(new FixedWidthFlexRowFormatter());

    ghostRow = FixedWidthTable.createGhostRow();
    DOM.insertChild(getBodyElement(), ghostRow, 0);
  }

  @Override
  public void clear() {
    super.clear();
    clearIdealWidths();
  }

  public int getColumnCount() {
    return maxRawColumnCount;
  }

  public int getColumnWidth(int column) {
    Object colWidth = colWidths.get(new Integer(column));
    if (colWidth == null) {
      return DEFAULT_COLUMN_WIDTH;
    } else {
      return ((Integer) colWidth).intValue();
    }
  }

  @Override
  public int getDOMCellCount(int row) {
    return super.getDOMCellCount(row + 1);
  }

  @Override
  public int getDOMRowCount() {
    return super.getDOMRowCount() - 1;
  }

  public int getIdealColumnWidth(int column) {
    maybeRecalculateIdealColumnWidths();
    if (idealWidths.length > column) {
      return idealWidths[column];
    }
    return -1;
  }

  @Override
  public Element insertCell(int beforeRow, int beforeColumn) {
    clearIdealWidths();
    Element td = super.insertCell(beforeRow, beforeColumn);
    td.getStyle().setOverflow(Overflow.HIDDEN);
    setNumColumnsPerRow(beforeRow, getNumColumnsPerRow(beforeRow) + 1);
    return td;
  }

  @Override
  public int insertRow(int beforeRow) {
    BeeFlexCellFormatter formatter = getFlexCellFormatter();
    int affectedColSpan = getNumColumnsPerRow(beforeRow);
    if (beforeRow != getRowCount()) {
      int numCellsInRow = getCellCount(beforeRow);
      for (int cell = 0; cell < numCellsInRow; cell++) {
        affectedColSpan -= formatter.getColSpan(beforeRow, cell);
      }
    }

    if (beforeRow != getRowCount()) {
      checkRowBounds(beforeRow);
    }
    Element tr = DOM.createTR();
    DOM.insertChild(getBodyElement(), tr, beforeRow + 1);
    columnsPerRow.add(beforeRow, new Integer(0));

    for (int curRow = beforeRow - 1; curRow >= 0; curRow--) {
      if (affectedColSpan <= 0) {
        break;
      }

      int numCells = getCellCount(curRow);
      for (int curCell = 0; curCell < numCells; curCell++) {
        int affectedRow = curRow + formatter.getRowSpan(curRow, curCell);
        if (affectedRow > beforeRow) {
          int colSpan = formatter.getColSpan(curRow, curCell);
          affectedColSpan -= colSpan;
          setNumColumnsPerRow(beforeRow, getNumColumnsPerRow(beforeRow)
              + colSpan);
          setNumColumnsPerRow(affectedRow, getNumColumnsPerRow(affectedRow)
              - colSpan);
        }
      }
    }

    return beforeRow;
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
  public void removeCell(int row, int column) {
    clearIdealWidths();
    int colSpan = getFlexCellFormatter().getColSpan(row, column);
    int rowSpan = getFlexCellFormatter().getRowSpan(row, column);
    super.removeCell(row, column);

    for (int i = row; i < row + rowSpan; i++) {
      setNumColumnsPerRow(i, getNumColumnsPerRow(i) - colSpan);
    }
  }

  @Override
  public void removeRow(int row) {
    BeeFlexCellFormatter formatter = getFlexCellFormatter();
    int affectedColSpan = getNumColumnsPerRow(row);
    int numCellsInRow = getCellCount(row);
    for (int cell = 0; cell < numCellsInRow; cell++) {
      formatter.setRowSpan(row, cell, 1);
      affectedColSpan -= formatter.getColSpan(row, cell);
    }

    super.removeRow(row);
    clearIdealWidths();
    setNumColumnsPerRow(row, -1);
    columnsPerRow.remove(row);

    for (int curRow = row - 1; curRow >= 0; curRow--) {
      if (affectedColSpan <= 0) {
        break;
      }

      int numCells = getCellCount(curRow);
      for (int curCell = 0; curCell < numCells; curCell++) {
        int affectedRow = curRow + formatter.getRowSpan(curRow, curCell) - 1;
        if (affectedRow >= row) {
          int colSpan = formatter.getColSpan(curRow, curCell);
          affectedColSpan -= colSpan;
          setNumColumnsPerRow(affectedRow, getNumColumnsPerRow(affectedRow)
              + colSpan);
        }
      }
    }
  }

  @Override
  public void setCellPadding(int padding) {
    super.setCellPadding(padding);
    for (Map.Entry<Integer, Integer> entry : colWidths.entrySet()) {
      setColumnWidth(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void setCellSpacing(int spacing) {
    super.setCellSpacing(spacing);

    for (Map.Entry<Integer, Integer> entry : colWidths.entrySet()) {
      setColumnWidth(entry.getKey(), entry.getValue());
    }
  }

  public void setColumnWidth(int column, int width) {
    Assert.nonNegative(column,  "Cannot access a column with a negative index: " + column);

    width = Math.max(1, width);
    colWidths.put(new Integer(column), new Integer(width));

    int numGhosts = getGhostColumnCount();
    if (column >= numGhosts) {
      return;
    }

    FixedWidthTable.setColumnWidth(ghostRow, column, width);
  }

  @Override
  public void setHTML(int row, int column, String html) {
    super.setHTML(row, column, html);
    clearIdealWidths();
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

  @Override
  protected void addCells(int row, int num) {
    super.addCells(row + 1, num);
    clearIdealWidths();
  }

  protected int getGhostColumnCount() {
    return super.getDOMCellCount(0);
  }

  protected Element getGhostRow() {
    return ghostRow;
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

  @Override
  protected void prepareCell(int row, int column) {
    int curNumCells = 0;
    if (getRowCount() > row) {
      curNumCells = getCellCount(row);
    }
    super.prepareCell(row, column);

    if (column >= curNumCells) {
      int cellsAdded = column - curNumCells + 1;
      setNumColumnsPerRow(row, getNumColumnsPerRow(row) + cellsAdded);

      for (int cell = curNumCells; cell < column; cell++) {
        Element td = getCellFormatter().getElement(row, cell);
        td.getStyle().setOverflow(Overflow.HIDDEN);
      }
    }
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
    idealWidths = FixedWidthTable.recalculateIdealColumnWidths(idealColumnWidthInfo);
  }

  void recalculateIdealColumnWidthsSetup() {
    idealColumnWidthInfo = FixedWidthTable.recalculateIdealColumnWidthsSetup(
        this, getColumnCount(), 0);
  }

  void recalculateIdealColumnWidthsTeardown() {
    FixedWidthTable.recalculateIdealColumnWidthsTeardown(idealColumnWidthInfo);
    idealColumnWidthInfo = null;
  }

  private int getNumColumnsPerRow(int row) {
    if (columnsPerRow.size() <= row) {
      return 0;
    } else {
      return columnsPerRow.get(row).intValue();
    }
  }

  private void maybeRecalculateIdealColumnWidths() {
    if (idealWidths == null) {
      recalculateIdealColumnWidths();
    }
  }

  private void setNumColumnsPerRow(int row, int numColumns) {
    int oldNumColumns = getNumColumnsPerRow(row);
    if (oldNumColumns == numColumns) {
      return;
    }

    Integer numColumnsI = new Integer(numColumns);
    Integer oldNumColumnsI = new Integer(oldNumColumns);
    if (row < columnsPerRow.size()) {
      columnsPerRow.set(row, numColumnsI);
    } else {
      columnsPerRow.add(numColumnsI);
    }

    boolean oldNumColumnsRemoved = false;
    if (columnCountMap.containsKey(oldNumColumnsI)) {
      int numRows = columnCountMap.get(oldNumColumnsI).intValue();
      if (numRows == 1) {
        columnCountMap.remove(oldNumColumnsI);
        oldNumColumnsRemoved = true;
      } else {
        columnCountMap.put(oldNumColumnsI, new Integer(numRows - 1));
      }
    }

    if (numColumns > 0) {
      if (columnCountMap.containsKey(numColumnsI)) {
        int numRows = columnCountMap.get(numColumnsI).intValue();
        columnCountMap.put(numColumnsI, new Integer(numRows + 1));
      } else {
        columnCountMap.put(numColumnsI, new Integer(1));
      }
    }

    if (numColumns > maxRawColumnCount) {
      maxRawColumnCount = numColumns;
    } else if ((numColumns < oldNumColumns)
        && (oldNumColumns == maxRawColumnCount) && oldNumColumnsRemoved) {
      maxRawColumnCount = 0;
      for (Integer curNumColumns : columnCountMap.keySet()) {
        maxRawColumnCount = Math.max(maxRawColumnCount,
            curNumColumns.intValue());
      }
    }

    updateGhostRow();
  }

  private void updateGhostRow() {
    int curNumGhosts = getGhostColumnCount();

    if (maxRawColumnCount > curNumGhosts) {
      super.addCells(0, maxRawColumnCount - curNumGhosts);
      for (int i = curNumGhosts; i < maxRawColumnCount; i++) {
        Element td = FixedWidthTable.getGhostCell(ghostRow, i);
        FixedWidthTable.createGhostCell(td);
        setColumnWidth(i, getColumnWidth(i));
      }

    } else if (maxRawColumnCount < curNumGhosts) {
      int cellsToRemove = curNumGhosts - maxRawColumnCount;
      for (int i = 0; i < cellsToRemove; i++) {
        DOM.removeChild(ghostRow, FixedWidthTable.getGhostCell(ghostRow, maxRawColumnCount));
      }
    }
  }
}
