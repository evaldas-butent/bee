package com.butent.bee.client.modules.projects;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.eventsboard.EventsBoard.EventFilesFilter;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants.ProjectEvent;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.modules.tasks.TaskConstants;
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
  private static final Set<String> AUDIT_FIELDS = Sets.newHashSet(COL_PROJECT_START_DATE,
      COL_PROJECT_END_DATE, COL_COMAPNY, COL_PROJECT_STATUS, COL_PROJECT_OWNER,
      COL_EXPECTED_DURATION, COL_PROJECT_PRICE, COL_CONTRACT_PRICE);

  private static final BeeLogger logger = LogUtils.getLogger(ProjectForm.class);

  private final Collection<HandlerRegistration> registry = new ArrayList<>();
  private final ProjectEventsHandler eventsHandler = new ProjectEventsHandler();
  private final Set<String> auditSilentFields = Sets.newHashSet();

  private DataSelector contractSelector;
  private Flow chartData;
  private Flow projectCommnets;

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
  }

  @Override
  public FormInterceptor getInstance() {
    return new ProjectForm();
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    contractSelector.getOracle().setAdditionalFilter(Filter.equals(COL_PROJECT, row.getId()), true);
    drawComments(form, row);
    drawChart(row);
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
    chartData.clear();
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

    List<String> oldData = event.getOldValues();
    List<String> newData = event.getNewValues();

    if (oldData == null) {
      return;
    }

    if (oldData.isEmpty()) {
      return;
    }

    if (auditSilentFields.isEmpty()) {
      return;
    }

    if (BeeUtils.sameElements(oldData, newData)) {
      return;
    }

    List<BeeColumn> cols = event.getColumns();

    Map<String, String> oldDataMap = Maps.newHashMap();
    Map<String, String> newDataMap = Maps.newHashMap();

    for (int i = 0; i < cols.size(); i++) {
      if (!auditSilentFields.contains(cols.get(i).getId())) {
        continue;
      }

      oldDataMap.put(cols.get(i).getId(), oldData.get(i));
      newDataMap.put(cols.get(i).getId(), newData.get(i));
    }

    if (oldDataMap.isEmpty() && newDataMap.isEmpty()) {
      return;
    }

    Map<String,  Map<String, String>> oldDataSent = Maps.newHashMap();
    Map<String,  Map<String, String>> newDataSent = Maps.newHashMap();

    oldDataSent.put(VIEW_PROJECTS, oldDataMap);
    newDataSent.put(VIEW_PROJECTS, newDataMap);

    ProjectUtils.registerProjectEvent(VIEW_PROJECT_EVENTS, ProjectEvent.EDIT,
        event.getRowId(), null, newDataSent, oldDataSent);
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    auditSilentFields.clear();
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

  @SuppressWarnings("unused")
  private static Handler getAuditColumnHandler(final FormView form, final IsRow row) {
    return new Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {
        // if (!event.sameValue()) {
        // ProjectUtils.registerReason(form, row, event);
        // }
        return Boolean.TRUE;
      }
    };
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

  private void setFormAuditValidation(FormView form, IsRow row) {
    auditSilentFields.clear();
    for (BeeColumn column : form.getDataColumns()) {
      if (AUDIT_FIELDS.contains(column.getId())) {
        form.addCellValidationHandler(column.getId(), getAuditColumnHandler(form, row));
      } else {
        auditSilentFields.add(column.getId());
      }
    }
  }

  private Flow getProjectComments() {
    return projectCommnets;
  }

}
