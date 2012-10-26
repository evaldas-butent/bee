package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Remembers old and new values of the field being updated to make further validations with them.
 */

public class ReadyForUpdateEvent extends GwtEvent<ReadyForUpdateEvent.Handler> {

  /**
   * Requires to have a method to handle read for update event.
   */

  public interface Handler extends EventHandler {
    boolean onReadyForUpdate(ReadyForUpdateEvent event);
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
  
  private final Callback<IsRow> callback;

  public ReadyForUpdateEvent(IsRow rowValue, IsColumn column, String oldValue, String newValue,
      boolean rowMode, Callback<IsRow> callback) {
    this.rowValue = rowValue;
    this.column = column;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.rowMode = rowMode;
    this.callback = callback;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Callback<IsRow> getCallback() {
    return callback;
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
