package com.butent.bee.client.view.edit;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public class SaveChangesEvent extends GwtEvent<SaveChangesEvent.Handler> {

  public interface Handler extends EventHandler {
    boolean onSaveChanges(SaveChangesEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }
  
  public static SaveChangesEvent of(ReadyForUpdateEvent event) {
    Assert.notNull(event);

    long rid;
    long ver;
    if (event.getRowValue() == null) {
      rid = BeeConst.UNDEF;
      ver = BeeConst.UNDEF;
    } else {
      rid = event.getRowValue().getId();
      ver = event.getRowValue().getVersion();
    }
    
    return new SaveChangesEvent(rid, ver,
        Lists.newArrayList(DataUtils.cloneColumn(event.getColumn())),
        Lists.newArrayList(event.getOldValue()), Lists.newArrayList(event.getNewValue()),
        event.getCallback());
  }

  private final long rowId;
  private final long version;
  
  private final List<BeeColumn> columns;
  
  private final List<String> oldValues;
  private final List<String> newValues;
  
  private final Callback<IsRow> callback;
  
  public SaveChangesEvent(long rowId, long version, List<BeeColumn> columns,
      List<String> oldValues, List<String> newValues, Callback<IsRow> callback) {
    super();
    this.rowId = rowId;
    this.version = version;
    this.columns = columns;
    this.oldValues = oldValues;
    this.newValues = newValues;
    this.callback = callback;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Callback<IsRow> getCallback() {
    return callback;
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
