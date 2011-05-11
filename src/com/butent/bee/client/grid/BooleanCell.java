package com.butent.bee.client.grid;

import com.google.gwt.cell.client.CheckboxCell;

/**
 * Enables using a cell for representing boolean data.
 */

public class BooleanCell extends CheckboxCell {

  public BooleanCell() {
    this(false, true);
  }

  public BooleanCell(boolean dependsOnSelection, boolean handlesSelection) {
    super(dependsOnSelection, handlesSelection);
  }
}
