package com.butent.bee.client.modules.payroll;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCheckedness;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class NewSubstitutionForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private static final String WIDGET_SHOW_AVAILABLE_EMPLOYEES_NAME = "ShowAvailableEmployees";

  NewSubstitutionForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewSubstitutionForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof HasCheckedness
        && BeeUtils.same(WIDGET_SHOW_AVAILABLE_EMPLOYEES_NAME, name)) {

      ((HasCheckedness) widget).setChecked(true);

    } else if (widget instanceof DataSelector && BeeUtils.same(COL_EMPLOYEE, name)) {
      ((DataSelector) widget).addSelectorHandler(this);

      ((DataSelector) widget).getOracle().addDataReceivedHandler(rowSet -> {
        if (rowSet.getNumberOfRows() > 1 && DataUtils.isId(getLongValue(COL_SUBSTITUTE_FOR))) {
          Long departmentId = getSubstituteForDepartmentId();
          int departmentIndex = rowSet.getColumnIndex(COL_DEPARTMENT);

          if (DataUtils.isId(departmentId) && departmentIndex >= 0) {
            List<BeeRow> sortedRows = sortRowsByDepartment(rowSet.getRows(),
                departmentId, departmentIndex);

            rowSet.setRows(sortedRows);
          }
        }
      });
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private Long getSubstituteForDepartmentId() {
    Widget widget = getWidgetByName(COL_SUBSTITUTE_FOR);

    if (widget instanceof DataSelector) {
      DataSelector selector = (DataSelector) widget;

      if (selector.getRelatedRow() != null) {
        int index = selector.getOracle().getDataInfo().getColumnIndex(COL_DEPARTMENT);
        return DataUtils.getLongQuietly(selector.getRelatedRow(), index);
      }
    }
    return null;
  }

  private static List<BeeRow> sortRowsByDepartment(List<BeeRow> rows, Long departmentId,
      int departmentIndex) {

    List<BeeRow> departmentRows = new ArrayList<>();
    List<BeeRow> residualRows = new ArrayList<>();

    for (BeeRow row : rows) {
      if (Objects.equals(row.getLong(departmentIndex), departmentId)) {
        departmentRows.add(row);
      } else {
        residualRows.add(row);
      }
    }

    departmentRows.addAll(residualRows);
    return departmentRows;
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isOpened()) {
      Widget widget = getWidgetByName(WIDGET_SHOW_AVAILABLE_EMPLOYEES_NAME);
      boolean checked = widget instanceof HasCheckedness && ((HasCheckedness) widget).isChecked();

      event.getSelector().setAdditionalFilter(createEmployeeFilter(checked));
    }
  }

  private Filter createEmployeeFilter(boolean showOnlyAvailable) {
    Long substituteFor = getLongValue(COL_SUBSTITUTE_FOR);

    JustDate startDate = getDateValue(COL_EMPLOYEE_OBJECT_FROM);
    JustDate endDate = getDateValue(COL_EMPLOYEE_OBJECT_UNTIL);

    CompoundFilter filter = Filter.and();

    if (DataUtils.isId(substituteFor)) {
      filter.add(Filter.compareId(Operator.NE, substituteFor));
    }

    DateValue startValue = (startDate == null) ? null : new DateValue(startDate);
    DateValue endValue = (endDate == null) ? null : new DateValue(endDate);

    if (showOnlyAvailable && DateRange.isValidClosedRange(startDate, endDate)) {
      String employeeIdColumn = Data.getIdColumn(VIEW_EMPLOYEES);

      filter.add(Filter.isNot(Filter.in(employeeIdColumn, VIEW_WORK_SCHEDULE, COL_EMPLOYEE,
          createWSFilter(startValue, endValue))));

      filter.add(Filter.isNot(Filter.in(employeeIdColumn, VIEW_TIME_CARD_CHANGES, COL_EMPLOYEE,
          createTCCFilter(startValue, endValue))));
    }

    DateValue today = new DateValue(TimeUtils.today());

    filter.add(Filter.or(Filter.isLessEqual(COL_DATE_OF_EMPLOYMENT,
        BeeUtils.nvl(startValue, today)),
        Filter.isNull(COL_DATE_OF_EMPLOYMENT)));

    filter.add(Filter.or(Filter.isMoreEqual(COL_DATE_OF_DISMISSAL,
        BeeUtils.nvl(endValue, today)),
        Filter.isNull(COL_DATE_OF_DISMISSAL)));

    return filter;
  }

  private static Filter createTCCFilter(DateValue start, DateValue end) {
    return Filter.and(Filter.isMoreEqual(COL_TIME_CARD_CHANGES_UNTIL, start),
        Filter.isLessEqual(COL_TIME_CARD_CHANGES_FROM, end));
  }

  private static Filter createWSFilter(DateValue start, DateValue end) {
    return Filter.and(Filter.isMoreEqual(COL_WORK_SCHEDULE_DATE, start),
        Filter.isLessEqual(COL_WORK_SCHEDULE_DATE, end));
  }
}
