package com.butent.bee.client.grid;

import com.google.gwt.cell.client.CheckboxCell;

public class BooleanCell extends CheckboxCell {

  public BooleanCell() {
    super();
  }

  public BooleanCell(boolean dependsOnSelection, boolean handlesSelection) {
    super(dependsOnSelection, handlesSelection);
  }
}
