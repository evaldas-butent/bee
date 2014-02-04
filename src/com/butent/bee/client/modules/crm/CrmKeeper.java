package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
// import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class CrmKeeper {

  private static final String COMPANY_TIMES_REPORT = "companytimes";
  private static final String TYPE_HOURS_REPORT = "typehours";
  private static final String USERS_HOURS_REPORT = "usershours";

  private static class RowTransformHandler implements RowTransformEvent.Handler {

    private final List<String> taskColumns = Lists.newArrayList(COL_SUMMARY,
        CommonsConstants.ALS_COMPANY_NAME, ALS_EXECUTOR_FIRST_NAME, ALS_EXECUTOR_LAST_NAME,
        COL_FINISH_TIME, COL_STATUS);

    private DataInfo taskViewInfo;

    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_TASKS)) {
        event.setResult(DataUtils.join(getTaskViewInfo(), event.getRow(), taskColumns,
            BeeConst.STRING_SPACE));
      }
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

    FormFactory.registerFormInterceptor(FORM_RECURRING_TASK, new RecurringTaskHandler());

    FormFactory.registerFormInterceptor(FORM_NEW_REQUEST, new RequestBuilder(null));
    FormFactory.registerFormInterceptor(FORM_REQUEST, new RequestEditor());

    GridFactory.registerGridInterceptor(GRID_REQUESTS, new RequestsGridInterceptor());

    GridFactory.registerGridInterceptor(GRID_RECURRING_TASKS, new RecurringTaskGrid()); 
    GridFactory.registerGridInterceptor(GRID_RT_FILES, 
        new FileGridInterceptor(COL_RTF_RECURRING_TASK, COL_RTF_FILE, COL_RTF_CAPTION,
            CommonsConstants.ALS_FILE_NAME));

    BeeKeeper.getMenu().registerMenuCallback("task_list", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        TaskList.open(parameters);
      }
    });

    BeeKeeper.getMenu().registerMenuCallback("task_reports", new MenuManager.MenuCallback() {
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

    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);

    // Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_ASSIGNED,
    // TaskList.getFeedFilterHandler(Feed.TASKS_ASSIGNED));
    // Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_DELEGATED,
    // TaskList.getFeedFilterHandler(Feed.TASKS_DELEGATED));
    // Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_OBSERVED,
    // TaskList.getFeedFilterHandler(Feed.TASKS_OBSERVED));
    // Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_ALL,
    // TaskList.getFeedFilterHandler(Feed.TASKS_ALL));
    
    Global.getNewsAggregator().registerAccessHandler(VIEW_TASKS, new Consumer<Long>() {
      @Override
      public void accept(Long input) {
        ParameterList params = createArgs(SVC_ACCESS_TASK);
        params.addQueryItem(VAR_TASK_ID, input);
        
        BeeKeeper.getRpc().makeRequest(params);
      }
    });
  }
  
  public static void scheduleTasks(final DateRange range) {
    Assert.notNull(range);
    
    ParameterList params = createArgs(SVC_RT_SCHEDULE);
    params.addQueryItem(COL_RT_SCHEDULE_FROM, range.getMinDays());
    if (range.size() > 1) {
      params.addQueryItem(COL_RT_SCHEDULE_UNTIL, range.getMaxDays());
    }
    
    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse() && BeeUtils.isPositiveInt(response.getResponseAsString())) {
          DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_TASKS);
          BeeKeeper.getScreen().notifyInfo(BeeUtils.joinWords(range, "sheduled",
              response.getResponseAsString(), "tasks"));

        } else {
          BeeKeeper.getScreen().notifyWarning(range.toString(), "no tasks sheduled");
        }
      }
    });
  }

  static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CRM_MODULE);
    args.addQueryItem(CRM_METHOD, method);
    return args;
  }

  static ParameterList createTaskRequestParameters(TaskEvent event) {
    return createArgs(CRM_TASK_PREFIX + event.name());
  }

  private CrmKeeper() {
  }
}
