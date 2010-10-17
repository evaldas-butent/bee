package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.GwtEvent;

public class PageChangeEvent extends GwtEvent<PageChangeHandler> {
  private static Type<PageChangeHandler> TYPE;

  public static void fire(HasPageChangeHandlers source, int oldPage, int newPage) {
    if (TYPE != null) {
      PageChangeEvent event = new PageChangeEvent(oldPage, newPage);
      source.fireEvent(event);
    }
  }

  public static Type<PageChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<PageChangeHandler>();
    }
    return TYPE;
  }
  
  private int oldPage;
  private int newPage;

  public PageChangeEvent(int oldPage, int newPage) {
    this.oldPage = oldPage;
    this.newPage = newPage;
  }
  
  @Override
  public Type<PageChangeHandler> getAssociatedType() {
    return TYPE;
  }

  public int getNewPage() {
    return newPage;
  }

  public int getOldPage() {
    return oldPage;
  }

  @Override
  protected void dispatch(PageChangeHandler handler) {
    handler.onPageChange(this);
  }

}
