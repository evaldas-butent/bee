package com.butent.bee.egg.client.pst;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;

import com.butent.bee.egg.client.grid.BeeGrid;
import com.butent.bee.egg.client.pst.TableEvent.Row;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SelectionGrid extends BeeGrid implements HasRowHighlightHandlers,
    HasRowUnhighlightHandlers, HasCellHighlightHandlers,
    HasCellUnhighlightHandlers, HasRowSelectionHandlers {

  public class SelectionGridCellFormatter extends BeeCellFormatter {
    @Override
    protected Element getRawElement(int row, int column) {
      if (selectionPolicy.hasInputColumn()) {
        column += 1;
      }
      return super.getRawElement(row, column);
    }
  }

  public class SelectionGridRowFormatter extends BeeRowFormatter {
    @Override
    protected Element getRawElement(int row) {
      return super.getRawElement(row);
    }
  }

  public static enum SelectionPolicy {
    ONE_ROW(null), MULTI_ROW(null), CHECKBOX("<input type='checkbox'/>"), RADIO(
        "<input name='%NAME%' type='radio'/>");

    private String inputHtml;

    private SelectionPolicy(String inputHtml) {
      this.inputHtml = inputHtml;
    }

    public boolean hasInputColumn() {
      return inputHtml != null;
    }

    private String getInputHtml() {
      return inputHtml;
    }
  }

  private static int uniqueID = 0;
  
  private static String styleSelected = "selected"; 
  private static String styleHighlighted = "highlighted"; 

  private Element highlightedCellElem = null;
  private int highlightedCellIndex = -1;

  private Element highlightedRowElem = null;
  private int highlightedRowIndex = -1;

  private int id;

  private int lastSelectedRowIndex = -1;
  private Map<Integer, Element> selectedRows = new HashMap<Integer, Element>();

  private boolean selectionEnabled = true;
  private SelectionPolicy selectionPolicy = SelectionPolicy.MULTI_ROW;

  public SelectionGrid() {
    super();
    id = uniqueID++;
    setCellFormatter(new SelectionGridCellFormatter());
    setRowFormatter(new SelectionGridRowFormatter());

    sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONMOUSEDOWN
        | Event.ONCLICK);
  }

  public SelectionGrid(int rows, int columns) {
    this();
    resize(rows, columns);
  }

  public HandlerRegistration addCellHighlightHandler(
      CellHighlightHandler handler) {
    return addHandler(handler, CellHighlightEvent.getType());
  }

  public HandlerRegistration addCellUnhighlightHandler(
      CellUnhighlightHandler handler) {
    return addHandler(handler, CellUnhighlightEvent.getType());
  }

  public HandlerRegistration addRowHighlightHandler(RowHighlightHandler handler) {
    return addHandler(handler, RowHighlightEvent.getType());
  }

  public HandlerRegistration addRowSelectionHandler(RowSelectionHandler handler) {
    return addHandler(handler, RowSelectionEvent.getType());
  }

  public HandlerRegistration addRowUnhighlightHandler(
      RowUnhighlightHandler handler) {
    return addHandler(handler, RowUnhighlightEvent.getType());
  }

  public void deselectAllRows() {
    deselectAllRows(true);
  }

  public void deselectRow(int row) {
    deselectRow(row, true);
  }

  @Override
  public int getDOMCellCount(int row) {
    int count = super.getDOMCellCount(row);
    if (getSelectionPolicy().hasInputColumn()) {
      count--;
    }
    return count;
  }

  public Set<Integer> getSelectedRows() {
    return selectedRows.keySet();
  }

  public SelectionGridCellFormatter getSelectionGridCellFormatter() {
    return (SelectionGridCellFormatter) getCellFormatter();
  }

  public SelectionGridRowFormatter getSelectionGridRowFormatter() {
    return (SelectionGridRowFormatter) getRowFormatter();
  }

  public SelectionPolicy getSelectionPolicy() {
    return selectionPolicy;
  }

  @Override
  public int insertRow(int beforeRow) {
    deselectAllRows();
    return super.insertRow(beforeRow);
  }

  public boolean isRowSelected(int row) {
    return selectedRows.containsKey(new Integer(row));
  }

  public boolean isSelectionEnabled() {
    return selectionEnabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    Element targetRow = null;
    Element targetCell = null;

    switch (DOM.eventGetType(event)) {
      case Event.ONMOUSEOVER:
        Element cellElem = getEventTargetCell(event);
        if (cellElem != null) {
          highlightCell(cellElem);
        }
        break;

      case Event.ONMOUSEOUT:
        Element toElem = DOM.eventGetToElement(event);
        if (highlightedRowElem != null
            && (toElem == null || !highlightedRowElem.isOrHasChild(toElem))) {
          int clientX = event.getClientX() + Window.getScrollLeft();
          int clientY = event.getClientY() + Window.getScrollTop();
          int rowLeft = highlightedRowElem.getAbsoluteLeft();
          int rowTop = highlightedRowElem.getAbsoluteTop();
          int rowWidth = highlightedRowElem.getOffsetWidth();
          int rowHeight = highlightedRowElem.getOffsetHeight();
          int rowBottom = rowTop + rowHeight;
          int rowRight = rowLeft + rowWidth;
          if (clientX > rowLeft && clientX < rowRight && clientY > rowTop
              && clientY < rowBottom) {
            return;
          }

          highlightCell(null);
        }
        break;

      case Event.ONMOUSEDOWN: {
        if (!selectionEnabled) {
          return;
        }

        targetCell = getEventTargetCell(event);
        if (targetCell == null) {
          return;
        }
        targetRow = DOM.getParent(targetCell);
        int targetRowIndex = getRowIndex(targetRow);

        if (selectionPolicy == SelectionPolicy.MULTI_ROW) {
          boolean shiftKey = DOM.eventGetShiftKey(event);
          boolean ctrlKey = DOM.eventGetCtrlKey(event)
              || DOM.eventGetMetaKey(event);

          if (ctrlKey || shiftKey) {
            event.preventDefault();
          }

          selectRow(targetRowIndex, ctrlKey, shiftKey);
        } else if (selectionPolicy == SelectionPolicy.ONE_ROW
            || (selectionPolicy == SelectionPolicy.RADIO && targetCell == targetRow.getFirstChild())) {
          selectRow(-1, targetRow, true, true);
          lastSelectedRowIndex = targetRowIndex;
        }
      }
        break;

      case Event.ONCLICK: {
        if (!selectionEnabled) {
          return;
        }

        targetCell = getEventTargetCell(event);
        if (targetCell == null) {
          return;
        }
        targetRow = DOM.getParent(targetCell);
        int targetRowIndex = getRowIndex(targetRow);

        if (selectionPolicy == SelectionPolicy.CHECKBOX
            && targetCell == targetRow.getFirstChild()) {
          selectRow(targetRowIndex, true, DOM.eventGetShiftKey(event));
        }
      }
        break;
    }
  }

  @Override
  public void removeRow(int row) {
    deselectAllRows();
    super.removeRow(row);
  }

  public void selectAllRows() {
    Set<Row> oldRowSet = getSelectedRowsSet();

    BeeRowFormatter rowFormatter = getRowFormatter();
    int rowCount = getRowCount();
    for (int i = 0; i < rowCount; i++) {
      if (!selectedRows.containsKey(i)) {
        selectRow(i, rowFormatter.getElement(i), false, false);
      }
    }

    fireRowSelectionEvent(oldRowSet);
  }

  public void selectRow(int row, boolean unselectAll) {
    selectRow(row, getRowFormatter().getElement(row), unselectAll, true);
  }

  public void selectRow(int row, boolean ctrlKey, boolean shiftKey) {
    checkRowBounds(row);

    Set<Row> oldRowList = getSelectedRowsSet();

    if (!ctrlKey) {
      deselectAllRows(false);
    }

    boolean isSelected = selectedRows.containsKey(new Integer(row));
    if (shiftKey && (lastSelectedRowIndex > -1)) {
      SelectionGridRowFormatter formatter = getSelectionGridRowFormatter();
      int firstRow = Math.min(row, lastSelectedRowIndex);
      int lastRow = Math.max(row, lastSelectedRowIndex);
      lastRow = Math.min(lastRow, getRowCount() - 1);
      for (int curRow = firstRow; curRow <= lastRow; curRow++) {
        if (isSelected) {
          deselectRow(curRow, false);
        } else {
          selectRow(curRow, formatter.getRawElement(curRow), false, false);
        }
      }

      lastSelectedRowIndex = row;
      fireRowSelectionEvent(oldRowList);
    } else if (isSelected) {
      deselectRow(row, false);
      lastSelectedRowIndex = row;
      fireRowSelectionEvent(oldRowList);
    } else {
      SelectionGridRowFormatter formatter = getSelectionGridRowFormatter();
      selectRow(row, formatter.getRawElement(row), false, false);
      lastSelectedRowIndex = row;
      fireRowSelectionEvent(oldRowList);
    }
  }

  @Override
  public void setBodyElement(Element element) {
    super.setBodyElement(element);
    if (!selectionEnabled) {
      setSelectionEnabled(selectionEnabled);
    }
  }

  public void setSelectionEnabled(boolean enabled) {
    selectionEnabled = enabled;

    if (selectionPolicy.hasInputColumn()) {
      SelectionGridCellFormatter formatter = getSelectionGridCellFormatter();
      int rowCount = getRowCount();
      for (int i = 0; i < rowCount; i++) {
        Element td = formatter.getRawElement(i, -1);
        setInputEnabled(td, enabled);
      }
    }
  }

  public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
    if (this.selectionPolicy == selectionPolicy) {
      return;
    }
    deselectAllRows();

    if (selectionPolicy.hasInputColumn()) {
      if (this.selectionPolicy.hasInputColumn()) {
        String inputHtml = getInputHtml(selectionPolicy);
        for (int i = 0; i < numRows; i++) {
          Element tr = getRowFormatter().getElement(i);
          tr.getFirstChildElement().setInnerHTML(inputHtml);
        }
      } else {
        String inputHtml = getInputHtml(selectionPolicy);
        Element td = createCell();
        td.setInnerHTML(inputHtml);
        for (int i = 0; i < numRows; i++) {
          Element tr = getRowFormatter().getElement(i);
          tr.insertBefore(td.cloneNode(true), tr.getFirstChildElement());
        }
      }
    } else if (this.selectionPolicy.hasInputColumn()) {
      for (int i = 0; i < numRows; i++) {
        Element tr = getRowFormatter().getElement(i);
        tr.removeChild(tr.getFirstChildElement());
      }
    }
    this.selectionPolicy = selectionPolicy;

    setSelectionEnabled(selectionEnabled);
  }

  @Override
  protected Element createRow() {
    Element tr = super.createRow();
    if (selectionPolicy.hasInputColumn()) {
      Element td = createCell();
      td.setPropertyString("align", "center");
      td.setInnerHTML(getInputHtml(selectionPolicy));
      DOM.insertChild(tr, td, 0);
      if (!selectionEnabled) {
        setInputEnabled(td, false);
      }
    }
    return tr;
  }

  protected void deselectAllRows(boolean fireEvent) {
    Set<Row> oldRows = null;
    if (fireEvent) {
      oldRows = getSelectedRowsSet();
    }

    boolean hasInputColumn = selectionPolicy.hasInputColumn();
    for (Element rowElem : selectedRows.values()) {
      setStyleName(rowElem, styleSelected, false);
      if (hasInputColumn) {
        setInputSelected((Element) rowElem.getFirstChildElement(), false);
      }
    }

    selectedRows.clear();

    if (fireEvent) {
      fireRowSelectionEvent(oldRows);
    }
  }

  protected void deselectRow(int row, boolean fireEvent) {
    Element rowElem = selectedRows.remove(new Integer(row));
    if (rowElem != null) {
      Set<Row> oldRows = null;
      if (fireEvent) {
        oldRows = getSelectedRowsSet();
      }

      setStyleName(rowElem, styleSelected, false);
      if (selectionPolicy.hasInputColumn()) {
        setInputSelected((Element) rowElem.getFirstChildElement(), false);
      }

      if (fireEvent) {
        fireRowSelectionEvent(oldRows);
      }
    }
  }

  protected void fireRowSelectionEvent(Set<Row> oldRowSet) {
    Set<Row> newRowList = getSelectedRowsSet();
    if (newRowList.equals(oldRowSet)) {
      return;
    }
    fireEvent(new RowSelectionEvent(oldRowSet, newRowList));
  }

  @Override
  protected int getCellIndex(Element rowElem, Element cellElem) {
    int index = super.getCellIndex(rowElem, cellElem);
    if (selectionPolicy.hasInputColumn()) {
      index--;
    }
    return index;
  }

  protected String getInputHtml(SelectionPolicy selectionPolicy) {
    String inputHtml = selectionPolicy.getInputHtml();
    if (inputHtml != null) {
      inputHtml = inputHtml.replace("%NAME%", "__beeSelectionGrid" + id);
    }
    return inputHtml;
  }

  protected Map<Integer, Element> getSelectedRowsMap() {
    return selectedRows;
  }

  protected Set<Row> getSelectedRowsSet() {
    Set<Row> rowSet = new TreeSet<Row>();
    for (Integer rowIndex : selectedRows.keySet()) {
      rowSet.add(new Row(rowIndex.intValue()));
    }
    return rowSet;
  }

  protected void highlightCell(Element cellElem) {
    if (cellElem == highlightedCellElem) {
      return;
    }

    Element rowElem = null;
    if (cellElem != null) {
      rowElem = DOM.getParent(cellElem);
    }

    if (highlightedCellElem != null) {
      setStyleName(highlightedCellElem, styleHighlighted, false);
      fireEvent(new CellUnhighlightEvent(highlightedRowIndex,
          highlightedCellIndex));
      highlightedCellElem = null;
      highlightedCellIndex = -1;

      if (rowElem != highlightedRowElem) {
        setStyleName(highlightedRowElem, styleHighlighted, false);
        fireEvent(new RowUnhighlightEvent(highlightedRowIndex));
        highlightedRowElem = null;
        highlightedRowIndex = -1;
      }
    }

    if (cellElem != null) {
      setStyleName(cellElem, styleHighlighted, true);
      highlightedCellElem = cellElem;
      highlightedCellIndex = DOM.getChildIndex(DOM.getParent(cellElem),
          cellElem);

      if (highlightedRowElem == null) {
        setStyleName(rowElem, styleHighlighted, true);
        highlightedRowElem = rowElem;
        highlightedRowIndex = getRowIndex(highlightedRowElem);
        fireEvent(new RowHighlightEvent(highlightedRowIndex));
      }

      fireEvent(new CellHighlightEvent(highlightedRowIndex, highlightedCellIndex));
    }
  }

  protected void selectRow(int row, Element rowElem, boolean unselectAll, boolean fireEvent) {
    if (row < 0) {
      row = getRowIndex(rowElem);
    }

    Integer rowI = new Integer(row);
    if (selectedRows.containsKey(rowI)) {
      return;
    }

    Set<Row> oldRowSet = null;
    if (fireEvent) {
      oldRowSet = getSelectedRowsSet();
    }

    if (unselectAll) {
      deselectAllRows(false);
    }

    selectedRows.put(rowI, rowElem);
    setStyleName(rowElem, styleSelected, true);
    if (selectionPolicy.hasInputColumn()) {
      setInputSelected((Element) rowElem.getFirstChildElement(), true);
    }

    if (fireEvent) {
      fireRowSelectionEvent(oldRowSet);
    }
  }

  protected void setInputEnabled(Element td, boolean enabled) {
    ((InputElement) td.getFirstChild()).setDisabled(!enabled);
  }

  protected void setInputSelected(Element td, boolean selected) {
    ((InputElement) td.getFirstChild()).setChecked(selected);
  }

}
