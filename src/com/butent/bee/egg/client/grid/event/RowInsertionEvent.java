package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

public class RowInsertionEvent extends GwtEvent<RowInsertionHandler> {
  private static Type<RowInsertionHandler> TYPE;

  public static void fire(HasRowInsertionHandlers source, int rowIndex) {
    if (TYPE != null) {
      RowInsertionEvent event = new RowInsertionEvent(rowIndex);
      source.fireEvent(event);
    }
  }

  public static Type<RowInsertionHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<RowInsertionHandler>();
    }
    return TYPE;
  }
  
  private int rowIndex;

  public RowInsertionEvent(int rowIndex) {
    this.rowIndex = rowIndex;
  }
  
  @Override
  public Type<RowInsertionHandler> getAssociatedType() {
    return TYPE;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  @Override
  protected void dispatch(RowInsertionHandler handler) {
    handler.onRowInsertion(this);
  }

}
