package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.utils.EnumUtils;

/**
 * Constants of Projects module.
 */
public final class ProjectConstants {

  public static final String ALS_PROJECT_START_DATE = "ProjectStartDate";
  public static final String ALS_PROJECT_END_DATE = "ProjectEndDate";
  public static final String ALS_STAGE_NAME = "StageName";
  public static final String ALS_STAGE_START = "StageStart";
  public static final String ALS_STAGE_END = "StageEnd";
  public static final String ALS_CHART_FLOW_COLOR = "ChartFlowColor";
  public static final String ALS_VIEW_NAME = "ViewName";

  public static final String ALS_CHART_START = "ChartStart";
  public static final String ALS_CHART_END = "ChartEnd";
  public static final String ALS_CHART_CAPTION = "ChartCaption";
  public static final String ALS_CHART_ID = "ChartID";

  public static final String COL_PROJECT = "Project";
  public static final String COL_PROJECT_END_DATE = "EndDate";
  public static final String COL_PROJECT_STAGE = "ProjectStage";
  public static final String COL_PROJECT_START_DATE = "StartDate";
  public static final String COL_PROJECT_STATUS = "Status";
  public static final String COL_PROJECT_OWNER = "Owner";

  public static final String COL_PSC_FOOTER_HEIGHT = "PSCFooterHeight";
  public static final String COL_PSC_HEADER_HEIGHT = "PSCHeaderHeight";
  public static final String COL_PSC_ROW_HEIGHT = "PSCRowHeight";
  public static final String COL_PSC_STRIP_OPACITY = "PSCStripOpacity";

  public static final String COL_STAGE_NAME = "Name";
  public static final String COL_STAGE_START_DATE = "StartDate";
  public static final String COL_STAGE_END_DATE = "EndDate";

  public static final String COL_DATES_START_DATE = "StartDate";
  public static final String COL_DATES_END_DATE = "EndDate";
  public static final String COL_DATES_COLOR = "Color";
  public static final String COL_DATES_NOTE = "Note";

  public static final String COL_USER_TYPE = "UserType";

  public static final String FORM_PROJECT = "Project";

  public static final String GRID_ALL_PROJECTS = "AllProjects";
  public static final String GRID_PROJECTS = "Projects";
  public static final String GRID_PROJECT_USERS = "ProjectUsers";

  public static final String PROP_USERS = "Users";

  public static final String SVC_PREFFIX = "svc_prj_";
  public static final String SVC_GET_PROJECT_CHART_DATA = SVC_PREFFIX + "GetChartData";

  public static final String VAR_PROJECT = "Project";

  public static final String VIEW_PROJECTS = "Projects";
  public static final String VIEW_PROJECT_USERS = "ProjectUsers";
  public static final String VIEW_PROJECT_STAGES = "ProjectStages";
  public static final String VIEW_PROJECT_DATES = "ProjectDates";

  /**
   * Register module Enumerations.
   */
  public static void register() {
    EnumUtils.register(ProjectPriority.class);
    EnumUtils.register(ProjectStatus.class);
  }

  private ProjectConstants() {
  }
}
