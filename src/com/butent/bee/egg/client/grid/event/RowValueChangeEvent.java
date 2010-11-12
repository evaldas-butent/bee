package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

public class RowValueChangeEvent<RowType> extends GwtEvent<RowValueChangeHandler<RowType>> {
  private static Type<RowValueChangeHandler<?>> TYPE;

  public static <RowType> void fire(HasRowValueChangeHandlers<RowType> source,
      int rowIndex, RowType rowValue) {
    if (TYPE != null) {
      RowValueChangeEvent<RowType> event = new RowValueChangeEvent<RowType>(rowIndex, rowValue);
      source.fireEvent(event);
    }
  }

  public static Type<RowValueChangeHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<RowValueChangeHandler<?>>();
    }
    return TYPE;
  }
  
  private int rowIndex;
  private RowType rowValue;

  public RowValueChangeEvent(int rowIndex, RowType rowValue) {
    this.rowIndex = rowIndex;
    this.rowValue = rowValue;
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Type<RowValueChangeHandler<RowType>> getAssociatedType() {
    return (Type) TYPE;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public RowType getRowValue() {
    return rowValue; 
  }
  
  @Override
  protected void dispatch(RowValueChangeHandler<RowType> handler) {
    handler.onRowValueChange(this);
  }

}
