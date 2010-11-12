package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

public class RowCountChangeEvent extends GwtEvent<RowCountChangeHandler> {
  private static Type<RowCountChangeHandler> TYPE;

  public static void fire(HasRowCountChangeHandlers source, int oldRowCount, int newRowCount) {
    if (TYPE != null) {
      RowCountChangeEvent event = new RowCountChangeEvent(oldRowCount, newRowCount);
      source.fireEvent(event);
    }
  }

  public static Type<RowCountChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<RowCountChangeHandler>();
    }
    return TYPE;
  }
  
  private int oldRowCount;
  private int newRowCount;

  public RowCountChangeEvent(int oldRowCount, int newRowCount) {
    this.oldRowCount = oldRowCount;
    this.newRowCount = newRowCount;
  }
  
  @Override
  public Type<RowCountChangeHandler> getAssociatedType() {
    return TYPE;
  }

  public int getNewRowCount() {
    return newRowCount;
  }

  public int getOldRowCount() {
    return oldRowCount;
  }

  @Override
  protected void dispatch(RowCountChangeHandler handler) {
    handler.onRowCountChange(this);
  }

}
