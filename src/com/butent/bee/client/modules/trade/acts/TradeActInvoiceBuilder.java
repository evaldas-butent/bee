package com.butent.bee.client.modules.trade.acts;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectionCountChangeEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.modules.trade.acts.TradeActTimeUnit;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TradeActInvoiceBuilder extends AbstractFormInterceptor implements
    SelectorEvent.Handler, SelectionCountChangeEvent.Handler {

  private static final class Act {

    private final BeeRow row;

    @SuppressWarnings("unused")
    private final Range<DateTime> range;

    @SuppressWarnings("unused")
    private final Long currency;
    private final String currencyName;

    private Act(BeeRow row, Range<DateTime> range, Long currency, String currencyName) {
      this.row = row;

      this.range = range;

      this.currency = currency;
      this.currencyName = currencyName;
    }

    private long id() {
      return row.getId();
    }

    private Double itemTotal() {
      String value = row.getProperty(PRP_ITEM_TOTAL);

      if (BeeUtils.isEmpty(value)) {
        return null;

      } else {
        double total = BeeUtils.toDouble(value);

        String returned = row.getProperty(PRP_RETURNED_TOTAL);
        if (!BeeUtils.isEmpty(returned)) {
          total -= BeeUtils.toDouble(returned);
        }

        return total;
      }
    }
  }

  private static final class Service {

    private final BeeRow row;

    @SuppressWarnings("unused")
    private final Range<DateTime> range;

    private Double tariffPrice;

    private Service(BeeRow row, Range<DateTime> range) {
      this.row = row;
      this.range = range;
    }

    private long id() {
      return row.getId();
    }
  }

  private static final String STYLE_COMMAND_PREFIX = TradeActKeeper.STYLE_PREFIX
      + "invoice-command-";

  private static final String STYLE_COMMAND_COMPOSE = STYLE_COMMAND_PREFIX + "compose";
  private static final String STYLE_COMMAND_SAVE = STYLE_COMMAND_PREFIX + "save";
  private static final String STYLE_COMMAND_DISABLED = STYLE_COMMAND_PREFIX + "disabled";

  private static final String STYLE_ACT_PREFIX = TradeActKeeper.STYLE_PREFIX
      + "invoice-act-";

  private static final String STYLE_ACT_TABLE = STYLE_ACT_PREFIX + "table";

  private static final String STYLE_ACT_HEADER = STYLE_ACT_PREFIX + "header";
  private static final String STYLE_ACT_ROW = STYLE_ACT_PREFIX + "row";
  private static final String STYLE_ACT_SELECTED = STYLE_ACT_PREFIX + "selected";

  private static final String STYLE_ACT_TOGGLE_PREFIX = STYLE_ACT_PREFIX + "toggle-";
  private static final String STYLE_ACT_TOGGLE_WIDGET = STYLE_ACT_PREFIX + "toggle";

  private static final String STYLE_ACT_ID_PREFIX = STYLE_ACT_PREFIX + "id-";
  private static final String STYLE_ACT_NAME_PREFIX = STYLE_ACT_PREFIX + "name-";
  private static final String STYLE_ACT_DATE_PREFIX = STYLE_ACT_PREFIX + "date-";
  private static final String STYLE_ACT_UNTIL_PREFIX = STYLE_ACT_PREFIX + "until-";
  private static final String STYLE_ACT_SERIES_PREFIX = STYLE_ACT_PREFIX + "series-";
  private static final String STYLE_ACT_NUMBER_PREFIX = STYLE_ACT_PREFIX + "number-";
  private static final String STYLE_ACT_OPERATION_PREFIX = STYLE_ACT_PREFIX + "operation-";
  private static final String STYLE_ACT_STATUS_PREFIX = STYLE_ACT_PREFIX + "status-";
  private static final String STYLE_ACT_OBJECT_PREFIX = STYLE_ACT_PREFIX + "object-";
  private static final String STYLE_ACT_TOTAL_PREFIX = STYLE_ACT_PREFIX + "total-";
  private static final String STYLE_ACT_CURRENCY_PREFIX = STYLE_ACT_PREFIX + "currency-";
  private static final String STYLE_ACT_LATEST_PREFIX = STYLE_ACT_PREFIX + "latest-";

  private static final String STYLE_SVC_PREFIX = TradeActKeeper.STYLE_PREFIX
      + "invoice-service-";

  private static final String STYLE_SVC_TABLE = STYLE_SVC_PREFIX + "table";

  private static final String STYLE_SVC_HEADER = STYLE_SVC_PREFIX + "header";
  private static final String STYLE_SVC_ROW = STYLE_SVC_PREFIX + "row";
  private static final String STYLE_SVC_SELECTED = STYLE_SVC_PREFIX + "selected";

  private static final String STYLE_SVC_TOGGLE_PREFIX = STYLE_SVC_PREFIX + "toggle-";
  private static final String STYLE_SVC_TOGGLE_WIDGET = STYLE_SVC_PREFIX + "toggle";

  private static final String STYLE_SVC_ACT_PREFIX = STYLE_SVC_PREFIX + "act-";
  private static final String STYLE_SVC_ITEM_PREFIX = STYLE_SVC_PREFIX + "item-";
  private static final String STYLE_SVC_NAME_PREFIX = STYLE_SVC_PREFIX + "name-";
  private static final String STYLE_SVC_ARTICLE_PREFIX = STYLE_SVC_PREFIX + "article-";
  private static final String STYLE_SVC_TIME_UNIT_PREFIX = STYLE_SVC_PREFIX + "time-unit-";
  private static final String STYLE_SVC_UNIT_PREFIX = STYLE_SVC_PREFIX + "unit-";
  private static final String STYLE_SVC_FROM_PREFIX = STYLE_SVC_PREFIX + "from-";
  private static final String STYLE_SVC_TO_PREFIX = STYLE_SVC_PREFIX + "to-";
  private static final String STYLE_SVC_FACTOR_PREFIX = STYLE_SVC_PREFIX + "factor-";
  private static final String STYLE_SVC_DPW_PREFIX = STYLE_SVC_PREFIX + "dpw-";
  private static final String STYLE_SVC_MIN_TERM_PREFIX = STYLE_SVC_PREFIX + "minterm-";
  private static final String STYLE_SVC_QTY_PREFIX = STYLE_SVC_PREFIX + "qty-";
  private static final String STYLE_SVC_PRICE_PREFIX = STYLE_SVC_PREFIX + "price-";
  private static final String STYLE_SVC_CURRENCY_PREFIX = STYLE_SVC_PREFIX + "currency-";
  private static final String STYLE_SVC_DISCOUNT_PREFIX = STYLE_SVC_PREFIX + "discount-";
  private static final String STYLE_SVC_AMOUNT_PREFIX = STYLE_SVC_PREFIX + "amount-";

  private static final String STYLE_HEADER_CELL_SUFFIX = "label";
  private static final String STYLE_CELL_SUFFIX = "cell";
  private static final String STYLE_INPUT_SUFFIX = "input";

  private static final String STORAGE_KEY_SEPARATOR = "-";

  private static Range<DateTime> createRange(HasDateValue start, HasDateValue end) {
    if (start == null) {
      return (end == null) ? null : Range.lessThan(end.getDateTime());

    } else if (end == null) {
      return Range.atLeast(start.getDateTime());

    } else {
      DateTime lower = start.getDateTime();
      DateTime upper = end.getDateTime();

      if (lower.equals(upper)) {
        return Range.singleton(lower);
      } else if (lower.getTime() < upper.getTime()) {
        return Range.closedOpen(lower, upper);
      } else {
        return Range.closedOpen(upper, lower);
      }
    }
  }

  private static void onToggle(Toggle toggle, String styleSelected) {
    TableRowElement rowElement = DomUtils.getParentRow(toggle.getElement(), false);

    if (rowElement != null) {
      if (toggle.isChecked()) {
        rowElement.addClassName(styleSelected);
      } else {
        rowElement.removeClassName(styleSelected);
      }
    }
  }

  private static String renderAmount(Double amount) {
    if (BeeUtils.isDouble(amount)) {
      return Format.getDefaultCurrencyFormat().format(amount);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static String renderLatestInvoice(BeeRow act) {
    return Strings.nullToEmpty(act.getProperty(PRP_LATEST_INVOICE));
  }

  private static String renderPrice(Double price) {
    return renderAmount(price);
  }

  private IdentifiableWidget commandCompose;
  private IdentifiableWidget commandSave;

  private final List<Act> acts = new ArrayList<>();
  private final List<Service> services = new ArrayList<>();

  TradeActInvoiceBuilder() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (COL_TA_COMPANY.equals(name) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(this);
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.REFRESH) {
      refresh(true);
      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public void configureRelation(String name, Relation relation) {
    if (COL_TA_COMPANY.equals(name)) {
      relation.setFilter(Filter.in(Data.getIdColumn(VIEW_COMPANIES),
          VIEW_TRADE_ACTS, COL_TA_COMPANY, TradeActKind.getFilterForInvoiceBuilder()));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActInvoiceBuilder();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isChanged() && event.getRelatedRow() != null
        && event.hasRelatedView(VIEW_COMPANIES)) {
      refresh(true);
    }
  }

  @Override
  public void onLoad(FormView form) {
    Widget widget;

    JustDate from = BeeKeeper.getStorage().getDate(storageKey(COL_TA_SERVICE_FROM));
    if (from != null) {
      widget = form.getWidgetByName(COL_TA_SERVICE_FROM);
      if (widget instanceof InputDate) {
        ((InputDate) widget).setDate(from);
      }
    }

    JustDate to = BeeKeeper.getStorage().getDate(storageKey(COL_TA_SERVICE_TO));
    if (to != null) {
      widget = form.getWidgetByName(COL_TA_SERVICE_TO);
      if (widget instanceof InputDate) {
        ((InputDate) widget).setDate(to);
      }
    }

    String pfx = BeeKeeper.getStorage().get(storageKey(COL_TRADE_INVOICE_PREFIX));
    if (!BeeUtils.isEmpty(pfx)) {
      widget = form.getWidgetByName(COL_TRADE_INVOICE_PREFIX);
      if (widget instanceof Editor) {
        ((Editor) widget).setValue(pfx);
      }
    }

    Long currency = BeeKeeper.getStorage().getLong(storageKey(COL_TA_CURRENCY));
    if (DataUtils.isId(currency)) {
      widget = form.getWidgetByName(COL_TA_CURRENCY);
      if (widget instanceof UnboundSelector) {
        ((UnboundSelector) widget).setValue(currency, false);
      }
    }
  }

  @Override
  public void onSelectionCountChange(SelectionCountChangeEvent event) {
  }

  @Override
  public void onShow(Presenter presenter) {
    HeaderView header = presenter.getHeader();

    if (header != null && !header.hasCommands()) {
      if (commandCompose == null) {
        commandCompose =
            new Button(Localized.getConstants().taInvoiceCompose(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                doCompose();
              }
            });

        commandCompose.addStyleName(STYLE_COMMAND_COMPOSE);
        commandCompose.addStyleName(STYLE_COMMAND_DISABLED);
      }

      header.addCommandItem(commandCompose);

      if (commandSave == null) {
        commandSave = new Button(Localized.getConstants().taInvoiceSave(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            doSave();
          }
        });

        commandSave.addStyleName(STYLE_COMMAND_SAVE);
        commandSave.addStyleName(STYLE_COMMAND_DISABLED);
      }

      header.addCommandItem(commandSave);
    }

    super.onShow(presenter);
  }

  private void doCompose() {
    Collection<Long> actIds = getSelectedIds(STYLE_ACT_SELECTED);

    if (actIds.isEmpty()) {
      getFormView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());

    } else {
      Queries.getRowSet(VIEW_TRADE_ACT_SERVICES, null, Filter.any(COL_TRADE_ACT, actIds),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              int actIndex = result.getColumnIndex(COL_TRADE_ACT);

              int dateFromIndex = result.getColumnIndex(COL_TA_SERVICE_FROM);
              int dateToIndex = result.getColumnIndex(COL_TA_SERVICE_TO);

              int tariffIndex = result.getColumnIndex(COL_TA_SERVICE_TARIFF);
              int priceScale = result.getColumn(COL_TRADE_ITEM_PRICE).getScale();

              services.clear();
              for (BeeRow row : result) {
                Range<DateTime> range = createRange(row.getDate(dateFromIndex),
                    row.getDate(dateToIndex));

                Service svc = new Service(row, range);

                Double tariff = row.getDouble(tariffIndex);
                if (BeeUtils.isPositive(tariff)) {
                  Act act = findAct(row.getLong(actIndex));

                  if (act != null) {
                    svc.tariffPrice = TradeActUtils.calculateServicePrice(act.itemTotal(), tariff,
                        priceScale);
                  }
                }

                services.add(svc);
              }

              renderServices();
            }
          });
    }
  }

  private void doSave() {
    Range<JustDate> range = getRange();
    if (range == null) {
      return;
    }

    Long company = getCompany();
    if (!DataUtils.isId(company)) {
      return;
    }

    String invoicePrefix = getInvoicePrefix();
    Long currency = getCurrency();

    Collection<Long> svcIds = getSelectedIds(STYLE_SVC_SELECTED);
    if (svcIds.isEmpty() || services.isEmpty()) {
      return;
    }

    List<BeeColumn> serviceColumns = Data.getColumns(VIEW_TRADE_ACT_SERVICES);

    BeeRowSet selectedServices = new BeeRowSet(VIEW_TRADE_ACT_SERVICES, serviceColumns);
    Set<Long> actIds = new HashSet<>();

    int actIndex = selectedServices.getColumnIndex(COL_TRADE_ACT);

    for (Service svc : services) {
      if (svcIds.contains(svc.id())) {
        selectedServices.addRow(DataUtils.cloneRow(svc.row));
        actIds.add(svc.row.getLong(actIndex));
      }
    }

    if (selectedServices.isEmpty() || actIds.isEmpty()) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(COL_TA_SERVICE_FROM), range.lowerEndpoint());
    BeeKeeper.getStorage().set(storageKey(COL_TA_SERVICE_TO), range.upperEndpoint());

    BeeKeeper.getStorage().set(storageKey(COL_TRADE_INVOICE_PREFIX), BeeUtils.trim(invoicePrefix));
    BeeKeeper.getStorage().set(storageKey(COL_TA_CURRENCY), currency);

    DataInfo dataInfo = Data.getDataInfo(VIEW_SALES);
    BeeRow invoice = RowFactory.createEmptyRow(dataInfo, true);

    if (!BeeUtils.isEmpty(invoicePrefix)) {
      Data.squeezeValue(VIEW_SALES, invoice, COL_TRADE_INVOICE_PREFIX, invoicePrefix.trim());
    }

    invoice.setValue(dataInfo.getColumnIndex(COL_TRADE_CUSTOMER), company);

    if (DataUtils.isId(currency)) {
      invoice.setValue(dataInfo.getColumnIndex(COL_TRADE_CURRENCY), currency);
    }

    invoice.setValue(dataInfo.getColumnIndex(COL_TRADE_MANAGER), BeeKeeper.getUser().getUserId());

    BeeRowSet sales = DataUtils.createRowSetForInsert(dataInfo.getViewName(),
        dataInfo.getColumns(), invoice);

    BeeRowSet saleItems = createInvoiceItems(selectedServices);
    BeeRowSet relations = createRelations(actIds, range.lowerEndpoint(), range.upperEndpoint());

    ParameterList params = TradeActKeeper.createArgs(SVC_CREATE_ACT_INVOICE);

    params.addDataItem(sales.getViewName(), sales.serialize());
    params.addDataItem(saleItems.getViewName(), saleItems.serialize());
    params.addDataItem(relations.getViewName(), relations.serialize());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRow.class)) {
          BeeRow result = BeeRow.restore(response.getResponseAsString());

          RowInsertEvent.fire(BeeKeeper.getBus(), VIEW_SALES, result, null);
          DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_TRADE_ACT_INVOICES);

          refresh(false);
          RowEditor.open(VIEW_SALES, result, Opener.MODAL);
        }
      }
    });
  }

  private static BeeRowSet createRelations(Collection<Long> actIds, JustDate start, JustDate end) {
    List<String> colNames = Lists.newArrayList(COL_TRADE_ACT, COL_SALE,
        COL_TA_INVOICE_FROM, COL_TA_INVOICE_TO);

    List<BeeColumn> columns = Data.getColumns(VIEW_TRADE_ACT_INVOICES, colNames);
    BeeRowSet relations = new BeeRowSet(VIEW_TRADE_ACT_INVOICES, columns);

    int actIndex = relations.getColumnIndex(COL_TRADE_ACT);

    int fromIndex = relations.getColumnIndex(COL_TA_INVOICE_FROM);
    int toIndex = relations.getColumnIndex(COL_TA_INVOICE_TO);

    for (Long actId : actIds) {
      BeeRow row = DataUtils.createEmptyRow(relations.getNumberOfColumns());

      row.setValue(actIndex, actId);

      row.setValue(fromIndex, start);
      row.setValue(toIndex, end);

      relations.addRow(row);
    }

    return relations;
  }

  private static BeeRowSet createInvoiceItems(BeeRowSet selectedServices) {
    List<String> colNames = Lists.newArrayList(COL_SALE, COL_ITEM, COL_TRADE_ITEM_ARTICLE,
        COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
        COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
        COL_TRADE_ITEM_NOTE);

    List<BeeColumn> columns = Data.getColumns(VIEW_SALE_ITEMS, colNames);

    Map<Integer, Integer> indexes = new HashMap<>();

    for (int i = 0; i < columns.size(); i++) {
      String colName = columns.get(i).getId();

      if (!COL_SALE.equals(colName)) {
        int svcIndex = selectedServices.getColumnIndex(colName);
        if (!BeeConst.isUndef(svcIndex)) {
          indexes.put(svcIndex, i);
        }
      }
    }

    BeeRowSet invoiceItems = new BeeRowSet(VIEW_SALE_ITEMS, columns);

    int qtyIndex = invoiceItems.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    for (BeeRow svc : selectedServices) {
      BeeRow inv = DataUtils.createEmptyRow(invoiceItems.getNumberOfColumns());

      for (Map.Entry<Integer, Integer> entry : indexes.entrySet()) {
        if (!svc.isNull(entry.getKey())) {
          inv.setValue(entry.getValue(), svc.getString(entry.getKey()));
        }
      }

      if (inv.isNull(qtyIndex)) {
        inv.setValue(qtyIndex, 1);
      }

      invoiceItems.addRow(inv);
    }

    return invoiceItems;
  }

  private Act findAct(long id) {
    for (Act act : acts) {
      if (act.id() == id) {
        return act;
      }
    }
    return null;
  }

  private Service findService(long id) {
    for (Service svc : services) {
      if (svc.id() == id) {
        return svc;
      }
    }
    return null;
  }

  private JustDate getDateByWidgetName(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof InputDate) {
      return ((InputDate) widget).getDate();
    } else {
      return null;
    }
  }

  private Flow getActContainer() {
    return getContainerByWidgetName(VIEW_TRADE_ACTS);
  }

  private JustDate getDateFrom() {
    return getDateByWidgetName(COL_TA_SERVICE_FROM);
  }

  private JustDate getDateTo() {
    return getDateByWidgetName(COL_TA_SERVICE_TO);
  }

  private Flow getContainerByWidgetName(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof Flow) {
      return (Flow) widget;
    } else {
      return null;
    }
  }

  private Long getCompany() {
    return getSelectedIdByWidgetName(COL_TA_COMPANY);
  }

  private Long getCurrency() {
    return getSelectedIdByWidgetName(COL_TA_CURRENCY);
  }

  private String getInvoicePrefix() {
    Widget widget = getFormView().getWidgetByName(COL_TRADE_INVOICE_PREFIX);
    if (widget instanceof Editor) {
      return ((Editor) widget).getValue();
    } else {
      return null;
    }
  }

  private Range<JustDate> getRange() {
    JustDate start = getDateFrom();
    JustDate end = getDateTo();

    List<String> messages = new ArrayList<>();

    if (start == null) {
      Collections.addAll(messages, Localized.getConstants().dateFrom(),
          Localized.getConstants().valueRequired());

    } else if (end == null) {
      Collections.addAll(messages, Localized.getConstants().dateTo(),
          Localized.getConstants().valueRequired());

    } else if (TimeUtils.isMeq(start, end)) {
      Collections.addAll(messages, Localized.getConstants().invalidRange(),
          TimeUtils.renderPeriod(start, end));
    }

    if (messages.isEmpty()) {
      return Range.closedOpen(start, end);
    } else {
      getFormView().notifyWarning(ArrayUtils.toArray(messages));
      return null;
    }
  }

  private Collection<Long> getSelectedIds(String styleSelected) {
    Set<Long> ids = new HashSet<>();

    NodeList<Element> nodes = Selectors.getNodes(getFormView().getElement(),
        Selectors.classSelector(styleSelected));

    if (nodes != null) {
      for (int i = 0; i < nodes.getLength(); i++) {
        long id = DomUtils.getDataIndexLong(nodes.getItem(i));
        if (DataUtils.isId(id)) {
          ids.add(id);
        }
      }
    }

    return ids;
  }

  private Long getSelectedIdByWidgetName(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof DataSelector) {
      return ((DataSelector) widget).getRelatedId();
    } else {
      return null;
    }
  }

  private Flow getServiceContainer() {
    return getContainerByWidgetName(VIEW_TRADE_ACT_SERVICES);
  }

  private void refresh(final boolean notify) {
    Flow actContainer = getActContainer();
    if (actContainer != null && !actContainer.isEmpty()) {
      actContainer.clear();
    }

    Flow serviceContainer = getServiceContainer();
    if (serviceContainer != null && !serviceContainer.isEmpty()) {
      serviceContainer.clear();
    }

    commandCompose.addStyleName(STYLE_COMMAND_DISABLED);
    commandSave.addStyleName(STYLE_COMMAND_DISABLED);

    JustDate start = getDateFrom();
    JustDate end = getDateTo();

    Long company = getCompany();
    if (!DataUtils.isId(company)) {
      if (notify) {
        List<String> messages = Lists.newArrayList(Localized.getConstants().client(),
            Localized.getConstants().valueRequired());
        Global.showError(messages);
      }
      return;
    }

    ParameterList params = TradeActKeeper.createArgs(SVC_GET_ACTS_FOR_INVOICE);

    if (start != null) {
      params.addQueryItem(COL_TA_SERVICE_FROM, start.getDays());
    }
    if (end != null) {
      params.addQueryItem(COL_TA_SERVICE_TO, end.getDays());
    }

    params.addQueryItem(COL_TA_COMPANY, company);

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          BeeRowSet rowSet = BeeRowSet.restore(response.getResponseAsString());

          int dateIndex = rowSet.getColumnIndex(COL_TA_DATE);
          int untilIndex = rowSet.getColumnIndex(COL_TA_UNTIL);

          int currencyIndex = rowSet.getColumnIndex(COL_TA_CURRENCY);
          int currencyNameIndex = rowSet.getColumnIndex(AdministrationConstants.ALS_CURRENCY_NAME);

          acts.clear();
          for (BeeRow row : rowSet) {
            Range<DateTime> range = createRange(row.getDateTime(dateIndex),
                row.getDateTime(untilIndex));

            Long currency = row.getLong(currencyIndex);
            String currencyName = row.getString(currencyNameIndex);

            acts.add(new Act(row, range, currency, currencyName));
          }

          renderActs();

        } else if (notify) {
          getFormView().notifyWarning(Localized.getConstants().noData());
        }
      }
    });
  }

  private void renderActs() {
    DataInfo dataInfo = Data.getDataInfo(VIEW_TRADE_ACTS);
    List<BeeColumn> columns = dataInfo.getColumns();

    int nameIndex = dataInfo.getColumnIndex(COL_TA_NAME);

    int dateIndex = dataInfo.getColumnIndex(COL_TA_DATE);
    int untilIndex = dataInfo.getColumnIndex(COL_TA_UNTIL);

    int seriesNameIndex = dataInfo.getColumnIndex(COL_SERIES_NAME);
    int numberIndex = dataInfo.getColumnIndex(COL_TA_NUMBER);

    int operationNameIndex = dataInfo.getColumnIndex(COL_OPERATION_NAME);
    int statusNameIndex = dataInfo.getColumnIndex(COL_STATUS_NAME);

    int objectNameIndex = dataInfo.getColumnIndex(COL_COMPANY_OBJECT_NAME);

    final HtmlTable table = new HtmlTable(STYLE_ACT_TABLE);

    int r = 0;
    int c = 0;

    Toggle toggle = new Toggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_ACT_TOGGLE_WIDGET, false);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          boolean checked = ((Toggle) event.getSource()).isChecked();

          Collection<Toggle> toggles = UiHelper.getChildren(table, Toggle.class);
          for (Toggle t : toggles) {
            if (t.isChecked() != checked) {
              t.setChecked(checked);
              onToggle(t, STYLE_ACT_SELECTED);
            }
          }

          commandCompose.setStyleName(STYLE_COMMAND_DISABLED, !checked);
        }
      }
    });

    table.setWidget(r, c++, toggle, STYLE_ACT_TOGGLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().captionId(),
        STYLE_ACT_ID_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(columns.get(nameIndex)),
        STYLE_ACT_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(dateIndex)),
        STYLE_ACT_DATE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(columns.get(untilIndex)),
        STYLE_ACT_UNTIL_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Data.getColumnLabel(dataInfo.getViewName(), COL_TA_SERIES),
        STYLE_ACT_SERIES_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(columns.get(numberIndex)),
        STYLE_ACT_NUMBER_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Data.getColumnLabel(dataInfo.getViewName(), COL_TA_OPERATION),
        STYLE_ACT_OPERATION_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Data.getColumnLabel(dataInfo.getViewName(), COL_TA_STATUS),
        STYLE_ACT_STATUS_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Data.getColumnLabel(dataInfo.getViewName(), COL_TA_OBJECT),
        STYLE_ACT_OBJECT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().goods(),
        STYLE_ACT_TOTAL_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().currencyShort(),
        STYLE_ACT_CURRENCY_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().dateTo(),
        STYLE_ACT_LATEST_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_ACT_HEADER);

    r++;
    for (Act act : acts) {
      c = 0;

      toggle = new Toggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
          STYLE_ACT_TOGGLE_WIDGET, false);

      toggle.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (event.getSource() instanceof Toggle) {
            onToggle((Toggle) event.getSource(), STYLE_ACT_SELECTED);

            commandCompose.setStyleName(STYLE_COMMAND_DISABLED,
                !Selectors.contains(table.getElement(),
                    Selectors.classSelector(STYLE_ACT_SELECTED)));
          }
        }
      });

      table.setWidget(r, c++, toggle, STYLE_ACT_TOGGLE_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, BeeUtils.toString(act.id()),
          STYLE_ACT_ID_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.row.getString(nameIndex),
          STYLE_ACT_NAME_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, TimeUtils.renderCompact(act.row.getDateTime(dateIndex)),
          STYLE_ACT_DATE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, TimeUtils.renderCompact(act.row.getDateTime(untilIndex)),
          STYLE_ACT_UNTIL_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.row.getString(seriesNameIndex),
          STYLE_ACT_SERIES_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.row.getString(numberIndex),
          STYLE_ACT_NUMBER_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.row.getString(operationNameIndex),
          STYLE_ACT_OPERATION_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.row.getString(statusNameIndex),
          STYLE_ACT_STATUS_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.row.getString(objectNameIndex),
          STYLE_ACT_OBJECT_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, renderAmount(act.itemTotal()),
          STYLE_ACT_TOTAL_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.currencyName,
          STYLE_ACT_CURRENCY_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, renderLatestInvoice(act.row),
          STYLE_ACT_LATEST_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_ACT_ROW);
      DomUtils.setDataIndex(table.getRow(r), act.id());

      r++;
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element target = EventUtils.getEventTargetElement(event);
        TableCellElement cell = DomUtils.getParentCell(target, true);

        if (cell != null
            && (cell.hasClassName(STYLE_ACT_ID_PREFIX + STYLE_CELL_SUFFIX)
            || cell.hasClassName(STYLE_ACT_NUMBER_PREFIX + STYLE_CELL_SUFFIX))) {

          long id = DomUtils.getDataIndexLong(DomUtils.getParentRow(cell, false));
          if (DataUtils.isId(id)) {
            RowEditor.open(VIEW_TRADE_ACTS, id, Opener.MODAL);
          }
        }
      }
    });

    Flow container = getActContainer();
    if (!container.isEmpty()) {
      container.clear();
    }

    container.add(table);
  }

  private void renderServices() {
    DataInfo dataInfo = Data.getDataInfo(VIEW_TRADE_ACT_SERVICES);
    List<BeeColumn> columns = dataInfo.getColumns();

    Totalizer totalizer = new Totalizer(columns);

    final int actIndex = dataInfo.getColumnIndex(COL_TRADE_ACT);
    final int itemIndex = dataInfo.getColumnIndex(COL_TA_ITEM);

    int nameIndex = dataInfo.getColumnIndex(ALS_ITEM_NAME);
    int articleIndex = dataInfo.getColumnIndex(COL_ITEM_ARTICLE);

    int unitNameIndex = dataInfo.getColumnIndex(ALS_UNIT_NAME);
    int timeUnitIndex = dataInfo.getColumnIndex(COL_TIME_UNIT);

    int dateFromIndex = dataInfo.getColumnIndex(COL_TA_SERVICE_FROM);
    int dateToIndex = dataInfo.getColumnIndex(COL_TA_SERVICE_TO);

    int factorIndex = dataInfo.getColumnIndex(COL_TA_SERVICE_FACTOR);

    int dpwIndex = dataInfo.getColumnIndex(COL_TA_SERVICE_DAYS);
    int minTermIndex = dataInfo.getColumnIndex(COL_TA_SERVICE_MIN);

    int qtyIndex = dataInfo.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = dataInfo.getColumnIndex(COL_TRADE_ITEM_PRICE);

    int discountIndex = dataInfo.getColumnIndex(COL_TRADE_DISCOUNT);

    final HtmlTable table = new HtmlTable(STYLE_SVC_TABLE);

    int r = 0;
    int c = 0;

    Toggle toggle = new Toggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_SVC_TOGGLE_WIDGET, false);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          boolean checked = ((Toggle) event.getSource()).isChecked();

          Collection<Toggle> toggles = UiHelper.getChildren(table, Toggle.class);
          for (Toggle t : toggles) {
            if (t.isChecked() != checked) {
              t.setChecked(checked);
              onToggle(t, STYLE_SVC_SELECTED);
            }
          }

          commandSave.setStyleName(STYLE_COMMAND_DISABLED, !checked);
        }
      }
    });

    table.setWidget(r, c++, toggle, STYLE_SVC_TOGGLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(actIndex)),
        STYLE_SVC_ACT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(dateFromIndex)),
        STYLE_SVC_FROM_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(columns.get(dateToIndex)),
        STYLE_SVC_TO_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(itemIndex)),
        STYLE_SVC_ITEM_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(nameIndex)),
        STYLE_SVC_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(columns.get(articleIndex)),
        STYLE_SVC_ARTICLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(timeUnitIndex)),
        STYLE_SVC_TIME_UNIT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(qtyIndex)),
        STYLE_SVC_QTY_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().unitShort(),
        STYLE_SVC_UNIT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(priceIndex)),
        STYLE_SVC_PRICE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().currencyShort(),
        STYLE_SVC_CURRENCY_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().taFactorShort(),
        STYLE_SVC_FACTOR_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().taDaysPerWeekShort(),
        STYLE_SVC_DPW_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().taMinTermShort(),
        STYLE_SVC_MIN_TERM_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(columns.get(discountIndex)),
        STYLE_SVC_DISCOUNT_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().amount(),
        STYLE_SVC_AMOUNT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_SVC_HEADER);

    Act act = null;

    r++;
    for (Service svc : services) {
      c = 0;

      long actId = svc.row.getLong(actIndex);
      if (act == null || act.id() != actId) {
        act = findAct(actId);
      }

      toggle = new Toggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
          STYLE_SVC_TOGGLE_WIDGET, false);

      toggle.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (event.getSource() instanceof Toggle) {
            onToggle((Toggle) event.getSource(), STYLE_SVC_SELECTED);

            commandSave.setStyleName(STYLE_COMMAND_DISABLED,
                !Selectors.contains(table.getElement(),
                    Selectors.classSelector(STYLE_SVC_SELECTED)));
          }
        }
      });

      table.setWidget(r, c++, toggle, STYLE_SVC_TOGGLE_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, BeeUtils.toString(actId),
          STYLE_SVC_ACT_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, TimeUtils.renderCompact(svc.row.getDate(dateFromIndex)),
          STYLE_SVC_FROM_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, TimeUtils.renderCompact(svc.row.getDate(dateToIndex)),
          STYLE_SVC_TO_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.row.getString(itemIndex),
          STYLE_SVC_ITEM_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.row.getString(nameIndex),
          STYLE_SVC_NAME_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.row.getString(articleIndex),
          STYLE_SVC_ARTICLE_PREFIX + STYLE_CELL_SUFFIX);

      TradeActTimeUnit timeUnit = EnumUtils.getEnumByIndex(TradeActTimeUnit.class,
          svc.row.getInteger(timeUnitIndex));
      table.setText(r, c++, (timeUnit == null) ? null : timeUnit.getCaption(),
          STYLE_SVC_TIME_UNIT_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.row.getString(qtyIndex),
          STYLE_SVC_QTY_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.row.getString(unitNameIndex),
          STYLE_SVC_UNIT_PREFIX + STYLE_CELL_SUFFIX);

      Double price = BeeUtils.positive(svc.tariffPrice, svc.row.getDouble(priceIndex));
      table.setText(r, c++, renderPrice(price),
          STYLE_SVC_PRICE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, (act == null) ? null : act.currencyName,
          STYLE_SVC_CURRENCY_PREFIX + STYLE_CELL_SUFFIX);

      if (timeUnit == null) {
        table.setText(r, c++, TimeUtils.renderCompact(svc.row.getDate(dateFromIndex)),
            STYLE_SVC_FACTOR_PREFIX + STYLE_CELL_SUFFIX);

      } else {
        InputNumber factorWidget = new InputNumber();
        factorWidget.addStyleName(STYLE_SVC_FACTOR_PREFIX + STYLE_INPUT_SUFFIX);

        Double factor = svc.row.getDouble(factorIndex);
        if (BeeUtils.isPositive(factor)) {
          factorWidget.setValue(factor);
        }

        table.setWidget(r, c++, factorWidget, STYLE_SVC_FACTOR_PREFIX + STYLE_CELL_SUFFIX);
      }

      if (timeUnit == TradeActTimeUnit.DAY) {
        ListBox dpwWidget = new ListBox();
        dpwWidget.addStyleName(STYLE_SVC_DPW_PREFIX + STYLE_INPUT_SUFFIX);

        dpwWidget.addItem("5d");
        dpwWidget.addItem("6d");
        dpwWidget.addItem("7d");

        Integer dpw = svc.row.getInteger(dpwIndex);
        if (dpw != null && BeeUtils.betweenInclusive(dpw, 5, 7)) {
          dpwWidget.setSelectedIndex(dpw - 5);
        } else {
          dpwWidget.deselect();
        }

        table.setWidget(r, c++, dpwWidget, STYLE_SVC_DPW_PREFIX + STYLE_CELL_SUFFIX);

      } else {
        table.setText(r, c++, null, STYLE_SVC_DPW_PREFIX + STYLE_CELL_SUFFIX);
      }

      table.setText(r, c++, svc.row.getString(minTermIndex),
          STYLE_SVC_MIN_TERM_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.row.getString(discountIndex),
          STYLE_SVC_DISCOUNT_PREFIX + STYLE_CELL_SUFFIX);

      Double amount = totalizer.getTotal(svc.row);
      table.setText(r, c++, renderAmount(amount),
          STYLE_SVC_AMOUNT_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_SVC_ROW);
      DomUtils.setDataIndex(table.getRow(r), svc.id());

      r++;
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element target = EventUtils.getEventTargetElement(event);
        TableCellElement cell = DomUtils.getParentCell(target, true);

        if (cell != null
            && (cell.hasClassName(STYLE_SVC_ACT_PREFIX + STYLE_CELL_SUFFIX)
            || cell.hasClassName(STYLE_SVC_ITEM_PREFIX + STYLE_CELL_SUFFIX))) {

          long id = DomUtils.getDataIndexLong(DomUtils.getParentRow(cell, false));
          Service svc = findService(id);

          if (svc != null) {
            String viewName;
            int index;

            if (cell.hasClassName(STYLE_SVC_ITEM_PREFIX + STYLE_CELL_SUFFIX)) {
              viewName = VIEW_ITEMS;
              index = itemIndex;
            } else {
              viewName = VIEW_TRADE_ACTS;
              index = actIndex;
            }

            RowEditor.open(viewName, svc.row.getLong(index), Opener.MODAL);
          }
        }
      }
    });

    Flow container = getServiceContainer();
    if (!container.isEmpty()) {
      container.clear();
    }

    container.add(table);
  }

  private String storageKey(String name) {
    return BeeUtils.join(STORAGE_KEY_SEPARATOR, NameUtils.getName(this),
        BeeKeeper.getUser().getUserId(), name);
  }
}
