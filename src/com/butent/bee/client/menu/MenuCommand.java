package com.butent.bee.client.menu;

import com.butent.bee.client.Global;
import com.butent.bee.client.utils.BeeCommand;

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
