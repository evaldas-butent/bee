package com.butent.bee.client.modules.payroll;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LocationSchedule extends WorkScheduleWidget {

  private static final String VALUE_SEPARATOR = BeeConst.STRING_COLON + BeeConst.STRING_SPACE;

  private static final String dateOfEmploymentLabel =
      Data.getColumnLabel(VIEW_EMPLOYEES, COL_DATE_OF_EMPLOYMENT);
  private static final String dateOfDismissalLabel =
      Data.getColumnLabel(VIEW_EMPLOYEES, COL_DATE_OF_DISMISSAL);

  private final long objectId;

  private BeeRowSet emData;

  LocationSchedule(long objectId) {
    super(ScheduleParent.LOCATION);

    this.objectId = objectId;
  }

  @Override
  protected void clearData() {
    super.clearData();
    setEmData(null);
  }

  @Override
  protected List<BeeRow> filterPartitions(DateRange filterRange) {
    List<BeeRow> result = new ArrayList<>();

    if (!DataUtils.isEmpty(emData)) {
      Set<Long> haveWs = new HashSet<>();
      Set<Long> haveObj = new HashSet<>();

      if (!DataUtils.isEmpty(getWsData())) {
        int employeeIndex = getWsData().getColumnIndex(COL_EMPLOYEE);
        int dateIndex = getWsData().getColumnIndex(COL_WORK_SCHEDULE_DATE);

        for (BeeRow row : getWsData()) {
          if (filterRange.contains(row.getDate(dateIndex))) {
            haveWs.add(row.getLong(employeeIndex));
          }
        }
      }

      if (!DataUtils.isEmpty(getEoData())) {
        int employeeIndex = getEoData().getColumnIndex(COL_EMPLOYEE);
        int fromIndex = getEoData().getColumnIndex(COL_EMPLOYEE_OBJECT_FROM);
        int untilIndex = getEoData().getColumnIndex(COL_EMPLOYEE_OBJECT_UNTIL);

        for (BeeRow row : getEoData()) {
          DateRange range = DateRange.closed(row.getDate(fromIndex), row.getDate(untilIndex));
          if (filterRange.intersects(range)) {
            haveObj.add(row.getLong(employeeIndex));
          }
        }
      }

      if (!haveWs.isEmpty() || !haveObj.isEmpty()) {
        int fromIndex = emData.getColumnIndex(COL_DATE_OF_EMPLOYMENT);
        int untilIndex = emData.getColumnIndex(COL_DATE_OF_DISMISSAL);

        for (BeeRow row : emData) {
          if (haveWs.contains(row.getId())) {
            result.add(row);

          } else if (haveObj.contains(row.getId())) {
            DateRange range = DateRange.closed(row.getDate(fromIndex), row.getDate(untilIndex));
            if (filterRange.intersects(range)) {
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
    return partId;
  }

  @Override
  protected String getPartitionCaption(long partId) {
    return getEmployeeFullName(partId);
  }

  @Override
  protected List<Integer> getPartitionContactIndexes() {
    List<Integer> contactIndexes = new ArrayList<>();
    contactIndexes.add(emData.getColumnIndex(COL_MOBILE));
    contactIndexes.add(emData.getColumnIndex(COL_PHONE));
    return contactIndexes;
  }

  @Override
  protected List<BeeColumn> getPartitionDataColumns() {
    return emData.getColumns();
  }

  @Override
  protected List<Integer> getPartitionInfoIndexes() {
    List<Integer> infoIndexes = new ArrayList<>();
    infoIndexes.add(emData.getColumnIndex(ALS_COMPANY_NAME));
    infoIndexes.add(emData.getColumnIndex(ALS_DEPARTMENT_NAME));
    infoIndexes.add(emData.getColumnIndex(COL_TAB_NUMBER));
    return infoIndexes;
  }

  @Override
  protected List<Integer> getPartitionNameIndexes() {
    List<Integer> nameIndexes = new ArrayList<>();
    nameIndexes.add(emData.getColumnIndex(COL_FIRST_NAME));
    nameIndexes.add(emData.getColumnIndex(COL_LAST_NAME));
    return nameIndexes;
  }

  @Override
  protected long getRelationId() {
    return objectId;
  }

  @Override
  protected Filter getWorkScheduleFilter() {
    return Filter.equals(COL_PAYROLL_OBJECT, objectId);
  }

  @Override
  protected void initCalendarInfo(YearMonth ym, CalendarInfo calendarInfo) {
  }

  @Override
  protected Widget renderAppender(Collection<Long> partIds, YearMonth ym,
      String selectorStyleName) {

    Flow panel = new Flow();

    Relation relation = Relation.create();
    relation.setViewName(VIEW_EMPLOYEES);
    relation.disableNewRow();
    relation.disableEdit();

    relation.setChoiceColumns(Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME,
        ALS_COMPANY_NAME, ALS_DEPARTMENT_NAME, ALS_POSITION_NAME));
    relation.setSearchableColumns(Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));

    Filter filter = Filter.and(
        Filter.or(Filter.isNull(COL_DATE_OF_EMPLOYMENT),
            Filter.isLessEqual(COL_DATE_OF_EMPLOYMENT, new DateValue(ym.getLast()))),
        Filter.or(Filter.isNull(COL_DATE_OF_DISMISSAL),
            Filter.isMoreEqual(COL_DATE_OF_DISMISSAL, new DateValue(ym.getDate()))));

    relation.setFilter(filter);

    UnboundSelector selector = UnboundSelector.create(relation,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));

    selector.addStyleName(selectorStyleName);
    DomUtils.setPlaceholder(selector, Localized.getConstants().newEmployee());

    if (!BeeUtils.isEmpty(partIds)) {
      selector.getOracle().setExclusions(partIds);
    }

    selector.addSelectorHandler(new SelectorEvent.Handler() {
      @Override
      public void onDataSelector(SelectorEvent event) {
        if (event.isChanged() && DataUtils.hasId(event.getRelatedRow())) {
          addEmployeeObject(event.getRelatedRow().getId(), objectId, false);
        }
      }
    });

    panel.add(selector);
    return panel;
  }

  @Override
  protected void updateCalendarInfo(YearMonth ym, BeeRow partition, CalendarInfo calendarInfo) {
    calendarInfo.setTcChanges(getTimeCardChanges(partition.getId(), ym));

    JustDate activeFrom = DataUtils.getDate(emData, partition, COL_DATE_OF_EMPLOYMENT);
    JustDate activeUntil = DataUtils.getDate(emData, partition, COL_DATE_OF_DISMISSAL);

    calendarInfo.setInactiveDays(getInactiveDays(ym, activeFrom, activeUntil));

    if (calendarInfo.getInactiveDays().isEmpty()) {
      calendarInfo.setSubTitle(null);

    } else {
      String from = (activeFrom == null)
          ? null : BeeUtils.join(VALUE_SEPARATOR, dateOfEmploymentLabel, activeFrom);
      String until = (activeUntil == null)
          ? null : BeeUtils.join(VALUE_SEPARATOR, dateOfDismissalLabel, activeUntil);

      calendarInfo.setSubTitle(BeeUtils.buildLines(from, until));
    }
  }

  @Override
  void refresh() {
    Set<String> viewNames = new HashSet<>();
    Map<String, Filter> filters = new HashMap<>();

    viewNames.add(VIEW_WORK_SCHEDULE);
    filters.put(VIEW_WORK_SCHEDULE, Filter.equals(COL_PAYROLL_OBJECT, objectId));

    viewNames.add(VIEW_EMPLOYEE_OBJECTS);
    filters.put(VIEW_EMPLOYEE_OBJECTS, Filter.equals(COL_PAYROLL_OBJECT, objectId));

    viewNames.add(VIEW_TIME_CARD_CODES);
    viewNames.add(VIEW_TIME_RANGES);

    Queries.getData(viewNames, filters, CachingPolicy.NONE, new Queries.DataCallback() {
      @Override
      public void onSuccess(Collection<BeeRowSet> result) {
        clearData();

        for (BeeRowSet rowSet : result) {
          switch (rowSet.getViewName()) {
            case VIEW_WORK_SCHEDULE:
              setWsData(rowSet);
              break;

            case VIEW_EMPLOYEE_OBJECTS:
              setEoData(rowSet);
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

            getEmployees(new Consumer<Set<Long>>() {
              @Override
              public void accept(Set<Long> employees) {
                if (employees.isEmpty()) {
                  setTcData(null);
                  render();

                } else {
                  Queries.getRowSet(VIEW_TIME_CARD_CHANGES, null,
                      Filter.any(COL_EMPLOYEE, employees), new Queries.RowSetCallback() {
                        @Override
                        public void onSuccess(BeeRowSet tcRowSet) {
                          setTcData(tcRowSet);
                          render();
                        }
                      });
                }
              }
            });
          }
        });
      }
    });
  }

  private BeeRow findEmployee(long id) {
    if (DataUtils.isEmpty(emData)) {
      return null;
    } else {
      return emData.getRowById(id);
    }
  }

  private String getEmployeeFullName(long id) {
    BeeRow row = findEmployee(id);

    if (row == null) {
      return null;
    } else {
      return BeeUtils.joinWords(DataUtils.getString(emData, row, COL_FIRST_NAME),
          DataUtils.getString(emData, row, COL_LAST_NAME));
    }
  }

  private void getEmployees(final Consumer<Set<Long>> consumer) {
    final Set<Long> employees = new HashSet<>();

    if (!DataUtils.isEmpty(getWsData())) {
      employees.addAll(getWsData().getDistinctLongs(getWsData().getColumnIndex(COL_EMPLOYEE)));
    }
    if (!DataUtils.isEmpty(getEoData())) {
      employees.addAll(getEoData().getDistinctLongs(getEoData().getColumnIndex(COL_EMPLOYEE)));
    }

    if (employees.isEmpty()) {
      consumer.accept(employees);

    } else {
      Queries.getRowSet(VIEW_EMPLOYEES, null, Filter.idIn(employees), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          setEmData(result);

          if (!DataUtils.isEmpty(result)) {
            employees.addAll(result.getRowIds());
          }

          consumer.accept(employees);
        }
      });
    }
  }

  private void setEmData(BeeRowSet emData) {
    this.emData = emData;
  }
}
