package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class TaskSelectorHandler implements SelectorEvent.Handler {

  TaskSelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {

    FormView form = ViewHelper.getForm(event.getSelector());
    if (form == null) {
      return;
    }
    if (!BeeUtils.same(form.getViewName(), VIEW_TASKS) || !form.isEnabled()) {
      return;
    }

    IsRow taskRow = form.getActiveRow();
    if (taskRow == null) {
      return;
    }

    if (BeeUtils.same(event.getRelatedViewName(), VIEW_TASK_TEMPLATES)) {
      handleTemplate(event, form, taskRow);

    } else if (event.getSelector() instanceof MultiSelector && event.isExclusions()) {
      CellSource cellSource = ((MultiSelector) event.getSelector()).getCellSource();
      String rowProperty = (cellSource == null) ? null : cellSource.getName();

      if (BeeUtils.same(rowProperty, PROP_EXECUTORS)) {
        handleExecutors(event, taskRow);
      } else if (BeeUtils.same(rowProperty, PROP_OBSERVERS)) {
        handleObservers(event, taskRow);
      } else if (event.hasRelatedView(ClassifierConstants.VIEW_COMPANIES)) {
        handleCompanies(event, taskRow);
      } else if (event.hasRelatedView(VIEW_TASKS)) {
        handleTasks(event, taskRow);
      }
    }
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

  private static void handleTemplate(SelectorEvent event, FormView form, IsRow taskRow) {
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

    List<String> exclusions = Arrays.asList(COL_NOT_SCHEDULED_TASK, PROP_MAIL, COL_START_TIME,
        COL_FINISH_TIME);

    Set<String> updatedColumns = new HashSet<>();

    for (int i = 0; i < templateColumns.size(); i++) {
      String colName = templateColumns.get(i).getId();
      String value = templateRow.getString(i);

      if (BeeUtils.same(colName, COL_TASK_TEMPLATE_NAME)) {
        selector.setDisplayValue(BeeUtils.trim(value));
      } else if (!BeeUtils.isEmpty(value) && !exclusions.contains(colName)) {
        int index = Data.getColumnIndex(VIEW_TASKS, colName);
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

    setTaskData(form, templateRow);
  }

  private static void setTaskData(FormView form, IsRow templateRow) {
    boolean notScheduledTask = false;

    for (String name : Arrays.asList(COL_NOT_SCHEDULED_TASK, PROP_MAIL)) {
      InputBoolean widget = (InputBoolean) form.getWidgetByName(name);
      boolean value = BeeUtils.unbox(templateRow.getBoolean(Data.getColumnIndex(VIEW_TASK_TEMPLATES,
          name)));

      if (widget != null) {
        widget.setChecked(value);
      }

      if (BeeUtils.same(name, COL_NOT_SCHEDULED_TASK)) {
        notScheduledTask = value;
      } else if (BeeUtils.same(name, PROP_MAIL) && value) {
        form.getActiveRow().setProperty(PROP_MAIL, BooleanValue.S_TRUE);
      }
    }

    Map<String, Pair<String, String>> widgets = new HashMap<>();

    if (!notScheduledTask) {
      widgets.put(VIEW_TT_EXECUTORS, Pair.of(PROP_EXECUTORS, PROP_USER));
      widgets.put(VIEW_TT_OBSERVERS, Pair.of(PROP_OBSERVERS, PROP_USER));
      widgets.put(VIEW_TT_EXECUTOR_GROUPS, Pair.of("UserGroupSettings", COL_RTEXGR_GROUP));
      widgets.put(VIEW_TT_OBSERVER_GROUPS, Pair.of(PROP_OBSERVER_GROUPS, COL_RTEXGR_GROUP));
    }

    widgets.put(VIEW_TT_FILES, Pair.of(PROP_FILES, AdministrationConstants.COL_FILE));

    Map<String, Filter> filters = new HashMap<>();

    for (String viewName : widgets.keySet()) {
      filters.put(viewName, Filter.equals(COL_TASK_TEMPLATE, templateRow.getId()));
    }

    Queries.getData(widgets.keySet(), filters, (Queries.DataCallback) result -> {
      for (BeeRowSet rowSet : result) {
        if (!DataUtils.isEmpty(rowSet)) {

          String widgetName = widgets.get(rowSet.getViewName()).getA();
          String column = widgets.get(rowSet.getViewName()).getB();

          Widget widget = form.getWidgetByName(widgetName);

          if (widget != null) {

            if (widget instanceof MultiSelector) {
              Set<Long> ids = rowSet.getDistinctLongs(rowSet.getColumnIndex(column));

              ((MultiSelector) widget).setIds(ids);

              if (Objects.equals(widgetName, "UserGroupSettings")) {
                form.getActiveRow().setProperty(PROP_EXECUTOR_GROUPS, DataUtils.buildIdList(ids));
              } else {
                form.getActiveRow().setProperty(widgetName, DataUtils.buildIdList(ids));
              }

            } else if (widget instanceof FileCollector) {
              List<FileInfo> files = new ArrayList<>();
              ((FileCollector) widget).clear();

              for (BeeRow row : rowSet) {
                FileInfo fileInfo = new FileInfo(
                    row.getLong(rowSet.getColumnIndex(AdministrationConstants.COL_FILE)),
                    row.getString(rowSet.getColumnIndex(AdministrationConstants.COL_FILE_HASH)),
                    row.getString(rowSet.getColumnIndex(AdministrationConstants.ALS_FILE_NAME)),
                    row.getLong(rowSet.getColumnIndex(AdministrationConstants.ALS_FILE_SIZE)),
                    row.getString(rowSet.getColumnIndex(AdministrationConstants.ALS_FILE_TYPE)));

                files.add(fileInfo);
              }

              ((FileCollector) widget).addFiles(files);
              form.refreshBySource(PROP_FILES);
            }
          }
        }
      }

      Queries.getRowSet(AdministrationConstants.VIEW_RELATIONS, null,
          Filter.equals(COL_TASK_TEMPLATE, templateRow.getId()), relations -> {
        Relations rel = (Relations) form.getWidgetByName(AdministrationConstants.VIEW_RELATIONS);
            if (!DataUtils.isEmpty(relations) && rel != null) {
              Map<String, MultiSelector> map = rel.getWidgetMap(false);

              for (String relation : map.keySet()) {
                Set<Long> ids = relations.getDistinctLongs(relations.getColumnIndex(relation));

                if (!BeeUtils.isEmpty(ids)) {
                  map.get(relation).setIds(ids);
                }
              }

              form.refresh();
            }
          });

      form.refresh();
    });
  }
}
