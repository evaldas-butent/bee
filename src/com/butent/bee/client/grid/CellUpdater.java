package com.butent.bee.client.grid;

import com.google.gwt.cell.client.FieldUpdater;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.HasTabularData;

public class CellUpdater implements FieldUpdater<Integer, String> {
  private HasTabularData view;
  private int column;
  private CellKeyProvider keyProvider;

  public CellUpdater(HasTabularData view, int column, CellKeyProvider keyProvider) {
    this.view = view;
    this.column = column;
    this.keyProvider = keyProvider;
  }

  @Override
  public void update(int index, Integer object, String value) {
    if (keyProvider != null) {
      BeeKeeper.getLog().info(keyProvider.getKeyName(), keyProvider.getKey(object));
    }
    BeeKeeper.getLog().info(object, view.getColumnNames()[column], value);

    if (view instanceof BeeRowSet) {
      view.setValue(object, column, value);
    }
  }
}
