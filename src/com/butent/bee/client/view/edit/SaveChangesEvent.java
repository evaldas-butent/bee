package com.butent.bee.client.view.edit;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.data.RowCallback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class SaveChangesEvent extends GwtEvent<SaveChangesEvent.Handler> implements Consumable {

  public interface Handler extends EventHandler {
    void onSaveChanges(SaveChangesEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static SaveChangesEvent create(IsRow oldRow, IsRow newRow, List<BeeColumn> dataColumns,
      RowCallback callback) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();

    for (int i = 0; i < dataColumns.size(); i++) {
      BeeColumn dataColumn = dataColumns.get(i);
      if (!dataColumn.isEditable()) {
        continue;
      }

      String oldValue = oldRow.getString(i);
      String newValue = newRow.getString(i);

      if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
        columns.add(dataColumn);
        oldValues.add(oldValue);
        newValues.add(newValue);
      }
    }

    return new SaveChangesEvent(oldRow, newRow, columns, oldValues, newValues, callback);
  }

  public static Type<Handler> getType() {
    return TYPE;
  }
  
  public static SaveChangesEvent of(ReadyForUpdateEvent event) {
    Assert.notNull(event);

    return new SaveChangesEvent(event.getRowValue(), DataUtils.cloneRow(event.getRowValue()),
        Lists.newArrayList(DataUtils.cloneColumn(event.getColumn())),
        Lists.newArrayList(event.getOldValue()), Lists.newArrayList(event.getNewValue()),
        event.getCallback());
  }

  private final IsRow oldRow;
  private final IsRow newRow;
  
  private final List<BeeColumn> columns;
  
  private final List<String> oldValues;
  private final List<String> newValues;
  
  private final RowCallback callback;
  
  private boolean consumed = false;
  
  public SaveChangesEvent(IsRow oldRow, IsRow newRow, List<BeeColumn> columns,
      List<String> oldValues, List<String> newValues, RowCallback callback) {
    super();
    this.oldRow = oldRow;
    this.newRow = newRow;
    this.columns = columns;
    this.oldValues = oldValues;
    this.newValues = newValues;
    this.callback = callback;
  }

  @Override
  public void consume() {
    setConsumed(true);
  }
  
  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public RowCallback getCallback() {
    return callback;
  }
  
  public List<BeeColumn> getColumns() {
    return columns;
  }

  public IsRow getNewRow() {
    return newRow;
  }

  public List<String> getNewValues() {
    return newValues;
  }

  public IsRow getOldRow() {
    return oldRow;
  }

  public List<String> getOldValues() {
    return oldValues;
  }

  public long getRowId() {
    return newRow.getId();
  }

  public long getVersion() {
    return newRow.getVersion();
  }

  @Override
  public boolean isConsumed() {
    return consumed;
  }

  public boolean isEmpty() {
    return columns.isEmpty();
  }
  
  @Override
  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSaveChanges(this);
  }
}
