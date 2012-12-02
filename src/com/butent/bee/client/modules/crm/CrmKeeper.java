package com.butent.bee.client.modules.crm;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.shared.modules.crm.CrmConstants;

public class CrmKeeper {

  public static void register() {
    TaskEventHandler.register();
    BeeKeeper.getMenu().registerMenuCallback("task_list", new MenuManager.MenuCallback() {
      public void onSelection(String parameters) {
        TaskList.open(parameters);
      }
    });

    SelectorEvent.register(new TaskSelectorHandler());
    
    ProjectEventHandler.register();
    BeeKeeper.getMenu().registerMenuCallback("project_list", new MenuManager.MenuCallback() {
      public void onSelection(String parameters) {
        ProjectList.open(parameters);
      }
    });

    DocumentHandler.register();
    
    Global.registerCaptions(CrmConstants.TaskPriority.class);
    Global.registerCaptions(CrmConstants.TaskEvent.class);
    Global.registerCaptions(CrmConstants.TaskStatus.class);

    Global.registerCaptions(CrmConstants.ProjectEvent.class);
  }

  private CrmKeeper() {
  }
}
