package com.butent.bee.egg.client.pst;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.client.pst.TableModelHelper.ColumnSortInfo;
import com.butent.bee.egg.client.pst.TableModelHelper.ColumnSortList;
import com.butent.bee.egg.shared.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SortableGrid extends SelectionGrid implements HasColumnSortHandlers {

  public abstract static class ColumnSorter {
    public abstract void onSortColumn(SortableGrid grid,
        ColumnSortList sortList, SortableGrid.ColumnSorterCallback callback);
  }

  private static class DefaultColumnSorter extends ColumnSorter {
    @Override
    public void onSortColumn(SortableGrid grid, ColumnSortList sortList,
        SortableGrid.ColumnSorterCallback callback) {
      int column = sortList.getPrimaryColumn();
      boolean ascending = sortList.isPrimaryAscending();

      SelectionGridCellFormatter formatter = grid.getSelectionGridCellFormatter();
      int rowCount = grid.getRowCount();
      List<Element> tdElems = new ArrayList<Element>(rowCount);
      for (int i = 0; i < rowCount; i++) {
        tdElems.add(formatter.getRawElement(i, column));
      }

      if (ascending) {
        Collections.sort(tdElems, new Comparator<Element>() {
          public int compare(Element o1, Element o2) {
            return o1.getInnerText().compareTo(o2.getInnerText());
          }
        });
      } else {
        Collections.sort(tdElems, new Comparator<Element>() {
          public int compare(Element o1, Element o2) {
            return o2.getInnerText().compareTo(o1.getInnerText());
          }
        });
      }

      Element[] trElems = new Element[rowCount];
      for (int i = 0; i < rowCount; i++) {
        trElems[i] = DOM.getParent(tdElems.get(i));
      }

      callback.onSortingComplete(trElems);
    }
  }

  public class ColumnSorterCallback {
    private Element[] selectedRows;

    protected ColumnSorterCallback(Element[] selectedRows) {
      this.selectedRows = selectedRows;
    }

    public void onSortingComplete(int[] trIndexes) {
      SelectionGridRowFormatter formatter = getSelectionGridRowFormatter();
      Element[] trElems = new Element[trIndexes.length];
      for (int i = 0; i < trElems.length; i++) {
        trElems[i] = formatter.getRawElement(trIndexes[i]);
      }

      onSortingComplete(trElems);
    }

    public void onSortingComplete(Element[] trElems) {
      applySort(trElems);
      onSortingComplete();
    }

    public void onSortingComplete() {
      for (int i = 0; i < selectedRows.length; i++) {
        int rowIndex = getRowIndex(selectedRows[i]);
        if (rowIndex >= 0) {
          selectRow(rowIndex, false);
        }
      }

      fireColumnSorted();
    }
  }

  private ColumnSorter columnSorter = null;
  private ColumnSortList columnSortList = new ColumnSortList();

  public SortableGrid() {
    super();
  }

  public SortableGrid(int rows, int columns) {
    this();
    resize(rows, columns);
  }

  public HandlerRegistration addColumnSortHandler(ColumnSortHandler handler) {
    return addHandler(handler, ColumnSortEvent.getType());
  }

  public ColumnSorter getColumnSorter() {
    return getColumnSorter(false);
  }

  public ColumnSortList getColumnSortList() {
    return columnSortList;
  }

  public void moveRowDown(int row) {
    swapRows(row, row + 1);
  }

  public void moveRowUp(int row) {
    swapRows(row, row - 1);
  }

  public void reverseRows() {
    int lastRow = numRows - 1;
    for (int i = 0; i < numRows / 2; i++) {
      swapRowsRaw(i, lastRow);
      lastRow--;
    }

    for (ColumnSortInfo sortInfo : columnSortList) {
      sortInfo.setAscending(!sortInfo.isAscending());
    }
    fireColumnSorted();
  }

  public void setColumnSorter(ColumnSorter sorter) {
    this.columnSorter = sorter;
  }

  public void setColumnSortList(ColumnSortList columnSortList) {
    setColumnSortList(columnSortList, true);
  }

  public void setColumnSortList(ColumnSortList columnSortList, boolean fireEvents) {
    Assert.notNull(columnSortList, "columnSortList cannot be null");
    this.columnSortList = columnSortList;
    if (fireEvents) {
      fireColumnSorted();
    }
  }

  public void sortColumn(int column) {
    if (column == columnSortList.getPrimaryColumn()) {
      sortColumn(column, !columnSortList.isPrimaryAscending());
    } else {
      sortColumn(column, true);
    }
  }

  public void sortColumn(int column, boolean ascending) {
    Assert.nonNegative(column, "Cannot access a column with a negative index: " + column);
    Assert.isTrue(column < numColumns,
        "Column index: " + column + ", Column size: " + numColumns);

    columnSortList.add(new ColumnSortInfo(column, ascending));

    Element[] selectedRows = getSelectedRowsMap().values().toArray(new Element[0]);
    deselectAllRows();
    getColumnSorter(true).onSortColumn(this, columnSortList,
        new SortableGrid.ColumnSorterCallback(selectedRows));
  }

  public void swapRows(int row1, int row2) {
    checkRowBounds(row1);
    checkRowBounds(row2);
    swapRowsRaw(row1, row2);
  }

  protected void fireColumnSorted() {
    fireEvent(new ColumnSortEvent(columnSortList));
  }

  protected ColumnSorter getColumnSorter(boolean createAsNeeded) {
    if ((columnSorter == null) && createAsNeeded) {
      columnSorter = new DefaultColumnSorter();
    }
    return columnSorter;
  }

  protected void swapRowsRaw(int row1, int row2) {
    Element tbody = getBodyElement();
    if (row1 == row2 + 1) {
      Element tr = getSelectionGridRowFormatter().getRawElement(row1);
      int index = TableRowElement.as(tr).getRowIndex();
      DOM.removeChild(tbody, tr);
      DOM.insertChild(tbody, tr, index - 1);
    } else if (row2 == row1 + 1) {
      Element tr = getSelectionGridRowFormatter().getRawElement(row2);
      int index = TableRowElement.as(tr).getRowIndex();
      DOM.removeChild(tbody, tr);
      DOM.insertChild(tbody, tr, index - 1);
    } else if (row1 == row2) {
      return;
    } else {
      Element tr1 = getSelectionGridRowFormatter().getRawElement(row1);
      Element tr2 = getSelectionGridRowFormatter().getRawElement(row2);
      int index1 = TableRowElement.as(tr1).getRowIndex();
      int index2 = TableRowElement.as(tr2).getRowIndex();
      DOM.removeChild(tbody, tr1);
      DOM.removeChild(tbody, tr2);

      if (row1 > row2) {
        DOM.insertChild(tbody, tr1, index2);
        DOM.insertChild(tbody, tr2, index1);
      } else if (row1 < row2) {
        DOM.insertChild(tbody, tr2, index1);
        DOM.insertChild(tbody, tr1, index2);
      }
    }

    Map<Integer, Element> selectedRows = getSelectedRowsMap();
    Element tr1 = selectedRows.remove(new Integer(row1));
    Element tr2 = selectedRows.remove(new Integer(row2));
    if (tr1 != null) {
      selectedRows.put(new Integer(row2), tr1);
    }
    if (tr2 != null) {
      selectedRows.put(new Integer(row1), tr2);
    }
  }

  void applySort(Element[] trElems) {
    Element bodyElem = getBodyElement();
    for (int i = trElems.length - 1; i >= 0; i--) {
      if (trElems[i] != null) {
        DOM.removeChild(bodyElem, trElems[i]);
        DOM.insertChild(bodyElem, trElems[i], 0);
      }
    }
  }
}
