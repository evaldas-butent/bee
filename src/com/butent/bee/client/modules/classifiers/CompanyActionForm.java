package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

final class CompanyActionForm extends AbstractFormInterceptor {

  private final Map<Long, IsRow> plannedDurations = Maps.newHashMap();
  private int planedDurationColumnIndex = -1;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action.equals(Action.SAVE) && getActiveRow() != null && getFormView() != null) {
      FormView form = getFormView();
      IsRow row = getActiveRow();
      return validateDates(row
          .getLong(form.getDataIndex(CalendarConstants.COL_START_DATE_TIME)),
          row
              .getLong(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME)), form);
    }
    return super.beforeAction(action, presenter);
  }


  @Override
  public FormInterceptor getInstance() {
    return new CompanyActionForm();
  }

  @Override
  public void onLoad(FormView form) {
    List<String> columns = Lists.newArrayList(CalendarConstants.COL_APPOINTMENT_TYPE_NAME,
        CalendarConstants.COL_APPOINTMENT_TYPE_DURATION);

    Filter filter = Filter.notNull(CalendarConstants.COL_APPOINTMENT_TYPE_DURATION);
    Queries.getRowSet(CalendarConstants.VIEW_APPOINTMENT_TYPES, columns, filter,
        getAppointmentTypesCallback());
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    createValidationEvents(form, row);
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    GridView parentGrid = getGridView();
    FormView parentForm = null;
    IsRow parentRow = null;

    int idxCompany = form.getDataIndex(ClassifierConstants.COL_COMPANY);
    int idxStartDate = form.getDataIndex(CalendarConstants.COL_START_DATE_TIME);

    if (idxStartDate > -1) {
      newRow.setValue(idxStartDate, TimeUtils.nowMinutes());
    }

    createValidationEvents(form, newRow);

    if (parentGrid != null) {
      parentForm = UiHelper.getForm(parentGrid.asWidget());
    }

    if (parentForm != null) {
      parentRow = parentForm.getActiveRow();
    }

    if (parentRow != null
        && BeeUtils.same(parentForm.getFormName(), ClassifierConstants.FORM_COMPANY)
        && idxCompany > -1) {
      newRow.setValue(idxCompany, parentRow.getId());

      int idxCompanyName = form.getDataIndex(ClassifierConstants.ALS_COMPANY_NAME);
      int idxParentCompanyName = parentForm.getDataIndex(ClassifierConstants.COL_COMPANY_NAME);

      if (idxCompanyName > -1 && idxParentCompanyName > -1) {
        newRow.setValue(idxCompanyName, parentRow.getValue(idxParentCompanyName));
      }
    }



    super.onStartNewRow(form, oldRow, newRow);
  }

  private void createValidationEvents(FormView form, IsRow row) {
    form.addCellValidationHandler(CalendarConstants.COL_START_DATE_TIME,
        getStartDateTimeValidation(form, row));
    form.addCellValidationHandler(CalendarConstants.COL_END_DATE_TIME,
        getEndDateTimeValidation(form, row));
    form.addCellValidationHandler(CalendarConstants.COL_APPOINTMENT_TYPE,
        getAppointmentTypeValidation(form, row));

  }

  private RowSetCallback getAppointmentTypesCallback() {
    return new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        plannedDurations.clear();
        setPlanedDurationColumnIndex(-1);

        if (result == null) {
          return;
        }

        if (result.isEmpty()) {
          return;
        }

        setPlanedDurationColumnIndex(result
            .getColumnIndex(CalendarConstants.COL_APPOINTMENT_TYPE_DURATION));

        for (IsRow row : result) {
          plannedDurations.put(row.getId(), row);
        }

      }
    };
  }

  private Handler getAppointmentTypeValidation(final FormView form, final IsRow row) {
    return new Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {
        IsRow appointmentTypeRow = null;

        if (getPlanedDurationColumnIndex() < 0) {
          return true;
        }

        if (!BeeUtils
            .isEmpty(row.getString(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME)))) {
          return true;
        }

        if (BeeUtils
            .isEmpty(row.getString(form.getDataIndex(CalendarConstants.COL_START_DATE_TIME)))) {
          return true;
        }

        if (!DataUtils.isId(BeeUtils.toLongOrNull(event.getNewValue()))) {
          return true;
        }

        appointmentTypeRow = plannedDurations.get(BeeUtils.toLong(event.getNewValue()));

        if (appointmentTypeRow == null) {
          return true;
        }

        DateTime time =
            row.getDateTime(form.getDataIndex(CalendarConstants.COL_START_DATE_TIME));
        DateTime plannedDurationTime = new DateTime(TimeUtils.parseTime(
            appointmentTypeRow.getString(getPlanedDurationColumnIndex())));

        DateTime plannedEndTime = new DateTime(time.getTime() + plannedDurationTime.getTime());

        row.setValue(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME), plannedEndTime);
        form.refreshBySource(CalendarConstants.COL_END_DATE_TIME);

        return true;
      }
    };
  }

  private static Handler getEndDateTimeValidation(final FormView form, final IsRow row) {
    return new Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {

        if (!BeeUtils
            .isEmpty(row.getString(form.getDataIndex(CalendarConstants.COL_START_DATE_TIME)))) {
          validateDates(row
              .getLong(form.getDataIndex(CalendarConstants.COL_START_DATE_TIME)),
              BeeUtils.toLongOrNull(event.getNewValue()), form);
          return true;
        }
        return true;
      }
    };
  }

  private int getPlanedDurationColumnIndex() {
    return planedDurationColumnIndex;
  }

  private void setPlanedDurationColumnIndex(int index) {
    planedDurationColumnIndex = index;
  }

  private Handler getStartDateTimeValidation(final FormView form, final IsRow row) {
    return new Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {
        int idxAppointmentType = form.getDataIndex(CalendarConstants.COL_APPOINTMENT_TYPE);

        Long appointmentTypeValue = null;
        IsRow appointmentTypeRow = null;

        if (getPlanedDurationColumnIndex() < 0) {
          return true;
        }

        if (!BeeUtils
            .isEmpty(row.getString(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME)))) {
          validateDates(BeeUtils.toLongOrNull(event.getNewValue()),
              row.getLong(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME)), form);
          return true;
        }

        if (idxAppointmentType > -1) {
          appointmentTypeValue = row.getLong(idxAppointmentType);
        }

        if (!DataUtils.isId(appointmentTypeValue)) {
          return true;
        }

        appointmentTypeRow = plannedDurations.get(appointmentTypeValue);

        if (appointmentTypeRow == null) {
          return true;
        }

        DateTime time = new DateTime(Long.parseLong(event.getNewValue()));
        DateTime plannedDurationTime = new DateTime(TimeUtils.parseTime(
            appointmentTypeRow.getString(getPlanedDurationColumnIndex())));

        DateTime plannedEndTime = new DateTime(time.getTime() + plannedDurationTime.getTime());

        row.setValue(form.getDataIndex(CalendarConstants.COL_END_DATE_TIME), plannedEndTime);
        form.refreshBySource(CalendarConstants.COL_END_DATE_TIME);

        return true;
      }
    };
  }

  private static boolean validateDates(Long from, Long to,
      final FormView form) {
    long now = System.currentTimeMillis();

    if (from == null && to == null) {

      if (form != null) {
        form.notifySevere(Localized.getConstants().calEnterPlannedStartTime());
      }
      return false;
    }

    if (from == null && to != null) {
      if (to.longValue() >= now) {
        return true;
      }
    }

    if (from != null && to == null) {
      if (from.longValue() <= now) {
        return true;
      }
    }

    if (from != null && to != null) {
      if (from <= to) {
        return true;
      } else {
        if (form != null) {
          form.notifySevere(
              Localized.getConstants().crmFinishDateMustBeGreaterThanStart());

        }
        return false;
      }
    }

    return true;
  }

}
