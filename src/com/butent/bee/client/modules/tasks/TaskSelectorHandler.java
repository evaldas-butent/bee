package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.SvcObjectStatus;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TaskSelectorHandler implements SelectorEvent.Handler {

  TaskSelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(final SelectorEvent event) {

    final FormView form = ViewHelper.getForm(event.getSelector());
    if (form == null) {
      return;
    }
    if (!BeeUtils.same(form.getViewName(), VIEW_TASKS) || !form.isEnabled()) {
      return;
    }

    final IsRow taskRow = form.getActiveRow();
    if (taskRow == null) {
      return;
    }

    if (BeeUtils.same(event.getRelatedViewName(), VIEW_TASK_TEMPLATES)) {

      TaskTemplateForm.getTemplateFiles(event.getValue(), new Callback<List<FileInfo>>() {

        @Override
        public void onSuccess(final List<FileInfo> files) {

          Queries.getRowSet(VIEW_TASK_TML_USERS, Lists.newArrayList(
              AdministrationConstants.COL_USER,
              COL_EXECUTOR), Filter.equals(COL_TASK_TEMPLATE, event.getValue()),
              new RowSetCallback() {

            @Override
            public void onSuccess(BeeRowSet users) {
              List<Long> executors = Lists.newArrayList();
              List<Long> observers = Lists.newArrayList();

              for (int i = 0; i < users.getNumberOfRows(); i++) {
                if (BeeUtils.isEmpty(users.getString(i, COL_EXECUTOR))) {
                  observers.add(users.getLong(i, AdministrationConstants.COL_USER));
                } else {
                  executors.add(users.getLong(i, AdministrationConstants.COL_USER));
                }
              }

              handleTemplate(event, form, taskRow, DataUtils.buildIdList(executors), DataUtils
                  .buildIdList(observers), files);
            }
          });

        }
      });

    } else if (event.getSelector() instanceof MultiSelector && event.isExclusions()) {
      CellSource cellSource = ((MultiSelector) event.getSelector()).getCellSource();
      String rowProperty = (cellSource == null) ? null : cellSource.getName();

      if (BeeUtils.same(rowProperty, PROP_EXECUTORS)) {
        handleExecutors(event, taskRow);
      } else if (BeeUtils.same(rowProperty, PROP_OBSERVERS)) {
        handleObservers(event, taskRow);

      } else if (BeeUtils.same(rowProperty, PROP_COMPANIES)) {
        handleCompanies(event, taskRow);
      } else if (BeeUtils.same(rowProperty, PROP_TASKS)) {
        handleTasks(event, taskRow);
      }
    } else if (event.getSelector() instanceof MultiSelector && event.isNewRow()) {
      CellSource cellSource = ((MultiSelector) event.getSelector()).getCellSource();
      String rowProperty = (cellSource == null) ? null : cellSource.getName();

      if (BeeUtils.same(rowProperty, PROP_SERVICE_OBJECTS)) {
        handleServiceObjects(event, taskRow);
      }
    }
  }

  private static String getMappedTaskTemplateColumn(String tmlColumn) {
    List<String> restricted = Lists.newArrayList("TMLStageName");

    if (restricted.contains(tmlColumn)) {
      return null;
    }
    return tmlColumn;
  }

  private static void handleCompanies(SelectorEvent event, IsRow taskRow) {
    Long company = Data.getLong(VIEW_TASKS, taskRow, ClassifierConstants.COL_COMPANY);
    if (company == null) {
      return;
    }

    Set<Long> exclusions = Sets.newHashSet(company);
    if (!BeeUtils.isEmpty(event.getExclusions())) {
      exclusions.addAll(event.getExclusions());
    }

    event.consume();
    event.getSelector().getOracle().setExclusions(exclusions);
  }

  private static void handleExecutors(SelectorEvent event, IsRow taskRow) {
    Set<Long> exclusions = DataUtils.parseIdSet(taskRow.getProperty(PROP_OBSERVERS));
    if (!BeeUtils.isEmpty(event.getExclusions())) {
      exclusions.addAll(event.getExclusions());
    }

    event.consume();
    event.getSelector().getOracle().setExclusions(exclusions);
  }

  private static void handleObservers(SelectorEvent event, IsRow taskRow) {
    Long owner = Data.getLong(VIEW_TASKS, taskRow, COL_OWNER);
    if (owner == null) {
      owner = BeeKeeper.getUser().getUserId();
    }
    Set<Long> exclusions = Sets.newHashSet(owner);

    if (DataUtils.isNewRow(taskRow)) {
      exclusions.addAll(DataUtils.parseIdSet(taskRow.getProperty(PROP_EXECUTORS)));
    } else {
      Long executor = Data.getLong(VIEW_TASKS, taskRow, COL_EXECUTOR);
      if (executor != null) {
        exclusions.add(executor);
      }
    }

    if (!BeeUtils.isEmpty(event.getExclusions())) {
      exclusions.addAll(event.getExclusions());
    }

    event.consume();
    event.getSelector().getOracle().setExclusions(exclusions);
  }

  private static void handleServiceObjects(SelectorEvent event, IsRow taskRow) {
    if (DataUtils.isNewRow(taskRow)) {
      return;
    }

    if (!event.isNewRow()) {
      return;
    }

    DataInfo serviceObject = Data.getDataInfo(ServiceConstants.VIEW_SERVICE_OBJECTS);
    IsRow svcRow = event.getNewRow();
    svcRow.setValue(serviceObject.getColumnIndex(ProjectConstants.COL_PROJECT), Data.getLong(
        VIEW_TASKS, taskRow, ProjectConstants.COL_PROJECT));
    svcRow.setValue(serviceObject.getColumnIndex(ProjectConstants.ALS_PROJECT_NAME), Data.getString(
        VIEW_TASKS, taskRow, ProjectConstants.ALS_PROJECT_NAME));
    svcRow.setValue(serviceObject.getColumnIndex(ServiceConstants.COL_OBJECT_STATUS),
        SvcObjectStatus.POTENTIAL_OBJECT.ordinal());
  }

  private static void handleTasks(SelectorEvent event, IsRow taskRow) {
    if (DataUtils.isNewRow(taskRow)) {
      return;
    }

    Set<Long> exclusions = Sets.newHashSet(taskRow.getId());
    if (!BeeUtils.isEmpty(event.getExclusions())) {
      exclusions.addAll(event.getExclusions());
    }

    event.consume();
    event.getSelector().getOracle().setExclusions(exclusions);
  }

  private static void handleTemplate(SelectorEvent event, FormView form, IsRow taskRow,
      String executors, String observers, List<FileInfo> files) {
    DataSelector selector = event.getSelector();

    if (event.isClosed()) {
      selector.clearDisplay();
    }
    if (!event.isChanged()) {
      return;
    }

    IsRow templateRow = event.getRelatedRow();
    if (templateRow == null) {
      selector.clearDisplay();
      return;
    }

    List<BeeColumn> templateColumns = Data.getColumns(VIEW_TASK_TEMPLATES);
    if (BeeUtils.isEmpty(templateColumns)) {
      return;
    }

    Set<String> updatedColumns = new HashSet<>();

    for (int i = 0; i < templateColumns.size(); i++) {
      String colName = templateColumns.get(i).getId();
      String value = templateRow.getString(i);

      if (BeeUtils.same(colName, COL_TASK_TEMPLATE_NAME)) {
        selector.setDisplayValue(BeeUtils.trim(value));
      } else if (!BeeUtils.isEmpty(value) && !BeeUtils.isEmpty(getMappedTaskTemplateColumn(
          colName))) {
        int index = Data.getColumnIndex(VIEW_TASKS, getMappedTaskTemplateColumn(colName));
        if (index >= 0 && taskRow.isNull(index)) {
          taskRow.setValue(index, value);
          if (templateColumns.get(i).isEditable()) {
            updatedColumns.add(colName);
          }
        }
      }
    }

    for (String colName : updatedColumns) {
      form.refreshBySource(colName);
    }

    if (!DataUtils.parseIdList(executors).isEmpty()) {
      taskRow.setProperty(PROP_EXECUTORS, executors);
      form.refreshBySource(PROP_EXECUTORS);
    }

    if (!DataUtils.parseIdList(observers).isEmpty()) {
      taskRow.setProperty(PROP_OBSERVERS, observers);
      form.refreshBySource(PROP_OBSERVERS);
    }

    if (!BeeUtils.isEmpty(files) && form.getWidgetByName(
        TaskBuilder.NAME_FILES) instanceof FileCollector) {
      ((FileCollector) form.getWidgetByName(TaskBuilder.NAME_FILES)).addFiles(files);
    }

  }
}
