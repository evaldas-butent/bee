package com.butent.bee.client.grid;

import com.google.gwt.cell.client.FieldUpdater;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;

/**
 * Updates cell values from data sources.
 */

public class CellUpdater implements FieldUpdater<IsRow, String> {
  private int column;

  public CellUpdater(int column) {
    this.column = column;
  }

  @Override
  public void update(int index, IsRow object, String value) {
    if (object instanceof BeeRow) {
      ((BeeRow) object).preliminaryUpdate(column, value);
    }
  }
}
