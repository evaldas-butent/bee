package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Handles edit event ending, gets old and new values of edited data.
 */

public class EditEndEvent extends GwtEvent<EditEndEvent.Handler> {

  /**
   * Requires implementing methods to have a method to handle edit end.
   */

  public interface Handler extends EventHandler {
    void onEditEnd(EditEndEvent event);
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
  
  private final Integer keyCode;
  private final boolean hasModifiers;
  
  private final String widgetId;

  public EditEndEvent(IsRow rowValue, IsColumn column, String oldValue, String newValue,
      boolean rowMode, Integer keyCode, boolean hasModifiers, String widgetId) {
    this.rowValue = rowValue;
    this.column = column;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.rowMode = rowMode;
    
    this.keyCode = keyCode;
    this.hasModifiers = hasModifiers;
    this.widgetId = widgetId;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public IsColumn getColumn() {
    return column;
  }

  public Integer getKeyCode() {
    return keyCode;
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

  public String getWidgetId() {
    return widgetId;
  }

  public boolean hasModifiers() {
    return hasModifiers;
  }

  public boolean isRowMode() {
    return rowMode;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditEnd(this);
  }
}
