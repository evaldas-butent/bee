package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class ReadyForUpdateEvent extends GwtEvent<ReadyForUpdateEvent.Handler> {

  public interface Handler extends EventHandler {
    void onReadyForUpdate(ReadyForUpdateEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final IsRow rowValue;
  private final IsColumn column;

  private final String oldValue;
  private final String newValue;

  private final boolean rowMode;

  public ReadyForUpdateEvent(IsRow rowValue, IsColumn column, String oldValue, String newValue,
      boolean rowMode) {
    this.rowValue = rowValue;
    this.column = column;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.rowMode = rowMode;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public IsColumn getColumn() {
    return column;
  }

  public String getNewValue() {
    return newValue;
  }

  public String getOldValue() {
    return oldValue;
  }

  public IsRow getRowValue() {
    return rowValue;
  }

  public boolean isRowMode() {
    return rowMode;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReadyForUpdate(this);
  }
}
