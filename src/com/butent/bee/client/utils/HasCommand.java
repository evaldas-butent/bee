package com.butent.bee.client.utils;

/**
 * Requires implementing classes to have methods for getting and setting commands.
 */

public interface HasCommand {
  BeeCommand getCommand();

  void setCommand(BeeCommand command);
}
