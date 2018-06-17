package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.modules.trade.acts.TradeActTimeUnit;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

public class PrintActForm extends AbstractFormInterceptor {

  private static final String ITEMS_WIDGET_NAME = "TradeActItems";
  private static final String SERVICES_WIDGET_NAME = "TradeActServices";

  /**
   * Perdavimo aktas (be liko)
   */
  private static final String FORM_PRINT_TA_NO_STOCK = "PrintTASaleNoStock";

  /**
   * Perdavimo aktas
   */
  private static final String FORM_PRINT_TA_SALE_PHYSICAL = "PrintTradeActSalePhysical";

  /**
   * Grąžinimo aktas
   */
  private static final String FORM_PRINT_TA_RETURN = "PrintTradeActReturn";

  /**
   * Grąžinimo aktas (be liko)
   */
  private static final String FORM_PRINT_TA_RETURN_EXTRA = "PrintTradeActReturnExtra";

  private static final String FORM_PRINT_TA_SALE_RENT = "PrintTASaleRent";
  private static final String FORM_PRINT_TA_SALE_ADDITION = "PrintTASaleAddition";
  private static final String FORM_PRINT_TA_SALE_PROFORMA = "PrintTASaleProforma";
  private static final String FORM_PRINT_TA_SUGGESTION = "PrintTASaleSuggestion";
  private static final String VAR_PRINT_RENTAL = "PRent";

  private static final String[] COLUMN_LIST = new String[] {
      COL_ITEM_ARTICLE, COL_ITEM_NAME, COL_TA_SERVICE_FROM, COL_TA_SERVICE_TO,
      COL_TRADE_ITEM_QUANTITY, COL_UNIT, COL_TRADE_TIME_UNIT, COL_TA_RETURNED_QTY,
      "RemainingQty", COL_TRADE_WEIGHT, COL_ITEM_AREA, COL_TA_SERVICE_TARIFF, COL_ITEM_RENTAL_PRICE,
      COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, "Amount", "AmountVat", COL_TRADE_VAT, "AmountTotal",
      "MinTermAmount", "RemainWeight"
  };

  final Map<String, String> tableHeaders = new HashMap<>();
  final Map<Long, Double> remainQty = new HashMap<>();
  final Table<String, String, Boolean> visibleServiceCols = HashBasedTable.create();
  final Table<String, String, Boolean> visibleItemsCols = HashBasedTable.create();
  Map<String, Widget> companies = new HashMap<>();
  List<Widget> totals = new ArrayList<>();
  List<Widget> totalsOf = new ArrayList<>();
  Consumer<Double> totConsumer;


  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.inListSame(name, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER,
        ClassifierConstants.COL_COMPANY)) {
      companies.put(name, widget.asWidget());

    } else if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.asWidget());
    } else if (BeeUtils.startsSame(name, "TotalOf")) {
      totalsOf.add(widget.asWidget());
    }
  }

  @Override
  public void onLoad(FormView form) {
    TradeActKeeper.ensureSendMailPrintableForm(form);
    super.onLoad(form);
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
    if (!DataUtils.isId(row.getLong(getDataIndex(COL_TA_PARENT)))
        && !TradeActKind.RETURN.equals(kind)
        && (BeeUtils.same(BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL),
        FORM_PRINT_TA_RETURN)
            || BeeUtils.same(BeeUtils.removePrefix(getFormView().getFormName(), VAR_PRINT_RENTAL ),
            FORM_PRINT_TA_RETURN_EXTRA))) {
      form.getViewPresenter().handleAction(Action.CLOSE);
      BeeKeeper.getScreen().notifySevere(Localized.dictionary().taSelectKindReturn());
      return false;
    }
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }

    totConsumer = new Consumer<Double>() {
      static final int MAX_COUNT = 2;
      double totalOf;
      int count;

      @Override
      public void accept(Double input) {
        totalOf += input;
        count++;

        if (count >= MAX_COUNT) {
          renderTotalOf(totalOf);
        }

      }
    };

    TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
    if (DataUtils.isId(row.getLong(getDataIndex(COL_TA_PARENT)))
            && TradeActKind.RETURN.equals(kind)) {
      ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_RETURN);
      params.addQueryItem(COL_TRADE_ACT, row.getLong(getDataIndex(COL_TA_PARENT)));
      BeeKeeper.getRpc().makeRequest(params, response -> {
        if (response != null && response.hasResponse(BeeRowSet.class)) {
          BeeRowSet parentItems = BeeRowSet.restore(response.getResponseAsString());
          remainQty.putAll(TradeActUtils.getItemQuantities(parentItems));
        }

        renderItems(ITEMS_WIDGET_NAME);
        renderItems(SERVICES_WIDGET_NAME);
      });

    } else if (!DataUtils.isId(row.getLong(getDataIndex(COL_TA_PARENT)))
            && DataUtils.isId(row.getLong(getDataIndex(COL_TA_CONTINUOUS)))
            && TradeActKind.RETURN.equals(kind)) {
      Queries.getRowSet(VIEW_TRADE_ACT_ITEMS, Lists.newArrayList(COL_TA_ITEM, COL_TRADE_ITEM_QUANTITY),
              Filter.and(Filter.equals(COL_TRADE_ACT, row.getLong(getDataIndex(COL_TA_CONTINUOUS))),
                      Filter.notNull(COL_TA_PARENT)), result -> {
                remainQty.putAll(TradeActUtils.getItemQuantities(result, true));
                renderItems(ITEMS_WIDGET_NAME);
                renderItems(SERVICES_WIDGET_NAME);
      });
    } else if (!DataUtils.isId(row.getLong(getDataIndex(COL_TA_PARENT)))
        && TradeActKind.RETURN.equals(kind)) {
      Queries.getRowSet(VIEW_TRADE_ACT_ITEMS, Lists.newArrayList(COL_TA_PARENT),
          Filter.and(Filter.equals(COL_TRADE_ACT, row.getId()), Filter.notNull(COL_TA_PARENT)),
              result -> {
                Set<Long> acts = result.getDistinctLongs(result.getColumnIndex(COL_TA_PARENT));

                if (BeeUtils.isEmpty(acts)) {
                  form.notifyWarning(Localized.dictionary().taParent(),
                      Localized.dictionary().tradeAct(), Localized.dictionary().noData());
                  return;
                }
                ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_MULTI_RETURN);
                params.addQueryItem(Service.VAR_LIST, DataUtils.buildIdList(acts));
                BeeKeeper.getRpc().makeRequest(params, response -> {
                  if (response != null && response.hasResponse(BeeRowSet.class)) {
                    BeeRowSet parentItems = BeeRowSet.restore(response.getResponseAsString());
                    remainQty.putAll(TradeActUtils.getItemQuantities(parentItems));
                  }

                  renderItems(ITEMS_WIDGET_NAME);
                  renderItems(SERVICES_WIDGET_NAME);
                });
              });
    } else {
      remainQty.clear();
      renderItems(ITEMS_WIDGET_NAME);
      renderItems(SERVICES_WIDGET_NAME);
    }

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintActForm();
  }

  public PrintActForm() {
    tableHeaders.put("Article", "Artikulas");
    tableHeaders.put("Quantity", "Kiekis");
    tableHeaders.put("Unit", "Mato vnt.");
    tableHeaders.put("TimeUnit", "Laiko vnt.");
    tableHeaders.put("ReturnedQty", "Grąžinta");
    tableHeaders.put("RemainingQty", "Liko");
    tableHeaders.put("DateFrom", "Data nuo");
    tableHeaders.put("DateTo", "Data iki");
    tableHeaders.put("Weight", "Svoris");
    tableHeaders.put("RemainWeight", "Likęs svoris");
    tableHeaders.put("Area", "Plotas");
    tableHeaders.put("Tariff", "Tarifas");
    tableHeaders.put(COL_ITEM_RENTAL_PRICE, "Nuomos kaina");
    tableHeaders.put("Price", "Kaina");
    tableHeaders.put("Discount", "Nuol.");
    tableHeaders.put("Amount", "Suma be PVM");
    tableHeaders.put("AmountVat", "Suma su PVM");
    tableHeaders.put("Vat", "PVM");
    tableHeaders.put("AmountTotal", "Suma");
    tableHeaders.put("MinTermAmount", "Suma už min. term.");
    tableHeaders.put("Name", "Nuomojama įranga");

    for (String column : COLUMN_LIST) {
      switch (column) {
        case "RemainingQty":
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          break;
        case "MinTermAmount":
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case "AmountTotal":
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          break;
        case "AmountVat":
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK  + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT  + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION  + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_TRADE_VAT:
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_ITEM_AREA:
          break;
        case "Amount":
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA  + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION  + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA  + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK  + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN  + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION  + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION  + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_ITEM_RENTAL_PRICE:
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_ITEM_ARTICLE:
        case COL_ITEM_NAME:
        case COL_TA_SERVICE_FROM:
        case COL_TA_SERVICE_TO:
        case COL_TRADE_ITEM_QUANTITY:
        case COL_UNIT:
        case COL_TRADE_TIME_UNIT:
        case COL_TA_RETURNED_QTY:
        case COL_TRADE_WEIGHT:
        case COL_TA_SERVICE_TARIFF:
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_TRADE_ITEM_PRICE:
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case COL_TRADE_DISCOUNT:
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
         // visibleItemsCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA, column, true);

          visibleItemsCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);

          visibleServiceCols.put(FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SUGGESTION + VAR_PRINT_RENTAL, column, true);
          visibleServiceCols.put(FORM_PRINT_TA_SALE_PROFORMA + VAR_PRINT_RENTAL, column, true);
          break;
        case "RemainWeight":
          visibleItemsCols.put(FORM_PRINT_TA_RETURN, column, true);
          visibleItemsCols.put(FORM_PRINT_TA_RETURN  + VAR_PRINT_RENTAL, column, true);
          break;
      }
    }
  }

  private void addDataToTable(Map<String, Map<String, String>> data, String id, String cell,
                                     String value) {
    switch (cell) {
      case "RemainingQty":
        BigDecimal val = BigDecimal.valueOf(BeeUtils.toDouble(getDataValue(data, id, cell))
            + BeeUtils.toDouble(value));

        if (BeeUtils.same(FORM_PRINT_TA_RETURN,
                BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL))) {
          val = BigDecimal.valueOf(BeeUtils.toDouble(value));
        }

        addDataEntry(data, id, cell, val.toPlainString());
        break;
      case COL_ITEM_RENTAL_PRICE:
        addDataEntry(data, id, cell, BeeUtils.round(
            BeeUtils.toString(BeeUtils.toDouble(value)), 2));
        break;
      case COL_TA_RETURNED_QTY:
      case COL_TRADE_ITEM_QUANTITY:
        addDataEntry(data, id, cell, BeeUtils.round(
          BeeUtils.toString(
                BeeUtils.toDouble(getDataValue(data, id, cell)) + BeeUtils.toDouble(value)), 2));
          break;
      case "AmountTotal":
      case "Amount":
      case "AmountVat":
        addDataEntry(data, id, cell, BeeUtils.round(
            BeeUtils.toString(BeeUtils.toDouble(value)), 2));
        break;
      default:
        addDataEntry(data, id, cell, value);
    }
  }

  private static void addDataEntry(Map<String, Map<String, String>> data, String row, String
      col, String value) {
    Map<String, String> rowData = data.computeIfAbsent(row, k -> new LinkedHashMap<>());

    rowData.put(col, value);
  }

  private static String getDataValue(Map<String, Map<String, String>> data, String row,
                                     String col) {
    String result = null;
    Map<String, String> rowData = data.get(row);

    if (rowData == null) {
      return result;
    }

    result = rowData.get(col);
    return result;
  }

  private boolean isDataContainsColumn(Map<String, Map<String, String>> data,
                                              String column) {
    switch (column) {
      case COL_TRADE_ITEM_QUANTITY:
      case "RemainingQty":
        if (FORM_PRINT_TA_RETURN.equals(getFormView().getFormName()) ||
                (FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL).equals(getFormView().getFormName())
                || FORM_PRINT_TA_RETURN_EXTRA.equals(getFormView().getFormName())
                || (FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL).equals(getFormView().getFormName())) {
          return true;
        }
        break;
    }

    boolean result = false;

    for (String row : data.keySet()) {
      result |= data.get(row) != null && data.get(row).containsKey(column);
    }

    return result;
  }

  private boolean isDataRowVisible(String widgetName, FormView form, Map<String, Map<String, String>> data, String id) {
    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, getActiveRow());
    String formName = getFormView().getFormName();

    switch (widgetName) {
      case ITEMS_WIDGET_NAME:
        if (TradeActKind.RENT_PROJECT.equals(kind)
                && BeeUtils.same(FORM_PRINT_TA_NO_STOCK, BeeUtils.removeSuffix(formName, VAR_PRINT_RENTAL))) {
          return BeeUtils.unbox(BeeUtils.toDoubleOrNull(getDataValue(data, id, "RemainingQty"))) > 0.0;
        }

       break;
      case SERVICES_WIDGET_NAME:
        break;
    }

    return true;
  }

  private boolean isVisibleColumn(String widgetName, String col) {

    String formName = getFormView().getFormName();
    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, getActiveRow());

    switch (widgetName) {
      case ITEMS_WIDGET_NAME:
        if (TradeActKind.RENT_PROJECT.equals(kind)
                && BeeUtils.same(col, "RemainingQty")
                && BeeUtils.same(FORM_PRINT_TA_NO_STOCK, BeeUtils.removeSuffix(formName, VAR_PRINT_RENTAL))) {
          return true;
        }

        return BeeUtils.isTrue(visibleItemsCols.get(formName, col));
      case SERVICES_WIDGET_NAME:
        return BeeUtils.isTrue(visibleServiceCols.get(formName, col));
        default:
          return false;
    }
  }

  private String [] getColumnList(String formName) {
    ArrayList<String> cols = Lists.newArrayList(COLUMN_LIST);
    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, getActiveRow());

    switch (formName) {
      case FORM_PRINT_TA_RETURN:
      case FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL:
        cols.remove(COL_TRADE_WEIGHT);
        cols.add(cols.indexOf(COL_TRADE_TIME_UNIT), COL_TRADE_WEIGHT);
        cols.add(cols.indexOf("RemainWeight"), COL_UNIT);
        return cols.toArray(new String[cols.size()]);
      case FORM_PRINT_TA_RETURN_EXTRA:
      case FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL:
        return COLUMN_LIST;
      case FORM_PRINT_TA_NO_STOCK:
      case FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL:
        if (TradeActKind.RENT_PROJECT.equals(kind)){
          if (cols.indexOf("Quantity") > 0) {
            cols.remove("RemainingQty");
            cols.add(cols.indexOf("Quantity"), "RemainingQty");
          } else {
            cols.add("RemainingQty");
          }
          cols.remove("Quantity");
          cols.remove(COL_TA_RETURNED_QTY);
          return cols.toArray(new String[cols.size()]);
        }
        break;
        default:
    }

    return COLUMN_LIST;
  }

  private String getTableHeader(FormView form, String widgetName, String column) {
    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, form.getActiveRow());
    switch (column) {
      case "Name":
        if (SERVICES_WIDGET_NAME.equals(widgetName)) {
          return "Teikiamos paslaugos/prekės";
        }
        break;
      case "AmountVat":
        if (ITEMS_WIDGET_NAME.equals(widgetName) && BeeUtils.inList(
            BeeUtils.removeSuffix(form.getFormName(), VAR_PRINT_RENTAL),
            FORM_PRINT_TA_SALE_PHYSICAL, FORM_PRINT_TA_NO_STOCK, FORM_PRINT_TA_SALE_RENT,
            FORM_PRINT_TA_SALE_ADDITION)) {
          return "Suma";
        }

        break;

      case "AmountTotal":
        if (SERVICES_WIDGET_NAME.equals(widgetName)) {
          return "Suma su PVM";
        }
        break;
      case "Quantity":
        if (BeeUtils.same(BeeUtils.removeSuffix(form.getFormName(), VAR_PRINT_RENTAL),
            FORM_PRINT_TA_RETURN)
                || BeeUtils.same(BeeUtils.removeSuffix(form.getFormName(), VAR_PRINT_RENTAL),
                FORM_PRINT_TA_RETURN_EXTRA)) {
          return "Grąžinta";
        }
        break;
      case "RemainingQty":
        if (ITEMS_WIDGET_NAME.equals(widgetName) && BeeUtils.same(
                BeeUtils.removeSuffix(form.getFormName(), VAR_PRINT_RENTAL),
                FORM_PRINT_TA_NO_STOCK) && TradeActKind.RENT_PROJECT.equals(kind)) {
          return "Kiekis";
        }
        break;
      default:
        break;
    }

    return tableHeaders.get(column);
  }

  private void renderItems(final String typeTable) {
    final Widget items = getFormView().getWidgetByName(typeTable);

    if (items == null) {
      return;
    }
    items.getElement().setInnerHTML(null);

    ParameterList args = TradeKeeper.createArgs(SVC_ITEMS_INFO);
    args.addDataItem("view_name", getViewName());
    args.addDataItem("id", getActiveRowId());
    args.addDataItem("TypeTable", typeTable);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          totConsumer.accept(0.0);
          return;
        }
        SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

        if (rs.isEmpty()) {
          totConsumer.accept(0.0);
          return;
        }
        Map<String, Map<String, String>> data = new LinkedHashMap<>();

        for (SimpleRowSet.SimpleRow row : rs) {
          String id = getDataTableId(typeTable, row);

          Long itemId = row.getLong(COL_ITEM);

          if ((BeeUtils.same(FORM_PRINT_TA_RETURN,
              BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL))
          || BeeUtils.same(FORM_PRINT_TA_RETURN_EXTRA,
                      BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL)))
              && remainQty.get(itemId) == null) {
            remainQty.put(itemId, 0D);
          }

          if (BeeUtils.same(
                  BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL),
                  FORM_PRINT_TA_RETURN_EXTRA)) {
            if (!BeeUtils.isPositive(row.getDouble(COL_TRADE_ITEM_QUANTITY))) {
              continue;
            }
          }

          for (String col : rs.getColumnNames()) {
            String value = row.getValue(col);

            if (Objects.equals(col, COL_TA_RETURNED_QTY)) {
              BigDecimal remaining = remainQty.get(itemId) != null
                  ? new BigDecimal(remainQty.get(itemId))
                  : row.getDecimal(COL_TRADE_ITEM_QUANTITY)
                  .subtract(BeeUtils.nvl(row.getDecimal(COL_TA_RETURNED_QTY), BigDecimal.ZERO));

              if (remaining.compareTo(BigDecimal.ZERO) != 0
                      || BeeUtils.same(BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL),
                      FORM_PRINT_TA_RETURN)) {
                addDataToTable(data, id, "RemainingQty", remaining.toPlainString());
              }
            }
            if (!BeeUtils.isEmpty(value)) {
              switch (col) {
                case COL_TA_SERVICE_FROM:
                case COL_TA_SERVICE_TO:
                  value = new JustDate(BeeUtils.toLong(value)).toString();
                  break;

                case COL_TRADE_TIME_UNIT:
                  value = EnumUtils.getCaption(TradeActTimeUnit.class, BeeUtils.toIntOrNull(value));
                  break;

                case COL_TRADE_ITEM_NOTE:
                  addDataToTable(data, id, COL_ITEM_NAME, BeeUtils.join("/",
                      row.getValue(COL_ITEM_NAME), row.getValue(COL_TRADE_ITEM_NOTE)));
                  break;

                case COL_TA_SERVICE_MIN:
                  String daysPerWeek = row.getValue(COL_TA_SERVICE_DAYS);
                  addDataToTable(data, id, COL_ITEM_NAME, BeeUtils.join("/",
                      row.getValue(COL_ITEM_NAME), row.getValue(COL_TRADE_ITEM_NOTE),
                      BeeUtils.joinWords("Minimalus nuomos terminas", value,
                          EnumUtils.getCaption(TradeActTimeUnit.class,
                              row.getInt(COL_TRADE_TIME_UNIT))),
                      BeeUtils.isEmpty(daysPerWeek) ? null : daysPerWeek + "d.per Sav."));
                  break;
                case COL_ITEM_WEIGHT:
                  addDataToTable(data, id, col, value);
                  addDataToTable(data, id, "RemainWeight", value);
                  break;

              }
              addDataToTable(data, id, col, value);
            }
          }
          double qty = getQuantity(data, id, itemId);
          double prc = BeeUtils.toDouble(getDataValue(data, id, COL_TRADE_ITEM_PRICE));
          double sum = qty * prc;

          double disc = BeeUtils.toDouble(getDataValue(data, id, COL_TRADE_DISCOUNT));
          double vat = BeeUtils.toDouble(getDataValue(data, id, COL_TRADE_VAT));
          boolean vatInPercents = BeeUtils.toBoolean(getDataValue(data, id, COL_TRADE_VAT_PERC));

          double dscSum = sum / 100 * disc;
          sum -= dscSum;

          if (BeeUtils.toBoolean(getDataValue(data, id, COL_TRADE_VAT_PLUS))) {
            if (vatInPercents) {
              vat = sum / 100 * vat;
            }
          } else {
            if (vatInPercents) {
              vat = sum - sum / (1 + vat / 100);
            }
            sum -= vat;
          }
          sum = BeeUtils.round(sum, 2);

          for (String col : new String[] {COL_ITEM_WEIGHT, COL_ITEM_AREA}) {
            if (data.get(id) != null && data.get(id).containsKey(col)) {
              addDataToTable(data, id, col,
                  BeeUtils.toString(BeeUtils.round(BeeUtils.toDouble(
                      getDataValue(data, id, col)) * qty, 5)));
            }
          }

          if (data.get(id) != null && data.get(id).containsKey("RemainWeight")) {
            addDataToTable(data, id, "RemainWeight",
                    BeeUtils.toString( BeeUtils.round(BeeUtils.toDouble(
                            getDataValue(data, id, "RemainWeight")) * remainQty.get(itemId), 5)
            ));
          }
          if (disc > 0) {
            addDataToTable(data, id, COL_TRADE_DISCOUNT,
                BeeUtils.removeTrailingZeros(BeeUtils.toString(disc)) + "%");
          }
          if (vat > 0) {
            addDataToTable(data, id, COL_TRADE_VAT, BeeUtils.toString(BeeUtils.round(vat, 2)));
          }

          if (BeeUtils.inList(getFormView().getFormName(), FORM_PRINT_TA_NO_STOCK,
              FORM_PRINT_TA_NO_STOCK + VAR_PRINT_RENTAL, FORM_PRINT_TA_SALE_PHYSICAL,
              FORM_PRINT_TA_SALE_PHYSICAL + VAR_PRINT_RENTAL, FORM_PRINT_TA_SALE_RENT,
              FORM_PRINT_TA_SALE_RENT + VAR_PRINT_RENTAL, FORM_PRINT_TA_SALE_ADDITION,
              FORM_PRINT_TA_SALE_ADDITION + VAR_PRINT_RENTAL, FORM_PRINT_TA_RETURN,
              FORM_PRINT_TA_RETURN + VAR_PRINT_RENTAL, FORM_PRINT_TA_RETURN_EXTRA,
                  FORM_PRINT_TA_RETURN_EXTRA + VAR_PRINT_RENTAL)
              && BeeUtils.same(typeTable, ITEMS_WIDGET_NAME)) {
            addDataToTable(data, id, COL_TRADE_ITEM_PRICE, BeeUtils.removeTrailingZeros(BeeUtils
                .toString(BeeUtils.round((sum + dscSum + vat) / (qty != 0 ? qty : 1d), 5))));
          } else {
            addDataToTable(data, id, COL_TRADE_ITEM_PRICE, BeeUtils.removeTrailingZeros(BeeUtils
                .toString(BeeUtils.round((sum + dscSum) / (qty != 0 ? qty : 1d), 5))));
          }
          addDataToTable(data, id, "Amount", BeeUtils.toString(BeeUtils.round(sum, 2)));
          addDataToTable(data, id, "AmountVat", BeeUtils.toString(
              BeeUtils.round(sum + BeeUtils.max(0D, vat), 2)
          ));

          if (BeeUtils.same(typeTable, SERVICES_WIDGET_NAME)) {
            if (BeeUtils.isEmpty(getDataValue(data, id, COL_TIME_UNIT))) {
              addDataToTable(data, id, "MinTermAmount",
                  BeeUtils.toString(BeeUtils.round(sum + vat, 2)));
            } else if (BeeUtils.isDouble(BeeUtils.toDouble(
                getDataValue(data, id, COL_TA_SERVICE_MIN)))) {
              double mint = BeeUtils.toDouble(getDataValue(data, id, COL_TA_SERVICE_MIN));
              addDataToTable(data, id, "MinTermAmount",
                  BeeUtils.toString(BeeUtils.round(mint * (sum + vat), 2)));
            }
          }

          addDataToTable(data, id, "AmountTotal", BeeUtils.toString(BeeUtils.round(sum + vat, 2)));

        }
        HtmlTable table = new HtmlTable(TradeUtils.STYLE_ITEMS_TABLE);
        int c = 0;

        Set<String> calc = new HashSet<>();
        calc.add(COL_TRADE_ITEM_QUANTITY);
        calc.add(COL_TA_RETURNED_QTY);
        calc.add("RemainingQty");
        calc.add(COL_TRADE_WEIGHT);
        calc.add("RemainWeight");
        calc.add(COL_ITEM_AREA);
//        calc.add(COL_ITEM_RENTAL_PRICE);
        calc.add("Amount");
        calc.add("AmountVat");
        calc.add(COL_TRADE_VAT);
        calc.add("AmountTotal");
        calc.add("MinTermAmount");

        for (String col : getColumnList(getFormView().getFormName())) {

          if (!isDataContainsColumn(data, col)) {
            continue;
          }

          if (isVisibleColumn(typeTable, col)) {
            table.setText(0, c, getTableHeader(getFormView(), typeTable, col),
                TradeUtils.STYLE_ITEMS + col);
          }
          int r = 1;
          BigDecimal sum = BigDecimal.ZERO;

          for (String id : data.keySet()) {
            if (!isDataRowVisible(typeTable, getFormView(), data, id)) {
              continue;
            }
            String value = getDataValue(data, id, col);

            if (calc.contains(col)) {
              sum = sum.add(BeeUtils.nvl(BeeUtils.toDecimalOrNull(value), BigDecimal.ZERO));
              value = BeeUtils.removeTrailingZeros(value);
            }

            if (isVisibleColumn(typeTable, col)) {
              table.setText(r++, c, value, TradeUtils.STYLE_ITEMS + col);
            }
          }
          String value = null;

          if (sum.compareTo(BigDecimal.ZERO) != 0) {
            value = BeeUtils.removeTrailingZeros(sum.toPlainString());
          }
          if ("AmountTotal".equals(col) && BeeUtils.same(typeTable, ITEMS_WIDGET_NAME)) {
            totConsumer.accept(sum.doubleValue());
          } else if ("MinTermAmount".equals(col)
              && BeeUtils.same(typeTable, SERVICES_WIDGET_NAME)) {
            totConsumer.accept(sum.doubleValue());
          }

          if (isVisibleColumn(typeTable, col)) {
            table.setText(r, c, value, TradeUtils.STYLE_ITEMS + col);
          }
          c++;
        }
        table.setText(table.getRowCount() - 1, 0, Localized.dictionary().totalOf());

        for (int i = 0; i < table.getRowCount(); i++) {
          table.getRowFormatter().addStyleName(i, i == 0 ? TradeUtils.STYLE_ITEMS_HEADER
              : (i < (table.getRowCount() - 1)
                  ? TradeUtils.STYLE_ITEMS_DATA : TradeUtils.STYLE_ITEMS_FOOTER));
        }
        items.getElement().setInnerHTML(table.getElement().getString());
      }
    });
  }

  private String getDataTableId(String typeTable, SimpleRowSet.SimpleRow row) {
    switch (BeeUtils.removeSuffix(getFormView().getFormName(), VAR_PRINT_RENTAL)) {
      default:
//      case FORM_PRINT_TA_NO_STOCK:
//      case FORM_PRINT_TA_RETURN:
//      case FORM_PRINT_TA_RETURN_EXTRA:
        return BeeUtils.same(SERVICES_WIDGET_NAME, typeTable) ? row.getValue(typeTable)
            : BeeUtils.toString(Objects.hash(row.getLong(COL_ITEM),
            row.getDouble(COL_TRADE_ITEM_PRICE),
            row.getBoolean(COL_TRADE_VAT_PLUS),
            row.getDouble(COL_TRADE_VAT), row.getBoolean(COL_TRADE_VAT_PERC),
            row.getDouble(COL_TRADE_DISCOUNT), row.getValue(COL_TRADE_ITEM_NOTE)));

//      default: return  row.getValue(typeTable);
    }
  }

  private double getQuantity(Map<String, Map<String, String>> data, String id, Long itemId) {
    return (BeeUtils.same(FORM_PRINT_TA_RETURN, BeeUtils.removeSuffix(getFormView()
      .getFormName(), VAR_PRINT_RENTAL))
    || BeeUtils.same(FORM_PRINT_TA_RETURN_EXTRA, BeeUtils.removeSuffix(getFormView()
            .getFormName(), VAR_PRINT_RENTAL)))
      ? BeeUtils.toDouble(getDataValue(data, id, COL_TRADE_ITEM_QUANTITY))
      : BeeUtils.nvl(remainQty.get(itemId), BeeUtils.toDouble(getDataValue(data, id,
      COL_TRADE_ITEM_QUANTITY)) - BeeUtils.toDouble(getDataValue(data, id,
      COL_TA_RETURNED_QTY)));
  }

  private void renderTotalOf(double tot) {

    for (Widget total : totals) {
      total.getElement().setInnerText("");
      TradeUtils.getTotalInWords(BeeUtils.round(tot, 2),
          getFormView().getLongValue(COL_TRADE_CURRENCY), total);
    }

    for (Widget total : totalsOf) {
      total.getElement().setInnerText("Iš viso: " + BeeUtils.round(tot, 2));
    }
  }

}
