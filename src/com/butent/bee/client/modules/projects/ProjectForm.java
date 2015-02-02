package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.eventsboard.EventsBoard.EventFilesFilter;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants.ProjectEvent;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ProjectForm extends AbstractFormInterceptor implements DataChangeEvent.Handler,
    RowInsertEvent.Handler {

  private static final String WIDGET_CONTRACT = "Contract";
  private static final String WIDGET_CHART_DATA = "ChartData";
  private static final String WIDGET_PROJECT_COMMENTS = "ProjectComments";
  private static final String WIDGET_TIME_UNIT = "TimeUnit";
  private static final String WIDGET_EXPECTED_TASKS_DURATION = "ExpectedTasksDuration";
  private static final String WIDGET_ACTUAL_TASKS_DURATION = "ActualTasksDuration";

  private static final Set<String> AUDIT_FIELDS = Sets.newHashSet(COL_PROJECT_START_DATE,
      COL_PROJECT_END_DATE, COL_COMAPNY, COL_PROJECT_STATUS, COL_PROJECT_OWNER,
      COL_EXPECTED_DURATION, COL_PROJECT_TIME_UNIT, COL_PROJECT_PRICE, COL_CONTRACT_PRICE);

  private static final BeeLogger logger = LogUtils.getLogger(ProjectForm.class);

  private final Collection<HandlerRegistration> registry = new ArrayList<>();
  private final ProjectEventsHandler eventsHandler = new ProjectEventsHandler();
  private final Set<String> auditSilentFields = Sets.newHashSet();
  private final Map<String, Boolean> lockedValidations = Maps.newHashMap();

  private DataSelector contractSelector;
  private Flow chartData;
  private Flow projectCommnets;
  private DataSelector unitSelector;
  private InputText expectedTasksDuration;
  private InputText actualTasksDuration;

  private BeeRowSet timeUnits;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof DataSelector && BeeUtils.same(name, WIDGET_CONTRACT)) {
      contractSelector = (DataSelector) widget;
    }

    if (widget instanceof Flow && BeeUtils.same(name, WIDGET_CHART_DATA)) {
      chartData = (Flow) widget;
    }

    if (widget instanceof Flow && BeeUtils.same(name, WIDGET_PROJECT_COMMENTS)) {
      projectCommnets = (Flow) widget;
    }

    if (widget instanceof DataSelector && BeeUtils.same(name, WIDGET_TIME_UNIT)) {
      unitSelector = (DataSelector) widget;

      ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_TIME_UNITS);
      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

        @Override
        public void onResponse(ResponseObject response) {
          DataSelector us = getUnitSelector();
          if (us == null) {
            return;
          }

          us.setEnabled(false);

          if (response == null) {
            return;
          }

          if (response.isEmpty() || !response.hasResponse(BeeRowSet.class)) {
            return;
          }

          BeeRowSet rs = BeeRowSet.restore(response.getResponseAsString());
          us.getOracle().setAdditionalFilter(Filter.idIn(rs.getRowIds()), true);
          us.setEnabled(true);
          setTimeUnits(rs);
        }
      });
    }

    if (widget instanceof InputText && BeeUtils.same(name, WIDGET_EXPECTED_TASKS_DURATION)) {
      expectedTasksDuration = (InputText) widget;
      expectedTasksDuration.clearValue();
    }

    if (widget instanceof InputText && BeeUtils.same(name, WIDGET_ACTUAL_TASKS_DURATION)) {
      actualTasksDuration = (InputText) widget;
      actualTasksDuration.clearValue();
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new ProjectForm();
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    contractSelector.getOracle().setAdditionalFilter(Filter.equals(COL_PROJECT, row.getId()), true);
    showComputedTimes(form, row);
    drawComments(form, row);
    drawChart(row);
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
    chartData.clear();
    EventUtils.clearRegistry(registry);
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (event.hasView(VIEW_PROJECTS) || event.hasView(VIEW_PROJECT_USERS)
        || event.hasView(VIEW_PROJECT_STAGES) || event.hasView(VIEW_PROJECT_DATES)
        || event.hasView(TaskConstants.VIEW_TASKS)) {

      getFormView().refresh();

    }
  }

  @Override
  public void onLoad(FormView form) {
    registry.add(BeeKeeper.getBus().registerRowInsertHandler(this, false));
    registry.add(BeeKeeper.getBus().registerDataChangeHandler(this, false));
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(registry);
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {

    if (event.hasView(VIEW_PROJECT_USERS)
        || event.hasView(VIEW_PROJECT_STAGES) || event.hasView(VIEW_PROJECT_DATES)
        || event.hasView(TaskConstants.VIEW_TASKS)) {

      // if (event.hasView(TaskConstants.VIEW_TASKS)) {
      // TODO: refresh tasks times
      // }
      getFormView().refresh();
    }

  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {

    IsRow oldData = event.getOldRow();
    IsRow newData = event.getNewRow();

    if (oldData == null) {
      return;
    }

    if (auditSilentFields.isEmpty()) {
      return;
    }

    DataInfo data = Data.getDataInfo(VIEW_PROJECTS);
    List<BeeColumn> cols = data.getColumns(); // event.getColumns(); Data

    Map<String, String> oldDataMap = Maps.newHashMap();
    Map<String, String> newDataMap = Maps.newHashMap();
    List<String> visitedCols = Lists.newArrayList();

    for (int i = 0; i < cols.size(); i++) {
      if (!auditSilentFields.contains(cols.get(i).getId())
          || visitedCols.contains(cols.get(i).getId())) {
        continue;
      }

      if (BeeUtils.same(oldData.getString(i), newData.getString(i))) {
        continue;
      }

      String oldValue = BeeConst.STRING_EMPTY;
      String newValue = BeeConst.STRING_EMPTY;

      if (data.hasRelation(cols.get(i).getId())) {
        for (ViewColumn vCol : data.getDescendants(cols.get(i).getId(), false)) {
          oldValue =
              BeeUtils.join(BeeConst.STRING_COMMA, oldValue, oldData.getString(data
                  .getColumnIndex(vCol.getName())));
          newValue =
              BeeUtils.join(BeeConst.STRING_COMMA, newValue, newData.getString(data
                  .getColumnIndex(vCol.getName())));
          visitedCols.add(vCol.getName());
        }

      } else {
        oldValue = oldData.getString(i);
        newValue = newData.getString(i);
      }
      oldDataMap.put(cols.get(i).getId(), oldValue);
      newDataMap.put(cols.get(i).getId(), newValue);
    }

    if (oldDataMap.isEmpty() && newDataMap.isEmpty()) {
      return;
    }

    Map<String, Map<String, String>> oldDataSent = Maps.newHashMap();
    Map<String, Map<String, String>> newDataSent = Maps.newHashMap();

    oldDataSent.put(VIEW_PROJECTS, oldDataMap);
    newDataSent.put(VIEW_PROJECTS, newDataMap);

    ProjectsHelper.registerProjectEvent(VIEW_PROJECT_EVENTS, ProjectEvent.EDIT,
        event.getRowId(), null, newDataSent, oldDataSent);
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    auditSilentFields.clear();
    lockedValidations.clear();
    if (isOwner(form, row)) {
      form.setEnabled(true);
    } else {
      form.setEnabled(false);
    }

    if (!isProjectScheduled(form, row) && form.isEnabled()) {
      setFormAuditValidation(form, row);
    }

    return super.onStartEdit(form, row, focusCommand);
  }

  private static boolean isOwner(FormView form, IsRow row) {
    int idxOwner = form.getDataIndex(COL_PROJECT_OWNER);

    if (BeeConst.isUndef(idxOwner)) {
      return false;
    }

    long currentUser = BeeUtils.unbox(BeeKeeper.getUser().getUserId());
    long projectUser = BeeUtils.unbox(row.getLong(idxOwner));

    return currentUser == projectUser;
  }

  private static boolean isProjectScheduled(FormView form, IsRow row) {
    int idxStatus = form.getDataIndex(COL_PROJECT_STATUS);

    if (BeeConst.isUndef(idxStatus)) {
      return false;
    }

    int status = BeeUtils.unbox(row.getInteger(idxStatus));

    return ProjectStatus.SCHEDULED.ordinal() == status;
  }

  private static boolean isProjectUser(FormView form, IsRow row) {
    int idxProjectUser = form.getDataIndex(ALS_FILTERED_PROJECT_USER);

    if (BeeConst.isUndef(idxProjectUser)) {
      return false;
    }

    long currentUser = BeeUtils.unbox(BeeKeeper.getUser().getUserId());
    long projectUser = BeeUtils.unbox(row.getLong(idxProjectUser));

    return currentUser == projectUser;
  }

  private void commitData(final FormView form, final String column, final String value) {
    Queries.update(form.getViewName(), Filter.compareId(form.getActiveRowId()), column, value,
        new IntCallback() {

          @Override
          public void onSuccess(Integer result) {
            IsRow oldRow = form.getOldRow();
            IsRow newRow = form.getActiveRow();

            int idx = form.getDataIndex(column);

            if (!BeeConst.isUndef(idx)) {
              oldRow.setValue(idx, value);
              newRow.setValue(idx, value);
            }
            form.refreshBySource(column);
            unlockValidationEvent(column);
          }
        });
  }

  private void drawChart(IsRow row) {
    if (row == null) {
      return;
    }

    if (chartData == null) {
      logger.warning("Widget chart data not found");
      return;
    }

    chartData.clear();

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    ProjectScheduleChart.open(chartData, row.getId());
  }

  private void drawComments(FormView form, IsRow row) {
    final Flow prjComments = getProjectComments();
    if (prjComments == null) {
      logger.warning("Widget of project comments not found");
      return;
    }

    if (eventsHandler == null) {
      logger.warning("Events handler not initialized");
      return;
    }

    prjComments.clear();

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    EventFilesFilter filter = new EventFilesFilter(VIEW_PROJECT_FILES,
        COL_PROJECT_EVENT, AdministrationConstants.COL_FILE, AdministrationConstants.ALS_FILE_NAME,
        AdministrationConstants.ALS_FILE_SIZE, AdministrationConstants.ALS_FILE_TYPE, COL_CAPTION);

    Set<Action> eventActions = eventsHandler.getEnabledActions();
    eventActions.clear();
    eventActions.add(Action.REFRESH);
    Set<Action> disabledActions = eventsHandler.getDisabledActions();
    disabledActions.clear();

    if (isProjectUser(form, row)) {
      eventActions.add(Action.ADD);
    } else {
      disabledActions.add(Action.ADD);
    }

    eventsHandler.create(prjComments, row.getId(), filter);
  }

  private Handler getAuditColumnHandler(final FormView form, final IsRow row) {
    return new Handler() {

      @Override
      public Boolean validateCell(final CellValidateEvent event) {
        if (event == null) {
          return Boolean.TRUE;
        }

        if (!event.sameValue()) {

          if (isLockedValidationEvent(event.getColumnId())) {
            return Boolean.TRUE;
          }

          setLockedValidationEvent(event.getColumnId());
          ProjectsHelper.registerReason(form, row, event, new Callback<Boolean>() {

            @Override
            public void onFailure(String... reason) {
              unlockValidationEvent(event.getColumnId());
              super.onFailure(reason);
            }

            @Override
            public void onSuccess(Boolean result) {
              if (result == null) {
                unlockValidationEvent(event.getColumnId());
                return;
              }

              if (result.booleanValue()) {
                commitData(form, event.getColumnId(), event.getNewValue());
              } else {
                IsRow oldRow = form.getOldRow();
                int idx = form.getDataIndex(event.getColumnId());

                if (BeeConst.isUndef(idx)) {
                  return;
                }

                row.setValue(idx, oldRow.getValue(idx));
                form.refreshBySource(event.getColumnId());
                unlockValidationEvent(event.getColumnId());
              }
            }
          });
        }
        return Boolean.TRUE;
      }

    };
  }

  private BeeRowSet getTimeUnits() {
    return timeUnits;
  }

  private DataSelector getUnitSelector() {
    return unitSelector;
  }

  private Flow getProjectComments() {
    return projectCommnets;
  }

  private boolean isLockedValidationEvent(String column) {
    return BeeUtils.unbox(lockedValidations.get(column));
  }

  private void setLockedValidationEvent(String column) {
    lockedValidations.put(column, Boolean.TRUE);
  }

  private void setFormAuditValidation(FormView form, IsRow row) {
    auditSilentFields.clear();
    for (BeeColumn column : form.getDataColumns()) {
      if (AUDIT_FIELDS.contains(column.getId())) {
        registry.add(form
            .addCellValidationHandler(column.getId(), getAuditColumnHandler(form, row)));
      } else {
        auditSilentFields.add(column.getId());
      }
    }
  }

  private void setTimeUnits(BeeRowSet timeUnits) {
    this.timeUnits = timeUnits;

    if (getFormView() == null) {
      return;
    }

    if (getFormView().getActiveRow() == null) {
      return;
    }

    showComputedTimes(getFormView(), getFormView().getActiveRow());
  }

  private void showComputedTimes(FormView form, IsRow row) {
    if (form == null) {
      return;
    }

    if (row == null) {
      return;
    }

    int idxExpTD = form.getDataIndex(COL_EXPECTED_TASKS_DURATION);
    int idxActTD = form.getDataIndex(COL_ACTUAL_TASKS_DURATION);
    int idxUnit = form.getDataIndex(COL_PROJECT_TIME_UNIT);

    double factor = BeeConst.DOUBLE_ONE;

    if (!BeeConst.isUndef(idxUnit) && getTimeUnits() != null) {
      long idValue = row.getLong(idxUnit);
      BeeRow unitRow = getTimeUnits().getRowById(idValue);

      if (unitRow != null) {
        String prop = unitRow.getProperty(PROP_REAL_FACTOR);

        if (!BeeUtils.isEmpty(prop) && BeeUtils.isDouble(prop)) {
          factor = BeeUtils.toDouble(prop);
        }
      }
    }

    if (expectedTasksDuration != null && !BeeConst.isUndef(idxExpTD)) {
      long value = BeeUtils.unbox(row.getLong(idxExpTD));

      if (factor == BeeConst.DOUBLE_ONE) {
        expectedTasksDuration.setText(Format.getDefaultTimeFormat().format(
          new DateTime(value)));
      } else {
        double factorMls = factor * TimeUtils.MILLIS_PER_HOUR;

        int calcValue = BeeUtils.toInt(value / BeeUtils.toInt(factorMls));
        int decValue = BeeUtils.toInt(value % BeeUtils.toInt(factorMls));

        expectedTasksDuration.setText(BeeUtils.joinWords(calcValue, Format.getDefaultTimeFormat()
            .format(new DateTime(decValue))));
      }
    }

    if (actualTasksDuration != null && !BeeConst.isUndef(idxActTD)) {
      long value = BeeUtils.unbox(row.getLong(idxActTD));


      actualTasksDuration.setText(Format.getDefaultTimeFormat().format(new DateTime(value)));
    }
  }

  private void unlockValidationEvent(String column) {
    lockedValidations.put(column, null);
  }

}
