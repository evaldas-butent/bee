package com.butent.bee.client.modules.tasks;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import static com.butent.bee.shared.modules.tasks.TasksConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.documents.DocumentHandler;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.tasks.TasksUtils;
import com.butent.bee.shared.modules.tasks.TasksConstants.TaskEvent;
import com.butent.bee.shared.modules.tasks.TasksConstants.TaskStatus;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public final class TasksKeeper {

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

  public static void extendTask(final long taskId, final DateTime start, final DateTime finish) {
    Queries.getRow(VIEW_TASKS, taskId, new RowCallback() {
      @Override
      public void onSuccess(final BeeRow row) {
        final TaskDialog dialog = new TaskDialog(Localized.getConstants().crmTaskTermChange());

        TaskStatus status = EnumUtils.getEnumByIndex(TaskStatus.class,
            Data.getInteger(VIEW_TASKS, row, COL_STATUS));
        final boolean isScheduled = status == TaskStatus.SCHEDULED;

        final String startId = isScheduled
            ? dialog.addDateTime(Localized.getConstants().crmStartDate(), true, start) : null;
        final String endId = dialog.addDateTime(Localized.getConstants().crmFinishDate(), true,
            finish);

        final String cid = dialog.addComment(false);

        dialog.addAction(Localized.getConstants().crmTaskChangeTerm(), new ScheduledCommand() {
          @Override
          public void execute() {
            DateTime oldStart = Data.getDateTime(VIEW_TASKS, row, COL_START_TIME);
            DateTime oldEnd = Data.getDateTime(VIEW_TASKS, row, COL_FINISH_TIME);

            DateTime newStart = (startId == null) ? oldStart
                : BeeUtils.nvl(dialog.getDateTime(startId), oldStart);
            DateTime newEnd = dialog.getDateTime(endId);

            if (newEnd == null) {
              Global.showError(Localized.getConstants().crmEnterFinishDate());
              return;
            }

            if (Objects.equal(newStart, oldStart) && Objects.equal(newEnd, oldEnd)) {
              Global.showError(Localized.getConstants().crmTermNotChanged());
              return;
            }

            if (TimeUtils.isLeq(newEnd, newStart)) {
              Global.showError(Localized.getConstants().crmFinishDateMustBeGreaterThanStart());
              return;
            }

            DateTime now = TimeUtils.nowMinutes();
            if (TimeUtils.isLess(newEnd, TimeUtils.nowMinutes())) {
              Global.showError("Time travel not supported",
                  Lists.newArrayList(Localized.getConstants().crmFinishDateMustBeGreaterThan()
                      + " "
                      + now.toCompactString()));
              return;
            }

            List<String> notes = Lists.newArrayList();

            ParameterList params = createArgs(SVC_EXTEND_TASK);
            params.addQueryItem(VAR_TASK_ID, taskId);

            if (startId != null && newStart != null && !Objects.equal(newStart, oldStart)) {
              params.addQueryItem(COL_START_TIME, newStart.getTime());
              notes.add(TasksUtils.getUpdateNote(Localized.getConstants().crmStartDate(),
                  TimeUtils.renderCompact(oldStart), TimeUtils.renderCompact(newStart)));
            }

            if (!Objects.equal(newEnd, oldEnd)) {
              params.addQueryItem(COL_FINISH_TIME, newEnd.getTime());
              notes.add(TasksUtils.getUpdateNote(Localized.getConstants().crmFinishDate(),
                  TimeUtils.renderCompact(oldEnd), TimeUtils.renderCompact(newEnd)));
            }

            String comment = dialog.getComment(cid);
            if (!BeeUtils.isEmpty(comment)) {
              params.addDataItem(VAR_TASK_COMMENT, comment);
            }

            if (!notes.isEmpty()) {
              params.addDataItem(VAR_TASK_NOTES, Codec.beeSerialize(notes));
            }

            BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                if (Queries.checkRowResponse(SVC_EXTEND_TASK, VIEW_TASKS, response)) {
                  BeeRow updatedRow = BeeRow.restore(response.getResponseAsString());
                  RowUpdateEvent.fire(BeeKeeper.getBus(), VIEW_TASKS, updatedRow);
                }
              }
            });

            dialog.close();
          }
        });

        dialog.display(endId);
      }
    });
  }

  public static void register() {
    FormFactory.registerFormInterceptor(FORM_NEW_TASK, new TaskBuilder());
    FormFactory.registerFormInterceptor(FORM_TASK, new TaskEditor());

    FormFactory.registerFormInterceptor(FORM_RECURRING_TASK, new RecurringTaskHandler());

    FormFactory.registerFormInterceptor(FORM_NEW_REQUEST, new RequestBuilder(null));
    FormFactory.registerFormInterceptor(FORM_REQUEST, new RequestEditor());

    GridFactory.registerGridInterceptor(GRID_REQUESTS, new RequestsGridInterceptor());

    GridFactory.registerGridInterceptor(GRID_TODO_LIST, new TodoListInterceptor());

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

    Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_ASSIGNED,
        TaskList.getFeedFilterHandler(Feed.TASKS_ASSIGNED));
    Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_DELEGATED,
        TaskList.getFeedFilterHandler(Feed.TASKS_DELEGATED));
    Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_OBSERVED,
        TaskList.getFeedFilterHandler(Feed.TASKS_OBSERVED));
    Global.getNewsAggregator().registerFilterHandler(Feed.TASKS_ALL,
        TaskList.getFeedFilterHandler(Feed.TASKS_ALL));

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
    ParameterList args = BeeKeeper.getRpc().createParameters(Module.TASKS.getName());
    args.addQueryItem(CommonsConstants.SERVICE, method);
    return args;
  }

  static ParameterList createTaskRequestParameters(TaskEvent event) {
    return createArgs(CRM_TASK_PREFIX + event.name());
  }

  private TasksKeeper() {
  }
}
