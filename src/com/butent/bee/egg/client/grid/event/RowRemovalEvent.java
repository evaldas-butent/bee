package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

public class RowRemovalEvent extends GwtEvent<RowRemovalHandler> {
  private static Type<RowRemovalHandler> TYPE;

  public static void fire(HasRowRemovalHandlers source, int rowIndex) {
    if (TYPE != null) {
      RowRemovalEvent event = new RowRemovalEvent(rowIndex);
      source.fireEvent(event);
    }
  }

  public static Type<RowRemovalHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<RowRemovalHandler>();
    }
    return TYPE;
  }
  
  private int rowIndex;

  public RowRemovalEvent(int rowIndex) {
    this.rowIndex = rowIndex;
  }
  
  @Override
  public Type<RowRemovalHandler> getAssociatedType() {
    return TYPE;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  @Override
  protected void dispatch(RowRemovalHandler handler) {
    handler.onRowRemoval(this);
  }

}
