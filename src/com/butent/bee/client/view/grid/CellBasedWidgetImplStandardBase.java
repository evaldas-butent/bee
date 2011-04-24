package com.butent.bee.client.view.grid;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class CellBasedWidgetImplStandardBase extends CellBasedWidgetImplStandard {

  @Override
  public void resetFocus(ScheduledCommand command) {
    Scheduler.get().scheduleDeferred(command);
  }
}
