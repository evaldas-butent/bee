package com.butent.bee.client.grid;

import com.google.gwt.cell.client.FieldUpdater;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsTable;

public class CellUpdater implements FieldUpdater<Integer, String> {
  private IsTable<?, ?> view;
  private int column;

  public CellUpdater(IsTable<?, ?> view, int column) {
    this.view = view;
    this.column = column;
  }

  @Override
  public void update(int index, Integer object, String value) {
    BeeKeeper.getLog().info(object, view.getColumnLabel(column), value);

    if (view instanceof BeeRowSet) {
      view.getRow(object).setValue(column, value);
    }
  }
}
