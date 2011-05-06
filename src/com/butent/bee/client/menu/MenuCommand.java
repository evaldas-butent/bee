package com.butent.bee.client.menu;

import com.butent.bee.client.Global;
import com.butent.bee.client.utils.BeeCommand;

/**
 * Extends {@code BeeCommand} class for command execution with it's service name and parameters.
 */

public class MenuCommand extends BeeCommand {

  public MenuCommand() {
    super();
  }

  public MenuCommand(String service) {
    super(service);
  }

  public MenuCommand(String service, String parameters) {
    super(service, parameters);
  }

  @Override
  public void execute() {
    Global.showDialog(getService(), getParameters());
  }

}
