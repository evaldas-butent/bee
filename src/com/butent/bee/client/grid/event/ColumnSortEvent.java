package com.butent.bee.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.grid.model.TableModelHelper.ColumnSortList;

public class ColumnSortEvent extends GwtEvent<ColumnSortHandler> {

  private static Type<ColumnSortHandler> TYPE;

  public static <S extends HasColumnSortHandlers & HasHandlers> void fire(S source,
      ColumnSortList lst) {
    if (TYPE != null) {
      ColumnSortEvent event = new ColumnSortEvent(lst);
      source.fireEvent(event);
    }
  }

  public static Type<ColumnSortHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<ColumnSortHandler>();
    }
    return TYPE;
  }
  
  private ColumnSortList sortList;

  public ColumnSortEvent(ColumnSortList sortList) {
    this.sortList = sortList;
  }

  @Override
  public Type<ColumnSortHandler> getAssociatedType() {
    return TYPE;
  }

  public ColumnSortList getColumnSortList() {
    return sortList;
  }
  
  @Override
  protected void dispatch(ColumnSortHandler handler) {
    handler.onColumnSorted(this);
  }

}
