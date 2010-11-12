package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

public class PageCountChangeEvent extends GwtEvent<PageCountChangeHandler> {
  private static Type<PageCountChangeHandler> TYPE;

  public static void fire(HasPageCountChangeHandlers source, int oldPageCount, int newPageCount) {
    if (TYPE != null) {
      PageCountChangeEvent event = new PageCountChangeEvent(oldPageCount, newPageCount);
      source.fireEvent(event);
    }
  }

  public static Type<PageCountChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<PageCountChangeHandler>();
    }
    return TYPE;
  }
  
  private int oldPageCount;
  private int newPageCount;

  public PageCountChangeEvent(int oldPageCount, int newPageCount) {
    this.oldPageCount = oldPageCount;
    this.newPageCount = newPageCount;
  }
  
  @Override
  public Type<PageCountChangeHandler> getAssociatedType() {
    return TYPE;
  }

  public int getNewPageCount() {
    return newPageCount;
  }

  public int getOldPageCount() {
    return oldPageCount;
  }

  @Override
  protected void dispatch(PageCountChangeHandler handler) {
    handler.onPageCountChange(this);
  }

}
