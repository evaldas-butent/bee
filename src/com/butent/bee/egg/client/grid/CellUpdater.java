package com.butent.bee.egg.client.grid;

import com.google.gwt.cell.client.FieldUpdater;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.shared.data.BeeView;

public class CellUpdater implements FieldUpdater<Integer, String> {
  private BeeView view;
  private int column;
  private CellKeyProvider keyProvider;
  
  public CellUpdater(BeeView view, int column, CellKeyProvider keyProvider) {
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
  }
}
