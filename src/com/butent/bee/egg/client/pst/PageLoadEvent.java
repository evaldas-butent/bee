package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.GwtEvent;

public class PageLoadEvent extends GwtEvent<PageLoadHandler> {
  private static Type<PageLoadHandler> TYPE;

  public static void fire(HasPageLoadHandlers source, int page) {
    if (TYPE != null) {
      PageLoadEvent event = new PageLoadEvent(page);
      source.fireEvent(event);
    }
  }

  public static Type<PageLoadHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<PageLoadHandler>();
    }
    return TYPE;
  }
  
  private int page;

  public PageLoadEvent(int page) {
    this.page = page;
  }
  
  @Override
  public Type<PageLoadHandler> getAssociatedType() {
    return TYPE;
  }

  public int getPage() {
    return page;
  }

  @Override
  protected void dispatch(PageLoadHandler handler) {
    handler.onPageLoad(this);
  }

}
