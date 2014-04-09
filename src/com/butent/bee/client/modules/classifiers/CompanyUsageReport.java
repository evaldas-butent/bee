package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CompanyUsageReport extends ReportInterceptor {

  private static final String NAME_RELATION = "Relation";
  private static final String NAME_OPERATOR = "Operator";
  private static final String NAME_COUNT = "Count";

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final List<String> RELATIONS = Lists.newArrayList();
  private static final List<String> SELECTOR_NAMES = Lists.newArrayList();

  private static void initRelations() {
    if (!RELATIONS.isEmpty()) {
      RELATIONS.clear();
    }

    List<String> viewNames = Lists.newArrayList(
        TradeConstants.VIEW_SALES,
        TradeConstants.VIEW_PURCHASES,
        DocumentConstants.VIEW_DOCUMENTS,
        TaskConstants.VIEW_TASKS,
        TaskConstants.VIEW_RECURRING_TASKS,
        CalendarConstants.VIEW_APPOINTMENTS,
        ServiceConstants.VIEW_OBJECTS,
        TransportConstants.VIEW_ORDERS,
        TransportConstants.VIEW_VEHICLES,
        VIEW_COMPANY_CONTACTS,
        VIEW_COMPANY_DEPARTMENTS,
        VIEW_COMPANY_PERSONS,
        VIEW_COMPANY_USERS,
        VIEW_COMPANY_RELATION_TYPE_STORE,
        VIEW_COMPANY_ACTIVITY_STORE);

    for (String viewName : viewNames) {
      if (isDataVisible(viewName)) {
        RELATIONS.add(viewName);
      }
    }
  }

  private static void initSelectorNames() {
    if (!SELECTOR_NAMES.isEmpty()) {
      SELECTOR_NAMES.clear();
    }

    if (isDataVisible(VIEW_COMPANY_TYPES)) {
      SELECTOR_NAMES.add(COL_COMPANY_TYPE);
    }
    if (isDataVisible(VIEW_COMPANY_GROUPS)) {
      SELECTOR_NAMES.add(COL_COMPANY_GROUP);
    }
    if (isDataVisible(VIEW_COMPANY_PRIORITIES)) {
      SELECTOR_NAMES.add(COL_COMPANY_PRIORITY);
    }

    if (isDataVisible(VIEW_COMPANY_RELATION_TYPES)
        && isDataVisible(VIEW_COMPANY_RELATION_TYPE_STORE)) {
      SELECTOR_NAMES.add(COL_RELATION_TYPE);
    }

    if (isDataVisible(VIEW_RELATION_TYPE_STATES)) {
      SELECTOR_NAMES.add(COL_COMPANY_RELATION_TYPE_STATE);
    }
    if (isDataVisible(VIEW_FINANCIAL_STATES)) {
      SELECTOR_NAMES.add(COL_COMPANY_FINANCIAL_STATE);
    }
    if (isDataVisible(VIEW_COMPANY_SIZES)) {
      SELECTOR_NAMES.add(COL_COMPANY_SIZE);
    }
    if (isDataVisible(VIEW_INFORMATION_SOURCES)) {
      SELECTOR_NAMES.add(COL_COMPANY_INFORMATION_SOURCE);
    }

    if (isDataVisible(VIEW_COMPANY_ACTIVITIES) && isDataVisible(VIEW_COMPANY_ACTIVITY_STORE)) {
      SELECTOR_NAMES.add(COL_ACTIVITY);
    }

    if (isDataVisible(VIEW_COUNTRIES)) {
      SELECTOR_NAMES.add(COL_COUNTRY);
    }
    if (isDataVisible(VIEW_CITIES)) {
      SELECTOR_NAMES.add(COL_CITY);
    }
  }

  private static boolean isDataVisible(String viewName) {
    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return false;
    }

    ModuleAndSub ms = ModuleAndSub.parse(dataInfo.getModule());
    return BeeKeeper.getUser().isModuleVisible(ms) && BeeKeeper.getUser().isDataVisible(viewName);
  }

  CompanyUsageReport() {
    if (RELATIONS.isEmpty()) {
      initRelations();
    }
    if (SELECTOR_NAMES.isEmpty()) {
      initSelectorNames();
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (NAME_RELATION.equals(name) && widget instanceof ListBox) {
      for (String viewName : RELATIONS) {
        ((ListBox) widget).addItem(Data.getViewCaption(viewName));
      }
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      return true;
    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CompanyUsageReport();
  }

  @Override
  public void onLoad(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    Widget widget = form.getWidgetByName(NAME_RELATION);
    Integer index = BeeKeeper.getStorage().getInteger(storageKey(NAME_RELATION, user));
    if (widget instanceof ListBox && BeeUtils.isPositive(index)
        && BeeUtils.isIndex(RELATIONS, index - 1)) {
      ((ListBox) widget).setSelectedIndex(index);
    }

    widget = form.getWidgetByName(NAME_OPERATOR);
    String op = BeeKeeper.getStorage().get(storageKey(NAME_OPERATOR, user));
    if (widget instanceof ListBox && !BeeUtils.isEmpty(op)) {
      index = ((ListBox) widget).getIndex(op);
      if (!BeeConst.isUndef(index)) {
        ((ListBox) widget).setSelectedIndex(index);
      }
    }

    widget = form.getWidgetByName(NAME_COUNT);
    String cnt = BeeKeeper.getStorage().get(storageKey(NAME_COUNT, user));
    if (widget instanceof HasStringValue && BeeUtils.isPositiveInt(cnt)) {
      ((HasStringValue) widget).setValue(cnt);
    }

    widget = form.getWidgetByName(NAME_START_DATE);
    DateTime dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_START_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_END_DATE);
    dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_END_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    for (String name : SELECTOR_NAMES) {
      widget = form.getWidgetByName(name);
      String idList = BeeKeeper.getStorage().get(storageKey(name, user));
      if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
        ((MultiSelector) widget).render(idList);
      }
    }
  }

  @Override
  public void onUnload(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(NAME_RELATION, user), getEditorValue(NAME_RELATION));
    BeeKeeper.getStorage().set(storageKey(NAME_OPERATOR, user), getEditorValue(NAME_OPERATOR));
    BeeKeeper.getStorage().set(storageKey(NAME_COUNT, user), getEditorValue(NAME_COUNT));

    BeeKeeper.getStorage().set(storageKey(NAME_START_DATE, user), getDateTime(NAME_START_DATE));
    BeeKeeper.getStorage().set(storageKey(NAME_END_DATE, user), getDateTime(NAME_END_DATE));

    for (String name : SELECTOR_NAMES) {
      BeeKeeper.getStorage().set(storageKey(name, user), getEditorValue(name));
    }
  }

  @Override
  protected void clearFilter() {
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);

    for (String name : SELECTOR_NAMES) {
      clearEditor(name);
    }
  }

  @Override
  protected void doReport() {
    List<String> args = Lists.newArrayList();

    String value = getEditorValue(NAME_RELATION);
    if (BeeUtils.isPositiveInt(value)) {
      args.add(Service.VAR_VIEW_NAME);
      args.add(RELATIONS.get(BeeUtils.toInt(value) - 1));

      value = getEditorValue(NAME_OPERATOR);
      if (!BeeUtils.isEmpty(value)) {
        args.add(Service.VAR_OPERATOR);
        args.add(value);
      }

      value = getEditorValue(NAME_COUNT);
      if (BeeUtils.isPositiveInt(value)) {
        args.add(Service.VAR_VALUE);
        args.add(value);
      }

      DateTime start = getDateTime(NAME_START_DATE);
      DateTime end = getDateTime(NAME_END_DATE);

      if (!checkRange(start, end)) {
        return;
      }

      if (start != null) {
        args.add(Service.VAR_FROM);
        args.add(start.serialize());
      }
      if (end != null) {
        args.add(Service.VAR_TO);
        args.add(end.serialize());
      }
    }

    for (String name : SELECTOR_NAMES) {
      value = getEditorValue(name);

      if (!BeeUtils.isEmpty(value)) {
        args.add(name);
        args.add(value);
      }
    }

    if (args.isEmpty()) {
      getFormView().notifyWarning(Localized.getConstants().specifyCondition());
      return;
    }

    final Filter filter = Filter.custom(FILTER_COMPANY_USAGE, args);

    Queries.getRowCount(VIEW_COMPANIES, filter, new Queries.IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (BeeUtils.isPositive(result)) {
          openGrid(filter);
        } else {
          getFormView().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  @Override
  protected String getStorageKeyPrefix() {
    return "CompanyUsageReport_";
  }

  private void openGrid(Filter filter) {
    HasIndexedWidgets container = getDataContainer();
    if (!container.isEmpty()) {
      container.clear();
    }

    GridFactory.openGrid("CompanyUsageReport", null, GridOptions.forFilter(filter),
        new PresenterCallback() {
          @Override
          public void onCreate(Presenter presenter) {
            Widget widget = presenter.getWidget().asWidget();
            StyleUtils.occupy(widget);

            getDataContainer().add(widget);
          }
        });
  }
}
