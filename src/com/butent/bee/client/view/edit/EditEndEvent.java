package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RelationInfo;

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
  private final RelationInfo relationInfo;

  private final String oldValue;
  private final String newValue;

  public EditEndEvent(IsRow rowValue, IsColumn column, RelationInfo relationInfo,
      String oldValue, String newValue) {
    this.rowValue = rowValue;
    this.column = column;
    this.relationInfo = relationInfo;
    this.oldValue = oldValue;
    this.newValue = newValue;
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

  public RelationInfo getRelationInfo() {
    return relationInfo;
  }

  public IsRow getRowValue() {
    return rowValue;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditEnd(this);
  }
}
