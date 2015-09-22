package com.butent.bee.client.modules.payroll;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
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
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class WorkScheduleWidget extends HtmlTable {

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "ws-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_MONTH_PANEL = STYLE_PREFIX + "month-panel";
  private static final String STYLE_MONTH_LABEL = STYLE_PREFIX + "month-label";
  private static final String STYLE_MONTH_ACTIVE = STYLE_PREFIX + "month-active";

  private static final String STYLE_DAY_LABEL = STYLE_PREFIX + "day-label";

  private static final String STYLE_EMPLOYEE_PANEL = STYLE_PREFIX + "employee-panel";
  private static final String STYLE_EMPLOYEE_CONTACT = STYLE_PREFIX + "employee-contact";
  private static final String STYLE_EMPLOYEE_NAME = STYLE_PREFIX + "employee-name";
  private static final String STYLE_EMPLOYEE_INFO = STYLE_PREFIX + "employee-info";

  private static final String STYLE_EMPLOYEE_APPEND_PANEL = STYLE_PREFIX + "append-panel";
  private static final String STYLE_EMPLOYEE_APPEND_SELECTOR = STYLE_PREFIX + "append-selector";

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

  private YearMonth activeMonth;

  WorkScheduleWidget(long objectId) {
    super(STYLE_TABLE);

    this.objectId = objectId;
  }

  void refresh() {
    Queries.getRowSet(VIEW_WORK_SCHEDULE, null, Filter.equals(COL_PAYROLL_OBJECT, objectId),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet wsRowSet) {
            setWsData(wsRowSet);

            Queries.getRowSet(VIEW_EMPLOYEE_OBJECTS, null,
                Filter.equals(COL_PAYROLL_OBJECT, objectId), new Queries.RowSetCallback() {
                  @Override
                  public void onSuccess(BeeRowSet eoRowSet) {
                    setEoData(eoRowSet);

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

        DomUtils.setDataIndex(getRowFormatter().getElement(r), employee.getId());
        r++;
      }
    }

    Widget appender = renderEmployeeAppender();
    setWidgetAndStyle(r, EMPLOYEE_PANEL_COL, appender, STYLE_EMPLOYEE_APPEND_PANEL);
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

  private static String format(YearMonth ym) {
    return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
  }

  private Widget renderMonths(List<YearMonth> months) {
    Flow panel = new Flow();

    for (YearMonth ym : months) {
      Label widget = new Label(format(ym));

      widget.addStyleName(STYLE_MONTH_LABEL);
      if (ym.equals(activeMonth)) {
        widget.addStyleName(STYLE_MONTH_ACTIVE);
      }

      DomUtils.setDataProperty(widget.getElement(), KEY_YM, ym.serialize());

      widget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String s = DomUtils.getDataProperty(EventUtils.getEventTargetElement(event), KEY_YM);

          if (!BeeUtils.isEmpty(s)) {
            activateMonth(YearMonth.parse(s));
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
      result.add(new YearMonth(TimeUtils.today()));
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
}
