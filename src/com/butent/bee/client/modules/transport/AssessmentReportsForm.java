package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.presenter.Presenter;
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
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class AssessmentReportsForm extends AbstractFormInterceptor {

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

  private static String renderPercent(Integer x, Integer y) {
    if (BeeUtils.isPositive(x) && BeeUtils.isPositive(y)) {
      return BeeUtils.toString(x * 100d / y, 2);
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

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentReportsForm();
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

    for (String groupName : NAME_GROUP_BY) {
      Widget widget = getFormView().getWidgetByName(groupName);
      if (widget instanceof ListBox) {
        ((ListBox) widget).setSelectedIndex(0);
      }
    }
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
  
  private void renderData(SimpleRowSet data) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }
    
    if (!container.isEmpty()) {
      container.clear();
    }
    
    HtmlTable table = new HtmlTable(STYLE_TABLE);
    
    int row = 0;
    int col = 0;
    for (int j = 0; j < data.getNumberOfColumns(); j++) {
      String colName = data.getColumnName(j);

      switch (colName) {
        case AR_YEAR:
          table.setText(row, col, Localized.getConstants().year(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, col, 2);
          col++;
          break;

        case AR_MONTH:
          table.setText(row, col, Localized.getConstants().month(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, col, 2);
          col++;
          break;

        case AdministrationConstants.COL_DEPARTMENT_NAME:
          table.setText(row, col, Localized.getConstants().department(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, col, 2);
          col++;
          break;

        case ClassifierConstants.COL_FIRST_NAME:
          table.setText(row, col, Localized.getConstants().manager(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, col, 2);
          col++;
          break;
          
        case ClassifierConstants.COL_LAST_NAME:
          break;

        case AR_RECEIVED:
          table.setText(row, col, Localized.getConstants().trAssessmentReportReceived(),
              STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, col, 2);
          col++;
          break;

        case AR_ANSWERED:
          table.setText(row, col, Localized.getConstants().trAssessmentReportAnswered(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, col, 2);

          table.setText(row + 1, col, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, col + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);
          col += 2;
          break;

        case AR_LOST:
          table.setText(row, col, Localized.getConstants().trAssessmentReportLost(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, col, 2);
          
          table.setText(row + 1, col, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, col + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);
          col += 2;
          break;

        case AR_APPROVED:
          table.setText(row, col, Localized.getConstants().trAssessmentReportApproved(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, col, 2);
          
          table.setText(row + 1, col, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, col + 1, 
              Localized.getConstants().trAssessmentReportApprovedToReceived(), STYLE_HEADER_2);
          table.setText(row + 1, col + 2, 
              Localized.getConstants().trAssessmentReportApprovedToAnswered(), STYLE_HEADER_2);
          col += 3;
          break;

        case AR_SECONDARY:
          table.setText(row, col, Localized.getConstants().trAssessmentReportSecondary(),
              STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, col, 2);
          
          table.setText(row + 1, col, Localized.getConstants().trAssessmentReportQuantity(),
              STYLE_HEADER_2);
          table.setText(row + 1, col + 1, Localized.getConstants().trAssessmentReportPercent(),
              STYLE_HEADER_2);
          col += 2;
          break;
          
        default:
          logger.warning("column not recognized", colName);
      }
    }
    
    row = 2;
    for (int i = 0; i < data.getNumberOfRows(); i++) {
      
      col = 0;
      for (int j = 0; j < data.getNumberOfColumns(); j++) {
        String colName = data.getColumnName(j);
        String value = data.getValue(i, j);

        switch (colName) {
          case AR_YEAR:
            table.setText(row, col++, value, STYLE_YEAR);
            break;

          case AR_MONTH:
            table.setText(row, col++, Format.renderMonthFullStandalone(BeeUtils.toInt(value)),
                STYLE_MONTH);
            break;

          case AdministrationConstants.COL_DEPARTMENT_NAME:
            table.setText(row, col++, value, STYLE_DEPARTMENT);
            break;

          case ClassifierConstants.COL_FIRST_NAME:
            table.setText(row, col++, 
                BeeUtils.joinWords(value, data.getValue(i, ClassifierConstants.COL_LAST_NAME)),
                STYLE_MANAGER);
            break;

          case AR_RECEIVED:
            table.setText(row, col++, value,
                StyleUtils.buildClasses(STYLE_RECEIVED, STYLE_QUANTITY));
            break;

          case AR_ANSWERED:
            if (BeeUtils.isPositiveInt(value)) {
              table.setText(row, col, value,
                  StyleUtils.buildClasses(STYLE_ANSWERED, STYLE_QUANTITY));
              table.setText(row, col + 1, 
                  renderPercent(BeeUtils.toInt(value), data.getInt(i, AR_RECEIVED)),
                  StyleUtils.buildClasses(STYLE_ANSWERED, STYLE_PERCENT));
            }
            col += 2;
            break;

          case AR_LOST:
            if (BeeUtils.isPositiveInt(value)) {
              table.setText(row, col, value,
                  StyleUtils.buildClasses(STYLE_LOST, STYLE_QUANTITY));
              table.setText(row, col + 1, 
                  renderPercent(BeeUtils.toInt(value), data.getInt(i, AR_RECEIVED)),
                  StyleUtils.buildClasses(STYLE_LOST, STYLE_PERCENT));
            }
            col += 2;
            break;

          case AR_APPROVED:
            if (BeeUtils.isPositiveInt(value)) {
              table.setText(row, col, value,
                  StyleUtils.buildClasses(STYLE_APPROVED, STYLE_QUANTITY));
              table.setText(row, col + 1, 
                  renderPercent(BeeUtils.toInt(value), data.getInt(i, AR_RECEIVED)),
                  StyleUtils.buildClasses(STYLE_APPROVED, STYLE_PERCENT));
              table.setText(row, col + 2, 
                  renderPercent(BeeUtils.toInt(value), data.getInt(i, AR_ANSWERED)),
                  StyleUtils.buildClasses(STYLE_APPROVED, STYLE_PERCENT));
            }
            col += 3;
            break;

          case AR_SECONDARY:
            if (BeeUtils.isPositiveInt(value)) {
              table.setText(row, col, value,
                  StyleUtils.buildClasses(STYLE_SECONDARY, STYLE_QUANTITY));
              table.setText(row, col + 1, 
                  renderPercent(BeeUtils.toInt(value), data.getInt(i, AR_RECEIVED)),
                  StyleUtils.buildClasses(STYLE_SECONDARY, STYLE_PERCENT));
            }
            col += 2;
            break;
          
        }
      }
    }
    
    container.add(table);
  }
}
