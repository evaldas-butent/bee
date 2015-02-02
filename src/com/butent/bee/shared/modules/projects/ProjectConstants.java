package com.butent.bee.shared.modules.projects;

import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.EnumUtils;

/**
 * Constants of Projects module.
 */
public final class ProjectConstants {

  public enum ProjectEvent implements HasCaption {
    COMMENT(Localized.getConstants().comment(), Localized.getConstants().crmActionComment(),
        FontAwesome.COMMENT_O),
    EDIT(Localized.getConstants().prjEventEdited(), null, null);

    private final String caption;
    private final String commandLabel;
    private final FontAwesome commandIcon;

    private ProjectEvent(String caption, String commandLabel, FontAwesome commandIcon) {
      this.caption = caption;
      this.commandLabel = commandLabel;
      this.commandIcon = commandIcon;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public String getCommandLabel() {
      return commandLabel;
    }

    public FontAwesome getCommandIcon() {
      return commandIcon;
    }
  }

  public static final String ALS_PROJECT_START_DATE = "ProjectStartDate";
  public static final String ALS_PROJECT_END_DATE = "ProjectEndDate";
  public static final String ALS_STAGE_NAME = "StageName";
  public static final String ALS_STAGE_START = "StageStart";
  public static final String ALS_STAGE_END = "StageEnd";
  public static final String ALS_CHART_FLOW_COLOR = "ChartFlowColor";
  public static final String ALS_VIEW_NAME = "ViewName";
  public static final String ALS_TASK_STATUS = "Status";

  public static final String ALS_CHART_START = "ChartStart";
  public static final String ALS_CHART_END = "ChartEnd";
  public static final String ALS_CHART_CAPTION = "ChartCaption";
  public static final String ALS_CHART_ID = "ChartID";

  public static final String ALS_PUBLISHER_FIRST_NAME = "PublisherFirstName";
  public static final String ALS_PUBLISHER_LAST_NAME = "PublisherLastName";
  public static final String ALS_FILTERED_PROJECT_USER = "FilteredProjectUser";

  public static final String ALS_ROW_ID = "RowID";
  public static final String ALS_TASK_COUNT = "TaskCount";

  public static final String COL_EXPECTED_TASKS_DURATION = "ExpectedTasksDuration";
  public static final String COL_ACTUAL_TASKS_DURATION = "ActualTasksDuration";

  public static final String COL_PROJECT = "Project";
  public static final String COL_PROJECT_NAME = "Name";
  public static final String COL_PROJECT_END_DATE = "EndDate";
  public static final String COL_PROJECT_STAGE = "ProjectStage";
  public static final String COL_PROJECT_START_DATE = "StartDate";
  public static final String COL_PROJECT_STATUS = "Status";
  public static final String COL_PROJECT_OWNER = "Owner";
  public static final String COL_PROJECT_PRICE = "Price";
  public static final String COL_PUBLISHER = "Publisher";
  public static final String COL_PUBLISH_TIME = "PublishTime";
  public static final String COL_PROJECT_EVENT = "ProjectEvent";
  public static final String COL_COMMENT = "Comment";
  public static final String COL_EVENT = "Event";
  public static final String COL_EVENT_PROPERTIES = "Properties";
  public static final String COL_CAPTION = "Caption";
  public static final String COL_COMAPNY = "Company";
  public static final String COL_EXPECTED_DURATION = "ExpectedDuration";
  public static final String COL_CONTRACT_PRICE = "ContractPrice";
  public static final String COL_PROJECT_TIME_UNIT = "TimeUnit";

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

  public static final String COL_DOCUMENT_REQUIRED = "DocumentRequired";

  public static final String COL_USER_TYPE = "UserType";
  public static final String COL_RATE = "Rate";

  public static final String FORM_PROJECT = "Project";
  public static final String FORM_NEW_COMMENT = "NewProjectComment";
  public static final String FORM_NEW_PROJECT_REASON_COMMENT = "NewProjectReasonComment";

  public static final String GRID_ALL_PROJECTS = "AllProjects";
  public static final String GRID_PROJECTS = "Projects";
  public static final String GRID_PROJECT_USERS = "ProjectUsers";

  public static final String PROP_USERS = "Users";
  public static final String PROP_REASON = "Reason";
  public static final String PROP_DOCUMENT = "Document";
  public static final String PROP_DOCUMENT_LINK = "DocumentLink";
  public static final String PROP_REASON_DATA = "ReasonData";

  public static final String PRM_PROJECT_COMMON_RATE = "ProjectCommonRate";
  public static final String PRM_PROJECT_HOUR_UNIT = "ProjectHourUnit";

  public static final String SVC_PREFFIX = "svc_prj_";
  public static final String SVC_GET_PROJECT_CHART_DATA = SVC_PREFFIX + "GetChartData";
  public static final String SVC_GET_TIME_UNITS = SVC_PREFFIX + "GetTimeUnits";

  public static final String VAR_PROJECT = "Project";

  public static final String VIEW_PROJECTS = "Projects";
  public static final String VIEW_PROJECT_USERS = "ProjectUsers";
  public static final String VIEW_PROJECT_STAGES = "ProjectStages";
  public static final String VIEW_PROJECT_DATES = "ProjectDates";
  public static final String VIEW_PROJECT_FILES = "ProjectFiles";
  public static final String VIEW_PROJECT_EVENTS = "ProjectEvents";

  /**
   * Register module Enumerations.
   */
  public static void register() {
    EnumUtils.register(ProjectPriority.class);
    EnumUtils.register(ProjectStatus.class);
    EnumUtils.register(ProjectEvent.class);
  }

  private ProjectConstants() {
  }
}
