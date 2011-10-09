package com.butent.bee.client.menu;

import com.butent.bee.client.Global;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.shared.utils.BeeUtils;

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
    String svc = getService();
    String args = getParameters();
    
    if (BeeUtils.same(svc, "form") && !BeeUtils.isEmpty(args)) {
      FormFactory.openForm(args);
    } else if (BeeUtils.same(svc, "grid") && !BeeUtils.isEmpty(args)) {
      GridFactory.openGrid(args);
    } else {
      Global.showDialog(svc, args);
    }
  }
}
