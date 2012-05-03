package com.butent.bee.client.utils;

import com.google.gwt.core.client.Scheduler;

/**
 * Requires implementing classes to have methods for getting and setting commands.
 */

public interface HasCommand {
  Scheduler.ScheduledCommand getCommand();

  void setCommand(Scheduler.ScheduledCommand command);
}
