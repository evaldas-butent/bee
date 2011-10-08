package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

public class SaveChangesEvent extends GwtEvent<SaveChangesEvent.Handler> {

  public interface Handler extends EventHandler {
    void onSaveChanges(SaveChangesEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final long rowId;
  private final long version;
  
  private final List<BeeColumn> columns;
  
  private final List<String> oldValues;
  private final List<String> newValues;
  
  public SaveChangesEvent(long rowId, long version, List<BeeColumn> columns,
      List<String> oldValues, List<String> newValues) {
    super();
    this.rowId = rowId;
    this.version = version;
    this.columns = columns;
    this.oldValues = oldValues;
    this.newValues = newValues;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public List<BeeColumn> getColumns() {
    return columns;
  }

  public List<String> getNewValues() {
    return newValues;
  }

  public List<String> getOldValues() {
    return oldValues;
  }

  public long getRowId() {
    return rowId;
  }

  public long getVersion() {
    return version;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSaveChanges(this);
  }
}
