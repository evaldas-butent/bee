package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsRow;

public class EditEndEvent extends GwtEvent<EditEndEvent.Handler> { 
  
  public interface Handler extends EventHandler {
    void onEditEnd(EditEndEvent event);
  }
  
  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final IsRow rowValue;
  private final String columnId;

  private final String oldValue;
  private final String newValue;
  
  public EditEndEvent(IsRow rowValue, String columnId, String oldValue, String newValue) {
    this.rowValue = rowValue;
    this.columnId = columnId;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getColumnId() {
    return columnId;
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

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditEnd(this);
  }
}
