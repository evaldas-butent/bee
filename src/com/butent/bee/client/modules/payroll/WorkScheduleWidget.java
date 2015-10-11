package com.butent.bee.client.modules.payroll;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class WorkScheduleWidget extends HtmlTable {

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "ws-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_MONTH_SELECTOR = STYLE_PREFIX + "month-selector";
  private static final String STYLE_MONTH_PANEL = STYLE_PREFIX + "month-panel";
  private static final String STYLE_MONTH_LABEL = STYLE_PREFIX + "month-label";
  private static final String STYLE_MONTH_ACTIVE = STYLE_PREFIX + "month-active";

  private static final String STYLE_DAY_LABEL = STYLE_PREFIX + "day-label";
  private static final String STYLE_DAY_CONTENT = STYLE_PREFIX + "day-content";
  private static final String STYLE_DAY_EMPTY = STYLE_PREFIX + "day-empty";

  private static final String STYLE_EMPLOYEE_PANEL = STYLE_PREFIX + "employee-panel";
  private static final String STYLE_EMPLOYEE_CONTACT = STYLE_PREFIX + "employee-contact";
  private static final String STYLE_EMPLOYEE_NAME = STYLE_PREFIX + "employee-name";
  private static final String STYLE_EMPLOYEE_INFO = STYLE_PREFIX + "employee-info";

  private static final String STYLE_EMPLOYEE_APPEND_PANEL = STYLE_PREFIX + "append-panel";
  private static final String STYLE_EMPLOYEE_APPEND_SELECTOR = STYLE_PREFIX + "append-selector";

  private static final String STYLE_TC_CHANGE = STYLE_PREFIX + "tc-change";

  private static final String STYLE_SCHEDULE_ITEM = STYLE_PREFIX + "item";
  private static final String STYLE_SCHEDULE_TR = STYLE_PREFIX + "item-tr";
  private static final String STYLE_SCHEDULE_TC = STYLE_PREFIX + "item-tc";
  private static final String STYLE_SCHEDULE_RANGE = STYLE_PREFIX + "item-range";
  private static final String STYLE_SCHEDULE_FROM = STYLE_PREFIX + "item-from";
  private static final String STYLE_SCHEDULE_UNTIL = STYLE_PREFIX + "item-until";
  private static final String STYLE_SCHEDULE_DURATION = STYLE_PREFIX + "item-duration";

  private static final String STYLE_WEEKEND = STYLE_PREFIX + "weekend";
  private static final String STYLE_HOLIDAY = STYLE_PREFIX + "holiday";
  private static final String STYLE_TODAY = STYLE_PREFIX + "today";

  private static final String STYLE_INPUT_MODE_PANEL = STYLE_PREFIX + "input-mode-panel";
  private static final String STYLE_INPUT_MODE_SIMPLE = STYLE_PREFIX + "input-mode-simple";
  private static final String STYLE_INPUT_MODE_FULL = STYLE_PREFIX + "input-mode-full";
  private static final String STYLE_INPUT_MODE_TOGGLE = STYLE_PREFIX + "input-mode-toggle";
  private static final String STYLE_INPUT_MODE_ACTIVE = STYLE_PREFIX + "input-mode-active";

  private static final String STYLE_INACTIVE = STYLE_PREFIX + "inactive";
  private static final String STYLE_OVERLAP_WARNING = STYLE_PREFIX + "overlap-warn";
  private static final String STYLE_OVERLAP_ERROR = STYLE_PREFIX + "overlap-err";

  private static final String KEY_YM = "ym";

  private static final int MONTH_ROW = 0;
  private static final int MONTH_COL = 1;
  private static final int DAY_ROW = 1;
  private static final int DAY_START_COL = 1;

  private static final int EMPLOYEE_START_ROW = 2;
  private static final int EMPLOYEE_PANEL_COL = 0;

  private static final String VALUE_SEPARATOR = BeeConst.STRING_COLON + BeeConst.STRING_SPACE;

  private static final String dateOfEmploymentLabel =
      Data.getColumnLabel(VIEW_EMPLOYEES, COL_DATE_OF_EMPLOYMENT);
  private static final String dateOfDismissalLabel =
      Data.getColumnLabel(VIEW_EMPLOYEES, COL_DATE_OF_DISMISSAL);

  private static String buildTitle(BeeRowSet rowSet, BeeRow row, String... colNames) {
    List<String> values = new ArrayList<>();

    for (String colName : colNames) {
      String value = DataUtils.getString(rowSet, row, colName);
      if (!BeeUtils.isEmpty(value)) {
        values.add(value);
      }
    }

    return BeeUtils.buildLines(values);
  }

  private static String extendStyleName(String styleName, String extension) {
    return BeeUtils.join(BeeConst.STRING_MINUS, styleName,
        BeeUtils.removeWhiteSpace(extension.toLowerCase()));
  }

  private static String formatDuration(String duration) {
    return BeeUtils.parenthesize(duration);
  }

  private static String formatYm(YearMonth ym) {
    return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
  }

  private final long objectId;

  private BeeRowSet wsData;
  private BeeRowSet eoData;
  private BeeRowSet emData;
  private BeeRowSet tcData;

  private BeeRowSet timeCardCodes;
  private BeeRowSet timeRanges;

  private final Set<Integer> holidays = new HashSet<>();

  private YearMonth activeMonth;
  private final Toggle inputMode;

  WorkScheduleWidget(long objectId) {
    super(STYLE_TABLE);

    this.objectId = objectId;

    this.inputMode = new Toggle(FontAwesome.TOGGLE_OFF, FontAwesome.TOGGLE_ON,
        STYLE_INPUT_MODE_TOGGLE, false);

    inputMode.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        activateInputMode(inputMode.isChecked());
      }
    });

    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element targetElement = EventUtils.getEventTargetElement(event);
        TableCellElement cell = DomUtils.getParentCell(targetElement, true);

        if (cell != null) {
          long employee = DomUtils.getDataIndexLong(DomUtils.getParentRow(cell, false));
          int day = DomUtils.getDataIndexInt(cell);

          if (DataUtils.isId(employee) && day > 0) {
            Widget content = getWidgetByElement(cell.getFirstChildElement());
            Flow panel = (content instanceof Flow) ? (Flow) content : null;

            if (EventUtils.hasModifierKey(event.getNativeEvent()) ^ inputMode.isChecked()) {
              editSchedule(employee, day, panel);
            } else {
              inputTimeRangeCode(employee, day, panel);
            }
          }
        }
      }
    });
  }

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
            BeeUtils.overwrite(holidays, input);

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

  private void activateInputMode(boolean modeFull) {
    Element el = Selectors.getElementByClassName(inputMode.getParent().getElement(),
        STYLE_INPUT_MODE_SIMPLE);
    if (el != null) {
      UIObject.setStyleName(el, STYLE_INPUT_MODE_ACTIVE, !modeFull);
    }

    el = Selectors.getElementByClassName(inputMode.getParent().getElement(), STYLE_INPUT_MODE_FULL);
    if (el != null) {
      UIObject.setStyleName(el, STYLE_INPUT_MODE_ACTIVE, modeFull);
    }

    if (inputMode.isChecked() != modeFull) {
      inputMode.setChecked(modeFull);
    }
  }

  private boolean activateMonth(YearMonth ym) {
    if (ym == null || Objects.equals(activeMonth, ym)) {
      return false;
    }

    if (activeMonth != null) {
      Element el = getMonthElement(activeMonth);
      if (el != null) {
        el.removeClassName(STYLE_MONTH_ACTIVE);
      }
    }

    Element monthElement = getMonthElement(ym);
    if (monthElement != null) {
      monthElement.addClassName(STYLE_MONTH_ACTIVE);
    }

    Element selectorElement = Selectors.getElementByClassName(getElement(), STYLE_MONTH_SELECTOR);
    if (selectorElement != null) {
      selectorElement.setInnerText(formatYm(ym));
    }

    setActiveMonth(ym);
    return true;
  }

  private void addDateStyles(int r, int c, JustDate date, boolean today) {
    if (TimeUtils.isWeekend(date)) {
      getCellFormatter().addStyleName(r, c, STYLE_WEEKEND);
    }

    if (holidays.contains(date.getDays())) {
      getCellFormatter().addStyleName(r, c, STYLE_HOLIDAY);
    }

    if (today) {
      getCellFormatter().addStyleName(r, c, STYLE_TODAY);
    }
  }

  private void addEmployee(long employeeId) {
    if (activeMonth != null) {
      List<BeeColumn> columns = Data.getColumns(VIEW_EMPLOYEE_OBJECTS,
          Lists.newArrayList(COL_EMPLOYEE, COL_PAYROLL_OBJECT, COL_EMPLOYEE_OBJECT_FROM));
      List<String> values = Queries.asList(employeeId, objectId, activeMonth.getDate());

      Queries.insert(VIEW_EMPLOYEE_OBJECTS, columns, values, null, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          refresh();
        }
      });
    }
  }

  private void checkOverlap() {
    List<Element> elements = Selectors.getElementsByClassName(getElement(), STYLE_OVERLAP_WARNING);
    if (!BeeUtils.isEmpty(elements)) {
      StyleUtils.removeClassName(elements, STYLE_OVERLAP_WARNING);
    }

    elements = Selectors.getElementsByClassName(getElement(), STYLE_OVERLAP_ERROR);
    if (!BeeUtils.isEmpty(elements)) {
      StyleUtils.removeClassName(elements, STYLE_OVERLAP_ERROR);
    }

    if (activeMonth != null) {
      final int startDay = activeMonth.getDate().getDays();
      final int lastDay = activeMonth.getLast().getDays();

      ParameterList params = PayrollKeeper.createArgs(SVC_GET_SCHEDULE_OVERLAP);

      params.addQueryItem(COL_PAYROLL_OBJECT, objectId);
      params.addQueryItem(Service.VAR_FROM, startDay);
      params.addQueryItem(Service.VAR_TO, lastDay);

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasResponse()) {
            Splitter splitter = Splitter.on(BeeConst.DEFAULT_ROW_SEPARATOR);

            for (String s : splitter.split(response.getResponseAsString())) {
              String pfx = BeeUtils.getPrefix(s, BeeConst.DEFAULT_VALUE_SEPARATOR);
              String sfx = BeeUtils.getSuffix(s, BeeConst.DEFAULT_VALUE_SEPARATOR);

              Long employeeId = BeeUtils.toLongOrNull(pfx);
              List<Integer> days = BeeUtils.toInts(sfx);

              if (DataUtils.isId(employeeId) && !BeeUtils.isEmpty(days)) {
                for (int day : days) {
                  if (BeeUtils.betweenInclusive(Math.abs(day), startDay, lastDay)) {
                    Element cell = findCell(employeeId, Math.abs(day) - startDay + 1);

                    if (cell != null) {
                      cell.addClassName((day > 0) ? STYLE_OVERLAP_WARNING : STYLE_OVERLAP_ERROR);
                    }
                  }
                }
              }
            }
          }
        }
      });
    }
  }

  private void clearData() {
    setWsData(null);
    setEoData(null);
    setEmData(null);
    setTcData(null);

    setTimeCardCodes(null);
    setTimeRanges(null);
  }

  private void editSchedule(final long employee, int day, final Flow contentPanel) {
    final JustDate date = new JustDate(activeMonth.getYear(), activeMonth.getMonth(), day);

    DataInfo dataInfo = Data.getDataInfo(VIEW_WORK_SCHEDULE);
    BeeRow row = RowFactory.createEmptyRow(dataInfo, true);

    row.setValue(dataInfo.getColumnIndex(COL_PAYROLL_OBJECT), objectId);
    row.setValue(dataInfo.getColumnIndex(COL_EMPLOYEE), employee);
    row.setValue(dataInfo.getColumnIndex(COL_WORK_SCHEDULE_DATE), date);

    String caption = getEmployeeFullName(employee);

    Filter filter = Filter.and(Filter.equals(COL_EMPLOYEE, employee),
        Filter.equals(COL_WORK_SCHEDULE_DATE, date));

    WorkScheduleEditor wsEditor = new WorkScheduleEditor(new Runnable() {
      @Override
      public void run() {
        updateSchedule(employee, date, contentPanel);
      }
    });

    GridFactory.registerImmutableFilter(GRID_WORK_SCHEDULE_DAY, filter);

    RowFactory.createRow(FORM_WORK_SCHEDULE_EDITOR, caption, dataInfo, row, contentPanel, wsEditor,
        new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            updateSchedule(employee, date, contentPanel);
          }
        });
  }

  private List<BeeRow> filterEmployees(DateRange filterRange) {
    List<BeeRow> result = new ArrayList<>();

    if (!DataUtils.isEmpty(emData)) {
      Set<Long> haveWs = new HashSet<>();
      Set<Long> haveObj = new HashSet<>();

      if (!DataUtils.isEmpty(wsData)) {
        int employeeIndex = wsData.getColumnIndex(COL_EMPLOYEE);
        int dateIndex = wsData.getColumnIndex(COL_WORK_SCHEDULE_DATE);

        for (BeeRow row : wsData) {
          if (filterRange.contains(row.getDate(dateIndex))) {
            haveWs.add(row.getLong(employeeIndex));
          }
        }
      }

      if (!DataUtils.isEmpty(eoData)) {
        int employeeIndex = eoData.getColumnIndex(COL_EMPLOYEE);
        int fromIndex = eoData.getColumnIndex(COL_EMPLOYEE_OBJECT_FROM);
        int untilIndex = eoData.getColumnIndex(COL_EMPLOYEE_OBJECT_UNTIL);

        for (BeeRow row : eoData) {
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

  private List<BeeRow> filterSchedule(long employee, JustDate date) {
    List<BeeRow> result = new ArrayList<>();

    if (!DataUtils.isEmpty(wsData)) {
      int employeeIndex = wsData.getColumnIndex(COL_EMPLOYEE);
      int dateIndex = wsData.getColumnIndex(COL_WORK_SCHEDULE_DATE);

      for (BeeRow row : wsData) {
        if (Objects.equals(row.getLong(employeeIndex), employee)
            && Objects.equals(row.getDate(dateIndex), date)) {

          result.add(DataUtils.cloneRow(row));
        }
      }
    }

    return result;
  }

  private Element findCell(long employeeId, int day) {
    for (int r = EMPLOYEE_START_ROW; r < getRowCount(); r++) {
      if (DomUtils.getDataIndexLong(getRow(r)) == employeeId) {
        int c = DAY_START_COL + day - 1;

        if (c < getCellCount(r)) {
          return getCellFormatter().getElement(r, c);
        }
      }
    }
    return null;
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

    if (!DataUtils.isEmpty(wsData)) {
      employees.addAll(wsData.getDistinctLongs(wsData.getColumnIndex(COL_EMPLOYEE)));
    }
    if (!DataUtils.isEmpty(eoData)) {
      employees.addAll(eoData.getDistinctLongs(eoData.getColumnIndex(COL_EMPLOYEE)));
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

  private static Set<Integer> getInactiveDays(YearMonth ym,
      JustDate activeFrom, JustDate activeUntil) {

    Set<Integer> days = new HashSet<>();

    if (activeFrom != null && BeeUtils.isMore(activeFrom, ym.getDate())) {
      int max = TimeUtils.sameMonth(activeFrom, ym) ? activeFrom.getDom() - 1 : ym.getLength();
      for (int day = 1; day <= max; day++) {
        days.add(day);
      }
    }

    if (activeUntil != null && BeeUtils.isLess(activeUntil, ym.getLast())) {
      int min = TimeUtils.sameMonth(activeUntil, ym) ? activeUntil.getDom() + 1 : 1;
      int max = ym.getLength();

      for (int day = min; day <= max; day++) {
        days.add(day);
      }
    }

    return days;
  }

  private Element getMonthElement(YearMonth ym) {
    return Selectors.getElement(this, Selectors.attributeEquals(Attributes.DATA_PREFIX + KEY_YM,
        ym.serialize()));
  }

  private List<YearMonth> getMonths() {
    List<YearMonth> result = new ArrayList<>();

    YearMonth ym = new YearMonth(TimeUtils.today());
    result.add(ym.previousMonth());
    result.add(ym);
    result.add(ym.nextMonth());

    if (!DataUtils.isEmpty(wsData)) {
      int dateIndex = wsData.getColumnIndex(COL_WORK_SCHEDULE_DATE);

      for (BeeRow row : wsData) {
        JustDate date = row.getDate(dateIndex);
        if (date != null) {
          ym = new YearMonth(date);
          if (!result.contains(ym)) {
            result.add(ym);
          }
        }
      }

      Collections.sort(result);
    }

    return result;
  }

  private Multimap<Integer, Long> getTimeCardChanges(long employeeId, YearMonth ym) {
    Multimap<Integer, Long> result = ArrayListMultimap.create();

    if (!DataUtils.isEmpty(tcData)) {
      DateRange activeRange = ym.getRange();

      int employeeIndex = tcData.getColumnIndex(COL_EMPLOYEE);
      int codeIndex = tcData.getColumnIndex(COL_TIME_CARD_CODE);

      int fromIndex = tcData.getColumnIndex(COL_TIME_CARD_CHANGES_FROM);
      int untilIndex = tcData.getColumnIndex(COL_TIME_CARD_CHANGES_UNTIL);

      for (BeeRow row : tcData) {
        if (Objects.equals(row.getLong(employeeIndex), employeeId)) {
          JustDate from = row.getDate(fromIndex);
          JustDate until = row.getDate(untilIndex);

          if (DateRange.isValidClosedRange(from, until)) {
            DateRange range = activeRange.intersection(DateRange.closed(from, until));

            if (range != null) {
              int min = range.getMinDate().getDom();
              int max = range.getMaxDate().getDom();

              for (int day = min; day <= max; day++) {
                result.put(day, row.getLong(codeIndex));
              }
            }
          }
        }
      }
    }

    return result;
  }

  private void inputTimeRangeCode(final long employee, int day, final Flow contentPanel) {
    if (DataUtils.isEmpty(timeRanges)) {
      return;
    }

    final JustDate date = new JustDate(activeMonth.getYear(), activeMonth.getMonth(), day);

    Set<Long> usedCodes = new HashSet<>();

    List<BeeRow> schedule = filterSchedule(employee, date);
    if (!schedule.isEmpty()) {
      int index = wsData.getColumnIndex(COL_TIME_RANGE_CODE);
      for (BeeRow row : schedule) {
        Long code = row.getLong(index);
        if (DataUtils.isId(code)) {
          usedCodes.add(code);
        }
      }
    }

    final List<Long> codes = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    int codeIndex = timeRanges.getColumnIndex(COL_TR_CODE);
    int nameIndex = timeRanges.getColumnIndex(COL_TR_NAME);

    int fromIndex = timeRanges.getColumnIndex(COL_TR_FROM);
    int untilIndex = timeRanges.getColumnIndex(COL_TR_UNTIL);

    for (BeeRow row : timeRanges) {
      if (!usedCodes.contains(row.getId())) {
        codes.add(row.getId());
        labels.add(BeeUtils.joinWords(row.getString(codeIndex), row.getString(nameIndex),
            TimeUtils.renderPeriod(row.getString(fromIndex), row.getString(untilIndex))));
      }
    }

    if (!codes.isEmpty()) {
      String caption = getEmployeeFullName(employee);
      String prompt = Format.renderDateFull(date);

      Global.choiceWithCancel(caption, prompt, labels, new ChoiceCallback() {
        @Override
        public void onSuccess(int value) {
          if (BeeUtils.isIndex(codes, value)) {
            List<BeeColumn> columns = Data.getColumns(VIEW_WORK_SCHEDULE,
                Lists.newArrayList(COL_EMPLOYEE, COL_PAYROLL_OBJECT, COL_WORK_SCHEDULE_DATE,
                    COL_TIME_RANGE_CODE));
            List<String> values = Queries.asList(employee, objectId, date, codes.get(value));

            Queries.insert(VIEW_WORK_SCHEDULE, columns, values, null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                updateSchedule(employee, date, contentPanel);
              }
            });
          }
        }
      });
    }
  }

  private void render() {
    if (!isEmpty()) {
      clear();
    }

    Widget monthSelector = renderMonthSelector();
    setWidgetAndStyle(MONTH_ROW, MONTH_COL - 1, monthSelector, STYLE_MONTH_SELECTOR);

    List<YearMonth> months = getMonths();
    Widget monthPanel = renderMonths(months);
    setWidgetAndStyle(MONTH_ROW, MONTH_COL, monthPanel, STYLE_MONTH_PANEL);

    if (activeMonth == null || !months.contains(activeMonth)) {
      activateMonth(new YearMonth(TimeUtils.today()));
    }

    int days = activeMonth.getLength();
    getCellFormatter().setColSpan(MONTH_ROW, MONTH_COL, days);

    JustDate date = activeMonth.getDate();

    JustDate today = TimeUtils.today();
    int td = TimeUtils.sameMonth(date, today) ? today.getDom() : BeeConst.UNDEF;

    for (int i = 0; i < days; i++) {
      int day = i + 1;
      date.setDom(day);

      Label label = new Label(BeeUtils.toString(day));

      int c = DAY_START_COL + i;
      setWidgetAndStyle(DAY_ROW, c, label, STYLE_DAY_LABEL);

      addDateStyles(DAY_ROW, c, date, day == td);
      getCellFormatter().getElement(DAY_ROW, c).setTitle(Format.renderDateFull(date));
    }

    int r = EMPLOYEE_START_ROW;

    List<BeeRow> employees = filterEmployees(activeMonth.getRange());

    if (!employees.isEmpty()) {
      List<Integer> nameIndexes = new ArrayList<>();
      nameIndexes.add(emData.getColumnIndex(COL_FIRST_NAME));
      nameIndexes.add(emData.getColumnIndex(COL_LAST_NAME));

      List<Integer> contactIndexes = new ArrayList<>();
      contactIndexes.add(emData.getColumnIndex(COL_MOBILE));
      contactIndexes.add(emData.getColumnIndex(COL_PHONE));

      List<Integer> infoIndexes = new ArrayList<>();
      infoIndexes.add(emData.getColumnIndex(ALS_COMPANY_NAME));
      infoIndexes.add(emData.getColumnIndex(ALS_DEPARTMENT_NAME));
      infoIndexes.add(emData.getColumnIndex(COL_TAB_NUMBER));

      for (BeeRow employee : employees) {
        Widget ew = renderEmployee(employee, nameIndexes, contactIndexes, infoIndexes);
        setWidgetAndStyle(r, EMPLOYEE_PANEL_COL, ew, STYLE_EMPLOYEE_PANEL);

        renderSchedule(employee.getId(), r);

        DomUtils.setDataIndex(getRowFormatter().getElement(r), employee.getId());
        r++;
      }

      checkOverlap();
    }

    Widget appender = renderEmployeeAppender(DataUtils.getRowIds(employees), activeMonth);
    setWidgetAndStyle(r, EMPLOYEE_PANEL_COL, appender, STYLE_EMPLOYEE_APPEND_PANEL);

    Widget inputModePanel = renderInputMode();
    setWidgetAndStyle(r, DAY_START_COL, inputModePanel, STYLE_INPUT_MODE_PANEL);
    getCellFormatter().setColSpan(r, DAY_START_COL, days);
  }

  private void renderDayContent(Flow panel, long employeeId, JustDate date,
      Multimap<Integer, Long> tcChanges) {

    if (!panel.isEmpty()) {
      panel.clear();
    }

    int day = date.getDom();

    List<BeeRow> schedule = filterSchedule(employeeId, date);

    if (tcChanges.containsKey(day)) {
      for (Long codeId : tcChanges.get(day)) {
        panel.add(renderTimeCardChange(codeId));
      }
    }

    for (BeeRow wsRow : schedule) {
      Widget widget = renderSheduleItem(wsRow);
      if (widget != null) {
        widget.addStyleName(STYLE_SCHEDULE_ITEM);
        panel.add(widget);
      }
    }

    panel.setStyleName(STYLE_DAY_EMPTY, panel.isEmpty());
  }

  private Widget renderEmployee(BeeRow row, List<Integer> nameIndexes,
      List<Integer> contactIndexes, List<Integer> infoIndexes) {

    Flow panel = new Flow();
    DomUtils.setDataIndex(panel.getElement(), row.getId());

    Label nameWidget = new Label(DataUtils.join(emData.getColumns(), row, nameIndexes,
        BeeConst.STRING_SPACE));
    nameWidget.addStyleName(STYLE_EMPLOYEE_NAME);

    nameWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element targetElement = EventUtils.getEventTargetElement(event);
        long id = DomUtils.getDataIndexLong(DomUtils.getParentRow(targetElement, false));

        if (DataUtils.isId(id)) {
          RowEditor.open(VIEW_EMPLOYEES, id, Opener.MODAL);
        }
      }
    });

    panel.add(nameWidget);

    Label contactWidget = new Label(DataUtils.join(emData.getColumns(), row, contactIndexes,
        BeeConst.DEFAULT_LIST_SEPARATOR));
    contactWidget.addStyleName(STYLE_EMPLOYEE_CONTACT);

    panel.add(contactWidget);

    Label infoWidget = new Label(DataUtils.join(emData.getColumns(), row, infoIndexes,
        BeeConst.DEFAULT_LIST_SEPARATOR));
    infoWidget.addStyleName(STYLE_EMPLOYEE_INFO);

    panel.add(infoWidget);

    return panel;
  }

  private Widget renderEmployeeAppender(Collection<Long> employees, YearMonth ym) {
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

    selector.addStyleName(STYLE_EMPLOYEE_APPEND_SELECTOR);
    DomUtils.setPlaceholder(selector, Localized.getConstants().newEmployee());

    if (!BeeUtils.isEmpty(employees)) {
      selector.getOracle().setExclusions(emData.getRowIds());
    }

    selector.addSelectorHandler(new SelectorEvent.Handler() {
      @Override
      public void onDataSelector(SelectorEvent event) {
        if (event.isChanged() && DataUtils.hasId(event.getRelatedRow())) {
          addEmployee(event.getRelatedRow().getId());
        }
      }
    });

    panel.add(selector);
    return panel;
  }

  private Widget renderInputMode() {
    Flow panel = new Flow();

    Label modeSimple = new Label(Localized.getConstants().inputSimple());
    modeSimple.addStyleName(STYLE_INPUT_MODE_SIMPLE);

    modeSimple.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (inputMode.isChecked()) {
          activateInputMode(false);
        }
      }
    });

    Label modeFull = new Label(Localized.getConstants().inputFull());
    modeFull.addStyleName(STYLE_INPUT_MODE_FULL);

    modeFull.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!inputMode.isChecked()) {
          activateInputMode(true);
        }
      }
    });

    if (inputMode.isChecked()) {
      modeFull.addStyleName(STYLE_INPUT_MODE_ACTIVE);
    } else {
      modeSimple.addStyleName(STYLE_INPUT_MODE_ACTIVE);
    }

    panel.add(modeSimple);
    panel.add(inputMode);
    panel.add(modeFull);

    return panel;
  }

  private Widget renderMonths(List<YearMonth> months) {
    Flow panel = new Flow();

    int size = months.size();
    int from = Math.max(size - 6, 0);

    for (YearMonth ym : months.subList(from, size)) {
      Label widget = new Label(formatYm(ym));

      widget.addStyleName(STYLE_MONTH_LABEL);
      if (ym.equals(activeMonth)) {
        widget.addStyleName(STYLE_MONTH_ACTIVE);
      }

      DomUtils.setDataProperty(widget.getElement(), KEY_YM, ym.serialize());

      widget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String s = DomUtils.getDataProperty(EventUtils.getEventTargetElement(event), KEY_YM);

          if (!BeeUtils.isEmpty(s) && activateMonth(YearMonth.parse(s))) {
            render();
          }
        }
      });

      panel.add(widget);
    }

    return panel;
  }

  private Widget renderMonthSelector() {
    Button selector = new Button();
    if (activeMonth != null) {
      selector.setText(formatYm(activeMonth));
    }

    selector.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final List<YearMonth> months = getMonths();

        List<String> labels = new ArrayList<>();
        for (YearMonth ym : months) {
          labels.add(formatYm(ym));
        }

        Global.choiceWithCancel(Localized.getConstants().yearMonth(), null, labels,
            new ChoiceCallback() {
              @Override
              public void onSuccess(int value) {
                if (BeeUtils.isIndex(months, value) && activateMonth(months.get(value))) {
                  render();
                }
              }
            });
      }
    });

    return selector;
  }

  private void renderSchedule(long employeeId, int r) {
    Multimap<Integer, Long> tcChanges = getTimeCardChanges(employeeId, activeMonth);

    JustDate date = activeMonth.getDate();
    int days = activeMonth.getLength();

    JustDate today = TimeUtils.today();
    int td = TimeUtils.sameMonth(date, today) ? today.getDom() : BeeConst.UNDEF;

    String emplName = getEmployeeFullName(employeeId);

    BeeRow row = findEmployee(employeeId);
    JustDate activeFrom = (row == null)
        ? null : DataUtils.getDate(emData, row, COL_DATE_OF_EMPLOYMENT);
    JustDate activeUntil = (row == null)
        ? null : DataUtils.getDate(emData, row, COL_DATE_OF_DISMISSAL);

    Set<Integer> inactiveDays = getInactiveDays(activeMonth, activeFrom, activeUntil);

    String activityTitle;
    if (inactiveDays.isEmpty()) {
      activityTitle = null;

    } else {
      String from = (activeFrom == null)
          ? null : BeeUtils.join(VALUE_SEPARATOR, dateOfEmploymentLabel, activeFrom);
      String until = (activeUntil == null)
          ? null : BeeUtils.join(VALUE_SEPARATOR, dateOfDismissalLabel, activeUntil);

      activityTitle = BeeUtils.buildLines(from, until);
    }

    for (int i = 0; i < days; i++) {
      int day = i + 1;
      date.setDom(day);

      Flow panel = new Flow();
      renderDayContent(panel, employeeId, date, tcChanges);

      int c = DAY_START_COL + i;
      setWidgetAndStyle(r, c, panel, STYLE_DAY_CONTENT);

      addDateStyles(r, c, date, day == td);

      TableCellElement cell = getCellFormatter().getElement(r, c);
      DomUtils.setDataIndex(cell, day);
      cell.setTitle(BeeUtils.buildLines(emplName, Format.renderDateLong(date),
          Format.renderDayOfWeek(date), activityTitle));

      if (inactiveDays.contains(day)) {
        cell.addClassName(STYLE_INACTIVE);
      }
    }
  }

  private Widget renderSheduleItem(BeeRow item) {
    String note = DataUtils.getString(wsData, item, COL_WORK_SCHEDULE_NOTE);

    Long trId = DataUtils.getLong(wsData, item, COL_TIME_RANGE_CODE);

    if (DataUtils.isId(trId) && !DataUtils.isEmpty(timeRanges)) {
      BeeRow trRow = timeRanges.getRowById(trId);

      if (trRow != null) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_TR);

        String trCode = DataUtils.getString(timeRanges, trRow, COL_TR_CODE);
        if (!BeeUtils.isEmpty(trCode)) {
          widget.setText(trCode);
          widget.addStyleName(extendStyleName(STYLE_SCHEDULE_TR, trCode));
        }

        String title = BeeUtils.buildLines(DataUtils.getString(timeRanges, trRow, COL_TR_NAME),
            TimeUtils.renderPeriod(DataUtils.getString(timeRanges, trRow, COL_TR_FROM),
                DataUtils.getString(timeRanges, trRow, COL_TR_UNTIL)),
            DataUtils.getString(timeRanges, trRow, COL_TR_DESCRIPTION), note);
        if (!BeeUtils.isEmpty(title)) {
          widget.setTitle(title);
        }

        UiHelper.setColor(widget,
            DataUtils.getString(timeRanges, trRow, AdministrationConstants.COL_BACKGROUND),
            DataUtils.getString(timeRanges, trRow, AdministrationConstants.COL_FOREGROUND));

        return widget;
      }
    }

    Long tcId = DataUtils.getLong(wsData, item, COL_TIME_CARD_CODE);

    if (DataUtils.isId(tcId) && !DataUtils.isEmpty(timeCardCodes)) {
      BeeRow tcRow = timeCardCodes.getRowById(tcId);

      if (tcRow != null) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_TC);

        String tcCode = DataUtils.getString(timeCardCodes, tcRow, COL_TC_CODE);
        if (!BeeUtils.isEmpty(tcCode)) {
          widget.setText(tcCode);
          widget.addStyleName(extendStyleName(STYLE_SCHEDULE_TC, tcCode));
        }

        String title = BeeUtils.buildLines(
            buildTitle(timeCardCodes, tcRow, COL_TC_NAME, COL_TC_DESCRIPTION), note);
        if (!BeeUtils.isEmpty(title)) {
          widget.setTitle(title);
        }

        UiHelper.setColor(widget,
            DataUtils.getString(timeCardCodes, tcRow, AdministrationConstants.COL_BACKGROUND),
            DataUtils.getString(timeCardCodes, tcRow, AdministrationConstants.COL_FOREGROUND));

        return widget;
      }
    }

    String from = DataUtils.getString(wsData, item, COL_WORK_SCHEDULE_FROM);
    String until = DataUtils.getString(wsData, item, COL_WORK_SCHEDULE_UNTIL);
    String duration = DataUtils.getString(wsData, item, COL_WORK_SCHEDULE_DURATION);

    if (!BeeUtils.isEmpty(from) || !BeeUtils.isEmpty(until)) {
      Flow panel = new Flow(STYLE_SCHEDULE_RANGE);
      if (!BeeUtils.isEmpty(note)) {
        panel.setTitle(note);
      }

      if (!BeeUtils.isEmpty(from)) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_FROM);
        widget.setText(from);
        panel.add(widget);
      }

      if (!BeeUtils.isEmpty(until)) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_UNTIL);
        widget.setText(until);
        panel.add(widget);
      }

      if (!BeeUtils.isEmpty(duration)) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_DURATION);
        widget.setText(formatDuration(duration));
        panel.add(widget);
      }

      return panel;
    }

    if (!BeeUtils.isEmpty(duration)) {
      CustomDiv widget = new CustomDiv(STYLE_SCHEDULE_DURATION);
      widget.setText(formatDuration(duration));

      if (!BeeUtils.isEmpty(note)) {
        widget.setTitle(note);
      }

      return widget;
    }

    return null;
  }

  private Widget renderTimeCardChange(long codeId) {
    CustomDiv widget = new CustomDiv(STYLE_TC_CHANGE);

    if (!DataUtils.isEmpty(timeCardCodes)) {
      BeeRow row = timeCardCodes.getRowById(codeId);

      if (row != null) {
        String code = DataUtils.getString(timeCardCodes, row, COL_TC_CODE);
        if (!BeeUtils.isEmpty(code)) {
          widget.setText(code);
          widget.addStyleName(extendStyleName(STYLE_TC_CHANGE, code));
        }

        String title = buildTitle(timeCardCodes, row, COL_TC_NAME, COL_TC_DESCRIPTION);
        if (!BeeUtils.isEmpty(title)) {
          widget.setTitle(title);
        }

        UiHelper.setColor(widget,
            DataUtils.getString(timeCardCodes, row, AdministrationConstants.COL_BACKGROUND),
            DataUtils.getString(timeCardCodes, row, AdministrationConstants.COL_FOREGROUND));
      }
    }

    return widget;
  }

  private void setActiveMonth(YearMonth activeMonth) {
    this.activeMonth = activeMonth;
  }

  private void setEmData(BeeRowSet emData) {
    this.emData = emData;
  }

  private void setEoData(BeeRowSet eoData) {
    this.eoData = eoData;
  }

  private void setTcData(BeeRowSet tcData) {
    this.tcData = tcData;
  }

  private void setTimeCardCodes(BeeRowSet timeCardCodes) {
    this.timeCardCodes = timeCardCodes;
  }

  private void setTimeRanges(BeeRowSet timeRanges) {
    this.timeRanges = timeRanges;
  }

  private void setWsData(BeeRowSet wsData) {
    this.wsData = wsData;
  }

  private void updateSchedule(final long employeeId, final JustDate date,
      final Flow contentPanel) {

    if (contentPanel == null) {
      refresh();

    } else {
      Queries.getRowSet(VIEW_WORK_SCHEDULE, null, Filter.equals(COL_PAYROLL_OBJECT, objectId),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              setWsData(result);

              Multimap<Integer, Long> tcc = getTimeCardChanges(employeeId, YearMonth.of(date));
              renderDayContent(contentPanel, employeeId, date, tcc);

              checkOverlap();
            }
          });
    }
  }
}
