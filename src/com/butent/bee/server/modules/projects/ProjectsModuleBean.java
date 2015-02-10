package com.butent.bee.server.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.news.Feed;
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

  @EJB
  NewsBean news;

  @EJB
  ParamHolderBean prm;

  @EJB
  UserServiceBean usr;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();

    List<SearchResult> tasksSr =
        qs.getSearchResults(VIEW_PROJECTS,
            Filter.anyContains(Sets.newHashSet(COL_PROJECT_NAME,
                ClassifierConstants.ALS_CONTACT_FIRST_NAME,
                ClassifierConstants.ALS_CONTACT_LAST_NAME, ALS_OWNER_FIRST_NAME,
                ALS_OWNER_LAST_NAME, ClassifierConstants.ALS_COMPANY_NAME),
                query));
    result.addAll(tasksSr);

    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    switch (svc) {
      case SVC_GET_PROJECT_CHART_DATA:
        response = getProjectChartData(reqInfo);
        break;
      case SVC_GET_TIME_UNITS:
        response = getTimeUnits();
        break;
      case SVC_PROJECT_REPORT:
        response = getReportData();
        break;
      default:
        break;
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createNumber(module, PRM_PROJECT_COMMON_RATE, false,
            BeeConst.DOUBLE_ZERO),
        BeeParameter.createRelation(module, PRM_PROJECT_HOUR_UNIT,
            ClassifierConstants.TBL_UNITS, ClassifierConstants.COL_UNIT_NAME)
        );
    return params;
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

        if (!BeeUtils.same(VIEW_PROJECTS, event.getTargetName())
            && !BeeUtils.same(VIEW_PROJECT_STAGES, event.getTargetName())) {
          return;
        }

        BeeRowSet viewRows = event.getRowset();

        if (viewRows.isEmpty()) {
          return;
        }

        int idxExpectedTasksDuration = DataUtils.getColumnIndex(COL_EXPECTED_TASKS_DURATION,
            viewRows.getColumns(), false);
        int idxActualTasksDuration = DataUtils.getColumnIndex(COL_ACTUAL_TASKS_DURATION,
            viewRows.getColumns(), false);

        int idxActualExpenses = DataUtils.getColumnIndex(TaskConstants.COL_ACTUAL_EXPENSES,
            viewRows.getColumns(), false);

        List<Long> rowIds = viewRows.getRowIds();

        SimpleRowSet times = getProjectsTasksTimesAndExpenses(rowIds, event.getTargetName());

        for (int i = 0; i < times.getNumberOfRows(); i++) {
          Long rowId = times.getLong(i, ALS_ROW_ID);
          Long expectedTaskDuration = times.getLong(i, COL_EXPECTED_TASKS_DURATION);
          Long actualTaskDuration = times.getLong(i, COL_ACTUAL_TASKS_DURATION);
          Double actualExpenses = times.getDouble(i, TaskConstants.COL_ACTUAL_EXPENSES);

          if (!DataUtils.isId(rowId)) {
            continue;
          }

          IsRow row = viewRows.getRowById(rowId);

          if (row == null) {
            continue;
          }

          if (!BeeUtils.isNegative(idxExpectedTasksDuration)) {
            row.setValue(idxExpectedTasksDuration, expectedTaskDuration);
          }

          if (!BeeUtils.isNegative(idxActualTasksDuration)) {
            row.setValue(idxActualTasksDuration, actualTaskDuration);
          }

          if (!BeeUtils.isNegative(idxActualExpenses)) {
            row.setValue(idxActualExpenses, actualExpenses);
          }
        }
      }

      @Subscribe
      public void fillProjectsTimeUnits(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (!BeeUtils.same(VIEW_PROJECTS, event.getTargetName())
            && !BeeUtils.same(VIEW_PROJECT_STAGES, event.getTargetName())) {
          return;
        }

        BeeRowSet viewRows = event.getRowset();

        if (viewRows.isEmpty()) {
          return;
        }

        ResponseObject units = getTimeUnits();

        if (units == null) {
          return;
        }

        if (!units.hasResponse(BeeRowSet.class)) {
          return;
        }

        BeeRowSet unitsRs = (BeeRowSet) units.getResponse();

        for (BeeRow row : viewRows) {
          row.setProperty(PROP_TIME_UNTIS, unitsRs.serialize());
        }
      }
    });

    news.registerUsageQueryProvider(Feed.PROJECT, new ProjectsUsageQueryProvider());
  }

  private static void fillUnitProperties(BeeRowSet units, long defUnit) {
    for (BeeRow row : units) {
      row.setProperty(PROP_REAL_FACTOR, getUnitRealFactor(units, row.getId(), defUnit));
    }
  }

  private static String getUnitRealFactor(BeeRowSet units, long id, long defUnit) {

    if (id == defUnit) {
      return BeeUtils.toString(BeeConst.DOUBLE_ONE);
    }

    BeeRow row = units.getRowById(id);

    if (!BeeUtils.isEmpty(row.getProperty(PROP_REAL_FACTOR))) {
      return row.getProperty(PROP_REAL_FACTOR);
    }

    int idxFact = units.getColumnIndex(ClassifierConstants.COL_UNIT_FACTOR);
    int idxParent = units.getColumnIndex(ClassifierConstants.COL_BASE_UNIT);

    double factValue = BeeConst.DOUBLE_ONE;
    long parent = BeeConst.LONG_UNDEF;

    if (!BeeConst.isUndef(idxFact)) {
      factValue = BeeUtils.unbox(row.getDouble(idxFact));
    }

    if (!BeeConst.isUndef(idxParent)) {
      parent = BeeUtils.unbox(row.getLong(idxParent));
    }

    if (!DataUtils.isId(parent)) {
      return BeeUtils.toString(factValue);
    }

    double result = factValue * BeeUtils.toDouble(getUnitRealFactor(units, parent, defUnit));

    return BeeUtils.toString(result);
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
        ALS_CHART_CAPTION, ALS_CHART_START, ALS_CHART_END, ALS_CHART_FLOW_COLOR, ALS_TASK_STATUS});

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
          rsRow.getString(idxColor), null});
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
          stage, stageName, stageStart, stageEnd, null, null
      });
    }

    rs = qs.getViewData(TaskConstants.VIEW_TASKS, Filter.equals(COL_PROJECT, projectId),
        Order.ascending(sys.getIdName(TaskConstants.VIEW_TASKS), TaskConstants.COL_START_TIME));

    int idxStage = rs.getColumnIndex(COL_PROJECT_STAGE);
    int idxSummary = rs.getColumnIndex(TaskConstants.COL_SUMMARY);
    int idxStartTime = rs.getColumnIndex(TaskConstants.COL_START_TIME);
    int idxFinishTime = rs.getColumnIndex(TaskConstants.COL_FINISH_TIME);
    int indTaskStatus = rs.getColumnIndex(TaskConstants.COL_STATUS);
    DateTime timeNow = new DateTime();

    for (IsRow rsRow : rs) {
      String stage = rsRow.getString(idxStage);
      String startTime = rsRow.getDateTime(idxStartTime) == null ? null
          : BeeUtils.toString(rsRow.getDateTime(idxStartTime).getDate().getDays());
      String finishTime = rsRow.getDateTime(idxFinishTime) == null ? null
          : BeeUtils.toString(rsRow.getDateTime(idxFinishTime).getDate().getDays());
      String taskStatus = BeeConst.STRING_EMPTY;

      if (rsRow.getInteger(indTaskStatus) == TaskStatus.ACTIVE.ordinal()
          || rsRow.getInteger(indTaskStatus) == TaskStatus.NOT_VISITED.ordinal()) {

        if (timeNow.getDate().getDays() < BeeUtils.toInt(finishTime)) {
          taskStatus = TaskConstants.VAR_TASK_ACTIVE;
        } else {
          taskStatus = TaskConstants.VAR_TASK_LATE;
        }

      } else if (rsRow.getInteger(indTaskStatus) == TaskStatus.COMPLETED.ordinal()) {
        taskStatus = TaskConstants.VAR_TASK_COMPLETED;
      } else if (rsRow.getInteger(indTaskStatus) == TaskStatus.SCHEDULED.ordinal()) {
        taskStatus = TaskConstants.VAR_TASK_SHEDULED;
      }

      insertOrderedChartData(chartData, new String[] {
          TaskConstants.VIEW_TASKS,
          stage, rsRow.getString(idxSummary),
          startTime, finishTime, null, taskStatus
      });
    }

    return ResponseObject.response(chartData);
  }

  private SimpleRowSet getProjectsTasksTimesAndExpenses(List<Long> ids, String viewName) {
    SimpleRowSet taskExpectedTimes = getTasksExpectedTimes(ids, viewName);
    SimpleRowSet taskActualTimesAndExpenses = getTasksActualTimesAndExpenses(ids, viewName);

    SimpleRowSet result = new SimpleRowSet(new String[] {
        ALS_ROW_ID, COL_EXPECTED_TASKS_DURATION, COL_ACTUAL_TASKS_DURATION,
        TaskConstants.COL_ACTUAL_EXPENSES
    });

    Map<Long, Integer> rowTimesHash = new HashMap<>();

    for (int i = 0; i < taskExpectedTimes.getNumberOfRows(); i++) {
      Long rowId = taskExpectedTimes.getLong(i, ALS_ROW_ID);
      Long time = taskExpectedTimes.getLong(i, COL_EXPECTED_TASKS_DURATION);

      Integer index = rowTimesHash.get(rowId);

      if (index == null) {
        result.addEmptyRow();
        index = result.getNumberOfRows() - 1;
        rowTimesHash.put(rowId, index);
      }

      result.setValue(index, ALS_ROW_ID, BeeUtils.toString(rowId));
      // double timeInHours =
      // Double.valueOf(time.doubleValue()) / Double.valueOf(TimeUtils.MILLIS_PER_HOUR);

      result.setValue(index, COL_EXPECTED_TASKS_DURATION, BeeUtils.toString(time));
    }

    for (int i = 0; i < taskActualTimesAndExpenses.getNumberOfRows(); i++) {
      Long rowId = taskActualTimesAndExpenses.getLong(i, ALS_ROW_ID);
      Long time = taskActualTimesAndExpenses.getLong(i, COL_ACTUAL_TASKS_DURATION);
      Double expenses = taskActualTimesAndExpenses.getDouble(i, TaskConstants.COL_ACTUAL_EXPENSES);

      Integer index = rowTimesHash.get(rowId);

      if (index == null) {
        result.addEmptyRow();
        index = result.getNumberOfRows() - 1;
        rowTimesHash.put(rowId, index);
      }

      result.setValue(index, ALS_ROW_ID, BeeUtils.toString(rowId));

      // double timeInHours =
      // Double.valueOf(time.doubleValue()) / Double.valueOf(TimeUtils.MILLIS_PER_HOUR);
      result.setValue(index, COL_ACTUAL_TASKS_DURATION, BeeUtils.toString(time));

      result.setValue(index, TaskConstants.COL_ACTUAL_EXPENSES, BeeUtils.toString(expenses));
    }

    return result;
  }

  private SimpleRowSet getTasksExpectedTimes(List<Long> ids, String viewName) {
    String filterColumn = BeeConst.EMPTY;
    SimpleRowSet result = new SimpleRowSet(new String[] {
        ALS_ROW_ID, COL_EXPECTED_TASKS_DURATION
    });

    switch (viewName) {
      case VIEW_PROJECTS:
        filterColumn = COL_PROJECT;
        break;
      case VIEW_PROJECT_STAGES:
        filterColumn = COL_PROJECT_STAGE;
        break;
      default:
        return result;
    }

    Filter idFilter = Filter.any(filterColumn, ids);
    Filter durationFilter = Filter.notNull(TaskConstants.COL_EXPECTED_DURATION);

    BeeRowSet tasks =
        qs.getViewData(TaskConstants.VIEW_TASKS, Filter.and(idFilter, durationFilter), null,
            Lists.newArrayList(filterColumn, TaskConstants.COL_EXPECTED_DURATION));

    if (tasks.isEmpty()) {
      return result;
    }

    Map<Long, Long> times = new HashMap<>();
    int idxExpectedDuration =
        DataUtils.getColumnIndex(TaskConstants.COL_EXPECTED_DURATION, tasks.getColumns(), false);
    int idxId = DataUtils.getColumnIndex(filterColumn, tasks.getColumns(), false);

    if (BeeUtils.isNegative(idxExpectedDuration) || BeeUtils.isNegative(idxId)) {
      return result;
    }

    for (IsRow row : tasks) {
      Long id = row.getLong(idxId);
      String newTime = row.getString(idxExpectedDuration);

      Long newTimeMls = TimeUtils.parseTime(newTime);
      Long currentTime = times.get(id);

      if (currentTime == null) {
        currentTime = Long.valueOf(0);
      }

      currentTime += newTimeMls;

      times.put(id, currentTime);
    }

    for (Long id : times.keySet()) {
      result.addRow(new String[] {
          BeeUtils.toString(id),
          BeeUtils.toString(times.get(id))});
    }

    return result;
  }

  private ResponseObject getReportData() {
    SqlSelect select = new SqlSelect();
    select.addField(TaskConstants.TBL_TASKS, ProjectConstants.COL_PROJECT,
        ProjectConstants.COL_PROJECT);
    select.addFields(ProjectConstants.TBL_PROJECTS,

        ProjectConstants.COL_PROJECT_NAME,
        ProjectConstants.COL_PROJECT_STATUS,
        ProjectConstants.COL_PROJECT_TYPE,
        ProjectConstants.COL_PROJECT_PRIORITY,
        ProjectConstants.COL_PROJECT_START_DATE,
        ProjectConstants.COL_PROJECT_END_DATE,
        ProjectConstants.COL_PROGRESS,
        ProjectConstants.COL_EXPECTED_DURATION,
        ProjectConstants.COL_PROJECT_PRICE
        );

    select.addExpr(SqlUtils.concat((Object[]) SqlUtils.fields(ClassifierConstants.TBL_PERSONS,
        ClassifierConstants.COL_FIRST_NAME, ClassifierConstants.COL_LAST_NAME)),
        ProjectConstants.COL_PROJECT_OWNER);
    select.addField(ClassifierConstants.TBL_COMPANIES, ClassifierConstants.COL_COMPANY_NAME,
        ProjectConstants.COL_COMAPNY);
    select.addFrom(TaskConstants.TBL_TASKS);
    select.addFromInner(ProjectConstants.TBL_PROJECTS, SqlUtils.join(TaskConstants.TBL_TASKS,
        ProjectConstants.COL_PROJECT, ProjectConstants.TBL_PROJECTS, sys
            .getIdName(ProjectConstants.TBL_PROJECTS)));

    select.addFromLeft(ClassifierConstants.TBL_COMPANIES, sys.joinTables(
        ClassifierConstants.TBL_COMPANIES, TBL_PROJECTS, COL_COMAPNY));

    select.addFromLeft(AdministrationConstants.TBL_USERS, sys.joinTables(
        AdministrationConstants.TBL_USERS, TBL_PROJECTS,
        COL_PROJECT_OWNER));

    select.addFromLeft(ClassifierConstants.TBL_COMPANY_PERSONS, sys.joinTables(
        ClassifierConstants.TBL_COMPANY_PERSONS, AdministrationConstants.TBL_USERS,
        ClassifierConstants.COL_COMPANY_PERSON));

    select.addFromLeft(ClassifierConstants.TBL_PERSONS, sys.joinTables(
        ClassifierConstants.TBL_PERSONS, ClassifierConstants.TBL_COMPANY_PERSONS,
        ClassifierConstants.COL_PERSON));

    SimpleRowSet rqs = qs.getData(select);

    return ResponseObject.response(rqs);
  }

  private SimpleRowSet getTasksActualTimesAndExpenses(List<Long> ids, String viewName) {
    String filterColumn = BeeConst.EMPTY;
    SimpleRowSet result = new SimpleRowSet(new String[] {
        ALS_ROW_ID, COL_ACTUAL_TASKS_DURATION, TaskConstants.COL_ACTUAL_EXPENSES
    });

    switch (viewName) {
      case VIEW_PROJECTS:
        filterColumn = COL_PROJECT;
        break;
      case VIEW_PROJECT_STAGES:
        filterColumn = COL_PROJECT_STAGE;
        break;
      default:
        return result;
    }

    Filter idFilter = Filter.any(filterColumn, ids);
    Filter durationFilter = Filter.notNull(TaskConstants.COL_DURATION);

    BeeRowSet taskEvents =
        qs.getViewData(TaskConstants.VIEW_TASK_DURATIONS, Filter.and(idFilter, durationFilter),
            null, Lists.newArrayList(filterColumn, TaskConstants.COL_DURATION, COL_RATE));

    if (taskEvents.isEmpty()) {
      return result;
    }

    Double defaultRate = prm.getDouble(ProjectConstants.PRM_PROJECT_COMMON_RATE);

    Map<Long, Long> times = new HashMap<>();
    Map<Long, Double> expenses = new HashMap<>();

    int idxEventDuration =
        DataUtils.getColumnIndex(TaskConstants.COL_DURATION, taskEvents.getColumns(), false);
    int idxId = DataUtils.getColumnIndex(filterColumn, taskEvents.getColumns(), false);
    int idxRate =
        DataUtils.getColumnIndex(COL_RATE, taskEvents.getColumns(), false);

    if (BeeUtils.isNegative(idxEventDuration) || BeeUtils.isNegative(idxId)) {
      return result;
    }

    for (IsRow row : taskEvents) {
      Long id = row.getLong(idxId);
      String newTime = row.getString(idxEventDuration);

      if (BeeUtils.isEmpty(newTime)) {
        continue;
      }

      Long newTimeMls = TimeUtils.parseTime(newTime);
      Long currentTime = times.get(id);

      if (currentTime == null) {
        currentTime = Long.valueOf(0);
      }

      currentTime += newTimeMls;

      times.put(id, currentTime);

      if (BeeUtils.isNegative(idxRate)) {
        continue;
      }

      Double rate = row.getDouble(idxRate);
      double currentTimeInHrs = Double.valueOf(newTimeMls)
          / Double.valueOf(TimeUtils.MILLIS_PER_HOUR);
      Double currentExpense = expenses.get(id);

      if (!BeeUtils.isPositive(rate)) {
        if (BeeUtils.isDouble(defaultRate)) {
          rate = defaultRate;
        } else {
          rate = Double.valueOf(BeeConst.DOUBLE_ZERO);
        }
      }

      if (currentExpense == null) {
        currentExpense = Double.valueOf(BeeConst.DOUBLE_ZERO);
      }

      currentExpense += rate * currentTimeInHrs;
      expenses.put(id, currentExpense);

    }

    for (Long id : ids) {
      Long timeMills = 0L;
      double expense = 0.0;

      if (times.containsKey(id)) {
        timeMills = times.get(id);
      }

      if (expenses.containsKey(id)) {
        expense = expenses.get(id) == null ? 0.0 : expenses.get(id);
      }

      result.addRow(new String[] {
          BeeUtils.toString(id),
          BeeUtils.toString(timeMills),
          BeeUtils.toString(expense)
      });
    }
    return result;

  }

  private ResponseObject getTimeUnits() {
    Long defUnit = prm.getRelation(PRM_PROJECT_HOUR_UNIT);

    if (!DataUtils.isId(defUnit)) {
      return ResponseObject.emptyResponse();
    }

    BeeRowSet units = qs.getViewData(ClassifierConstants.TBL_UNITS, Filter.compareId(defUnit));

    List<Long> idFilter = Lists.newArrayList(defUnit);

    while (!idFilter.isEmpty()) {
      BeeRowSet relUnits =
          qs.getViewData(ClassifierConstants.TBL_UNITS, Filter.any(
              ClassifierConstants.COL_BASE_UNIT, idFilter));

      idFilter.clear();
      if (!relUnits.isEmpty()) {
        idFilter.addAll(relUnits.getRowIds());
        units.addRows(relUnits.getRows());
      }
    }

    fillUnitProperties(units, defUnit);

    return ResponseObject.response(units);
  }
}
