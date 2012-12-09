package com.butent.bee.client.menu;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager.MenuCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.Command;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Extends {@code BeeCommand} class for command execution with it's service name and parameters.
 */

public class MenuCommand extends Command {
  
  private static final String SERVICE_FORM = "form";
  private static final String SERVICE_GRID = "grid";
  private static final String SERVICE_NEW = "new";

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
    
    if (!BeeUtils.isEmpty(args)) {
      if (BeeUtils.same(svc, SERVICE_FORM)) {
        FormFactory.openForm(args);
        return;
      }

      if (BeeUtils.same(svc, SERVICE_GRID)) {
        GridFactory.openGrid(args);
        return;
      }

      if (BeeUtils.same(svc, SERVICE_NEW)) {
        RowFactory.createRow(args);
        return;
      }
    }
    
    if (!BeeUtils.isEmpty(svc)) {
      MenuCallback callback = BeeKeeper.getMenu().getMenuCallback(svc);
      if (callback != null) {
        callback.onSelection(args);
        return;
      }
    }
      
    Global.showError(Lists.newArrayList("Menu service not recognized", svc, args));
  }
}
