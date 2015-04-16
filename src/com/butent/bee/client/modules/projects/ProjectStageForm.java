package com.butent.bee.client.modules.projects;

import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;

class ProjectStageForm extends AbstractFormInterceptor implements DataChangeEvent.Handler,
    RowInsertEvent.Handler, HandlesUpdateEvents {

  private InputText wActualTasksDuration;
  private InputText wExpectedTasksDuration;
  private BeeRowSet timeUnits;

  private final Collection<HandlerRegistration> timesRegistry = new ArrayList<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    switch (name) {
      case COL_ACTUAL_TASKS_DURATION:
        if (widget instanceof InputText) {
          wActualTasksDuration = (InputText) widget;
        }
        break;
      case COL_EXPECTED_TASKS_DURATION:
        if (widget instanceof InputText) {
          wExpectedTasksDuration = (InputText) widget;
        }
      break;
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    if (!BeeUtils.isEmpty(row.getProperty(PROP_TIME_UNTIS))) {
      String prop = row.getProperty(PROP_TIME_UNTIS);
      BeeRowSet unitsRows = BeeRowSet.maybeRestore(prop);
      setTimeUnits(unitsRows);
    }

    if (getTimeUnits() != null) {
      showComputedTimes(form, row, false);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new ProjectStageForm();
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {

    if (getFormView() == null) {
      return;
    }

    if (getActiveRow() == null) {
      return;
    }

    if (event.hasView(TaskConstants.VIEW_TASKS)
        || event.hasView(TaskConstants.VIEW_TASK_EVENTS)
        || event.hasView(TaskConstants.VIEW_RELATED_TASKS)) {

      showComputedTimes(getFormView(), getActiveRow(), true);
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (getFormView() == null) {
      return;
    }

    if (getActiveRow() == null) {
      return;
    }

    if (event.hasView(TaskConstants.VIEW_TASKS)
        || event.hasView(TaskConstants.VIEW_TASK_EVENTS)
        || event.hasView(TaskConstants.VIEW_RELATED_TASKS)) {

      showComputedTimes(getFormView(), getActiveRow(), true);
    }
  }

  @Override
  public void onLoad(FormView form) {
    timesRegistry.add(BeeKeeper.getBus().registerRowInsertHandler(this, false));
    timesRegistry.add(BeeKeeper.getBus().registerDataChangeHandler(this, false));
    timesRegistry.addAll(BeeKeeper.getBus().registerUpdateHandler(this, false));
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (getFormView() == null) {
      return;
    }

    if (getActiveRow() == null) {
      return;
    }

    if (event.hasView(TaskConstants.VIEW_TASKS)
        || event.hasView(TaskConstants.VIEW_TASK_EVENTS)
        || event.hasView(TaskConstants.VIEW_RELATED_TASKS)) {

      showComputedTimes(getFormView(), getActiveRow(), true);

    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (getFormView() == null) {
      return;
    }

    if (getActiveRow() == null) {
      return;
    }

    if (event.hasView(TaskConstants.VIEW_TASKS)
        || event.hasView(TaskConstants.VIEW_TASK_EVENTS)
        || event.hasView(TaskConstants.VIEW_RELATED_TASKS)) {

      showComputedTimes(getFormView(), getActiveRow(), true);
    }
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(timesRegistry);
  }

  private BeeRowSet getTimeUnits() {
    return timeUnits;
  }

  private void setTimeUnits(BeeRowSet timeUnits) {
    this.timeUnits = timeUnits;
  }

  private void showComputedTimes(final FormView form, final IsRow row, boolean requery) {
    if (form == null) {
      return;
    }

    if (row == null) {
      return;
    }

    final int idxExpTD = form.getDataIndex(COL_EXPECTED_TASKS_DURATION);
    final int idxActTD = form.getDataIndex(COL_ACTUAL_TASKS_DURATION);
    final int idxExpE = form.getDataIndex(ALS_EXPECTED_TASKS_EXPENSES);
    final int idxActE = form.getDataIndex(ALS_ACTUAL_TASKS_EXPENSES);
    int idxUnit = form.getDataIndex(COL_PROJECT_TIME_UNIT);

    double factor = BeeConst.DOUBLE_ONE;
    String unitName = BeeConst.STRING_EMPTY;

    if (requery) {

      Queries.getRow(form.getViewName(), row.getId(), new RowCallback() {

        @Override
        public void onSuccess(BeeRow result) {
          row.setValue(idxExpTD, result.getValue(idxExpTD));
          row.setValue(idxActTD, result.getValue(idxActTD));
          row.setValue(idxExpE, result.getValue(idxExpE));
          row.setValue(idxActE, result.getValue(idxActE));

          form.refreshBySource(COL_EXPECTED_TASKS_DURATION);
          form.refreshBySource(COL_ACTUAL_TASKS_DURATION);
          form.refreshBySource(ALS_EXPECTED_TASKS_EXPENSES);
          form.refreshBySource(ALS_ACTUAL_TASKS_EXPENSES);

          showComputedTimes(form, row, false);
        }
      });

      return;
    }

    if (!BeeConst.isUndef(idxUnit) && getTimeUnits() != null) {
      Long idValue = row.getLong(idxUnit);
      BeeRow unitRow = null;

      if (DataUtils.isId(idValue)) {
        unitRow = getTimeUnits().getRowById(idValue);
      }

      if (unitRow != null) {
        String prop = unitRow.getProperty(PROP_REAL_FACTOR);

        if (!BeeUtils.isEmpty(prop) && BeeUtils.isDouble(prop)) {
          factor = BeeUtils.toDouble(prop);
        }

        int idxName = getTimeUnits().getColumnIndex(ClassifierConstants.COL_UNIT_NAME);

        if (!BeeConst.isUndef(idxName)) {
          unitName = unitRow.getString(idxName);
        }
      }
    }

    if (wExpectedTasksDuration != null && !BeeConst.isUndef(idxExpTD)) {
      long value = BeeUtils.unbox(row.getLong(idxExpTD));
      wExpectedTasksDuration.setValue(BeeConst.STRING_EMPTY);

      if (factor == BeeConst.DOUBLE_ONE) {
        wExpectedTasksDuration.setText(TimeUtils.renderMinutes(BeeUtils.toInt(value
            / TimeUtils.MILLIS_PER_MINUTE), true));
      } else {
        long factorMls = BeeUtils.toLong(factor * TimeUtils.MILLIS_PER_HOUR);

        int calcValue = BeeUtils.toInt(value / factorMls);
        long decValue = value % factorMls;

        wExpectedTasksDuration.setText(BeeUtils.joinWords(calcValue, unitName, decValue != 0
            ? TimeUtils
                .renderMinutes(
                    BeeUtils.toInt(decValue
                        / TimeUtils.MILLIS_PER_MINUTE), true) : BeeConst.STRING_EMPTY));
      }
    }

    if (wActualTasksDuration != null && !BeeConst.isUndef(idxActTD)) {
      long value = BeeUtils.unbox(row.getLong(idxActTD));
      wActualTasksDuration.setValue(BeeConst.STRING_EMPTY);

      if (factor == BeeConst.DOUBLE_ONE) {
        wActualTasksDuration.setText(TimeUtils.renderMinutes(BeeUtils.toInt(value
            / TimeUtils.MILLIS_PER_MINUTE), true));
      } else {
        long factorMls = BeeUtils.toLong(factor * TimeUtils.MILLIS_PER_HOUR);

        int calcValue = BeeUtils.toInt(value / factorMls);
        long decValue = value % factorMls;

        wActualTasksDuration.setText(BeeUtils.joinWords(calcValue, unitName, decValue != 0
            ? TimeUtils
                .renderMinutes(
                    BeeUtils.toInt(decValue
                        / TimeUtils.MILLIS_PER_MINUTE), true) : BeeConst.STRING_EMPTY));
      }
    }
  }
}
