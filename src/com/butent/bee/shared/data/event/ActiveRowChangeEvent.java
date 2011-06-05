package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsRow;

public class ActiveRowChangeEvent extends GwtEvent<ActiveRowChangeEvent.Handler> {
  
  public interface Handler extends EventHandler {
    void onActiveRowChange(ActiveRowChangeEvent event);
  }
  
  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final IsRow rowValue;

  public ActiveRowChangeEvent(IsRow rowValue) {
    super();
    this.rowValue = rowValue;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
  
  public IsRow getRowValue() {
    return rowValue;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onActiveRowChange(this);
  }
}
