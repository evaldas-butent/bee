package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.egg.client.grid.event.TableEvent.Row;

import java.util.Set;
import java.util.TreeSet;

public class RowSelectionEvent extends GwtEvent<RowSelectionHandler> {
  private static Type<RowSelectionHandler> TYPE;

  public static void fire(HasRowSelectionHandlers source, Set<Row> oldList,
      Set<Row> newList) {
    if (TYPE != null) {
      RowSelectionEvent event = new RowSelectionEvent(oldList, newList);
      source.fireEvent(event);
    }
  }

  public static Type<RowSelectionHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<RowSelectionHandler>();
    }
    return TYPE;
  }

  private Set<Row> oldValue;
  private Set<Row> newValue;

  public RowSelectionEvent(Set<Row> oldList, Set<Row> newList) {
    this.oldValue = oldList;
    this.newValue = newList;
  }

  @Override
  public final Type<RowSelectionHandler> getAssociatedType() {
    return TYPE;
  }

  public Set<Row> getDeselectedRows() {
    Set<Row> deselected = new TreeSet<Row>();
    Set<Row> oldList = getOldValue();
    Set<Row> newList = getNewValue();
    for (Row row : oldList) {
      if (!newList.contains(row)) {
        deselected.add(row);
      }
    }
    return deselected;
  }

  public Set<Row> getNewValue() {
    return newValue;
  }

  public Set<Row> getOldValue() {
    return oldValue;
  }

  public Set<Row> getSelectedRows() {
    Set<Row> selected = new TreeSet<Row>();
    Set<Row> oldList = getOldValue();
    Set<Row> newList = getNewValue();
    for (Row row : newList) {
      if (!oldList.contains(row)) {
        selected.add(row);
      }
    }
    return selected;
  }

  @Override
  protected void dispatch(RowSelectionHandler handler) {
    handler.onRowSelection(this);
  }

}
