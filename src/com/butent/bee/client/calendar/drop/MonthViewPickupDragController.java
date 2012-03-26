package com.butent.bee.client.calendar.drop;

import com.google.gwt.user.client.ui.AbsolutePanel;

import com.butent.bee.client.dnd.PickupDragController;

public class MonthViewPickupDragController extends PickupDragController {

  public MonthViewPickupDragController(AbsolutePanel boundaryPanel,
      boolean allowDroppingOnBoundaryPanel) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
  }

  @Override
  public void dragMove() {
    try {
      super.dragMove();
    } catch (NullPointerException ex) {
    }
  }
}
