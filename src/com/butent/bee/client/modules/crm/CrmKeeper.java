package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class CrmKeeper {

  private static final String COMPANY_TIMES_REPORT = "companytimes";
  private static final String TYPE_HOURS_REPORT = "typehours";
  private static final String USERS_HOURS_REPORT = "usershours";

  private static class RowTransformHandler implements RowTransformEvent.Handler {

    private final List<String> taskColumns = Lists.newArrayList(COL_SUMMARY, COL_COMPANY_NAME,
        COL_EXECUTOR_FIRST_NAME, COL_EXECUTOR_LAST_NAME, COL_FINISH_TIME);

    private DataInfo taskViewInfo = null;

    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_TASKS)) {
        event.setResult(BeeUtils.joinWords(DataUtils.join(getTaskViewInfo(), event.getRow(),
            taskColumns, BeeConst.STRING_SPACE), getTaskStatus(event.getRow())));
      }
    }

    private String getTaskStatus(BeeRow row) {
      TaskStatus status = NameUtils.getEnumByIndex(TaskStatus.class,
          row.getInteger(getTaskViewInfo().getColumnIndex(COL_STATUS)));
      return (status == null) ? null : status.getCaption();
    }

    private DataInfo getTaskViewInfo() {
      if (this.taskViewInfo == null) {
        this.taskViewInfo = Data.getDataInfo(VIEW_TASKS);
      }
      return this.taskViewInfo;
    }
  }

  public static void register() {
    FormFactory.registerFormInterceptor(FORM_NEW_TASK, new TaskBuilder());
    FormFactory.registerFormInterceptor(FORM_TASK, new TaskEditor());

    BeeKeeper.getMenu().registerMenuCallback("task_list", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        TaskList.open(parameters);
      }
    });

    BeeKeeper.getMenu().registerMenuCallback("tasks_reports", new MenuManager.MenuCallback() {

      @Override
      public void onSelection(String parameters) {

        if (BeeUtils.startsSame(parameters, COMPANY_TIMES_REPORT)) {
          FormFactory.openForm(FORM_TASKS_REPORT, new TasksReportsInterceptor(
              TasksReportsInterceptor.ReportType.COMPANY_TIMES));
        } else if (BeeUtils.startsSame(parameters, TYPE_HOURS_REPORT)) {
          FormFactory.openForm(FORM_TASKS_REPORT, new TasksReportsInterceptor(
              TasksReportsInterceptor.ReportType.TYPE_HOURS));
        } else if (BeeUtils.startsSame(parameters, USERS_HOURS_REPORT)) {
          FormFactory.openForm(FORM_TASKS_REPORT, new TasksReportsInterceptor(
              TasksReportsInterceptor.ReportType.USERS_HOURS));
        } else {
          Global.showError("Service type '" + parameters + "' not found");
        }
      }
    });

    SelectorEvent.register(new TaskSelectorHandler());

    DocumentHandler.register();

    String key = Captions.register(CrmConstants.TaskPriority.class);
    Captions.registerColumn(VIEW_TASKS, COL_PRIORITY, key);
    Captions.registerColumn(VIEW_TASK_TEMPLATES, COL_PRIORITY, key);
    Captions.registerColumn(VIEW_RECURRING_TASKS, COL_PRIORITY, key);

    key = Captions.register(CrmConstants.TaskEvent.class);
    Captions.registerColumn(VIEW_TASK_EVENTS, COL_EVENT, key);

    key = Captions.register(CrmConstants.TaskStatus.class);
    Captions.registerColumn(VIEW_TASKS, COL_STATUS, key);

    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);
  }

  static ParameterList createTaskRequestParameters(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CRM_MODULE);
    args.addQueryItem(CRM_METHOD, method);
    return args;
  }

  static ParameterList createTaskRequestParameters(TaskEvent event) {
    return createTaskRequestParameters(CRM_TASK_PREFIX + event.name());
  }

  private CrmKeeper() {
  }
}
