package com.butent.bee.server.modules.orders;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
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
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.modules.orders.ec.OrdEcCartItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.webservice.ButentWS;

import java.io.IOException;
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
  @EJB
  FileStorageBean fs;
  @EJB
  TradeModuleBean trd;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;

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

      case SVC_CREATE_PDF_FILE:
        response = createPdf(reqInfo);
        break;

      case SVC_GET_ERP_STOCKS:
        Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(Service.VAR_DATA));
        getERPStocks(ids);
        response = ResponseObject.emptyResponse();
        break;

      case SVC_ITEMS_INFO:
        response = getItemsInfo(reqInfo.getParameter("view_name"),
            BeeUtils.toLongOrNull(reqInfo.getParameter("id")),
            reqInfo.getParameter(COL_CURRENCY));
        break;

      case SVC_GET_CREDIT_INFO:
        response = getCreditInfo(reqInfo);
        break;

      case SVC_EC_SEARCH_BY_ITEM_ARTICLE:
        response = searchByItemArticle(Operator.STARTS, reqInfo);
        break;

      case SVC_EC_SEARCH_BY_ITEM_CATEGORY:
        response = searchByCategory(reqInfo);
        break;

      case SVC_GET_PICTURES:
        Set<Long> articles = DataUtils.parseIdSet(reqInfo.getParameter(COL_ITEM));
        response = getPictures(articles);
        break;

      case SVC_GET_CATEGORIES:
        response = getCategories();
        break;

      case SVC_GLOBAL_SEARCH:
        response = doGlobalSearch(reqInfo);
        break;

      case SVC_GET_CLIENT_STOCK_LABELS:
        response = getClientWarehouse(reqInfo);
        break;

      case SVC_GET_CONFIGURATION:
        response = getConfiguration();
        break;

      case SVC_SAVE_CONFIGURATION:
        response = saveConfiguration(reqInfo);
        break;

      case SVC_CLEAR_CONFIGURATION:
        response = clearConfiguration(reqInfo);
        break;

      case SVC_FINANCIAL_INFORMATION:
        response = getFinancialInformation(getCurrentClientId());
        break;

      case SVC_UPDATE_SHOPPING_CART:
        response = updateShoppingCart(reqInfo);
        break;

      case SVC_GET_SHOPPING_CARTS:
        response = getShoppingCarts();
        break;

      case SVC_UPLOAD_BANNERS:
        response = uploadBanners(reqInfo);
        break;

      case SVC_GET_PROMO:
        response = getPromo(reqInfo);
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
      getERPStocks(null);
    }
    if (cb.isParameterTimer(timer, PRM_EXPORT_ERP_RESERVATIONS_TIME)) {
      exportReservations();
    }
    if (cb.isParameterTimer(timer, PRM_AUTO_RESERVATION)) {
      autoReservation();
    }
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createBoolean(module, PRM_UPDATE_ITEMS_PRICES, false, false),
        BeeParameter.createNumber(module, PRM_CLEAR_RESERVATIONS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_ITEMS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_STOCKS_TIME, false, null),
        BeeParameter.createNumber(module, PRM_EXPORT_ERP_RESERVATIONS_TIME, false, null),
        BeeParameter.createRelation(module, PRM_DEFAULT_SALE_OPERATION, false,
            VIEW_TRADE_OPERATIONS, COL_OPERATION_NAME),
        BeeParameter.createNumber(module, PRM_MANAGER_DISCOUNT, false, null),
        BeeParameter.createNumber(module, PRM_AUTO_RESERVATION, false, null));

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
    cb.createIntervalTimer(this.getClass(), PRM_AUTO_RESERVATION);

    sys.registerDataEventHandler(new DataEventHandler() {

      @Subscribe
      @AllowConcurrentEvents
      public void setFreeRemainder(ViewQueryEvent event) {
        if ((event.isAfter(VIEW_ORDER_ITEMS) || event.isAfter(VIEW_ORDER_SALES)) && event.hasData()
            && event.getColumnCount() >= sys.getView(event.getTargetName()).getColumnCount()) {

          BeeRowSet rowSet = event.getRowset();

          if (BeeUtils.isPositive(rowSet.getNumberOfRows())) {
            List<Long> itemIds = DataUtils.getDistinct(rowSet, COL_ITEM);
            int itemIndex = rowSet.getColumnIndex(COL_ITEM);
            int ordIndex = rowSet.getColumnIndex(COL_ORDER);
            Long order = rowSet.getRow(0).getLong(ordIndex);

            Map<Long, Double> freeRemainders = totReservedRemainders(itemIds, order, null);
            Map<Long, Double> compInvoices = getCompletedInvoices(order);

            Long warehouseId = getWarehouse(order);

            if (DataUtils.isId(warehouseId)) {
              for (BeeRow row : rowSet) {
                row.setProperty(PRP_FREE_REMAINDER, BeeUtils.toString(freeRemainders.get(row
                    .getLong(itemIndex))));

                Long key = Long.valueOf(row.getId());
                if (BeeUtils.isPositive(compInvoices.get(key))) {
                  row.setProperty(PRP_COMPLETED_INVOICES, compInvoices.get(key));
                } else {
                  row.setProperty(PRP_COMPLETED_INVOICES, BeeConst.DOUBLE_ZERO);
                }
              }
            } else {
              Map<Long, Double> resReminders =
                  getAllRemainders(itemIds, TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER);
              Map<Long, Double> wrhReminders =
                  getAllRemainders(itemIds, VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER);

              for (BeeRow row : rowSet) {

                Long key = Long.valueOf(row.getId());
                if (BeeUtils.isPositive(compInvoices.get(key))) {
                  row.setProperty(PRP_COMPLETED_INVOICES, compInvoices.get(key));
                } else {
                  row.setProperty(PRP_COMPLETED_INVOICES, BeeConst.DOUBLE_ZERO);
                }

                double resRemainder = BeeUtils.unbox(resReminders.get(row
                    .getLong(itemIndex)));
                double wrhRemainder = BeeUtils.unbox(wrhReminders.get(row
                    .getLong(itemIndex)));

                row.setProperty(PRP_FREE_REMAINDER, BeeUtils.toString(wrhRemainder - resRemainder));
              }
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
            String number = getNextNumber(column.getPrecision(), COL_TA_NUMBER);

            if (!BeeUtils.isEmpty(number)) {
              event.addValue(column, new TextValue(number));
            }
          }
        }
      }
    });
  }

  private ResponseObject autoReservation() {

    SqlSelect slc = new SqlSelect()
        .addFields(TBL_ORDER_ITEMS, COL_ITEM)
        .addFields(TBL_ORDERS, COL_WAREHOUSE)
        .addFrom(TBL_ORDER_ITEMS)
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
        .setWhere(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED.ordinal()))
        .addGroup(TBL_ORDER_ITEMS, COL_ITEM)
        .addGroup(TBL_ORDERS, COL_WAREHOUSE);

    for (SimpleRow r : qs.getData(slc)) {
      Long itemId = r.getLong(COL_ITEM);
      Long warehouse = r.getLong(COL_WAREHOUSE);

      double freeRemainder =
          BeeUtils.unbox(totReservedRemainders(Arrays.asList(itemId), null, warehouse).get(itemId));

      if (freeRemainder == 0) {
        continue;
      }

      SqlSelect select = new SqlSelect()
          .addFields(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER,
              COL_TRADE_ITEM_QUANTITY, COL_ORDER, sys.getIdName(TBL_ORDER_ITEMS))
          .addFields(TBL_ORDERS, COL_DATES_START_DATE)
          .addFrom(TBL_ORDER_ITEMS)
          .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
          .setWhere(
              SqlUtils.and(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ITEM, itemId), SqlUtils.equals(
                  TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED.ordinal()), SqlUtils
                  .equals(TBL_ORDERS, COL_WAREHOUSE, warehouse)))
          .addOrder(TBL_ORDERS, COL_DATES_START_DATE);

      for (SimpleRow row : qs.getData(select)) {
        double quantity =
            BeeUtils.unbox(row.getDouble(COL_TRADE_ITEM_QUANTITY));
        double reserved =
            BeeUtils.unbox(row.getDouble(COL_RESERVED_REMAINDER));

        Map<Long, Double> completedMap = getCompletedInvoices(row.getLong(COL_ORDER));
        double completed =
            BeeUtils.unbox(completedMap.get(row.getLong(sys.getIdName(TBL_ORDER_ITEMS))));
        double uncompleted = quantity - completed;
        double value = 0;

        if (uncompleted != 0 && reserved != uncompleted && freeRemainder > 0) {
          if (reserved + freeRemainder <= uncompleted) {
            value = reserved + freeRemainder;
          } else {
            value = uncompleted;
          }
          freeRemainder -= value - reserved;

          if (value > 0) {
            SqlUpdate update = new SqlUpdate(TBL_ORDER_ITEMS)
                .addConstant(COL_RESERVED_REMAINDER, value).setWhere(sys.idEquals(TBL_ORDER_ITEMS,
                    row.getLong(sys.getIdName(TBL_ORDER_ITEMS))));

            qs.updateData(update);
          }
        }
      }
    }

    return ResponseObject.emptyResponse();
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {

    Long orderId = reqInfo.getParameterLong(COL_ORDER);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    Long warehouse = reqInfo.getParameterLong(COL_WAREHOUSE);
    boolean remChecked = reqInfo.hasParameter(COL_WAREHOUSE_REMAINDER);

    CompoundFilter filter = Filter.and();

    Set<Long> orderItems = getOrderItems(orderId, TBL_ORDER_ITEMS, COL_ORDER);
    if (!orderItems.isEmpty()) {
      filter.add(Filter.idNotIn(orderItems));
    }

    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    if (warehouse != null && !remChecked) {
      filter.add(Filter.in(sys.getIdName(TBL_ITEMS), VIEW_ITEM_REMAINDERS, COL_ITEM, Filter.and(
          Filter.equals(COL_WAREHOUSE, warehouse), Filter.notNull(COL_WAREHOUSE_REMAINDER))));
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
    items.addColumn(ValueType.NUMBER, COL_TRADE_SUPPLIER);
    items.addColumn(ValueType.NUMBER, COL_UNPACKING);
    items.addColumn(ValueType.DATE, COL_DATE_TO);
    items.addColumn(ValueType.NUMBER, COL_DEFAULT_VAT);
    items.addColumn(remView.getBeeColumn(ALS_WAREHOUSE_CODE));
    items.addColumn(remView.getBeeColumn(COL_WAREHOUSE_REMAINDER));
    items.addColumn(ValueType.NUMBER, PRP_FREE_REMAINDER);
    items.addColumn(ValueType.NUMBER, COL_RESERVED_REMAINDER);

    for (BeeRow row : items) {
      Long itemId = row.getId();

      SqlSelect suppliersQry = new SqlSelect()
          .addFields(VIEW_ITEM_SUPPLIERS, COL_TRADE_SUPPLIER, COL_UNPACKING, COL_DATE_TO)
          .addFrom(VIEW_ITEM_SUPPLIERS)
          .setWhere(SqlUtils.equals(VIEW_ITEM_SUPPLIERS, COL_ITEM, itemId));

      SimpleRowSet suppliers = qs.getData(suppliersQry);

      if (suppliers.getNumberOfRows() == 1) {
        row.setValue(row.getNumberOfCells() - 8, suppliers.getLong(0, COL_TRADE_SUPPLIER));
        row.setValue(row.getNumberOfCells() - 7, suppliers.getDouble(0, COL_UNPACKING));
        row.setValue(row.getNumberOfCells() - 6, suppliers.getDate(0, COL_DATE_TO));
      }

      Double res = totRemainders.get(itemId);
      row.setValue(row.getNumberOfCells() - 5, prm.getInteger(PRM_VAT_PERCENT));
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
    items.addColumn(ValueType.NUMBER, COL_DEFAULT_VAT);

    for (BeeRow row : items) {
      row.setValue(row.getNumberOfCells() - 1, prm.getInteger(PRM_VAT_PERCENT));
    }

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
        totReservedRemainders(Arrays.asList(data.getLongColumn(COL_ITEM)), data.getRow(0).getLong(
            COL_ORDER), null);
    Map<Long, Double> compInvoices = getCompletedInvoices(data.getRow(0).getLong(
        COL_ORDER));

    SqlInsert si = new SqlInsert(VIEW_ORDER_CHILD_INVOICES)
        .addConstant(COL_ORDER, data.getRow(0).getLong(COL_ORDER))
        .addConstant("Sale", saleId);

    qs.insertData(si);

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
        double value = BeeConst.DOUBLE_ZERO;

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

  private ResponseObject createPdf(RequestInfo reqInfo) {

    String content = reqInfo.getParameter(MailConstants.COL_CONTENT);
    String printSize = reqInfo.getParameter(DocumentConstants.PRM_PRINT_SIZE);

    Long fileId = fs.createPdf(printSize, content, "bee", "trade", "commons");

    try {
      return ResponseObject.response(fs.getFile(fileId));
    } catch (IOException e) {
      e.printStackTrace();
      return ResponseObject.emptyResponse();
    }
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
            .addFields("Reservations", COL_ITEM_EXTERNAL_CODE, COL_WAREHOUSE_CODE)
            .addSum("Reservations", COL_RESERVED_REMAINDER, ALS_TOTAL_AMOUNT)
            .addGroup("Reservations", COL_ITEM_EXTERNAL_CODE)
            .addGroup("Reservations", COL_WAREHOUSE_CODE)
            .addFrom(
                new SqlSelect()
                    .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
                    .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
                    .addFields(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)
                    .addFrom(TBL_ORDER_ITEMS)
                    .addFromLeft(TBL_ITEMS,
                        sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
                    .addFromLeft(TBL_ORDERS,
                        sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
                    .addFromLeft(TBL_WAREHOUSES,
                        sys.joinTables(TBL_WAREHOUSES, TBL_ORDERS, COL_WAREHOUSE))
                    .setWhere(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS,
                        OrdersStatus.APPROVED.ordinal())).setUnionAllMode(true).addUnion(
                        new SqlSelect()
                            .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
                            .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
                            .addField(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY,
                                COL_RESERVED_REMAINDER)
                            .addFrom(VIEW_ORDER_CHILD_INVOICES)
                            .addFromLeft(TBL_ORDERS,
                                sys.joinTables(TBL_ORDERS, VIEW_ORDER_CHILD_INVOICES, COL_ORDER))
                            .addFromLeft(TBL_WAREHOUSES,
                                sys.joinTables(TBL_WAREHOUSES, TBL_ORDERS, COL_WAREHOUSE))
                            .addFromLeft(TBL_SALES,
                                sys.joinTables(TBL_SALES, VIEW_ORDER_CHILD_INVOICES, COL_SALE))
                            .addFromLeft(TBL_SALE_ITEMS,
                                sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
                            .addFromLeft(TBL_ITEMS,
                                sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM)).setWhere(
                                SqlUtils.isNull(TBL_SALES, COL_TRADE_EXPORTED))),
                "Reservations");

    SimpleRowSet rs = qs.getData(select);

    if (rs.getNumberOfRows() > 0) {
      try {
        ButentWS.connect(remoteAddress, remoteLogin, remotePassword).importItemReservation(rs);
      } catch (BeeException e) {
        logger.error(e);
        sys.eventEnd(sys.eventStart(PRM_EXPORT_ERP_RESERVATIONS_TIME), "ERROR", e.getMessage());
      }
    }
  }

  private ResponseObject getCreditInfo(RequestInfo reqInfo) {
    Long orderId = BeeUtils.toLongOrNull(reqInfo.getParameter(VIEW_ORDERS));

    if (!DataUtils.isId(orderId)) {
      return ResponseObject.emptyResponse();
    }

    SqlSelect select = new SqlSelect()
        .addFields(VIEW_ORDERS, COL_COMPANY)
        .addFrom(VIEW_ORDERS)
        .setWhere(SqlUtils.equals(VIEW_ORDERS, sys.getIdName(VIEW_ORDERS), orderId));

    Long companyId = qs.getLong(select);

    if (DataUtils.isId(companyId)) {
      ResponseObject response = trd.getCreditInfo(companyId);

      if (!response.hasErrors()) {
        return response;
      }
    }
    return ResponseObject.emptyResponse();
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

      boolean updatePrc = BeeUtils.unbox(prm.getBoolean(PRM_UPDATE_ITEMS_PRICES));

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
        currenciesMap.put("VAL_4", row.getValue("VAL_4"));
        currenciesMap.put("VAL_5", row.getValue("VAL_5"));
        currenciesMap.put("VAL_6", row.getValue("VAL_6"));
        currenciesMap.put("VAL_7", row.getValue("VAL_7"));
        currenciesMap.put("VAL_8", row.getValue("VAL_8"));
        currenciesMap.put("VAL_9", row.getValue("VAL_9"));
        currenciesMap.put("VAL_10", row.getValue("VAL_10"));

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
              .addConstant(COL_ITEM_PRICE_4, row.getDouble("KAINA_4"))
              .addConstant(COL_ITEM_PRICE_5, row.getDouble("KAINA_5"))
              .addConstant(COL_ITEM_PRICE_6, row.getDouble("KAINA_6"))
              .addConstant(COL_ITEM_PRICE_7, row.getDouble("KAINA_7"))
              .addConstant(COL_ITEM_PRICE_8, row.getDouble("KAINA_8"))
              .addConstant(COL_ITEM_PRICE_9, row.getDouble("KAINA_9"))
              .addConstant(COL_ITEM_PRICE_10, row.getDouble("KAINA_10"))
              .addConstant(COL_ITEM_GROUP, typesGroups.get(group))
              .addConstant(COL_ITEM_TYPE, typesGroups.get(type))
              .addConstant(COL_ITEM_CURRENCY, currencies.get(currenciesMap.get("PARD_VAL")))
              .addConstant(COL_ITEM_COST_CURRENCY, currencies.get(currenciesMap.get("SAV_VAL")))
              .addConstant(COL_ITEM_CURRENCY_1, currencies.get(currenciesMap.get("VAL_1")))
              .addConstant(COL_ITEM_CURRENCY_2, currencies.get(currenciesMap.get("VAL_2")))
              .addConstant(COL_ITEM_CURRENCY_3, currencies.get(currenciesMap.get("VAL_3")))
              .addConstant(COL_ITEM_CURRENCY_4, currencies.get(currenciesMap.get("VAL_4")))
              .addConstant(COL_ITEM_CURRENCY_5, currencies.get(currenciesMap.get("VAL_5")))
              .addConstant(COL_ITEM_CURRENCY_6, currencies.get(currenciesMap.get("VAL_6")))
              .addConstant(COL_ITEM_CURRENCY_7, currencies.get(currenciesMap.get("VAL_7")))
              .addConstant(COL_ITEM_CURRENCY_8, currencies.get(currenciesMap.get("VAL_8")))
              .addConstant(COL_ITEM_CURRENCY_9, currencies.get(currenciesMap.get("VAL_9")))
              .addConstant(COL_ITEM_CURRENCY_10, currencies.get(currenciesMap.get("VAL_10")))
              .addConstant(COL_TRADE_VAT, true)
              .addConstant(COL_TRADE_VAT_PERC, prm.getInteger(PRM_VAT_PERCENT)));

          if (!response.hasErrors()) {
            externalCodes.add(exCode);
            articles.add(article);
          }
        } else if (updatePrc) {
          SqlUpdate update = new SqlUpdate(TBL_ITEMS)
              .addConstant(COL_ITEM_PRICE, row.getDouble("PARD_KAINA"))
              .addConstant(COL_ITEM_COST, row.getDouble("SAVIKAINA"))
              .addConstant(COL_ITEM_PRICE_1, row.getDouble("KAINA_1"))
              .addConstant(COL_ITEM_PRICE_2, row.getDouble("KAINA_2"))
              .addConstant(COL_ITEM_PRICE_3, row.getDouble("KAINA_3"))
              .addConstant(COL_ITEM_PRICE_4, row.getDouble("KAINA_4"))
              .addConstant(COL_ITEM_PRICE_5, row.getDouble("KAINA_5"))
              .addConstant(COL_ITEM_PRICE_6, row.getDouble("KAINA_6"))
              .addConstant(COL_ITEM_PRICE_7, row.getDouble("KAINA_7"))
              .addConstant(COL_ITEM_PRICE_8, row.getDouble("KAINA_8"))
              .addConstant(COL_ITEM_PRICE_9, row.getDouble("KAINA_9"))
              .addConstant(COL_ITEM_PRICE_10, row.getDouble("KAINA_10"))
              .addConstant(COL_ITEM_CURRENCY, currencies.get(currenciesMap.get("PARD_VAL")))
              .addConstant(COL_ITEM_COST_CURRENCY, currencies.get(currenciesMap.get("SAV_VAL")))
              .addConstant(COL_ITEM_CURRENCY_1, currencies.get(currenciesMap.get("VAL_1")))
              .addConstant(COL_ITEM_CURRENCY_2, currencies.get(currenciesMap.get("VAL_2")))
              .addConstant(COL_ITEM_CURRENCY_3, currencies.get(currenciesMap.get("VAL_3")))
              .addConstant(COL_ITEM_CURRENCY_4, currencies.get(currenciesMap.get("VAL_4")))
              .addConstant(COL_ITEM_CURRENCY_5, currencies.get(currenciesMap.get("VAL_5")))
              .addConstant(COL_ITEM_CURRENCY_6, currencies.get(currenciesMap.get("VAL_6")))
              .addConstant(COL_ITEM_CURRENCY_7, currencies.get(currenciesMap.get("VAL_7")))
              .addConstant(COL_ITEM_CURRENCY_8, currencies.get(currenciesMap.get("VAL_8")))
              .addConstant(COL_ITEM_CURRENCY_9, currencies.get(currenciesMap.get("VAL_9")))
              .addConstant(COL_ITEM_CURRENCY_10, currencies.get(currenciesMap.get("VAL_10")))
              .setWhere(SqlUtils.equals(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE, exCode));

          qs.updateData(update);
        }
      }
    }
  }

  private void getERPStocks(Set<Long> ids) {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs = null;
    SqlSelect select = null;
    SimpleRowSet srs = null;

    if (ids != null) {
      select = new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE, sys.getIdName(TBL_ITEMS))
          .addFrom(TBL_SALES)
          .addFromLeft(TBL_SALE_ITEMS, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
          .setWhere(sys.idInList(TBL_SALES, ids));
    }

    try {

      if (ids != null) {
        srs = qs.getData(select);
        String[] codeList = srs.getColumn(COL_ITEM_EXTERNAL_CODE);
        for (int i = 0; i < codeList.length; i++) {
          if (i == 0) {
            rs =
                ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks(codeList[i]);
          } else {
            rs.append(ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks(
                codeList[i]));
          }
        }
      } else {
        rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks("");
      }

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
          SqlInsert insert = new SqlInsert(TBL_ITEM_REMAINDERS_TMP)
              .addConstant(COL_ITEM, externalCodes.get(exCode))
              .addConstant(COL_WAREHOUSE, warehouses.get(warehouse))
              .addConstant(COL_WAREHOUSE_REMAINDER, stock);

          qs.insertData(insert);
        }
      }

      SqlUpdate updateTmp =
          new SqlUpdate(TBL_ITEM_REMAINDERS_TMP)
              .addExpression(COL_ITEM_REMAINDER_ID,
                  SqlUtils.field(VIEW_ITEM_REMAINDERS, sys.getIdName(VIEW_ITEM_REMAINDERS)))
              .setFrom(
                  VIEW_ITEM_REMAINDERS,
                  SqlUtils.joinUsing(VIEW_ITEM_REMAINDERS, TBL_ITEM_REMAINDERS_TMP, COL_ITEM,
                      COL_WAREHOUSE));

      qs.updateData(updateTmp);

      SqlUpdate updateRem =
          new SqlUpdate(VIEW_ITEM_REMAINDERS)
              .addExpression(COL_WAREHOUSE_REMAINDER,
                  SqlUtils.field(TBL_ITEM_REMAINDERS_TMP, COL_WAREHOUSE_REMAINDER))
              .setFrom(
                  TBL_ITEM_REMAINDERS_TMP,
                  sys.joinTables(VIEW_ITEM_REMAINDERS, TBL_ITEM_REMAINDERS_TMP,
                      COL_ITEM_REMAINDER_ID))
              .setWhere(
                  SqlUtils.or(SqlUtils.notEqual(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER,
                      SqlUtils.field(
                          TBL_ITEM_REMAINDERS_TMP, COL_WAREHOUSE_REMAINDER)), SqlUtils.isNull(
                      VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)));

      qs.updateData(updateRem);

      SqlUpdate updRem = new SqlUpdate(VIEW_ITEM_REMAINDERS)
          .addConstant(COL_WAREHOUSE_REMAINDER, null);

      IsCondition whereCondition;
      if (ids == null) {
        whereCondition =
            SqlUtils.not(SqlUtils.in(VIEW_ITEM_REMAINDERS, sys.getIdName(VIEW_ITEM_REMAINDERS), new
                SqlSelect().addFields(TBL_ITEM_REMAINDERS_TMP, COL_ITEM_REMAINDER_ID)
                    .addFrom(TBL_ITEM_REMAINDERS_TMP)));
      } else {
        whereCondition =
            SqlUtils.and(SqlUtils.not(SqlUtils.in(VIEW_ITEM_REMAINDERS, sys
                .getIdName(VIEW_ITEM_REMAINDERS), new SqlSelect().addFields(
                TBL_ITEM_REMAINDERS_TMP,
                COL_ITEM_REMAINDER_ID).addFrom(TBL_ITEM_REMAINDERS_TMP))),
                SqlUtils.inList(VIEW_ITEM_REMAINDERS, COL_ITEM,
                    Lists.newArrayList(srs.getLongColumn(sys.getIdName(TBL_ITEMS)))));
      }
      updRem.setWhere(whereCondition);
      qs.updateData(updRem);

      qs.loadData(VIEW_ITEM_REMAINDERS, new SqlSelect().setLimit(10000).addFields(
          TBL_ITEM_REMAINDERS_TMP, COL_ITEM, COL_WAREHOUSE, COL_WAREHOUSE_REMAINDER)
          .addFrom(TBL_ITEM_REMAINDERS_TMP).setWhere(SqlUtils.isNull(TBL_ITEM_REMAINDERS_TMP,
              COL_ITEM_REMAINDER_ID)).addOrder(TBL_ITEM_REMAINDERS_TMP, COL_ITEM, COL_WAREHOUSE));

      qs.updateData(new SqlDelete(TBL_ITEM_REMAINDERS_TMP).setWhere(SqlUtils.notNull(
          TBL_ITEM_REMAINDERS_TMP, sys.getIdName(TBL_ITEM_REMAINDERS_TMP))));

    }
  }

  private ResponseObject getItemsInfo(String viewName, Long id, String currencyTo) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    if (!DataUtils.isId(id)) {
      return ResponseObject.error("Wrong document ID");
    }
    String trade = sys.getView(viewName).getSourceName();

    String source = "";
    String table = "";

    switch (viewName) {
      case VIEW_ORDERS_INVOICES:
        table = VIEW_ORDER_CHILD_INVOICES;
        source = TBL_SALE_ITEMS;
        break;

      case VIEW_ORDERS:
        table = TBL_ORDER_ITEMS;
        source = table;
        break;

    }

    SqlSelect query =
        new SqlSelect()
            .addFields(TBL_ITEMS, sys.getIdName(TBL_ITEMS),
                COL_ITEM_NAME, COL_ITEM_NAME + "2", COL_ITEM_NAME + "3", COL_ITEM_BARCODE,
                COL_ITEM_LINK, COL_TRADE_ITEM_ARTICLE)
            .addField(TBL_UNITS, COL_UNIT_NAME, COL_UNIT)
            .addFields(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER, COL_SUPPLIER_TERM)
            .addFields(source, COL_TRADE_ITEM_QUANTITY, COL_TRADE_DISCOUNT, COL_TRADE_ITEM_PRICE,
                COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_TRADE_ITEM_NOTE)
            .addFields(TBL_ORDERS, ProjectConstants.COL_DATES_START_DATE, COL_WAREHOUSE)
            .addEmptyDouble(PRP_FREE_REMAINDER)
            .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
            .addFrom(table);

    if (BeeUtils.same(viewName, VIEW_ORDERS_INVOICES)) {
      query.addFromLeft(TBL_SALES,
          sys.joinTables(TBL_SALES, VIEW_ORDER_CHILD_INVOICES, COL_SALE))
          .addFromLeft(TBL_SALE_ITEMS,
              sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
          .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
          .addFromLeft(TBL_ORDERS,
              sys.joinTables(TBL_ORDERS, VIEW_ORDER_CHILD_INVOICES, COL_ORDER))
          .addFromInner(TBL_ORDER_ITEMS,
              sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
          .addFromInner(TBL_CURRENCIES,
              sys.joinTables(TBL_CURRENCIES, TBL_SALES, COL_CURRENCY))
          .setWhere(SqlUtils.and(SqlUtils.inList(VIEW_ORDER_CHILD_INVOICES, COL_SALE, id), SqlUtils
              .joinUsing(TBL_SALE_ITEMS, TBL_ORDER_ITEMS, COL_ITEM)));

    } else {
      query.addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
          .addFromInner(TBL_CURRENCIES,
              sys.joinTables(TBL_CURRENCIES, TBL_ORDER_ITEMS, COL_CURRENCY))
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
          .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
          .setWhere(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, id));
    }

    if (!BeeUtils.isEmpty(currencyTo)) {
      String currAlias = SqlUtils.uniqueName();

      IsExpression xpr = ExchangeUtils.exchangeFieldTo(query.addFromLeft(TBL_CURRENCIES, currAlias,
          SqlUtils.equals(currAlias, COL_CURRENCY_NAME, currencyTo)),
          SqlUtils.constant(1),
          SqlUtils.field(trade, COL_CURRENCY),
          SqlUtils.field(trade, COL_TRADE_DATE),
          SqlUtils.field(currAlias, sys.getIdName(TBL_CURRENCIES)));

      query.addExpr(xpr, COL_CURRENCY_RATE)
          .addField(currAlias, COL_CURRENCY_NAME, COL_CURRENCY_RATE + COL_CURRENCY);
    }

    SimpleRowSet rs = qs.getData(query);

    if (rs.getNumberOfRows() > 0) {
      Map<Long, Double> totRemainders =
          totReservedRemainders(Arrays.asList(rs.getLongColumn(sys.getIdName(TBL_ITEMS))), null, rs
              .getRow(0).getLong(COL_WAREHOUSE));

      for (SimpleRow row : rs) {
        row.setValue(PRP_FREE_REMAINDER, String.valueOf(BeeUtils.unbox(totRemainders.get(row
            .getLong(sys.getIdName(TBL_ITEMS))))));
      }
    }

    return ResponseObject.response(rs);
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
    String columnName = reqInfo.getParameter(Service.VAR_COLUMN);
    String viewName = reqInfo.getParameter(VAR_VIEW_NAME);

    if (BeeUtils.isEmpty(columnName) || BeeUtils.isEmpty(viewName)) {
      logger.warning("Missing one of parameter (columnName, viewname)", columnName, viewName);
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

    return ResponseObject.response(getNextNumber(col.getPrecision(), columnName));
  }

  private String getNextNumber(int maxLength, String column) {

    SqlSelect query = new SqlSelect()
        .addFields(TBL_ORDERS, column)
        .addFrom(TBL_ORDERS);

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

  private Map<Long, Double> getCompletedInvoices(Long order) {
    Map<Long, Double> complInvoices = new HashMap<>();

    SqlSelect select =
        new SqlSelect()
            .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
            .addFields(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS))
            .addFrom(VIEW_ORDER_CHILD_INVOICES)
            .addFromLeft(TBL_ORDERS,
                sys.joinTables(TBL_ORDERS, VIEW_ORDER_CHILD_INVOICES, COL_ORDER))
            .addFromInner(TBL_ORDER_ITEMS,
                sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
            .addFromLeft(TBL_SALES,
                sys.joinTables(TBL_SALES, VIEW_ORDER_CHILD_INVOICES, COL_SALE))
            .addFromInner(TBL_SALE_ITEMS,
                sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
            .setWhere(
                SqlUtils.and(SqlUtils.equals(VIEW_ORDER_CHILD_INVOICES, COL_ORDER, order), SqlUtils
                    .joinUsing(TBL_ORDER_ITEMS, TBL_SALE_ITEMS, COL_ITEM)))
            .addGroup(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS));

    SimpleRowSet rs = qs.getData(select);

    if (rs.getNumberOfRows() > 0) {
      for (SimpleRow row : rs) {
        complInvoices.put(row.getLong(sys.getIdName(TBL_ORDER_ITEMS)), row
            .getDouble(COL_TRADE_ITEM_QUANTITY));
      }
    }
    return complInvoices;
  }

  private Map<Long, Double> getAllRemainders(List<Long> ids, String source, String field) {

    Map<Long, Double> reminders = new HashMap<>();
    Map<Long, Double> invoices = new HashMap<>();

    if (!BeeUtils.isEmpty(ids)) {
      SqlSelect select = new SqlSelect()
          .addFields(source, COL_ITEM)
          .addSum(source, field)
          .addFrom(source)
          .setWhere(SqlUtils.inList(source, COL_ITEM, ids))
          .addGroup(source, COL_ITEM);

      if (source == TBL_ORDER_ITEMS) {
        SqlSelect slcInvoices =
            new SqlSelect()
                .addFields(TBL_SALE_ITEMS, COL_ITEM)
                .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
                .addFrom(TBL_SALE_ITEMS)
                .addFromLeft(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
                .setWhere(
                    SqlUtils.and(SqlUtils.inList(TBL_SALE_ITEMS, COL_ITEM, ids), SqlUtils.isNull(
                        TBL_SALES, COL_TRADE_EXPORTED)))
                .addGroup(TBL_SALE_ITEMS, COL_ITEM);

        for (SimpleRow row : qs.getData(slcInvoices)) {
          invoices.put(row.getLong(COL_ITEM), BeeUtils
              .unbox(row.getDouble(COL_TRADE_ITEM_QUANTITY)));
        }

        for (SimpleRow row : qs.getData(select)) {
          if (invoices.containsKey(row.getLong(COL_ITEM))) {
            reminders.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getDouble(field))
                + BeeUtils.unbox(invoices.get(row.getLong(COL_ITEM))));
          } else {
            reminders.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getDouble(field)));
          }
        }
      } else {
        for (SimpleRow row : qs.getData(select)) {
          reminders.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getDouble(field)));
        }
      }
    }

    return reminders;
  }

  private Long getWarehouse(Long orderId) {
    SqlSelect select = new SqlSelect()
        .addFields(TBL_ORDERS, COL_WAREHOUSE)
        .addFrom(TBL_ORDERS)
        .setWhere(sys.idEquals(TBL_ORDERS, orderId));

    return qs.getLong(select);
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
              .addSum(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)
              .addFrom(TBL_ORDERS)
              .addFromLeft(TBL_ORDER_ITEMS,
                  SqlUtils.join(TBL_ORDER_ITEMS, COL_ORDER, TBL_ORDERS, sys.getIdName(TBL_ORDERS)))
              .setWhere(SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_WAREHOUSE, warehouseId),
                  SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS, OrdersStatus.APPROVED.ordinal()),
                  SqlUtils.equals(TBL_ORDER_ITEMS, COL_ITEM, itemId)))
              .addGroup(TBL_ORDER_ITEMS, COL_ITEM);

      Double totRes = qs.getDouble(qry);

      if (totRes == null) {
        totRes = BeeConst.DOUBLE_ZERO;
      }

      SqlSelect invoiceQry =
          new SqlSelect()
              .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
              .addFrom(VIEW_ORDER_CHILD_INVOICES)
              .addFromLeft(TBL_SALES,
                  sys.joinTables(TBL_SALES, VIEW_ORDER_CHILD_INVOICES, COL_SALE))
              .addFromLeft(TBL_SALE_ITEMS, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
              .setWhere(
                  SqlUtils.and(SqlUtils.equals(TBL_SALES, COL_TRADE_WAREHOUSE_FROM,
                      warehouseId), SqlUtils.equals(TBL_SALE_ITEMS, COL_ITEM, itemId), SqlUtils
                      .isNull(TBL_SALES, COL_TRADE_EXPORTED)))
              .addGroup(TBL_SALE_ITEMS, COL_ITEM);

      Double totInvc = qs.getDouble(invoiceQry);

      if (totInvc == null) {
        totInvc = BeeConst.DOUBLE_ZERO;
      }

      SqlSelect q =
          new SqlSelect()
              .addFields(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)
              .addFrom(VIEW_ITEM_REMAINDERS)
              .setWhere(
                  SqlUtils.and(SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_ITEM, itemId), SqlUtils
                      .equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE, warehouseId), SqlUtils.notNull(
                      VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)));

      if (BeeUtils.isDouble(qs.getDouble(q))) {
        Double rem = qs.getDouble(q);
        totRemainders.put(itemId, rem - totRes - totInvc);
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

  private ResponseObject uploadBanners(RequestInfo reqInfo) {
    String picture = reqInfo.getParameter(COL_BANNER_PICTURE);
    if (BeeUtils.isEmpty(picture)) {
      return ResponseObject.parameterNotFound(SVC_UPLOAD_BANNERS, COL_BANNER_PICTURE);
    }

    return qs.insertDataWithResponse(new SqlInsert(TBL_ORD_EC_BANNERS)
        .addConstant(COL_BANNER_PICTURE, picture));
  }

  // E-Commerce

  private ResponseObject clearConfiguration(RequestInfo reqInfo) {
    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(SVC_CLEAR_CONFIGURATION, Service.VAR_COLUMN);
    }

    if (updateConfiguration(column, null)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_CLEAR_CONFIGURATION, column,
          "cannot clear configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private ResponseObject getPictures(Set<Long> items) {
    if (BeeUtils.isEmpty(items)) {
      return ResponseObject.parameterNotFound(SVC_GET_PICTURES, COL_ITEM);
    }

    SqlSelect graphicsQuery = new SqlSelect()
        .addFields("ItemGraphics", COL_ITEM, "Picture")
        .addFrom("ItemGraphics")
        .setWhere(SqlUtils.inList("ItemGraphics", COL_ITEM, items));

    SimpleRowSet graphicsData = qs.getData(graphicsQuery);

    if (DataUtils.isEmpty(graphicsData)) {
      return ResponseObject.emptyResponse();
    }

    List<String> pictures = Lists.newArrayListWithExpectedSize(graphicsData.getNumberOfRows() * 2);
    for (SimpleRow row : graphicsData) {
      pictures.add(row.getValue(COL_ITEM));
      pictures.add(row.getValue("Picture"));
    }

    return ResponseObject.response(pictures).setSize(pictures.size() / 2);
  }

  private ResponseObject didNotMatch(String query) {
    return ResponseObject.warning(usr.getDictionary().ecSearchDidNotMatch(query));
  }

  private static IsCondition getArticleCondition(String article, Operator defOperator) {
    if (BeeUtils.isEmpty(article)) {
      return null;
    }
    Operator operator;
    String value;

    if (article.contains(Operator.CHAR_ANY) || article.contains(Operator.CHAR_ONE)) {
      operator = Operator.MATCHES;
      value = article.trim().toUpperCase();

    } else if (BeeUtils.isPrefixOrSuffix(article, BeeConst.CHAR_EQ)) {
      operator = Operator.EQ;
      value = BeeUtils.removePrefixAndSuffix(article, BeeConst.CHAR_EQ).trim().toUpperCase();

    } else {
      operator = BeeUtils.nvl(defOperator, Operator.CONTAINS);
      value = EcUtils.normalizeCode(article);

      if (operator == Operator.STARTS) {
        value += Operator.CHAR_ANY;
        operator = Operator.MATCHES;
      }
      if (BeeUtils.length(value) < MIN_SEARCH_QUERY_LENGTH) {
        return null;
      }
    }

    return SqlUtils.compare(TBL_ITEMS, COL_ITEM_ARTICLE, operator, value);
  }

  private List<OrdEcItem> getItems(IsCondition condition, Long companyId) {
    List<OrdEcItem> items = new ArrayList<>();

    String unitName = "UnitName";

    SqlSelect categoryQuery = new SqlSelect()
        .addFields(TBL_ITEM_CATEGORY_TREE, sys.getIdName(TBL_ITEM_CATEGORY_TREE))
        .addFrom(TBL_ITEM_CATEGORY_TREE)
        .setWhere(SqlUtils.notNull(TBL_ITEM_CATEGORY_TREE, COL_CATEGORY_INCLUDED));

    Set<Long> categories = qs.getLongSet(categoryQuery);

    IsCondition categoryCondition =
        SqlUtils.or(SqlUtils.inList(TBL_ITEMS, COL_ITEM_TYPE, categories), SqlUtils.inList(
            TBL_ITEMS, COL_ITEM_GROUP, categories), SqlUtils.in(TBL_ITEMS,
            sys.getIdName(TBL_ITEMS), VIEW_ITEM_CATEGORIES, COL_ITEM, SqlUtils.inList(
                VIEW_ITEM_CATEGORIES, COL_CATEGORY, categories)));

    SqlSelect itemsQuery =
        new SqlSelect()
            .addFields(TBL_ITEMS, sys.getIdName(TBL_ITEMS), COL_ITEM_ARTICLE, COL_ITEM_NAME,
                COL_ITEM_PRICE, COL_ITEM_DESCRIPTION, COL_ITEM_LINK, COL_ITEM_MIN_QUANTITY)
            .addField(TBL_UNITS, COL_UNIT_NAME, unitName)
            .addFrom(TBL_ITEMS)
            .addFromLeft(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
            .setWhere(SqlUtils.and(condition, categoryCondition, SqlUtils.isNull(TBL_ITEMS,
                COL_ITEM_NOT_INCLUDED)));

    SimpleRowSet itemData = qs.getData(itemsQuery);

    if (!DataUtils.isEmpty(itemData)) {

      Pair<Map<Long, Integer>, Boolean> stocks =
           getStocks(itemData.getLongColumn(sys.getIdName(TBL_ITEMS)), companyId);

      for (SimpleRow row : itemData) {
        OrdEcItem item = new OrdEcItem();

        if (!BeeUtils.isEmpty(row.getValue(COL_ITEM_ARTICLE))) {
          item.setArticle(row.getValue(COL_ITEM_ARTICLE));
        }
        item.setId(row.getLong(sys.getIdName(TBL_ITEMS)));
        item.setName(row.getValue(COL_ITEM_NAME));
        if (BeeUtils.isPositive(row.getDouble(COL_ITEM_PRICE))) {
          item.setPrice(row.getDouble(COL_ITEM_PRICE));
        }
        item.setUnit(row.getValue(unitName));

        Long itemId = row.getLong(sys.getIdName(TBL_ITEMS));

        if (BeeUtils.unbox(stocks.getB())) {
          if (stocks.getA().containsKey(itemId)) {
            item.setRemainder(stocks.getA().get(itemId).toString());
          }
        } else {
          if (stocks.getA().containsKey(itemId) && BeeUtils.isPositive(stocks.getA().get(itemId))) {
            item.setRemainder(usr.getDictionary().is());
          } else {
            item.setRemainder(usr.getDictionary().isNot());
          }
        }

        String description = row.getValue(COL_ITEM_DESCRIPTION);
        if (!BeeUtils.isEmpty(description)) {
          item.setDescription(description);
        }

        String link = row.getValue(COL_ITEM_LINK);
        if (!BeeUtils.isEmpty(link)) {
          item.setLink(link);
        }

        int minQuantity = BeeUtils.unbox(row.getInt(COL_ITEM_MIN_QUANTITY));
        item.setMinQuantity(minQuantity);

        items.add(item);
      }
    }

    return items;
  }

  private ResponseObject saveConfiguration(RequestInfo reqInfo) {
    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(SVC_SAVE_CONFIGURATION, Service.VAR_COLUMN);
    }

    String value = reqInfo.getParameter(Service.VAR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.parameterNotFound(SVC_SAVE_CONFIGURATION, Service.VAR_VALUE);
    }

    if (updateConfiguration(column, value)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_SAVE_CONFIGURATION, column,
          "cannot save configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private ResponseObject searchByItemArticle(Operator defOperator, RequestInfo reqInfo) {
    String article = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);

    if (BeeUtils.isEmpty(article)) {
      return ResponseObject.parameterNotFound(SVC_EC_SEARCH_BY_ITEM_ARTICLE, VAR_QUERY);
    }

    IsCondition articleCondition = getArticleCondition(article, defOperator);

    if (articleCondition == null) {
      return ResponseObject
          .error(EcUtils.normalizeCode(article),
              usr.getDictionary().searchQueryRestriction(MIN_SEARCH_QUERY_LENGTH));
    }

    List<OrdEcItem> items = getItems(articleCondition, companyId);
    if (items.isEmpty()) {
      return didNotMatch(article);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject searchByCategory(RequestInfo reqInfo) {
    String category = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);

    if (BeeUtils.isEmpty(category)) {
      return ResponseObject
          .parameterNotFound(SVC_EC_SEARCH_BY_ITEM_CATEGORY, VAR_QUERY);
    }

    IsCondition categoryCondition =
        SqlUtils.or(SqlUtils.equals(TBL_ITEMS, COL_ITEM_TYPE, category), SqlUtils.equals(TBL_ITEMS,
            COL_ITEM_GROUP, category), SqlUtils.in(TBL_ITEMS, sys.getIdName(TBL_ITEMS),
            VIEW_ITEM_CATEGORIES, COL_ITEM, SqlUtils.equals(
                VIEW_ITEM_CATEGORIES, COL_CATEGORY, category)));

    List<OrdEcItem> items = getItems(categoryCondition, companyId);
    if (items.isEmpty()) {
      return didNotMatch(category);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private BeeRowSet getBanners(List<RowInfo> cachedBanners) {
    DateTimeValue now = new DateTimeValue(TimeUtils.nowMinutes());

    Filter filter = Filter.and(
        Filter.or(Filter.isNull(EcConstants.COL_BANNER_SHOW_AFTER),
            Filter.isLessEqual(EcConstants.COL_BANNER_SHOW_AFTER, now)),
        Filter.or(Filter.isNull(EcConstants.COL_BANNER_SHOW_BEFORE),
            Filter.isMore(EcConstants.COL_BANNER_SHOW_BEFORE, now)));

    BeeRowSet rowSet = qs.getViewData(TBL_ORD_EC_BANNERS, filter);
    boolean changed;

    if (DataUtils.isEmpty(rowSet)) {
      changed = !cachedBanners.isEmpty();

    } else if (cachedBanners.size() != rowSet.getNumberOfRows()) {
      changed = true;

    } else {
      changed = false;

      for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
        RowInfo rowInfo = cachedBanners.get(i);
        BeeRow row = rowSet.getRow(i);

        if (rowInfo.getId() != row.getId() || rowInfo.getVersion() != row.getVersion()) {
          changed = true;
          break;
        }
      }
    }
    return changed ? rowSet : null;
  }

  private ResponseObject getCategories() {
    String idName = sys.getIdName(TBL_ITEM_CATEGORY_TREE);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_ITEM_CATEGORY_TREE, idName, COL_CATEGORY_PARENT,
            COL_CATEGORY_NAME)
        .addFrom(TBL_ITEM_CATEGORY_TREE)
        .addOrder(TBL_ITEM_CATEGORY_TREE, idName);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      String msg = TBL_ITEM_CATEGORY_TREE + ": data not available";
      logger.warning(msg);
      return ResponseObject.error(msg);
    }

    int rc = data.getNumberOfRows();
    int cc = data.getNumberOfColumns();

    String[] arr = new String[rc * cc];
    int i = 0;

    for (String[] row : data.getRows()) {
      for (int j = 0; j < cc; j++) {
        arr[i * cc + j] = row[j];
      }
      i++;
    }

    return ResponseObject.response(arr).setSize(rc);
  }

  private ResponseObject getConfiguration() {
    BeeRowSet rowSet = qs.getViewData(VIEW_ORD_EC_CONFIGURATION);
    if (rowSet == null) {
      return ResponseObject.error("cannot read", VIEW_ORD_EC_CONFIGURATION);
    }

    Map<String, String> result = new HashMap<>();
    if (rowSet.isEmpty()) {
      for (BeeColumn column : rowSet.getColumns()) {
        result.put(column.getId(), null);
      }
    } else {
      BeeRow row = rowSet.getRow(0);
      for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
        result.put(rowSet.getColumnId(i), row.getString(i));
      }
    }

    return ResponseObject.response(result);
  }

  private ResponseObject getFinancialInformation(Long clientId) {
    return ResponseObject.emptyResponse();
  }

  private ResponseObject doGlobalSearch(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);

    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    IsCondition condition;

    if (BeeUtils.isEmpty(BeeUtils.parseDigits(query))) {
      condition = SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME, query);

    } else {
      condition = SqlUtils.or(SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_ARTICLE, query));
    }

    List<OrdEcItem> items = getItems(condition, companyId);
    if (items.isEmpty()) {
      return didNotMatch(query);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject getClientWarehouse(RequestInfo reqInfo) {

    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);
    Pair<Long, Boolean> warehouseData = getClientWarehouseId(companyId);

    if (DataUtils.isId(warehouseData.getA())) {

      SqlSelect qry = new SqlSelect()
          .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
          .addFrom(TBL_WAREHOUSES)
          .setWhere(sys.idEquals(TBL_WAREHOUSES, warehouseData.getA()));

      String warehouseCode = qs.getValue(qry);
      return ResponseObject.response(warehouseCode);
    }

    return ResponseObject.emptyResponse();
  }

  private Pair<Long, Boolean> getClientWarehouseId(Long companyId) {
    if (DataUtils.isId(companyId)) {

      SqlSelect query = new SqlSelect()
          .addFields(TBL_COMPANIES, COL_EC_WAREHOUSE, COL_EC_SHOW_REMAINDER)
          .addFrom(TBL_COMPANIES).setWhere(sys.idEquals(TBL_COMPANIES, companyId));

      SimpleRowSet srs = qs.getData(query);

      return Pair.of(srs.getLong(0, COL_EC_WAREHOUSE), srs.getBoolean(0, COL_EC_SHOW_REMAINDER));
    }
    return null;
  }

  private Long getCurrentClientId() {
    Long id =
        qs.getLong(new SqlSelect().addFrom(TBL_COMPANY_PERSONS)
            .addFields(TBL_COMPANY_PERSONS, COL_COMAPNY)
            .setWhere(
                SqlUtils.equals(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS), usr
                    .getCompanyPerson(usr.getCurrentUserId()))));

    if (!DataUtils.isId(id)) {
      logger.severe("client not available for user", usr.getCurrentUser());
    }
    return id;
  }

  private ResponseObject getPromo(RequestInfo reqInfo) {
    List<RowInfo> cachedBanners = new ArrayList<>();

    String param = reqInfo.getParameter(EcConstants.VAR_BANNERS);
    if (!BeeUtils.isEmpty(param)) {
      String[] arr = Codec.beeDeserializeCollection(param);
      if (arr != null) {
        for (int i = 0; i < arr.length; i++) {
          cachedBanners.add(RowInfo.restore(arr[i]));
        }
      }
    }

    BeeRowSet banners = getBanners(cachedBanners);

    String banner = (banners == null) ? null : Codec.beeSerialize(banners);
    return ResponseObject.response(banner);
  }

  private ResponseObject getShoppingCarts() {
    Long client = getCurrentClientId();
    if (client == null) {
      return ResponseObject.emptyResponse();
    }

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CREATED,
            COL_SHOPPING_CART_ITEM, COL_SHOPPING_CART_QUANTITY)
        .addFrom(TBL_ORD_EC_SHOPPING_CARTS)
        .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, client))
        .addOrder(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CREATED));

    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();
    }

    Set<Long> items = Sets.newHashSet(data.getLongColumn(COL_SHOPPING_CART_ITEM));

    IsCondition condition = sys.idInList(TBL_ITEMS, items);

    List<OrdEcItem> ecItems = getItems(condition, client);
    if (ecItems.isEmpty()) {
      return ResponseObject.emptyResponse();
    }

    List<OrdEcCartItem> result = new ArrayList<>();

    for (SimpleRow row : data) {
      Long itemId = row.getLong(COL_SHOPPING_CART_ITEM);

      for (OrdEcItem ecItem : ecItems) {
        if (Objects.equals(itemId, ecItem.getId())) {
          OrdEcCartItem cartItem =
              new OrdEcCartItem(ecItem, row.getInt(COL_SHOPPING_CART_QUANTITY));
          result.add(cartItem);
          break;
        }
      }
    }

    return ResponseObject.response(result);
  }

  private Pair<Map<Long, Integer>, Boolean> getStocks(Long[] itemIds, Long companyId) {

    Map<Long, Integer> stocks = new HashMap<>();
    Pair<Long, Boolean> warehouseData = getClientWarehouseId(companyId);

    if (DataUtils.isId(warehouseData.getA())) {

      SqlSelect select = new SqlSelect()
          .addFields(VIEW_ITEM_REMAINDERS, COL_ITEM, COL_WAREHOUSE_REMAINDER)
          .addFrom(VIEW_ITEM_REMAINDERS)
          .setWhere(SqlUtils.and(SqlUtils
              .inList(VIEW_ITEM_REMAINDERS, COL_ITEM, Arrays.asList(itemIds)), SqlUtils
              .equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE, warehouseData.getA())));

      for (SimpleRow row : qs.getData(select)) {
        stocks.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getInt(COL_WAREHOUSE_REMAINDER)));
      }
    }
    return Pair.of(stocks, warehouseData.getB());
  }

  private boolean updateConfiguration(String column, String value) {
    BeeRowSet rowSet = qs.getViewData(VIEW_ORD_EC_CONFIGURATION);

    if (DataUtils.isEmpty(rowSet)) {
      if (BeeUtils.isEmpty(value)) {
        return true;
      } else {
        SqlInsert ins = new SqlInsert(VIEW_ORD_EC_CONFIGURATION).addConstant(column, value);

        ResponseObject response = qs.insertDataWithResponse(ins);
        return !response.hasErrors();
      }

    } else {
      String oldValue = rowSet.getString(0, column);
      if (BeeUtils.equalsTrimRight(value, oldValue)) {
        return true;
      } else {
        SqlUpdate upd = new SqlUpdate(VIEW_ORD_EC_CONFIGURATION).addConstant(column, value);
        upd.setWhere(SqlUtils.equals(VIEW_ORD_EC_CONFIGURATION, sys
            .getIdName(VIEW_ORD_EC_CONFIGURATION), rowSet.getRow(0).getId()));

        ResponseObject response = qs.updateDataWithResponse(upd);
        return !response.hasErrors();
      }
    }
  }

  private ResponseObject updateShoppingCart(RequestInfo reqInfo) {

    Long itemId =
        BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SHOPPING_CART_ITEM));
    if (!DataUtils.isId(itemId)) {
      return ResponseObject.parameterNotFound(SVC_UPDATE_SHOPPING_CART, COL_SHOPPING_CART_ITEM);
    }

    Integer quantity =
        BeeUtils.toIntOrNull(reqInfo.getParameter(COL_SHOPPING_CART_QUANTITY));
    if (quantity == null) {
      return ResponseObject.parameterNotFound(SVC_UPDATE_SHOPPING_CART, COL_SHOPPING_CART_QUANTITY);
    }

    Long client = getCurrentClientId();
    if (!DataUtils.isId(client)) {
      return ResponseObject.emptyResponse();
    }

    IsCondition where =
        SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, client,
            COL_SHOPPING_CART_ITEM, itemId);

    if (BeeUtils.isPositive(quantity)) {
      if (qs.sqlExists(TBL_ORD_EC_SHOPPING_CARTS, where)) {
        qs.updateData(new SqlUpdate(TBL_ORD_EC_SHOPPING_CARTS)
            .addConstant(COL_SHOPPING_CART_QUANTITY, quantity)
            .setWhere(where));
      } else {
        qs.insertData(new SqlInsert(TBL_ORD_EC_SHOPPING_CARTS)
            .addConstant(COL_SHOPPING_CART_CREATED, System.currentTimeMillis())
            .addConstant(COL_SHOPPING_CART_CLIENT, client)
            .addConstant(COL_SHOPPING_CART_ITEM, itemId)
            .addConstant(COL_SHOPPING_CART_QUANTITY, quantity));
      }

    } else {
      qs.updateData(new SqlDelete(TBL_ORD_EC_SHOPPING_CARTS).setWhere(where));
    }

    return ResponseObject.response(itemId);
  }
}