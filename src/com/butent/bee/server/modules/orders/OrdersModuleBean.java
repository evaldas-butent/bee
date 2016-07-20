package com.butent.bee.server.modules.orders;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.concurrency.ConcurrencyBean.HasTimerService;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.webservice.ButentWS;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Stateless
@LocalBean
public class OrdersModuleBean implements BeeModule, HasTimerService {

  private static BeeLogger logger = LogUtils.getLogger(OrdersModuleBean.class);
  Map<Long, Double> itemsRemainders;

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;
  @EJB
  ConcurrencyBean cb;

  @Resource
  TimerService timerService;

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);

    switch (svc) {
      case SVC_GET_ITEMS_FOR_SELECTION:
        response = getItemsForSelection(reqInfo);
        break;

      case SVC_GET_TMPL_ITEMS_FOR_SELECTION:
        response = getTmplItemsForSelection(reqInfo);
        break;

      case SVC_GET_TEMPLATE_ITEMS:
        response = getTemplateItems(reqInfo);
        break;

      case OrdersConstants.SVC_CREATE_INVOICE_ITEMS:
        response = createInvoiceItems(reqInfo);
        break;

      case SVC_GET_NEXT_NUMBER:
        response = getNextNumber(reqInfo);
        break;

      case SVC_FILL_RESERVED_REMAINDERS:
        response = fillReservedRemainders(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public void ejbTimeout(Timer timer) {
    if (cb.isParameterTimer(timer, PRM_CLEAR_RESERVATIONS_TIME)) {
      clearReservations();
    }
    if (cb.isParameterTimer(timer, PRM_IMPORT_ERP_ITEMS_TIME)) {
      getERPItems();
    }
    if (cb.isParameterTimer(timer, PRM_IMPORT_ERP_STOCKS_TIME)) {
      getERPStocks();
    }
    if (cb.isParameterTimer(timer, PRM_EXPORT_ERP_RESERVATIONS_TIME)) {
      exportReservations();
    }
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createNumber(module, PRM_CLEAR_RESERVATIONS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_ITEMS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_STOCKS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_EXPORT_ERP_RESERVATIONS_TIME, false, null),
        BeeParameter.createRelation(module, PRM_DEFAULT_SALE_OPERATION, false,
            VIEW_TRADE_OPERATIONS, COL_OPERATION_NAME));

    return params;
  }

  @Override
  public Module getModule() {
    return Module.ORDERS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  @Override
  public void init() {
    cb.createIntervalTimer(this.getClass(), PRM_CLEAR_RESERVATIONS_TIME);
    cb.createIntervalTimer(this.getClass(), PRM_IMPORT_ERP_ITEMS_TIME);
    cb.createIntervalTimer(this.getClass(), PRM_IMPORT_ERP_STOCKS_TIME);
    cb.createIntervalTimer(this.getClass(), PRM_EXPORT_ERP_RESERVATIONS_TIME);

    sys.registerDataEventHandler(new DataEventHandler() {

      @Subscribe
      @AllowConcurrentEvents
      public void setFreeRemainder(ViewQueryEvent event) {
        if (event.isAfter(VIEW_ORDER_ITEMS) && event.hasData()
            && event.getColumnCount() >= sys.getView(event.getTargetName()).getColumnCount()) {

          BeeRowSet rowSet = event.getRowset();

          if (BeeUtils.isPositive(rowSet.getNumberOfRows())) {
            List<Long> itemIds = DataUtils.getDistinct(rowSet, COL_ITEM);
            int itemIndex = rowSet.getColumnIndex(COL_ITEM);
            int ordIndex = rowSet.getColumnIndex(COL_ORDER);
            Long order = rowSet.getRow(0).getLong(ordIndex);

            Map<Long, Double> freeRemainders = totReservedRemainders(itemIds, order, null);

            for (BeeRow row : rowSet) {
              row.setProperty(PRP_FREE_REMAINDER, BeeUtils.toString(freeRemainders.get(row
                  .getLong(itemIndex))));
            }
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillOrderNumber(ViewInsertEvent event) {
        if (event.isBefore(TBL_ORDERS) && !DataUtils.contains(event.getColumns(), COL_TA_NUMBER)) {
          BeeView view = sys.getView(VIEW_ORDERS);
          Long series = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_TA_SERIES:
                series = event.getRow().getLong(i);
                break;
            }
          }

          if (DataUtils.isId(series)) {
            BeeColumn column = view.getBeeColumn(COL_TA_NUMBER);
            String number = getNextNumber(series, column.getPrecision(), COL_TA_NUMBER);

            if (!BeeUtils.isEmpty(number)) {
              event.addValue(column, new TextValue(number));
            }
          }
        }
      }
    });
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {

    Long orderId = reqInfo.getParameterLong(COL_ORDER);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    Long warehouse = reqInfo.getParameterLong(ClassifierConstants.COL_WAREHOUSE);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

    Set<Long> orderItems = getOrderItems(orderId, TBL_ORDER_ITEMS, COL_ORDER);
    if (!orderItems.isEmpty()) {
      filter.add(Filter.idNotIn(orderItems));
    }

    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    if (warehouse != null) {
      filter.add(Filter.in(sys.getIdName(TBL_ITEMS), VIEW_ITEM_REMAINDERS, COL_ITEM, Filter.equals(
          ClassifierConstants.COL_WAREHOUSE, warehouse)));
    }

    BeeRowSet items = qs.getViewData(VIEW_ITEMS, filter);

    if (DataUtils.isEmpty(items)) {
      logger.debug(reqInfo.getService(), "no items found", filter);
      return ResponseObject.emptyResponse();
    }

    Map<Long, Double> totRemainders = totReservedRemainders(items.getRowIds(), null, warehouse);

    SqlSelect query =
        new SqlSelect()
            .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
            .addFrom(TBL_WAREHOUSES)
            .setWhere(
                SqlUtils.equals(TBL_WAREHOUSES, sys.getIdName(TBL_WAREHOUSES), warehouse));

    String code = qs.getValue(query);

    BeeView remView = sys.getView(VIEW_ITEM_REMAINDERS);
    items.addColumn(remView.getBeeColumn(ALS_WAREHOUSE_CODE));
    items.addColumn(remView.getBeeColumn(COL_WAREHOUSE_REMAINDER));
    items.addColumn(ValueType.NUMBER, PRP_FREE_REMAINDER);
    items.addColumn(ValueType.NUMBER, COL_RESERVED_REMAINDER);

    for (BeeRow row : items) {
      Long itemId = row.getId();

      Double res = totRemainders.get(itemId);
      row.setValue(row.getNumberOfCells() - 4, code);
      row.setValue(row.getNumberOfCells() - 3, itemsRemainders.get(itemId));
      row.setValue(row.getNumberOfCells() - 2, res);
      row.setValue(row.getNumberOfCells() - 1, itemsRemainders.get(itemId) - res);
    }

    return ResponseObject.response(items);
  }

  private ResponseObject getTmplItemsForSelection(RequestInfo reqInfo) {

    Long templateId = reqInfo.getParameterLong(COL_ORDER);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

    Set<Long> orderItems = getOrderItems(templateId, VIEW_ORDER_TMPL_ITEMS, COL_TEMPLATE);
    if (!orderItems.isEmpty()) {
      filter.add(Filter.idNotIn(orderItems));
    }

    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    BeeRowSet items = qs.getViewData(VIEW_ITEMS, filter);

    if (DataUtils.isEmpty(items)) {
      logger.debug(reqInfo.getService(), "no items found", filter);
      return ResponseObject.emptyResponse();
    }

    return ResponseObject.response(items);
  }

  private ResponseObject createInvoiceItems(RequestInfo reqInfo) {
    Long saleId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SALE));
    Long currency = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CURRENCY));
    Map<String, String> map = Codec.deserializeMap(reqInfo.getParameter(Service.VAR_DATA));
    Map<Long, Double> idsQty = new HashMap<>();

    for (Entry<String, String> entry : map.entrySet()) {
      idsQty.put(Long.valueOf(entry.getKey()), Double.valueOf(entry.getValue()));
    }

    if (!DataUtils.isId(saleId)) {
      return ResponseObject.error("Wrong account ID");
    }
    if (!DataUtils.isId(currency)) {
      return ResponseObject.error("Wrong currency ID");
    }
    if (BeeUtils.isEmpty(idsQty)) {
      return ResponseObject.error("Empty ID list");
    }

    IsCondition where = sys.idInList(TBL_ORDER_ITEMS, idsQty.keySet());

    SqlSelect query = new SqlSelect();
    query.addFields(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS), COL_ORDER, COL_TRADE_VAT_PLUS,
        TradeConstants.COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_INCOME_ITEM, COL_RESERVED_REMAINDER,
        COL_TRADE_DISCOUNT, COL_TRADE_ITEM_QUANTITY)
        .addFields(TBL_ITEMS, COL_ITEM_ARTICLE)
        .addFrom(TBL_ORDER_ITEMS)
        .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
        .setWhere(where);

    IsExpression vatExch =
        ExchangeUtils.exchangeFieldTo(query, TBL_ORDER_ITEMS, COL_TRADE_VAT,
            COL_TRADE_CURRENCY, COL_INCOME_DATE, currency);

    String vatAlias = "Vat_" + SqlUtils.uniqueName();

    String priceAlias = "Price_" + SqlUtils.uniqueName();
    IsExpression priceExch =
        ExchangeUtils.exchangeFieldTo(query, TBL_ORDER_ITEMS,
            COL_TRADE_ITEM_PRICE, COL_TRADE_CURRENCY,
            COL_INCOME_DATE, currency);

    query.addExpr(priceExch, priceAlias)
        .addExpr(vatExch, vatAlias)
        .addOrder(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.error(TBL_ORDER_ITEMS, idsQty, "not found");
    }

    Map<Long, Double> freeRemainders =
        getFreeRemainders(Arrays.asList(data.getLongColumn(COL_ITEM)), data.getRow(0).getLong(
            COL_ORDER), null);
    Map<Long, Double> compInvoices = getCompletedInvoices(data.getRow(0).getLong(
        COL_ORDER));

    ResponseObject response = new ResponseObject();

    for (SimpleRow row : data) {
      Long item = row.getLong(COL_INCOME_ITEM);
      String article = row.getValue(COL_ITEM_ARTICLE);

      SqlInsert insert = new SqlInsert(TBL_SALE_ITEMS)
          .addConstant(COL_SALE, saleId)
          .addConstant(COL_ITEM_ARTICLE, article)
          .addConstant(COL_ITEM, item);

      Boolean vatPerc = row.getBoolean(COL_TRADE_VAT_PERC);
      Double vat;
      if (BeeUtils.isTrue(vatPerc)) {
        insert.addConstant(COL_TRADE_VAT_PERC, vatPerc);
        vat = row.getDouble(COL_TRADE_VAT);
      } else {
        vat = row.getDouble(vatAlias);
      }

      if (BeeUtils.nonZero(vat)) {
        insert.addConstant(COL_TRADE_VAT, vat);
      }

      Boolean vatPlus = row.getBoolean(COL_TRADE_VAT_PLUS);

      if (BeeUtils.isTrue(vatPlus)) {
        insert.addConstant(COL_TRADE_VAT_PLUS, vatPlus);
      }

      double saleQuantity = BeeUtils.unbox(idsQty.get(row.getLong(sys.getIdName(TBL_ORDER_ITEMS))));
      double price = BeeUtils.unbox(row.getDouble(priceAlias));
      double discount = BeeUtils.unbox(row.getDouble(COL_TRADE_DISCOUNT));
      if (discount > 0) {
        insert.addConstant(COL_TRADE_DISCOUNT, discount);
      }

      insert.addConstant(COL_TRADE_ITEM_QUANTITY, saleQuantity);

      if (price > 0) {
        insert.addConstant(COL_TRADE_ITEM_PRICE, price);
      }

      ResponseObject insResponse = qs.insertDataWithResponse(insert);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
        break;
      } else {
        double quantity = BeeUtils.unbox(row.getDouble(COL_TRADE_ITEM_QUANTITY));
        double invoiceQty =
            BeeUtils.unbox(compInvoices.get(row.getLong(sys.getIdName(TBL_ORDER_ITEMS))));
        double resRemainder = BeeUtils.unbox(row.getDouble(COL_RESERVED_REMAINDER));
        double freeRemainder = BeeUtils.unbox(freeRemainders.get(row.getLong(COL_ITEM)));
        double value;

        if (quantity == invoiceQty + saleQuantity) {
          value = 0;
        } else if (quantity - invoiceQty - saleQuantity <= freeRemainder + resRemainder
            - saleQuantity) {
          value = quantity - invoiceQty - saleQuantity;
        } else {
          value = freeRemainder + resRemainder - saleQuantity;
        }

        SqlUpdate update = new SqlUpdate(TBL_ORDER_ITEMS)
            .addConstant(COL_RESERVED_REMAINDER, value)
            .setWhere(sys.idEquals(TBL_ORDER_ITEMS, row.getLong(sys.getIdName(TBL_ORDER_ITEMS))));

        qs.updateData(update);
      }
    }

    return response;
  }

  private void clearReservations() {

    Double hours = prm.getDouble(PRM_CLEAR_RESERVATIONS_TIME);

    SqlSelect select =
        new SqlSelect()
            .addFields(TBL_ORDERS, COL_DATES_START_DATE, sys.getIdName(TBL_ORDERS))
            .addFrom(TBL_ORDERS)
            .addFromLeft(TBL_ORDER_ITEMS,
                sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
            .setWhere(
                SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED
                    .ordinal()), SqlUtils.positive(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)));

    SimpleRowSet rowSet = qs.getData(select);

    for (SimpleRow row : rowSet) {
      DateTime orderTime = row.getDateTime(COL_DATES_START_DATE);

      if (TimeUtils.nowMillis().getTime() > orderTime.getTime() + hours
          * TimeUtils.MILLIS_PER_HOUR) {

        SqlUpdate update =
            new SqlUpdate(TBL_ORDER_ITEMS)
                .addConstant(COL_RESERVED_REMAINDER, null)
                .setWhere(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, row.getLong(sys
                    .getIdName(TBL_ORDERS))));

        qs.updateData(update);
      }
    }
  }

  private void exportReservations() {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);

    SqlSelect select =
        new SqlSelect()
            .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
            .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
            .addSum(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER, ALS_TOTAL_AMOUNT)
            .addFrom(TBL_ORDER_ITEMS)
            .addFromLeft(TBL_ITEMS,
                sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
            .addFromLeft(TBL_ORDERS,
                sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
            .addFromLeft(TBL_WAREHOUSES,
                sys.joinTables(TBL_WAREHOUSES, TBL_ORDERS, COL_WAREHOUSE))
            .addGroup(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
            .addGroup(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
            .setWhere(
                SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED
                    .ordinal()), SqlUtils.positive(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)));

    SimpleRowSet rs = qs.getData(select);

    if (rs.getNumberOfRows() > 0) {
      for (SimpleRow sr : rs) {
        try {
          ButentWS.connect(remoteAddress, remoteLogin, remotePassword).importItemReservation(
              sr.getValue(COL_WAREHOUSE_CODE), sr.getLong(COL_ITEM_EXTERNAL_CODE),
              sr.getDouble(ALS_TOTAL_AMOUNT));

        } catch (BeeException e) {
          logger.error(e);
          sys.eventEnd(sys.eventStart(PRM_EXPORT_ERP_RESERVATIONS_TIME), "ERROR", e.getMessage());
          continue;
        }
      }
    }
  }

  private void getERPItems() {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs = null;

    try {
      rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getGoods("e");

    } catch (BeeException e) {
      logger.error(e);
      sys.eventEnd(sys.eventStart(PRM_IMPORT_ERP_ITEMS_TIME), "ERROR", e.getMessage());
      return;
    }

    if (rs.getNumberOfColumns() > 0) {

      List<String> externalCodes = new ArrayList<>();

      externalCodes.addAll(Arrays.asList(qs.getColumn(new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
          .addFrom(TBL_ITEMS))));

      Map<String, Long> currencies = new HashMap<>();

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(TBL_CURRENCIES, COL_CURRENCY_NAME)
          .addField(TBL_CURRENCIES, sys.getIdName(TBL_CURRENCIES), COL_CURRENCY)
          .addFrom(TBL_CURRENCIES))) {

        currencies.put(row.getValue(COL_CURRENCY_NAME), row.getLong(COL_CURRENCY));
      }

      Map<String, Long> typesGroups = new HashMap<>();

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(TBL_ITEM_CATEGORY_TREE, COL_CATEGORY_NAME)
          .addField(TBL_ITEM_CATEGORY_TREE, sys.getIdName(TBL_ITEM_CATEGORY_TREE), COL_CATEGORY)
          .addFrom(TBL_ITEM_CATEGORY_TREE))) {

        typesGroups.put(row.getValue(COL_CATEGORY_NAME), row.getLong(COL_CATEGORY));
      }

      List<String> articles = new ArrayList<>();
      articles.addAll(Arrays.asList(qs.getColumn(new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_ARTICLE)
          .addFrom(TBL_ITEMS))));

      Map<String, Long> units = new HashMap<>();

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(TBL_UNITS, COL_UNIT_NAME)
          .addField(TBL_UNITS, sys.getIdName(TBL_UNITS), COL_UNIT)
          .addFrom(TBL_UNITS))) {

        units.put(row.getValue(COL_UNIT_NAME), row.getLong(COL_UNIT));
      }

      for (SimpleRow row : rs) {

        String type = row.getValue("TIPAS");
        String group = row.getValue("GRUPE");
        String article = row.getValue("ARTIKULAS");
        String unit = row.getValue("MATO_VIEN");
        String exCode = row.getValue("PREKE");

        Map<String, String> currenciesMap = new HashMap<>();
        currenciesMap.put("PARD_VAL", row.getValue("PARD_VAL"));
        currenciesMap.put("SAV_VAL", row.getValue("SAV_VAL"));
        currenciesMap.put("VAL_1", row.getValue("VAL_1"));
        currenciesMap.put("VAL_2", row.getValue("VAL_2"));
        currenciesMap.put("VAL_3", row.getValue("VAL_3"));

        if (!articles.contains(article) && !externalCodes.contains(exCode)) {

          if (!typesGroups.containsKey(type)) {
            typesGroups.put(type, qs.insertData(new
                SqlInsert(TBL_ITEM_CATEGORY_TREE).addConstant(COL_CATEGORY_NAME, type)));
          }

          if (!typesGroups.containsKey(group)) {
            typesGroups.put(group, qs.insertData(new
                SqlInsert(TBL_ITEM_CATEGORY_TREE).addConstant(COL_CATEGORY_NAME, group)));
          }

          if (!units.containsKey(unit)) {
            units.put(unit, qs.insertData(new SqlInsert(TBL_UNITS)
                .addConstant(COL_UNIT_NAME, unit)));
          }

          for (String value : currenciesMap.values()) {
            if (!currencies.containsKey(value) && !BeeUtils.isEmpty(value)) {
              currencies.put(value, qs.insertData(new SqlInsert(TBL_CURRENCIES)
                  .addConstant(COL_CURRENCY_NAME, value)));
            }
          }

          ResponseObject response = qs.insertDataWithResponse(new SqlInsert(TBL_ITEMS)
              .addConstant(COL_ITEM_NAME, row.getValue("PAVAD"))
              .addConstant(COL_ITEM_EXTERNAL_CODE, exCode)
              .addConstant(COL_UNIT, units.get(unit))
              .addNotEmpty(COL_ITEM_ARTICLE, article)
              .addConstant(COL_ITEM_PRICE, row.getDouble("PARD_KAINA"))
              .addConstant(COL_ITEM_COST, row.getDouble("SAVIKAINA"))
              .addConstant(COL_ITEM_PRICE_1, row.getDouble("KAINA_1"))
              .addConstant(COL_ITEM_PRICE_2, row.getDouble("KAINA_2"))
              .addConstant(COL_ITEM_PRICE_3, row.getDouble("KAINA_3"))
              .addConstant(COL_ITEM_GROUP, typesGroups.get(group))
              .addConstant(COL_ITEM_TYPE, typesGroups.get(type))
              .addConstant(COL_ITEM_CURRENCY, currencies.get(currenciesMap.get("PARD_VAL")))
              .addConstant(COL_ITEM_COST_CURRENCY, currencies.get(currenciesMap.get("SAV_VAL")))
              .addConstant(COL_ITEM_CURRENCY_1, currencies.get(currenciesMap.get("VAL_1")))
              .addConstant(COL_ITEM_CURRENCY_2, currencies.get(currenciesMap.get("VAL_2")))
              .addConstant(COL_ITEM_CURRENCY_3, currencies.get(currenciesMap.get("VAL_3"))));

          if (!response.hasErrors()) {
            externalCodes.add(exCode);
            articles.add(article);
          }
        }
      }
    }
  }

  private void getERPStocks() {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs = null;

    try {
      rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks();

    } catch (BeeException e) {
      logger.error(e);
      sys.eventEnd(sys.eventStart(PRM_IMPORT_ERP_STOCKS_TIME), "ERROR", e.getMessage());
      return;
    }

    if (rs.getNumberOfRows() > 0) {
      Map<String, Long> externalCodes = new HashMap<>();

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
          .addField(TBL_ITEMS, sys.getIdName(TBL_ITEMS), COL_ITEM)
          .addFrom(TBL_ITEMS))) {

        externalCodes.put(row.getValue(COL_ITEM_EXTERNAL_CODE), row.getLong(COL_ITEM));
      }

      Map<String, Long> warehouses = new HashMap<>();

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
          .addField(TBL_WAREHOUSES, sys.getIdName(TBL_WAREHOUSES), COL_WAREHOUSE)
          .addFrom(TBL_WAREHOUSES))) {

        warehouses.put(row.getValue(COL_WAREHOUSE_CODE), row.getLong(COL_WAREHOUSE));
      }

      for (SimpleRow row : rs) {
        String exCode = row.getValue("PREKE");
        String warehouse = row.getValue("SANDELIS");
        String stock = row.getValue("LIKUTIS");

        if (externalCodes.containsKey(exCode) && warehouses.containsKey(warehouse)) {
          SqlUpdate update =
              new SqlUpdate(VIEW_ITEM_REMAINDERS)
                  .addConstant(COL_WAREHOUSE_REMAINDER, stock)
                  .setWhere(
                      SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_ITEM, externalCodes.get(exCode),
                          COL_WAREHOUSE, warehouses.get(warehouse)));

          int updatedRows = qs.updateData(update);

          if (updatedRows == 0) {
            SqlInsert insert = new SqlInsert(VIEW_ITEM_REMAINDERS)
                .addConstant(COL_ITEM, externalCodes.get(exCode))
                .addConstant(COL_WAREHOUSE, warehouses.get(warehouse))
                .addConstant(COL_WAREHOUSE_REMAINDER, stock);

            qs.insertData(insert);
          }
        }
      }
    }
  }

  private Set<Long> getOrderItems(Long targetId, String source, String column) {
    if (DataUtils.isId(targetId)) {
      return qs.getLongSet(new SqlSelect()
          .addFields(source, COL_ITEM)
          .addFrom(source)
          .setWhere(SqlUtils.equals(source, column, targetId)));
    } else {
      return BeeConst.EMPTY_IMMUTABLE_LONG_SET;
    }
  }

  private ResponseObject getNextNumber(RequestInfo reqInfo) {
    Long seriesId = reqInfo.getParameterLong(COL_SERIES);
    String columnName = reqInfo.getParameter(Service.VAR_COLUMN);
    String viewName = reqInfo.getParameter(VAR_VIEW_NAME);

    if (!DataUtils.isId(seriesId) || BeeUtils.isEmpty(columnName) || BeeUtils.isEmpty(viewName)) {
      logger.warning("Missing one of parameter (seriesId, columnName, viewname)", seriesId,
          columnName, viewName);
      return ResponseObject.emptyResponse();
    }

    DataInfo viewData = sys.getDataInfo(viewName);

    if (viewData == null) {
      return ResponseObject.emptyResponse();
    }

    BeeColumn col = viewData.getColumn(columnName);

    if (col == null) {
      return ResponseObject.emptyResponse();
    }

    return ResponseObject.response(getNextNumber(seriesId, col.getPrecision(), columnName));
  }

  private String getNextNumber(long series, int maxLength, String column) {
    IsCondition where = SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_SERIES, series),
        SqlUtils.notNull(TBL_ORDERS, column));

    SqlSelect query = new SqlSelect()
        .addFields(TBL_ORDERS, column)
        .addFrom(TBL_ORDERS)
        .setWhere(where);

    String[] values = qs.getColumn(query);

    long max = 0;
    BigInteger bigMax = null;

    if (!ArrayUtils.isEmpty(values)) {
      for (String value : values) {
        if (BeeUtils.isDigit(value)) {
          if (BeeUtils.isLong(value)) {
            max = Math.max(max, BeeUtils.toLong(value));

          } else {
            BigInteger big = new BigInteger(value);

            if (bigMax == null || BeeUtils.isLess(bigMax, big)) {
              bigMax = big;
            }
          }
        }
      }
    }

    BigInteger big = new BigInteger(BeeUtils.toString(max));
    if (bigMax != null) {
      big = big.max(bigMax);
    }

    String number = big.add(BigInteger.ONE).toString();

    Integer length = prm.getInteger(PRM_TA_NUMBER_LENGTH);
    if (BeeUtils.isPositive(length) && length > number.length()) {
      number = BeeUtils.padLeft(number, length, BeeConst.CHAR_ZERO);
    }

    if (maxLength > 0 && number.length() > maxLength) {
      number = number.substring(number.length() - maxLength);
    }

    return number;
  }

  private ResponseObject getTemplateItems(RequestInfo reqInfo) {
    Long templateId = reqInfo.getParameterLong(COL_TEMPLATE);
    if (!DataUtils.isId(templateId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TEMPLATE);
    }

    Long orderId = reqInfo.getParameterLong(COL_ORDER);

    List<BeeRowSet> result = new ArrayList<>();

    Set<Long> itemIds = new HashSet<>();

    Set<Long> ordItems = getOrderItems(orderId, TBL_ORDER_ITEMS, COL_ORDER);
    Filter filter = getTemplateChildrenFilter(templateId, ordItems);

    BeeRowSet templateItems = qs.getViewData(VIEW_ORDER_TMPL_ITEMS, filter);
    if (!DataUtils.isEmpty(templateItems)) {
      result.add(templateItems);

      int index = templateItems.getColumnIndex(COL_ITEM);
      itemIds.addAll(templateItems.getDistinctLongs(index));
    }

    if (!itemIds.isEmpty()) {
      BeeRowSet items = qs.getViewData(VIEW_ITEMS, Filter.idIn(itemIds));
      if (!DataUtils.isEmpty(items)) {
        result.add(items);
      }
    }

    if (result.isEmpty()) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result).setSize(result.size());
    }
  }

  private static Filter getTemplateChildrenFilter(Long templateId, Collection<Long> excludeItems) {
    if (BeeUtils.isEmpty(excludeItems)) {
      return Filter.equals(COL_TEMPLATE, templateId);
    } else {
      return Filter.and(Filter.equals(COL_TEMPLATE, templateId),
          Filter.exclude(COL_ITEM, excludeItems));
    }
  }

  private Map<Long, Double> totReservedRemainders(List<Long> itemIds, Long order, Long whId) {
    Long warehouseId;
    if (whId == null) {
      SqlSelect query = new SqlSelect()
          .addFields(TBL_ORDERS, COL_WAREHOUSE)
          .addFrom(TBL_ORDERS)
          .setWhere(SqlUtils.equals(TBL_ORDERS, sys.getIdName(TBL_ORDERS), order));

      warehouseId = qs.getLong(query);
    } else {
      warehouseId = whId;
    }

    Map<Long, Double> totRemainders = new HashMap<>();
    itemsRemainders = new HashMap<>();

    for (Long itemId : itemIds) {
      SqlSelect qry =
          new SqlSelect()
              .addFields(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)
              .addFrom(TBL_ORDERS)
              .addFromLeft(TBL_ORDER_ITEMS,
                  SqlUtils.join(TBL_ORDER_ITEMS, COL_ORDER, TBL_ORDERS, sys.getIdName(TBL_ORDERS)))
              .setWhere(SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_WAREHOUSE, warehouseId),
                  SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED.ordinal()),
                  SqlUtils.equals(TBL_ORDER_ITEMS, COL_ITEM, itemId)));

      SimpleRowSet srs = qs.getData(qry);
      Double totRes = BeeConst.DOUBLE_ZERO;

      for (SimpleRow sr : srs) {
        if (BeeUtils.isDouble(sr.getDouble(COL_RESERVED_REMAINDER))) {
          totRes += sr.getDouble(COL_RESERVED_REMAINDER);
        }
      }

      SqlSelect q = new SqlSelect()
          .addFields(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)
          .addFrom(VIEW_ITEM_REMAINDERS)
          .setWhere(
              SqlUtils.and(SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_ITEM, itemId), SqlUtils
                  .equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE, warehouseId)));

      if (BeeUtils.isDouble(qs.getDouble(q))) {
        Double rem = qs.getDouble(q);
        totRemainders.put(itemId, rem - totRes);
        itemsRemainders.put(itemId, rem);
      } else {
        totRemainders.put(itemId, BeeConst.DOUBLE_ZERO);
        itemsRemainders.put(itemId, BeeConst.DOUBLE_ZERO);
      }
    }

    return totRemainders;
  }

  private ResponseObject fillReservedRemainders(RequestInfo reqInfo) {
    Long orderId = reqInfo.getParameterLong(COL_ORDER);
    Long warehouseId = reqInfo.getParameterLong(COL_WAREHOUSE);

    if (!DataUtils.isId(orderId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_ORDER);
    }
    if (!DataUtils.isId(warehouseId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_WAREHOUSE);
    }

    SqlSelect itemsQry =
        new SqlSelect()
            .addField(VIEW_ORDER_ITEMS, sys.getIdName(VIEW_ORDER_ITEMS), "OrderItem")
            .addFields(VIEW_ORDER_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY)
            .addFrom(VIEW_ORDER_ITEMS)
            .setWhere(SqlUtils.equals(VIEW_ORDER_ITEMS, COL_ORDER, orderId));

    SimpleRowSet srs = qs.getData(itemsQry);
    Map<Long, Double> rem =
        totReservedRemainders(Arrays.asList(srs.getLongColumn(COL_ITEM)), null, warehouseId);

    for (SimpleRow sr : srs) {
      Double resRemainder = BeeConst.DOUBLE_ZERO;
      Double qty = sr.getDouble(COL_TRADE_ITEM_QUANTITY);
      Double free = rem.get(sr.getLong(COL_ITEM));
      if (qty <= free) {
        resRemainder = qty;
      } else {
        resRemainder = free;
      }

      SqlUpdate update =
          new SqlUpdate(VIEW_ORDER_ITEMS)
              .addConstant(COL_RESERVED_REMAINDER, resRemainder)
              .setWhere(sys.idEquals(VIEW_ORDER_ITEMS, sr.getLong("OrderItem")));

      qs.updateData(update);
    }

    return ResponseObject.emptyResponse();
  }
}