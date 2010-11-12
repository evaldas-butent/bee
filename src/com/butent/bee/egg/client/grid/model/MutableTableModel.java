package com.butent.bee.egg.client.grid.model;

import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.egg.client.grid.event.HasRowInsertionHandlers;
import com.butent.bee.egg.client.grid.event.HasRowRemovalHandlers;
import com.butent.bee.egg.client.grid.event.HasRowValueChangeHandlers;
import com.butent.bee.egg.client.grid.event.RowInsertionEvent;
import com.butent.bee.egg.client.grid.event.RowInsertionHandler;
import com.butent.bee.egg.client.grid.event.RowRemovalEvent;
import com.butent.bee.egg.client.grid.event.RowRemovalHandler;
import com.butent.bee.egg.client.grid.event.RowValueChangeEvent;
import com.butent.bee.egg.client.grid.event.RowValueChangeHandler;

public abstract class MutableTableModel<RowType> extends TableModel<RowType> implements
    HasRowInsertionHandlers, HasRowRemovalHandlers, HasRowValueChangeHandlers<RowType> {
  public HandlerRegistration addRowInsertionHandler(RowInsertionHandler handler) {
    return addHandler(RowInsertionEvent.getType(), handler);
  }

  public HandlerRegistration addRowRemovalHandler(RowRemovalHandler handler) {
    return addHandler(RowRemovalEvent.getType(), handler);
  }

  public HandlerRegistration addRowValueChangeHandler(RowValueChangeHandler<RowType> handler) {
    return addHandler(RowValueChangeEvent.getType(), handler);
  }

  public void insertRow(int beforeRow) {
    if (onRowInserted(beforeRow)) {
      fireEvent(new RowInsertionEvent(beforeRow));

      int numRows = getRowCount();
      if (numRows != UNKNOWN_ROW_COUNT) {
        setRowCount(numRows + 1);
      }
    }
  }

  public void removeRow(int row) {
    if (onRowRemoved(row)) {
      fireEvent(new RowRemovalEvent(row));

      int numRows = getRowCount();
      if (numRows != UNKNOWN_ROW_COUNT) {
        setRowCount(numRows - 1);
      }
    }
  }

  public void setRowValue(int row, RowType rowValue) {
    if (onSetRowValue(row, rowValue)) {
      fireEvent(new RowValueChangeEvent<RowType>(row, rowValue));

      int numRows = getRowCount();
      if (numRows != UNKNOWN_ROW_COUNT && row >= numRows) {
        setRowCount(row + 1);
      }
    }
  }

  protected abstract boolean onRowInserted(int beforeRow);

  protected abstract boolean onRowRemoved(int row);

  protected abstract boolean onSetRowValue(int row, RowType rowValue);
}
