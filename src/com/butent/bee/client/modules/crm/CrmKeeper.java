package com.butent.bee.client.modules.crm;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.shared.modules.crm.CrmConstants;

public class CrmKeeper {

  public static void register() {
    TaskEventHandler.register();
    BeeKeeper.getMenu().registerMenuCallback("task_list", new MenuManager.MenuCallback() {
      public void onSelection(String parameters) {
        TaskList.open(parameters);
      }
    });

    ProjectEventHandler.register();
    BeeKeeper.getMenu().registerMenuCallback("project_list", new MenuManager.MenuCallback() {
      public void onSelection(String parameters) {
        ProjectList.open(parameters);
      }
    });

    DocumentHandler.register();
    
    Global.registerCaptions(CrmConstants.Priority.class);
    Global.registerCaptions(CrmConstants.TaskEvent.class);
    Global.registerCaptions(CrmConstants.ProjectEvent.class);
  }

  private CrmKeeper() {
  }
}
