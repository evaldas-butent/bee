package com.butent.bee.client.modules.payroll;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.payroll.PayrollConstants.ObjectStatus;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EmployeeSchedule extends WorkScheduleWidget {

  private final long employeeId;

  private BeeRowSet emData;
  private BeeRowSet obData;

  EmployeeSchedule(long employeeId) {
    super(ScheduleParent.EMPLOYEE);

    this.employeeId = employeeId;
  }

  @Override
  protected void clearData() {
    super.clearData();
    setObData(null);
  }

  @Override
  protected List<BeeRow> filterPartitions(DateRange filterRange) {
    List<BeeRow> result = new ArrayList<>();

    if (!DataUtils.isEmpty(obData)) {
      Set<Long> haveWs = new HashSet<>();
      Set<Long> haveEmpl = new HashSet<>();

      if (!DataUtils.isEmpty(getWsData())) {
        int objectIndex = getWsData().getColumnIndex(COL_PAYROLL_OBJECT);
        int dateIndex = getWsData().getColumnIndex(COL_WORK_SCHEDULE_DATE);

        for (BeeRow row : getWsData()) {
          if (filterRange.contains(row.getDate(dateIndex))) {
            haveWs.add(row.getLong(objectIndex));
          }
        }
      }

      if (!DataUtils.isEmpty(getEoData())) {
        int objectIndex = getEoData().getColumnIndex(COL_PAYROLL_OBJECT);
        int fromIndex = getEoData().getColumnIndex(COL_EMPLOYEE_OBJECT_FROM);
        int untilIndex = getEoData().getColumnIndex(COL_EMPLOYEE_OBJECT_UNTIL);

        for (BeeRow row : getEoData()) {
          DateRange range = DateRange.closed(row.getDate(fromIndex), row.getDate(untilIndex));
          if (filterRange.intersects(range)) {
            haveEmpl.add(row.getLong(objectIndex));
          }
        }
      }

      if (!haveWs.isEmpty() || !haveEmpl.isEmpty()) {
        int statusIndex = obData.getColumnIndex(COL_LOCATION_STATUS);

        DateRange employeeRange = getEmployeeRange();

        for (BeeRow row : obData) {
          if (haveWs.contains(row.getId())) {
            result.add(row);

          } else if (haveEmpl.contains(row.getId())) {
            ObjectStatus status = EnumUtils.getEnumByIndex(ObjectStatus.class,
                row.getInteger(statusIndex));

            if (status == ObjectStatus.ACTIVE
                && (employeeRange == null || filterRange.intersects(employeeRange))) {
              result.add(row);
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  protected long getEmployeeId(long partId) {
    return employeeId;
  }

  @Override
  protected String getPartitionCaption(long partId) {
    return getObjectName(partId);
  }

  @Override
  protected List<Integer> getPartitionContactIndexes() {
    return Collections.singletonList(obData.getColumnIndex(COL_ADDRESS));
  }

  @Override
  protected List<BeeColumn> getPartitionDataColumns() {
    return obData.getColumns();
  }

  @Override
  protected List<Integer> getPartitionInfoIndexes() {
    return Collections.singletonList(obData.getColumnIndex(ALS_COMPANY_NAME));
  }

  @Override
  protected List<Integer> getPartitionNameIndexes() {
    return Collections.singletonList(obData.getColumnIndex(COL_LOCATION_NAME));
  }

  @Override
  protected long getRelationId() {
    return employeeId;
  }

  @Override
  protected Filter getWorkScheduleFilter() {
    return Filter.equals(COL_EMPLOYEE, employeeId);
  }

  @Override
  protected void initCalendarInfo(YearMonth ym, CalendarInfo calendarInfo) {
    calendarInfo.setTcChanges(getTimeCardChanges(employeeId, ym));

    BeeRow employee = DataUtils.isEmpty(emData) ? null : emData.getRow(0);
    JustDate activeFrom = (employee == null)
        ? null : DataUtils.getDate(emData, employee, COL_DATE_OF_EMPLOYMENT);
    JustDate activeUntil = (employee == null)
        ? null : DataUtils.getDate(emData, employee, COL_DATE_OF_DISMISSAL);

    calendarInfo.setInactiveDays(getInactiveDays(ym, activeFrom, activeUntil));
  }

  @Override
  protected Widget renderAppender(Collection<Long> partIds, YearMonth ym,
      String selectorStyleName) {

    Flow panel = new Flow();

    Relation relation = Relation.create();
    relation.setViewName(VIEW_LOCATIONS);
    relation.disableNewRow();
    relation.disableEdit();

    relation.setChoiceColumns(Lists.newArrayList(COL_LOCATION_NAME, ALS_COMPANY_NAME, COL_ADDRESS));

    Filter filter = Filter.equals(COL_LOCATION_STATUS, ObjectStatus.ACTIVE);
    relation.setFilter(filter);

    UnboundSelector selector = UnboundSelector.create(relation,
        Lists.newArrayList(COL_LOCATION_NAME));

    selector.addStyleName(selectorStyleName);
    DomUtils.setPlaceholder(selector, Localized.getConstants().newObject());

    if (!BeeUtils.isEmpty(partIds)) {
      selector.getOracle().setExclusions(partIds);
    }

    selector.addSelectorHandler(new SelectorEvent.Handler() {
      @Override
      public void onDataSelector(SelectorEvent event) {
        if (event.isChanged() && DataUtils.hasId(event.getRelatedRow())) {
          addEmployeeObject(employeeId, event.getRelatedRow().getId(), true);
        }
      }
    });

    panel.add(selector);
    return panel;
  }

  @Override
  protected void updateCalendarInfo(YearMonth ym, BeeRow partition, CalendarInfo calendarInfo) {
  }

  @Override
  void refresh() {
    Set<String> viewNames = new HashSet<>();
    Map<String, Filter> filters = new HashMap<>();

    viewNames.add(VIEW_EMPLOYEES);
    filters.put(VIEW_EMPLOYEES, Filter.compareId(employeeId));

    viewNames.add(VIEW_WORK_SCHEDULE);
    filters.put(VIEW_WORK_SCHEDULE, Filter.equals(COL_EMPLOYEE, employeeId));

    viewNames.add(VIEW_EMPLOYEE_OBJECTS);
    filters.put(VIEW_EMPLOYEE_OBJECTS, Filter.equals(COL_EMPLOYEE, employeeId));

    viewNames.add(VIEW_TIME_CARD_CHANGES);
    filters.put(VIEW_TIME_CARD_CHANGES, Filter.equals(COL_EMPLOYEE, employeeId));

    viewNames.add(VIEW_TIME_CARD_CODES);
    viewNames.add(VIEW_TIME_RANGES);

    Queries.getData(viewNames, filters, CachingPolicy.NONE, new Queries.DataCallback() {
      @Override
      public void onSuccess(Collection<BeeRowSet> result) {
        clearData();

        for (BeeRowSet rowSet : result) {
          switch (rowSet.getViewName()) {
            case VIEW_EMPLOYEES:
              setEmData(rowSet);
              break;

            case VIEW_WORK_SCHEDULE:
              setWsData(rowSet);
              break;

            case VIEW_EMPLOYEE_OBJECTS:
              setEoData(rowSet);
              break;

            case VIEW_TIME_CARD_CHANGES:
              setTcData(rowSet);
              break;

            case VIEW_TIME_CARD_CODES:
              setTimeCardCodes(rowSet);
              break;

            case VIEW_TIME_RANGES:
              setTimeRanges(rowSet);
              break;
          }
        }

        ClassifierKeeper.getHolidays(new Consumer<Set<Integer>>() {
          @Override
          public void accept(Set<Integer> input) {
            setHolidays(input);

            getObjects(new Runnable() {
              @Override
              public void run() {
                render();
              }
            });
          }
        });
      }
    });
  }

  private BeeRow findObject(long id) {
    if (DataUtils.isEmpty(obData)) {
      return null;
    } else {
      return obData.getRowById(id);
    }
  }

  private DateRange getEmployeeRange() {
    if (DataUtils.isEmpty(emData)) {
      return null;

    } else {
      BeeRow row = emData.getRow(0);

      JustDate from = DataUtils.getDate(emData, row, COL_DATE_OF_EMPLOYMENT);
      JustDate until = DataUtils.getDate(emData, row, COL_DATE_OF_DISMISSAL);

      return DateRange.closed(from, until);
    }
  }

  private String getObjectName(long id) {
    BeeRow row = findObject(id);

    if (row == null) {
      return null;
    } else {
      return DataUtils.getString(obData, row, COL_LOCATION_NAME);
    }
  }

  private void getObjects(final Runnable callback) {
    Set<Long> objects = new HashSet<>();

    if (!DataUtils.isEmpty(getWsData())) {
      objects.addAll(getWsData().getDistinctLongs(getWsData().getColumnIndex(COL_PAYROLL_OBJECT)));
    }
    if (!DataUtils.isEmpty(getEoData())) {
      objects.addAll(getEoData().getDistinctLongs(getEoData().getColumnIndex(COL_PAYROLL_OBJECT)));
    }

    if (objects.isEmpty()) {
      callback.run();

    } else {
      Queries.getRowSet(VIEW_LOCATIONS, null, Filter.idIn(objects), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          setObData(result);
          callback.run();
        }
      });
    }
  }

  private void setEmData(BeeRowSet emData) {
    this.emData = emData;
  }

  private void setObData(BeeRowSet obData) {
    this.obData = obData;
  }
}
