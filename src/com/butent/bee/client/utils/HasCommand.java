package com.butent.bee.client.utils;

import com.google.gwt.user.client.Command;

/**
 * Requires implementing classes to have methods for getting and setting commands.
 */

public interface HasCommand {
  Command getCommand();

  void setCommand(Command command);
}
