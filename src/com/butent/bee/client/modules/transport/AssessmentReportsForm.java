package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasStringValue;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Set;

public class AssessmentReportsForm extends AbstractFormInterceptor implements Printable {

  private static BeeLogger logger = LogUtils.getLogger(AssessmentReportsForm.class);

  private static final String STORAGE_KEY_PREFIX = "AssessmentReports_";

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final String NAME_DEPARTMENTS = "Departments";
  private static final String NAME_MANAGERS = "Managers";

  private static final String NAME_DATA_CONTAINER = "DataContainer";

  private static final List<String> NAME_GROUP_BY =
      Lists.newArrayList("Group0", "Group1", "Group2");

  private static final String STYLE_PREFIX = StyleUtils.CLASS_NAME_PREFIX + "tr-ar-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";
  private static final String STYLE_HEADER_1 = STYLE_HEADER + "-1";
  private static final String STYLE_HEADER_2 = STYLE_HEADER + "-2";

  private static final String STYLE_YEAR = STYLE_PREFIX + "year";
  private static final String STYLE_MONTH = STYLE_PREFIX + "month";
  private static final String STYLE_DEPARTMENT = STYLE_PREFIX + "department";
  private static final String STYLE_MANAGER = STYLE_PREFIX + "manager";

  private static final String STYLE_QUANTITY = STYLE_PREFIX + "quantity";
  private static final String STYLE_PERCENT = STYLE_PREFIX + "percent";

  private static final String STYLE_RECEIVED = STYLE_PREFIX + "received";
  private static final String STYLE_ANSWERED = STYLE_PREFIX + "answered";
  private static final String STYLE_LOST = STYLE_PREFIX + "lost";
  private static final String STYLE_APPROVED = STYLE_PREFIX + "approved";
  private static final String STYLE_SECONDARY = STYLE_PREFIX + "secondary";

  private static final String STYLE_DETAILS = STYLE_PREFIX + "details";
  private static final String STYLE_SUMMARY = STYLE_PREFIX + "summary";

  private static final NumberFormat percentFormat = Format.getNumberFormat("0.0");
  private static final NumberFormat quantityFormat = Format.getNumberFormat("#,###");

  private static void drillDown(String caption, Filter filter, boolean modal) {
    GridOptions gridOptions = GridOptions.forCaptionAndFilter(caption, filter);

    PresenterCallback presenterCallback;
    if (modal) {
      presenterCallback = ModalGrid.opener(80, CssUnit.PCT, 50, CssUnit.PCT);
    } else {
      presenterCallback = PresenterCallback.SHOW_IN_NEW_TAB;
    }

    GridFactory.openGrid("AssessmentReportDrillDown", null, gridOptions, presenterCallback);
  }

  private static String renderPercent(int x, int y) {
    if (x > 0 && y > 0) {
      return percentFormat.format(x * 100d / y);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static String renderQuantity(int x) {
    if (x > 0) {
      return quantityFormat.format(x);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static String storageKey(String name, long user) {
    return STORAGE_KEY_PREFIX + name + user;
  }

  private static void widgetNotFound(String name) {
    logger.severe("widget not found", name);
  }

  AssessmentReportsForm() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case REFRESH:
      case FILTER:
        doReport();
        return false;

      case REMOVE_FILTER:
        clearFilter();
        return false;

      case PRINT:
        if (hasReport()) {
          Printer.print(this);
        }
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public String getCaption() {
    return getFormView().getCaption();
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentReportsForm();
  }

  @Override
  public Element getPrintElement() {
    if (hasReport()) {
      return getDataContainer().getWidget(0).getElement();
    } else {
      return null;
    }
  }

  @Override
  public void onLoad(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    Widget widget = form.getWidgetByName(NAME_START_DATE);
    DateTime dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_START_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_END_DATE);
    dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_END_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_DEPARTMENTS);
    String idList = BeeKeeper.getStorage().get(storageKey(NAME_DEPARTMENTS, user));
    if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
      ((MultiSelector) widget).render(idList);
    }

    widget = form.getWidgetByName(NAME_MANAGERS);
    idList = BeeKeeper.getStorage().get(storageKey(NAME_MANAGERS, user));
    if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
      ((MultiSelector) widget).render(idList);
    }

    for (String groupName : NAME_GROUP_BY) {
      widget = form.getWidgetByName(groupName);
      Integer index = BeeKeeper.getStorage().getInteger(storageKey(groupName, user));
      if (widget instanceof ListBox && BeeUtils.isPositive(index)) {
        ((ListBox) widget).setSelectedIndex(index);
      }
    }
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    return true;
  }

  @Override
  public void onUnload(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(NAME_START_DATE, user), getDateTime(NAME_START_DATE));
    BeeKeeper.getStorage().set(storageKey(NAME_END_DATE, user), getDateTime(NAME_END_DATE));

    BeeKeeper.getStorage().set(storageKey(NAME_DEPARTMENTS, user),
        getEditorValue(NAME_DEPARTMENTS));
    BeeKeeper.getStorage().set(storageKey(NAME_MANAGERS, user),
        getEditorValue(NAME_MANAGERS));

    for (String groupName : NAME_GROUP_BY) {
      Widget widget = form.getWidgetByName(groupName);
      if (widget instanceof ListBox) {
        Integer index = ((ListBox) widget).getSelectedIndex();
        if (!BeeUtils.isPositive(index)) {
          index = null;
        }

        BeeKeeper.getStorage().set(storageKey(groupName, user), index);
      }
    }
  }

  private void clearEditor(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof Editor) {
      ((Editor) widget).clearValue();
    } else {
      widgetNotFound(name);
    }
  }

  private void clearFilter() {
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);

    clearEditor(NAME_DEPARTMENTS);
    clearEditor(NAME_MANAGERS);
  }

  private void doReport() {
    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (start != null && end != null && TimeUtils.isMore(start, end)) {
      getFormView().notifyWarning(Localized.getConstants().invalidRange(),
          TimeUtils.renderPeriod(start, end));
      return;
    }

    ParameterList params = TransportHandler.createArgs(SVC_GET_ASSESSMENT_REPORT);

    if (start != null) {
      params.addDataItem(Service.VAR_FROM, start.getTime());
    }
    if (end != null) {
      params.addDataItem(Service.VAR_TO, end.getTime());
    }

    String departments = getEditorValue(NAME_DEPARTMENTS);
    if (!BeeUtils.isEmpty(departments)) {
      params.addDataItem(AR_DEPARTMENT, departments);
    }
    String managers = getEditorValue(NAME_MANAGERS);
    if (!BeeUtils.isEmpty(managers)) {
      params.addDataItem(AR_MANAGER, managers);
    }

    List<String> groupBy = Lists.newArrayList();
    for (String groupName : NAME_GROUP_BY) {
      Widget widget = getFormView().getWidgetByName(groupName);

      if (widget instanceof ListBox) {
        int index = ((ListBox) widget).getSelectedIndex();
        String group;

        switch (index) {
          case 1:
            group = AR_MONTH;
            break;
          case 2:
            group = AR_DEPARTMENT;
            break;
          case 3:
            group = AR_MANAGER;
            break;
          default:
            group = null;
        }

        if (group != null && !groupBy.contains(group)) {
          groupBy.add(group);
        }
      }
    }

    if (!groupBy.isEmpty()) {
      params.addDataItem(Service.VAR_GROUP_BY, NameUtils.join(groupBy));
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasMessages()) {
          response.notify(getFormView());
        }

        if (response.hasResponse(SimpleRowSet.class)) {
          renderData(SimpleRowSet.restore(response.getResponseAsString()));
        } else {
          getFormView().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  private HasIndexedWidgets getDataContainer() {
    Widget widget = getFormView().getWidgetByName(NAME_DATA_CONTAINER);
    if (widget instanceof HasIndexedWidgets) {
      return (HasIndexedWidgets) widget;
    } else {
      widgetNotFound(NAME_DATA_CONTAINER);
      return null;
    }
  }

  private DateTime getDateTime(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof InputDateTime) {
      return ((InputDateTime) widget).getDateTime();
    } else {
      widgetNotFound(name);
      return null;
    }
  }

  private String getEditorValue(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof HasStringValue) {
      return ((HasStringValue) widget).getValue();
    } else {
      widgetNotFound(name);
      return null;
    }
  }

  private String getFilterLabel(String name) {
    Widget widget = getFormView().getWidgetByName(name);

    if (widget instanceof MultiSelector) {
      MultiSelector selector = (MultiSelector) widget;
      List<Long> ids = DataUtils.parseIdList(selector.getValue());

      if (ids.isEmpty()) {
        return null;
      } else {
        List<String> labels = Lists.newArrayList();
        for (Long id : ids) {
          labels.add(selector.getRowLabel(id));
        }

        return BeeUtils.joinItems(labels);
      }

    } else {
      widgetNotFound(name);
      return null;
    }
  }

  private boolean hasReport() {
    HasIndexedWidgets container = getDataContainer();
    return container != null && !container.isEmpty();
  }

  private void renderData(final SimpleRowSet data) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }

    if (!container.isEmpty()) {
      container.clear();
    }

    final HtmlTable table = new HtmlTable(STYLE_TABLE);

    int row = 0;
    int c1 = 0;
    int c2 = 0;
    int col = 0;

    int colYear = BeeConst.UNDEF;
    int colMonth = BeeConst.UNDEF;
    int colDepartment = BeeConst.UNDEF;
    int colManager = BeeConst.UNDEF;
    int colReceived = BeeConst.UNDEF;
    int colAnswered = BeeConst.UNDEF;
    int colLost = BeeConst.UNDEF;
    int colApproved = BeeConst.UNDEF;
    int colSecondary = BeeConst.UNDEF;

    for (int j = 0; j < data.getNumberOfColumns(); j++) {
      String colName = data.getColumnName(j);

      switch (colName) {
        case AR_YEAR:
          colYear = col;

          table.setText(row, c1, Localized.getConstants().year(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AR_MONTH:
          colMonth = col;

          table.setText(row, c1, Localized.getConstants().month(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AdministrationConstants.COL_DEPARTMENT:
          colDepartment = col;

          table.setText(row, c1, Localized.getConstants().department(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AdministrationConstants.COL_DEPARTMENT_NAME:
          break;

        case ClassifierConstants.COL_COMPANY_PERSON:
          colManager = col;

          table.setText(row, c1, Localized.getConstants().manager(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case ClassifierConstants.COL_FIRST_NAME:
        case ClassifierConstants.COL_LAST_NAME:
          break;

        case AR_RECEIVED:
          colReceived = col;

          table.setText(row, c1, Localized.getConstants().trAssessmentReportReceived(),
              STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AR_ANSWERED:
          colAnswered = col;

          table.setText(row, c1, Localized.getConstants().trAssessmentReportAnswered(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, c1, 2);

          table.setText(row + 1, c2, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);

          c1++;
          c2 += 2;
          col += 2;
          break;

        case AR_LOST:
          colLost = col;

          table.setText(row, c1, Localized.getConstants().trAssessmentReportLost(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, c1, 2);

          table.setText(row + 1, c2, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);

          c1++;
          c2 += 2;
          col += 2;
          break;

        case AR_APPROVED:
          colApproved = col;

          table.setText(row, c1, Localized.getConstants().trAssessmentReportApproved(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, c1, 3);

          table.setText(row + 1, c2, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1,
              Localized.getConstants().trAssessmentReportApprovedToReceived(), STYLE_HEADER_2);
          table.setText(row + 1, c2 + 2,
              Localized.getConstants().trAssessmentReportApprovedToAnswered(), STYLE_HEADER_2);

          c1++;
          c2 += 3;
          col += 3;
          break;

        case AR_SECONDARY:
          colSecondary = col;

          table.setText(row, c1, Localized.getConstants().trAssessmentReportSecondary(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, c1, 2);

          table.setText(row + 1, c2, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);

          c1++;
          c2 += 2;
          col += 3;
          break;

        default:
          logger.warning("column not recognized", colName);
      }
    }

    int totReceived = 0;
    int totAnswered = 0;
    int totLost = 0;
    int totApproved = 0;
    int totSecondary = 0;

    row = 2;

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      int received = BeeUtils.unbox(data.getInt(i, AR_RECEIVED));
      int answered = BeeUtils.unbox(data.getInt(i, AR_ANSWERED));
      int lost = BeeUtils.unbox(data.getInt(i, AR_LOST));
      int approved = BeeUtils.unbox(data.getInt(i, AR_APPROVED));
      int secondary = BeeUtils.unbox(data.getInt(i, AR_SECONDARY));

      for (int j = 0; j < data.getNumberOfColumns(); j++) {
        String colName = data.getColumnName(j);

        switch (colName) {
          case AR_YEAR:
            table.setText(row, colYear, data.getValue(i, colName), STYLE_YEAR);
            break;

          case AR_MONTH:
            table.setText(row, colMonth,
                Format.renderMonthFullStandalone(data.getInt(i, colName)), STYLE_MONTH);
            break;

          case AdministrationConstants.COL_DEPARTMENT_NAME:
            table.setText(row, colDepartment, data.getValue(i, colName), STYLE_DEPARTMENT);
            break;

          case ClassifierConstants.COL_FIRST_NAME:
            table.setText(row, colManager, BeeUtils.joinWords(data.getValue(i, colName),
                data.getValue(i, ClassifierConstants.COL_LAST_NAME)), STYLE_MANAGER);
            break;

          case AR_RECEIVED:
            table.setText(row, colReceived, renderQuantity(received),
                STYLE_RECEIVED, STYLE_QUANTITY);
            break;

          case AR_ANSWERED:
            table.setText(row, colAnswered, renderQuantity(answered),
                STYLE_ANSWERED, STYLE_QUANTITY);
            table.setText(row, colAnswered + 1, renderPercent(answered, received),
                STYLE_ANSWERED, STYLE_PERCENT);
            break;

          case AR_LOST:
            table.setText(row, colLost, renderQuantity(lost), STYLE_LOST, STYLE_QUANTITY);
            table.setText(row, colLost + 1, renderPercent(lost, received),
                STYLE_LOST, STYLE_PERCENT);
            break;

          case AR_APPROVED:
            table.setText(row, colApproved, renderQuantity(approved),
                STYLE_APPROVED, STYLE_QUANTITY);

            table.setText(row, colApproved + 1, renderPercent(approved, received),
                STYLE_APPROVED, STYLE_PERCENT);
            table.setText(row, colApproved + 2, renderPercent(approved, answered + approved),
                STYLE_APPROVED, STYLE_PERCENT);
            break;

          case AR_SECONDARY:
            table.setText(row, colSecondary, renderQuantity(secondary),
                STYLE_SECONDARY, STYLE_QUANTITY);
            table.setText(row, colSecondary + 1, renderPercent(secondary, received),
                STYLE_SECONDARY, STYLE_PERCENT);
            break;
        }
      }

      totReceived += received;
      totAnswered += answered;
      totLost += lost;
      totApproved += approved;
      totSecondary += secondary;

      table.getRowFormatter().addStyleName(row, STYLE_DETAILS);
      DomUtils.setDataIndex(table.getRow(row), i);

      row++;
    }

    if (data.getNumberOfRows() > 1) {
      table.setText(row, colReceived, renderQuantity(totReceived),
          STYLE_RECEIVED, STYLE_QUANTITY);

      table.setText(row, colAnswered, renderQuantity(totAnswered),
          STYLE_ANSWERED, STYLE_QUANTITY);
      table.setText(row, colAnswered + 1, renderPercent(totAnswered, totReceived),
          STYLE_ANSWERED, STYLE_PERCENT);

      table.setText(row, colLost, renderQuantity(totLost), STYLE_LOST, STYLE_QUANTITY);
      table.setText(row, colLost + 1, renderPercent(totLost, totReceived),
          STYLE_LOST, STYLE_PERCENT);

      table.setText(row, colApproved, renderQuantity(totApproved),
          STYLE_APPROVED, STYLE_QUANTITY);

      table.setText(row, colApproved + 1, renderPercent(totApproved, totReceived),
          STYLE_APPROVED, STYLE_PERCENT);
      table.setText(row, colApproved + 2, renderPercent(totApproved, totAnswered + totApproved),
          STYLE_APPROVED, STYLE_PERCENT);

      table.setText(row, colSecondary, renderQuantity(totSecondary),
          STYLE_SECONDARY, STYLE_QUANTITY);
      table.setText(row, colSecondary + 1, renderPercent(totSecondary, totReceived),
          STYLE_SECONDARY, STYLE_PERCENT);

      table.getRowFormatter().addStyleName(row, STYLE_SUMMARY);
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TableCellElement cellElement =
            DomUtils.getParentCell(EventUtils.getEventTargetElement(event), true);
        TableRowElement rowElement = DomUtils.getParentRow(cellElement, false);

        if (cellElement != null
            && !BeeUtils.isEmpty(cellElement.getInnerText())
            && (cellElement.hasClassName(STYLE_QUANTITY) || cellElement.hasClassName(STYLE_PERCENT))
            && rowElement != null && rowElement.hasClassName(STYLE_DETAILS)) {

          int dataIndex = DomUtils.getDataIndexInt(rowElement);

          if (!BeeConst.isUndef(dataIndex)) {
            boolean modal = Popup.getActivePopup() != null
                || EventUtils.hasModifierKey(event.getNativeEvent());
            showDetails(data.getRow(dataIndex), cellElement, modal);
          }
        }
      }
    });

    container.add(table);
  }

  private void showDetails(SimpleRow dataRow, TableCellElement cellElement, final boolean modal) {
    Set<Long> departments = Sets.newHashSet();

    final CompoundFilter filter = Filter.and();
    List<String> captions = Lists.newArrayList();

    String[] colNames = dataRow.getColumnNames();

    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (ArrayUtils.contains(colNames, AR_YEAR) && ArrayUtils.contains(colNames, AR_MONTH)) {
      Integer year = BeeUtils.unbox(dataRow.getInt(AR_YEAR));
      Integer month = BeeUtils.unbox(dataRow.getInt(AR_MONTH));

      if (TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {
        if (start == null && end == null) {
          captions.add(BeeUtils.joinWords(year, Format.renderMonthFullStandalone(month)));
        }

        YearMonth ym = new YearMonth(year, month);

        if (start == null) {
          start = ym.getDate().getDateTime();
        }
        if (end == null) {
          end = TimeUtils.startOfNextMonth(ym).getDateTime();
        }
      }
    }

    if (start != null) {
      filter.add(Filter.isMoreEqual(COL_ORDER_DATE, new DateTimeValue(start)));
    }
    if (end != null) {
      filter.add(Filter.isLess(COL_ORDER_DATE, new DateTimeValue(end)));
    }

    if (captions.isEmpty() && (start != null || end != null)) {
      captions.add(TimeUtils.renderPeriod(start, end));
    }

    if (ArrayUtils.contains(colNames, ClassifierConstants.COL_COMPANY_PERSON)) {
      Long companyPerson = dataRow.getLong(ClassifierConstants.COL_COMPANY_PERSON);
      if (DataUtils.isId(companyPerson)) {
        filter.add(Filter.equals(ClassifierConstants.COL_COMPANY_PERSON, companyPerson));
        captions.add(BeeUtils.joinWords(dataRow.getValue(ClassifierConstants.COL_FIRST_NAME),
            dataRow.getValue(ClassifierConstants.COL_LAST_NAME)));
      }

    } else {
      if (ArrayUtils.contains(colNames, AdministrationConstants.COL_DEPARTMENT)) {
        departments.add(dataRow.getLong(AdministrationConstants.COL_DEPARTMENT));
        captions.add(dataRow.getValue(AdministrationConstants.COL_DEPARTMENT_NAME));

      } else {
        String input = getEditorValue(NAME_DEPARTMENTS);
        if (!BeeUtils.isEmpty(input)) {
          departments.addAll(DataUtils.parseIdSet(input));
          captions.add(getFilterLabel(NAME_DEPARTMENTS));
        }
      }

      String managers = getEditorValue(NAME_MANAGERS);
      if (!BeeUtils.isEmpty(managers)) {
        filter.add(Filter.any(ClassifierConstants.COL_COMPANY_PERSON,
            DataUtils.parseIdSet(managers)));
        captions.add(getFilterLabel(NAME_MANAGERS));
      }
    }

    AssessmentStatus status = null;

    if (cellElement.hasClassName(STYLE_ANSWERED)) {
      status = AssessmentStatus.ANSWERED;
      captions.add(Localized.getConstants().trAssessmentReportAnswered());

    } else if (cellElement.hasClassName(STYLE_LOST)) {
      status = AssessmentStatus.LOST;
      captions.add(Localized.getConstants().trAssessmentReportLost());

    } else if (cellElement.hasClassName(STYLE_APPROVED)) {
      status = AssessmentStatus.APPROVED;
      captions.add(Localized.getConstants().trAssessmentReportApproved());

    } else if (cellElement.hasClassName(STYLE_SECONDARY)) {
      filter.add(Filter.notNull(COL_ASSESSMENT));
      captions.add(Localized.getConstants().trAssessmentReportSecondary());
    }

    if (status == null) {
      filter.add(Filter.notNull(COL_ASSESSMENT_STATUS));
    } else {
      filter.add(Filter.isEqual(COL_ASSESSMENT_STATUS, new IntegerValue(status.ordinal())));
    }

    final String caption = BeeUtils.notEmpty(BeeUtils.joinItems(captions),
        Localized.getConstants().trAssessmentRequests());

    if (departments.isEmpty()) {
      drillDown(caption, filter, modal);

    } else {
      ParameterList params = TransportHandler.createArgs(SVC_GET_MANAGERS_BY_DEPARTMENT);

      String value = DataUtils.buildIdList(departments);
      if (BeeUtils.isEmpty(value)) {
        value = BeeConst.STRING_ZERO;
      }
      params.addDataItem(AdministrationConstants.COL_DEPARTMENT, value);

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasResponse()) {
            filter.add(Filter.any(ClassifierConstants.COL_COMPANY_PERSON,
                DataUtils.parseIdSet(response.getResponseAsString())));
          }

          drillDown(caption, filter, modal);
        }
      });
    }
  }
}
