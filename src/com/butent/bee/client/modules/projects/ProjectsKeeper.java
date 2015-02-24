package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.trade.SalesGrid;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.rights.Module;

/**
 * Client-side projects module handler.
 */
public final class ProjectsKeeper {

  public static final String STYLE_PREFIX = "bee-prj-";

  /**
   * Creates rpc parameters of projects module.
   * 
   * @param method name of method.
   * @return rpc parameters to call queries of server-side.
   */
  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.PROJECTS, method);
  }

  /**
   * Register projects client-side module handler.
   */
  public static void register() {
    /* Register grid handlers */
    GridFactory.registerGridInterceptor(GRID_ALL_PROJECTS, new AllProjectsGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECTS, new ProjectsGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_USERS, new ProjectUsersGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_STAGES, new ProjectStagesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_INCOMES, new ProjectIncomesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_INVOICES, new SalesGrid());

    /* Register form handlers */
    FormFactory.registerFormInterceptor(FORM_PROJECT, new ProjectForm());
    FormFactory.registerFormInterceptor(FORM_PROJECT_STAGE, new ProjectStageForm());
  }

  private ProjectsKeeper() {

  }
}
