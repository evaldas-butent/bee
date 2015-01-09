package com.butent.bee.server.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Server-side Projects module bean.
 */
@Stateless
@LocalBean
public class ProjectsModuleBean implements BeeModule {

  @EJB
  SystemBean sys;

  @EJB
  QueryServiceBean qs;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();
    // TODO: implement global search in this module
    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_GET_PROJECT_CHART_DATA)) {
      response = getProjectChartData(reqInfo);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public Module getModule() {
    return Module.PROJECTS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void fillProjectsTimeData(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (!BeeUtils.same(VIEW_PROJECTS, event.getTargetName())) {
          return;
        }

        BeeRowSet projectsRowSet = event.getRowset();

        if (projectsRowSet.isEmpty()) {
          return;
        }

        int idxExpectedTasksDuration = DataUtils.getColumnIndex(COL_EXPECTED_TASKS_DURATION,
            projectsRowSet.getColumns(), false);
        int idxActualTasksDuration = DataUtils.getColumnIndex(COL_ACTUAL_TASKS_DURATION,
            projectsRowSet.getColumns(), false);

        List<Long> prjIds = projectsRowSet.getRowIds();
        SimpleRowSet times = getProjectsTasksTimes(prjIds);

        for (int i = 0; i < times.getNumberOfRows(); i++) {
          Long projectId = times.getLong(i, COL_PROJECT);
          Double expectedTaskDuration = times.getDouble(i, COL_EXPECTED_TASKS_DURATION);
          Double actualTaskDuration = times.getDouble(i, COL_ACTUAL_TASKS_DURATION);

          if (!DataUtils.isId(projectId)) {
            continue;
          }

          IsRow row = projectsRowSet.getRowById(projectId);

          if (!BeeUtils.isNegative(idxExpectedTasksDuration)) {
            row.setValue(idxExpectedTasksDuration, expectedTaskDuration);
          }

          if (!BeeUtils.isNegative(idxActualTasksDuration)) {
            row.setValue(idxActualTasksDuration, actualTaskDuration);
          }
        }
      }
    });
  }

  private static void insertOrderedChartData(SimpleRowSet chartData, String[] data) {
    long stage1 =
        BeeUtils.unbox(BeeUtils.toLongOrNull(data[chartData.getColumnIndex(ALS_CHART_ID)]));

    for (int i = 0; i < chartData.getNumberOfRows(); i++) {
      long stage2 = BeeUtils.unbox(chartData.getLong(i, ALS_CHART_ID));

      if (stage1 < stage2) {
        chartData.getRows().add(i, data);
        return;
      }
    }

    chartData.getRows().add(data);
  }

  private ResponseObject getProjectChartData(RequestInfo req) {
    Long projectId = BeeUtils.toLongOrNull(req.getParameter(VAR_PROJECT));

    if (!DataUtils.isId(projectId)) {
      return ResponseObject.error(projectId);
    }

    final SimpleRowSet chartData = new SimpleRowSet(new String[] {ALS_VIEW_NAME, ALS_CHART_ID,
        ALS_CHART_CAPTION, ALS_CHART_START, ALS_CHART_END, ALS_CHART_FLOW_COLOR});

    BeeRowSet rs = qs.getViewData(VIEW_PROJECT_DATES, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(COL_DATES_START_DATE));

    int idxColor = rs.getColumnIndex(COL_DATES_COLOR);
    int idxCaption = rs.getColumnIndex(COL_DATES_NOTE);
    int idxStartDate = rs.getColumnIndex(COL_DATES_START_DATE);

    for (IsRow rsRow : rs) {
      DateTime startDate = rsRow.getDateTime(idxStartDate);
      chartData.addRow(new String[] {VIEW_PROJECT_DATES,
          null,
          rsRow.getString(idxCaption),
          startDate == null ? null : BeeUtils.toString(startDate.getDate().getDays()),
          null,
          rsRow.getString(idxColor)});
    }

    rs = qs.getViewData(VIEW_PROJECT_STAGES, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(sys.getIdName(VIEW_PROJECT_STAGES), COL_STAGE_START_DATE));

    int idxStageName = rs.getColumnIndex(COL_STAGE_NAME);
    int idxStageStart = rs.getColumnIndex(COL_STAGE_START_DATE);
    int idxStageEnd = rs.getColumnIndex(COL_STAGE_END_DATE);

    for (IsRow rsRow : rs) {
      String stage = BeeUtils.toString(rsRow.getId());
      String stageName = BeeUtils.isNegative(idxStageName) ? stage : rsRow.getString(idxStageName);

      String stageStart = rsRow.getString(idxStageStart);
      String stageEnd = rsRow.getString(idxStageEnd);

      chartData.addRow(new String[] {
          VIEW_PROJECT_STAGES,
          stage, stageName, stageStart, stageEnd, null
      });
    }

    rs = qs.getViewData(TaskConstants.VIEW_TASKS, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(sys.getIdName(TaskConstants.VIEW_TASKS), TaskConstants.COL_START_TIME));

    int idxStage = rs.getColumnIndex(COL_PROJECT_STAGE);
    int idxSummary = rs.getColumnIndex(TaskConstants.COL_SUMMARY);
    int idxStartTime = rs.getColumnIndex(TaskConstants.COL_START_TIME);
    int idxFinishTime = rs.getColumnIndex(TaskConstants.COL_FINISH_TIME);

    for (IsRow rsRow : rs) {
      String stage = rsRow.getString(idxStage);
      String startTime = rsRow.getDateTime(idxStartTime) == null ? null
          : BeeUtils.toString(rsRow.getDateTime(idxStartTime).getDate().getDays());
      String finishTime = rsRow.getDateTime(idxFinishTime) == null ? null
          : BeeUtils.toString(rsRow.getDateTime(idxFinishTime).getDate().getDays());

      insertOrderedChartData(chartData, new String[] {
          TaskConstants.VIEW_TASKS,
          stage, rsRow.getString(idxSummary),
          startTime, finishTime, null
      });
    }

    return ResponseObject.response(chartData);
  }

  private SimpleRowSet getProjectsTasksTimes(List<Long> prjIds) {
    SimpleRowSet taskExpectedTimes = getTasksExpectedTimes(prjIds);
    SimpleRowSet taskActualTimes = getTasksActualTimes(prjIds);

    SimpleRowSet result = new SimpleRowSet(new String[] {
        COL_PROJECT, COL_EXPECTED_TASKS_DURATION, COL_ACTUAL_TASKS_DURATION
    });

    Map<Long, Integer> projectTimesHash = new HashMap<>();

    for (int i = 0; i < taskExpectedTimes.getNumberOfRows(); i++) {
      Long projectId = taskExpectedTimes.getLong(i, COL_PROJECT);
      Long time = taskExpectedTimes.getLong(i, COL_EXPECTED_TASKS_DURATION);

      Integer index = projectTimesHash.get(projectId);

      if (index == null) {
        result.addEmptyRow();
        index = result.getNumberOfRows() - 1;
        projectTimesHash.put(projectId, index);
      }

      result.setValue(index, COL_PROJECT, BeeUtils.toString(projectId));
      double timeInHours =
          Double.valueOf(time.doubleValue()) / Double.valueOf(TimeUtils.MILLIS_PER_HOUR);

      result.setValue(index, COL_EXPECTED_TASKS_DURATION, BeeUtils.toString(timeInHours));
    }

    for (int i = 0; i < taskActualTimes.getNumberOfRows(); i++) {
      Long projectId = taskActualTimes.getLong(i, COL_PROJECT);
      Long time = taskActualTimes.getLong(i, COL_ACTUAL_TASKS_DURATION);

      Integer index = projectTimesHash.get(projectId);

      if (index == null) {
        result.addEmptyRow();
        index = result.getNumberOfRows() - 1;
        projectTimesHash.put(projectId, index);
      }

      result.setValue(index, COL_PROJECT, BeeUtils.toString(projectId));
      double timeInHours =
          Double.valueOf(time.doubleValue()) / Double.valueOf(TimeUtils.MILLIS_PER_HOUR);
      result.setValue(index, COL_ACTUAL_TASKS_DURATION, BeeUtils.toString(timeInHours));
    }

    return result;
  }

  private SimpleRowSet getTasksExpectedTimes(List<Long> prjIds) {
    Filter prjIdFilter = Filter.any(COL_PROJECT, prjIds);
    Filter durationFilter = Filter.notNull(TaskConstants.COL_EXPECTED_DURATION);

    BeeRowSet tasks =
        qs.getViewData(TaskConstants.VIEW_TASKS, Filter.and(prjIdFilter, durationFilter), null,
            Lists.newArrayList(COL_PROJECT, TaskConstants.COL_EXPECTED_DURATION));

    SimpleRowSet result = new SimpleRowSet(new String[] {COL_PROJECT, COL_EXPECTED_TASKS_DURATION});

    if (tasks.isEmpty()) {
      return result;
    }

    Map<Long, Long> times = new HashMap<>();
    int idxExpectedDuration =
        DataUtils.getColumnIndex(TaskConstants.COL_EXPECTED_DURATION, tasks.getColumns(), false);
    int idxProject = DataUtils.getColumnIndex(COL_PROJECT, tasks.getColumns(), false);

    if (BeeUtils.isNegative(idxExpectedDuration) || BeeUtils.isNegative(idxProject)) {
      return result;
    }

    for (IsRow row : tasks) {
      Long projectId = row.getLong(idxProject);
      String newTime = row.getString(idxExpectedDuration);

      Long newTimeMls = TimeUtils.parseTime(newTime);
      Long currentTime = times.get(projectId);

      if (currentTime == null) {
        currentTime = Long.valueOf(0);
      }

      currentTime += newTimeMls;

      times.put(projectId, currentTime);
    }

    for (Long projectId : times.keySet()) {
      result.addRow(new String[] {
          BeeUtils.toString(projectId), BeeUtils.toString(times.get(projectId))});
    }

    return result;
  }

  private SimpleRowSet getTasksActualTimes(List<Long> prjIds) {
    Filter prjIdFilter = Filter.any(COL_PROJECT, prjIds);
    Filter durationFilter = Filter.notNull(TaskConstants.COL_DURATION);

    BeeRowSet taskEvents =
        qs.getViewData(TaskConstants.VIEW_TASK_DURATIONS, Filter.and(prjIdFilter, durationFilter),
            null, Lists.newArrayList(COL_PROJECT, TaskConstants.COL_DURATION));

    SimpleRowSet result = new SimpleRowSet(new String[] {COL_PROJECT, COL_ACTUAL_TASKS_DURATION});

    if (taskEvents.isEmpty()) {
      return result;
    }

    Map<Long, Long> times = new HashMap<>();
    int idxEventDuration =
        DataUtils.getColumnIndex(TaskConstants.COL_DURATION, taskEvents.getColumns(), false);
    int idxProject = DataUtils.getColumnIndex(COL_PROJECT, taskEvents.getColumns(), false);

    if (BeeUtils.isNegative(idxEventDuration) || BeeUtils.isNegative(idxProject)) {
      return result;
    }

    for (IsRow row : taskEvents) {
      Long projectId = row.getLong(idxProject);
      String newTime = row.getString(idxEventDuration);

      Long newTimeMls = TimeUtils.parseTime(newTime);
      Long currentTime = times.get(projectId);

      if (currentTime == null) {
        currentTime = Long.valueOf(0);
      }

      currentTime += newTimeMls;

      times.put(projectId, currentTime);
    }

    for (Long projectId : times.keySet()) {
      result.addRow(new String[] {
          BeeUtils.toString(projectId), BeeUtils.toString(times.get(projectId))});
    }

    return result;

  }
}
