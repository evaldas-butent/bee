package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.utils.BeeCommand;

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
    BeeGlobal.showDialog(getService(), getParameters());
  }

}
