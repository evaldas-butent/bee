package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class NewSubstitutionForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private static final String WIDGET_SHOW_ALL_EMPLOYEES_NAME = "ShowAvailableEmployees";
  private static final String WIDGET_EMPLOYEE_SELECTOR_NAME = "Employee";
  private static final String WIDGET_SUBSTITUTE_FOR_NAME = "SubstituteFor";

  NewSubstitutionForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewSubstitutionForm();
  }

  InputBoolean showAllEmployees;
  DataSelector substituteForSelector;
  DataSelector employeeSelector;
  InputDate dateFrom;
  InputDate dateUntil;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof InputBoolean && BeeUtils.same(WIDGET_SHOW_ALL_EMPLOYEES_NAME, name)) {
      showAllEmployees = (InputBoolean) widget;
      showAllEmployees.setChecked(Boolean.TRUE);

    } else if (widget instanceof DataSelector) {
      if (BeeUtils.same(WIDGET_EMPLOYEE_SELECTOR_NAME, name)) {
        employeeSelector = (DataSelector) widget;
        employeeSelector.addSelectorHandler(this);

        employeeSelector.getOracle().addDataReceivedHandler(rowSet -> {
          if (substituteForSelector.getRelatedId() != null) {
            int departmentIndex = rowSet.getColumnIndex(COL_DEPARTMENT);
            Long substituteForDepartmentId =
                substituteForSelector.getRelatedRow().getLong(departmentIndex);

            if (!BeeUtils.isEmpty(rowSet.getRows()) && rowSet.getRows().size() > 1
                && substituteForDepartmentId != null) {
              List<BeeRow> sortedRows = sortRowSetByDepartments(rowSet.getRows(),
                  substituteForDepartmentId, departmentIndex);
              employeeSelector.getOracle().getViewData().clearRows();
              employeeSelector.getOracle().getViewData().setRows(sortedRows);
            }
          }
        });
      } else if (BeeUtils.same(WIDGET_SUBSTITUTE_FOR_NAME, name)) {
        substituteForSelector = (DataSelector) widget;
      }

    } else if (widget instanceof InputDate) {
      if (BeeUtils.same(COL_EMPLOYEE_OBJECT_FROM, name)) {
        dateFrom = (InputDate) widget;
      } else if (BeeUtils.same(COL_EMPLOYEE_OBJECT_UNTIL, name)) {
        dateUntil = (InputDate) widget;
      }
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private List<BeeRow> sortRowSetByDepartments(List<BeeRow> rowSet, Long substituteForDepartmentId,
      int departmentIndex) {
    List<BeeRow> rowSetWithDepartments = new ArrayList<>();
    List<BeeRow> rowSetResidual = new ArrayList<>();

    for (BeeRow row : rowSet) {
      if (Objects.equals(row.getLong(departmentIndex), substituteForDepartmentId)) {
        rowSetWithDepartments.add(row);
      } else {
        rowSetResidual.add(row);
      }
    }
    rowSetWithDepartments.addAll(rowSetResidual);
    return rowSetWithDepartments;
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.getState().equals(State.OPEN)) {
      createFilterToEmployeeSelector(showAllEmployees.isChecked());
    }
  }

  private void createFilterToEmployeeSelector(boolean showAllAvailable) {
    CompoundFilter flt = Filter.and();
    String employeeIdColumn = Data.getIdColumn(VIEW_EMPLOYEES);

    if (substituteForSelector.getValue() != null) {
      flt.add(Filter.isNot(Filter.compareId(BeeUtils.toLong(substituteForSelector.getValue()))));
    }

    DateValue startDateValue = null;
    DateValue endDateValue = null;

    if (!dateFrom.isEmpty()) {
      startDateValue = new DateValue(dateFrom.getDate());
    }
    if (!dateUntil.isEmpty()) {
      endDateValue = new DateValue(dateUntil.getDate());
    }

    if (showAllAvailable) {
      flt.add(Filter.isNot(
          Filter.in(employeeIdColumn, VIEW_WORK_SCHEDULE, COL_EMPLOYEE,
              createWSFilter(startDateValue, endDateValue))));
      flt.add(Filter.isNot(
          Filter.in(employeeIdColumn, VIEW_TIME_CARD_CHANGES, COL_EMPLOYEE,
              createTCCFilter(startDateValue, endDateValue))));
    }

    DateTimeValue now = new DateTimeValue(TimeUtils.nowMillis());
    if (startDateValue != null) {
      flt.add(Filter.or(Filter.isLessEqual(COL_DATE_OF_EMPLOYMENT, startDateValue),
          Filter.isNull(COL_DATE_OF_EMPLOYMENT)));
    } else {
      flt.add(Filter.or(Filter.isLessEqual(COL_DATE_OF_EMPLOYMENT, now),
          Filter.isNull(COL_DATE_OF_EMPLOYMENT)));
    }
    if (endDateValue != null) {
      flt.add(Filter.or(Filter.isMoreEqual(COL_DATE_OF_DISMISSAL, endDateValue),
          Filter.isNull(COL_DATE_OF_DISMISSAL)));
    } else {
      flt.add(Filter.or(Filter.isMoreEqual(COL_DATE_OF_DISMISSAL, now),
          Filter.isNull(COL_DATE_OF_DISMISSAL)));
    }

    employeeSelector.setAdditionalFilter(flt);
  }

  private static Filter createTCCFilter(DateValue startTime, DateValue endTime) {
    return Filter.and(startTime != null
            ? Filter.isMore(COL_TIME_CARD_CHANGES_FROM, startTime) : null,
        endTime != null
            ? Filter.isLess(COL_TIME_CARD_CHANGES_UNTIL, endTime) : null);
  }

  private static Filter createWSFilter(DateValue startTime, DateValue endTime) {
    return Filter.and(startTime != null
            ? Filter.isMoreEqual(COL_WORK_SCHEDULE_DATE, startTime) : null,
        endTime != null
            ? Filter.isLessEqual(COL_WORK_SCHEDULE_DATE, endTime) : null);
  }
}
