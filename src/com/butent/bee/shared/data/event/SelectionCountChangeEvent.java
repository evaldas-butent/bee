package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class SelectionCountChangeEvent extends GwtEvent<SelectionCountChangeEvent.Handler> {
  
  public interface Handler extends EventHandler {
    void onSelectionCountChange(SelectionCountChangeEvent event);
  }
  
  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final int count;

  public SelectionCountChangeEvent(int count) {
    super();
    this.count = count;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
  
  public int getCount() {
    return count;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSelectionCountChange(this);
  }
}
