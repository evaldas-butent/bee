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
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputDate;
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
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.modules.trade.acts.TradeActTimeUnit;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
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

public class TradeActInvoiceBuilder extends AbstractFormInterceptor implements
    SelectorEvent.Handler, SelectionCountChangeEvent.Handler {

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
  private static final String STYLE_SVC_UNIT_PREFIX = STYLE_SVC_PREFIX + "unit-";
  private static final String STYLE_SVC_FROM_PREFIX = STYLE_SVC_PREFIX + "from-";
  private static final String STYLE_SVC_TO_PREFIX = STYLE_SVC_PREFIX + "to-";
  private static final String STYLE_SVC_TARIFF_PREFIX = STYLE_SVC_PREFIX + "tariff-";
  private static final String STYLE_SVC_FACTOR_PREFIX = STYLE_SVC_PREFIX + "factor-";
  private static final String STYLE_SVC_DPW_PREFIX = STYLE_SVC_PREFIX + "dpw-";
  private static final String STYLE_SVC_MIN_TERM_PREFIX = STYLE_SVC_PREFIX + "minterm-";
  private static final String STYLE_SVC_QTY_PREFIX = STYLE_SVC_PREFIX + "qty-";
  private static final String STYLE_SVC_PRICE_PREFIX = STYLE_SVC_PREFIX + "price-";
  private static final String STYLE_SVC_DISCOUNT_PREFIX = STYLE_SVC_PREFIX + "discount-";

  private static final String STYLE_HEADER_CELL_SUFFIX = "label";
  private static final String STYLE_CELL_SUFFIX = "cell";

  private IdentifiableWidget commandCompose;
  private IdentifiableWidget commandSave;

  private BeeRowSet services;

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

  private static String renderItemTotal(BeeRow act) {
    String itemTotal = act.getProperty(PRP_ITEM_TOTAL);

    if (!BeeUtils.isEmpty(itemTotal)) {
      double amount = BeeUtils.toDouble(itemTotal);

      String returned = act.getProperty(PRP_RETURNED_TOTAL);
      if (!BeeUtils.isEmpty(returned)) {
        amount -= BeeUtils.toDouble(returned);
      }

      return Format.getDefaultCurrencyFormat().format(amount);

    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static String renderLatestInvoice(BeeRow act) {
    return Strings.nullToEmpty(act.getProperty(PRP_LATEST_INVOICE));
  }

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
              setServices(result);
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
    if (svcIds.isEmpty() || DataUtils.isEmpty(services)) {
      return;
    }

    BeeRowSet selectedServices = new BeeRowSet(services.getViewName(), services.getColumns());
    Set<Long> actIds = new HashSet<>();

    int actIndex = services.getColumnIndex(COL_TRADE_ACT);

    for (BeeRow svc : services) {
      if (svcIds.contains(svc.getId())) {
        selectedServices.addRow(DataUtils.cloneRow(svc));
        actIds.add(svc.getLong(actIndex));
      }
    }

    if (selectedServices.isEmpty() || actIds.isEmpty()) {
      return;
    }

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
          BeeRowSet acts = BeeRowSet.restore(response.getResponseAsString());
          renderActs(acts);
        } else if (notify) {
          getFormView().notifyWarning(Localized.getConstants().noData());
        }
      }
    });
  }

  private void renderActs(BeeRowSet acts) {
    int nameIndex = acts.getColumnIndex(COL_TA_NAME);

    int dateIndex = acts.getColumnIndex(COL_TA_DATE);
    int untilIndex = acts.getColumnIndex(COL_TA_UNTIL);

    int seriesNameIndex = acts.getColumnIndex(COL_SERIES_NAME);
    int numberIndex = acts.getColumnIndex(COL_TA_NUMBER);

    int operationNameIndex = acts.getColumnIndex(COL_OPERATION_NAME);
    int statusNameIndex = acts.getColumnIndex(COL_STATUS_NAME);

    int objectNameIndex = acts.getColumnIndex(COL_COMPANY_OBJECT_NAME);

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
    table.setText(r, c++, Localized.getLabel(acts.getColumn(nameIndex)),
        STYLE_ACT_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(acts.getColumn(dateIndex)),
        STYLE_ACT_DATE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(acts.getColumn(untilIndex)),
        STYLE_ACT_UNTIL_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(acts.getColumn(COL_TA_SERIES)),
        STYLE_ACT_SERIES_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(acts.getColumn(numberIndex)),
        STYLE_ACT_NUMBER_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(acts.getColumn(COL_TA_OPERATION)),
        STYLE_ACT_OPERATION_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(acts.getColumn(COL_TA_STATUS)),
        STYLE_ACT_STATUS_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(acts.getColumn(COL_TA_OBJECT)),
        STYLE_ACT_OBJECT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().goods(),
        STYLE_ACT_TOTAL_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().dateTo(),
        STYLE_ACT_LATEST_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_ACT_HEADER);

    r++;
    for (BeeRow act : acts) {
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

      table.setText(r, c++, BeeUtils.toString(act.getId()),
          STYLE_ACT_ID_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.getString(nameIndex),
          STYLE_ACT_NAME_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, TimeUtils.renderCompact(act.getDateTime(dateIndex)),
          STYLE_ACT_DATE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, TimeUtils.renderCompact(act.getDateTime(untilIndex)),
          STYLE_ACT_UNTIL_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.getString(seriesNameIndex),
          STYLE_ACT_SERIES_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.getString(numberIndex),
          STYLE_ACT_NUMBER_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.getString(operationNameIndex),
          STYLE_ACT_OPERATION_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, act.getString(statusNameIndex),
          STYLE_ACT_STATUS_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, act.getString(objectNameIndex),
          STYLE_ACT_OBJECT_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, renderItemTotal(act), STYLE_ACT_TOTAL_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, renderLatestInvoice(act), STYLE_ACT_LATEST_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_ACT_ROW);
      DomUtils.setDataIndex(table.getRow(r), act.getId());

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
    final int actIndex = services.getColumnIndex(COL_TRADE_ACT);
    final int itemIndex = services.getColumnIndex(COL_TA_ITEM);

    int nameIndex = services.getColumnIndex(ALS_ITEM_NAME);
    int articleIndex = services.getColumnIndex(COL_ITEM_ARTICLE);

    int unitNameIndex = services.getColumnIndex(ALS_UNIT_NAME);
    int timeUnitIndex = services.getColumnIndex(COL_TIME_UNIT);

    int isServiceIndex = services.getColumnIndex(COL_ITEM_IS_SERVICE);

    int dateFromIndex = services.getColumnIndex(COL_TA_SERVICE_FROM);
    int dateToIndex = services.getColumnIndex(COL_TA_SERVICE_TO);

    int tariffIndex = services.getColumnIndex(COL_TA_SERVICE_TARIFF);
    int factorIndex = services.getColumnIndex(COL_TA_SERVICE_FACTOR);

    int dpwIndex = services.getColumnIndex(COL_TA_SERVICE_DAYS);
    int minTermIndex = services.getColumnIndex(COL_TA_SERVICE_MIN);

    int qtyIndex = services.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = services.getColumnIndex(COL_TRADE_ITEM_PRICE);

    int discountIndex = services.getColumnIndex(COL_TRADE_DISCOUNT);

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

    table.setText(r, c++, Localized.getLabel(services.getColumn(actIndex)),
        STYLE_SVC_ACT_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(itemIndex)),
        STYLE_SVC_ITEM_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(services.getColumn(nameIndex)),
        STYLE_SVC_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(articleIndex)),
        STYLE_SVC_ARTICLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().unitShort(),
        STYLE_SVC_UNIT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getLabel(services.getColumn(dateFromIndex)),
        STYLE_SVC_FROM_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(dateToIndex)),
        STYLE_SVC_TO_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(tariffIndex)),
        STYLE_SVC_TARIFF_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(factorIndex)),
        STYLE_SVC_FACTOR_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(dpwIndex)),
        STYLE_SVC_DPW_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(minTermIndex)),
        STYLE_SVC_MIN_TERM_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(qtyIndex)),
        STYLE_SVC_QTY_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(priceIndex)),
        STYLE_SVC_PRICE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getLabel(services.getColumn(discountIndex)),
        STYLE_SVC_DISCOUNT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_SVC_HEADER);

    r++;
    for (BeeRow svc : services) {
      c = 0;

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

      table.setText(r, c++, svc.getString(actIndex), STYLE_SVC_ACT_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(itemIndex), STYLE_SVC_ITEM_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.getString(nameIndex), STYLE_SVC_NAME_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(articleIndex),
          STYLE_SVC_ARTICLE_PREFIX + STYLE_CELL_SUFFIX);

      String unit = svc.getString(unitNameIndex);
      if (!svc.isNull(isServiceIndex) && !svc.isNull(timeUnitIndex)) {
        unit = EnumUtils.getCaption(TradeActTimeUnit.class, svc.getInteger(timeUnitIndex));
      }
      table.setText(r, c++, unit, STYLE_SVC_UNIT_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, TimeUtils.renderCompact(svc.getDate(dateFromIndex)),
          STYLE_SVC_FROM_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, TimeUtils.renderCompact(svc.getDate(dateToIndex)),
          STYLE_SVC_TO_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.getString(tariffIndex),
          STYLE_SVC_TARIFF_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, svc.getString(factorIndex),
          STYLE_SVC_FACTOR_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(dpwIndex),
          STYLE_SVC_DPW_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(minTermIndex),
          STYLE_SVC_MIN_TERM_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(qtyIndex),
          STYLE_SVC_QTY_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(priceIndex),
          STYLE_SVC_PRICE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, svc.getString(discountIndex),
          STYLE_SVC_DISCOUNT_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_SVC_ROW);
      DomUtils.setDataIndex(table.getRow(r), svc.getId());

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
          BeeRow svc = services.getRowById(id);

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

            RowEditor.open(viewName, svc.getLong(index), Opener.MODAL);
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

  private void setServices(BeeRowSet services) {
    this.services = services;
  }
}
