package com.butent.bee.client.modules.payroll;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
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

  private static final String KEY_YM = "ym";

  private static final int MONTH_ROW = 0;
  private static final int MONTH_COL = 1;
  private static final int DAY_ROW = 1;
  private static final int DAY_START_COL = 1;

  private static final int EMPLOYEE_START_ROW = 2;
  private static final int EMPLOYEE_PANEL_COL = 0;

  private final long objectId;

  private BeeRowSet wsData;
  private BeeRowSet eoData;
  private BeeRowSet emData;
  private BeeRowSet tcData;

  private BeeRowSet timeCardCodes;
  private BeeRowSet timeRanges;

  private YearMonth activeMonth;

  WorkScheduleWidget(long objectId) {
    super(STYLE_TABLE);

    this.objectId = objectId;
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

  private void clearData() {
    setWsData(null);
    setEoData(null);
    setEmData(null);
    setTcData(null);

    setTimeCardCodes(null);
    setTimeRanges(null);
  }

  private void render() {
    if (!isEmpty()) {
      clear();
    }

    List<YearMonth> months = getMonths();
    Widget monthPanel = renderMonths(months);
    setWidgetAndStyle(MONTH_ROW, MONTH_COL, monthPanel, STYLE_MONTH_PANEL);

    if (activeMonth == null || !months.contains(activeMonth)) {
      activateMonth(BeeUtils.getLast(months));
    }

    int days = activeMonth.getLength();
    getCellFormatter().setColSpan(MONTH_ROW, MONTH_COL, days);

    for (int i = 0; i < days; i++) {
      Label label = new Label(BeeUtils.toString(i + 1));
      setWidgetAndStyle(DAY_ROW, DAY_START_COL + i, label, STYLE_DAY_LABEL);
    }

    int r = EMPLOYEE_START_ROW;

    if (!DataUtils.isEmpty(emData)) {
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

      for (BeeRow employee : emData) {
        Widget ew = renderEmployee(employee, nameIndexes, contactIndexes, infoIndexes);
        setWidgetAndStyle(r, EMPLOYEE_PANEL_COL, ew, STYLE_EMPLOYEE_PANEL);

        renderSchedule(employee.getId(), r);

        DomUtils.setDataIndex(getRowFormatter().getElement(r), employee.getId());
        r++;
      }
    }

    Widget appender = renderEmployeeAppender();
    setWidgetAndStyle(r, EMPLOYEE_PANEL_COL, appender, STYLE_EMPLOYEE_APPEND_PANEL);

    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element targetElement = EventUtils.getEventTargetElement(event);
        TableCellElement cell = DomUtils.getParentCell(targetElement, true);

        if (cell != null) {
          long employee = DomUtils.getDataIndexLong(DomUtils.getParentRow(cell, false));
          int day = DomUtils.getDataIndexInt(cell);

          if (DataUtils.isId(employee) && day > 0) {
            editSchedule(employee, day);
          }
        }
      }
    });
  }

  private void renderSchedule(long employeeId, int r) {
    Multimap<Integer, Long> tcChanges = getTimeCardChanges(employeeId, activeMonth);

    JustDate date = activeMonth.getDate();
    int days = activeMonth.getLength();

    for (int i = 0; i < days; i++) {
      int day = i + 1;
      date.setDom(day);

      Flow panel = new Flow();

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

      if (panel.isEmpty()) {
        panel.addStyleName(STYLE_DAY_EMPTY);
      }

      int c = DAY_START_COL + i;
      setWidgetAndStyle(r, c, panel, STYLE_DAY_CONTENT);

      DomUtils.setDataIndex(getCellFormatter().getElement(r, c), day);
    }
  }

  private static String extendStyleName(String styleName, String extension) {
    return BeeUtils.join(BeeConst.STRING_MINUS, styleName,
        BeeUtils.removeWhiteSpace(extension.toLowerCase()));
  }

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

  private void editSchedule(long employee, int day) {
    JustDate date = new JustDate(activeMonth.getYear(), activeMonth.getMonth(), day);

    // List<BeeRow> schedule = filterSchedule(employee, date);

    DataInfo dataInfo = Data.getDataInfo(VIEW_WORK_SCHEDULE);
    BeeRow row = RowFactory.createEmptyRow(dataInfo, true);

    row.setValue(dataInfo.getColumnIndex(COL_PAYROLL_OBJECT), objectId);
    row.setValue(dataInfo.getColumnIndex(COL_EMPLOYEE), employee);
    row.setValue(dataInfo.getColumnIndex(COL_WORK_SCHEDULE_DATE), date);

    String caption = BeeUtils.joinWords(getEmployeeFullName(employee),
        Format.renderDateFull(date));

    RowFactory.createRow(FORM_WORK_SCHEDULE_EDITOR, caption, dataInfo, row, null, null,
        new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
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

  private Multimap<Integer, Long> getTimeCardChanges(long employeeId, YearMonth ym) {
    Multimap<Integer, Long> result = ArrayListMultimap.create();

    if (!DataUtils.isEmpty(tcData)) {
      DateRange activeRange = DateRange.closed(ym.getDate(), ym.getLast());

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

  private Widget renderEmployeeAppender() {
    Flow panel = new Flow();

    Relation relation = Relation.create();
    relation.setViewName(VIEW_EMPLOYEES);
    relation.disableNewRow();
    relation.disableEdit();

    relation.setChoiceColumns(Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME,
        ALS_COMPANY_NAME, ALS_DEPARTMENT_NAME, ALS_POSITION_NAME));
    relation.setSearchableColumns(Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));

    UnboundSelector selector = UnboundSelector.create(relation,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));

    selector.addStyleName(STYLE_EMPLOYEE_APPEND_SELECTOR);
    DomUtils.setPlaceholder(selector, Localized.getConstants().actionAppend());

    if (!DataUtils.isEmpty(emData)) {
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

  private static String formatYm(YearMonth ym) {
    return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
  }

  private static String formatDuration(String duration) {
    return BeeUtils.parenthesize(duration);
  }

  private Widget renderMonths(List<YearMonth> months) {
    Flow panel = new Flow();

    for (YearMonth ym : months) {
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

  private Element getMonthElement(YearMonth ym) {
    return Selectors.getElement(this, Selectors.attributeEquals(Attributes.DATA_PREFIX + KEY_YM,
        ym.serialize()));
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

    setActiveMonth(ym);
    return true;
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

  private List<YearMonth> getMonths() {
    List<YearMonth> result = new ArrayList<>();

    if (!DataUtils.isEmpty(wsData)) {
      int dateIndex = wsData.getColumnIndex(COL_WORK_SCHEDULE_DATE);

      for (BeeRow row : wsData) {
        JustDate date = row.getDate(dateIndex);
        if (date != null) {
          YearMonth ym = new YearMonth(date);
          if (!result.contains(ym)) {
            result.add(ym);
          }
        }
      }
    }

    if (result.isEmpty()) {
      YearMonth ym = new YearMonth(TimeUtils.today());
      result.add(ym.previousMonth());
      result.add(ym);

    } else if (result.size() > 1) {
      Collections.sort(result);
    }

    return result;
  }

  private void setActiveMonth(YearMonth activeMonth) {
    this.activeMonth = activeMonth;
  }

  private void setWsData(BeeRowSet wsData) {
    this.wsData = wsData;
  }

  private void setEoData(BeeRowSet eoData) {
    this.eoData = eoData;
  }

  private void setEmData(BeeRowSet emData) {
    this.emData = emData;
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
}
