package com.butent.bee.server.modules.orders;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.gwt.thirdparty.guava.common.collect.HashBasedTable;
import com.google.gwt.thirdparty.guava.common.collect.Table;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.COL_OBJECT;
import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.concurrency.ConcurrencyBean.HasTimerService;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent;
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
import com.butent.bee.server.modules.classifiers.ClassifiersModuleBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.news.ExtendedUsageQueryProvider;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.BorderStyle;
import com.butent.bee.shared.css.values.FontSize;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.WhiteSpace;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.html.Tags;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Text;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Tbody;
import com.butent.bee.shared.html.builder.elements.Td;
import com.butent.bee.shared.html.builder.elements.Tr;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.orders.Bundle;
import com.butent.bee.shared.modules.orders.Configuration;
import com.butent.bee.shared.modules.orders.Dimension;
import com.butent.bee.shared.modules.orders.Option;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants.*;
import com.butent.bee.shared.modules.orders.Specification;
import com.butent.bee.shared.modules.orders.ec.NotSubmittedOrdersInfo;
import com.butent.bee.shared.modules.orders.ec.OrdEcCart;
import com.butent.bee.shared.modules.orders.ec.OrdEcCartItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcFinInfo;
import com.butent.bee.shared.modules.orders.ec.OrdEcInvoice;
import com.butent.bee.shared.modules.orders.ec.OrdEcInvoiceItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcOrder;
import com.butent.bee.shared.modules.orders.ec.OrdEcOrderItem;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.webservice.ButentWS;

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
import java.util.stream.Collectors;

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

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;
  @EJB
  ConcurrencyBean cb;
  @EJB
  TradeModuleBean trd;
  @EJB
  UserServiceBean usr;
  @EJB
  ClassifiersModuleBean cmb;
  @EJB
  MailModuleBean mail;
  @EJB
  NewsBean news;

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

      case SVC_FILL_RESERVED_REMAINDERS:
        response = fillReservedRemainders(reqInfo);
        break;

      case SVC_GET_CONFIGURATION:
        response = getConfiguration(reqInfo.getParameterLong(COL_BRANCH));
        break;

      case SVC_SAVE_DIMENSIONS:
        Pair<String, String> pair = Pair.restore(reqInfo.getParameter(TBL_CONF_DIMENSIONS));

        response = saveDimensions(reqInfo.getParameterLong(COL_BRANCH),
            Codec.deserializeIdList(pair.getA()), Codec.deserializeIdList(pair.getB()));
        break;

      case SVC_SET_BUNDLE:
        response = setBundle(reqInfo.getParameterLong(COL_BRANCH),
            Bundle.restore(reqInfo.getParameter(COL_BUNDLE)),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)),
            Codec.unpack(reqInfo.getParameter(COL_BLOCKED)));
        break;

      case SVC_DELETE_BUNDLES:
        qs.updateData(new SqlDelete(TBL_CONF_BRANCH_BUNDLES)
            .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH,
                reqInfo.getParameterLong(COL_BRANCH)),
                SqlUtils.in(TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE,
                    new SqlSelect()
                        .addFields(TBL_CONF_BUNDLES, sys.getIdName(TBL_CONF_BUNDLES))
                        .addFrom(TBL_CONF_BUNDLES)
                        .setWhere(SqlUtils.inList(TBL_CONF_BUNDLES, COL_KEY, (Object[])
                            Codec.beeDeserializeCollection(reqInfo.getParameter(COL_KEY))))))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_DELETE_OPTION:
        qs.updateData(new SqlDelete(TBL_CONF_BRANCH_OPTIONS)
            .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH,
                reqInfo.getParameterLong(COL_BRANCH), COL_OPTION,
                reqInfo.getParameterLong(COL_OPTION))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_SET_OPTION:
        response = setOption(reqInfo.getParameterLong(COL_BRANCH),
            reqInfo.getParameterLong(COL_OPTION),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)));
        break;

      case SVC_SET_RELATION:
        response = setRelation(reqInfo.getParameterLong(COL_BRANCH), reqInfo.getParameter(COL_KEY),
            reqInfo.getParameterLong(COL_OPTION),
            Configuration.DataInfo.restore(reqInfo.getParameter(Service.VAR_DATA)));
        break;

      case SVC_DELETE_RELATION:
        qs.updateData(new SqlDelete(TBL_CONF_RELATIONS)
            .setWhere(sys.idEquals(TBL_CONF_RELATIONS, qs.getLong(new SqlSelect()
                .addFields(TBL_CONF_RELATIONS, sys.getIdName(TBL_CONF_RELATIONS))
                .addFrom(TBL_CONF_BRANCH_BUNDLES)
                .addFromInner(TBL_CONF_BUNDLES, SqlUtils.and(sys.joinTables(TBL_CONF_BUNDLES,
                    TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE), SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY,
                    reqInfo.getParameter(COL_KEY))))
                .addFromInner(TBL_CONF_BRANCH_OPTIONS,
                    SqlUtils.and(SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES,
                        TBL_CONF_BRANCH_OPTIONS, COL_BRANCH),
                        SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_OPTION,
                            reqInfo.getParameterLong(COL_OPTION))))
                .addFromInner(TBL_CONF_RELATIONS,
                    SqlUtils.and(sys.joinTables(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_RELATIONS,
                        COL_BRANCH_BUNDLE), sys.joinTables(TBL_CONF_BRANCH_OPTIONS,
                        TBL_CONF_RELATIONS, COL_BRANCH_OPTION)))
                .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH,
                    reqInfo.getParameterLong(COL_BRANCH)))))));

        response = ResponseObject.emptyResponse();
        break;

      case SVC_SET_RESTRICTIONS:
        Map<Long, Map<Long, Boolean>> data = new HashMap<>();

        for (Map.Entry<String, String> entry : Codec.deserializeLinkedHashMap(
            reqInfo.getParameter(TBL_CONF_RESTRICTIONS)).entrySet()) {
          Map<Long, Boolean> map = new HashMap<>();

          for (Map.Entry<String, String> subEntry : Codec.deserializeLinkedHashMap(
              entry.getValue()).entrySet()) {
            map.put(BeeUtils.toLong(subEntry.getKey()), BeeUtils.toBoolean(subEntry.getValue()));
          }
          data.put(BeeUtils.toLong(entry.getKey()), map);
        }
        response = setRestrictions(reqInfo.getParameterLong(COL_BRANCH), data);
        break;

      case SVC_SAVE_OBJECT:
        response = saveObject(Specification.restore(reqInfo.getParameter(COL_OBJECT)));
        break;

      case SVC_GET_OBJECT:
        response = getObject(reqInfo.getParameterLong(COL_OBJECT));
        break;

      case SVC_GET_ERP_STOCKS:
        Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(Service.VAR_DATA));
        getERPStocks(ids);
        response = ResponseObject.emptyResponse();
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

      case SVC_EC_GET_CONFIGURATION:
        response = getConfiguration();
        break;

      case SVC_EC_SAVE_CONFIGURATION:
        response = saveConfiguration(reqInfo);
        break;

      case SVC_EC_CLEAR_CONFIGURATION:
        response = clearConfiguration(reqInfo);
        break;

      case SVC_FINANCIAL_INFORMATION:
        response = getFinancialInformation(getCurrentUserCompany());
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

      case SVC_SUBMIT_ORDER:
        response = submitOrder(reqInfo);
        break;

      case SVC_SAVE_ORDER:
        response = saveOrder(reqInfo);
        break;

      case SVC_EC_GET_NOT_SUBMITTED_ORDERS:
        response = getNotSubmittedOrders();
        break;

      case SVC_EC_OPEN_SHOPPING_CART:
        response = openShoppingCart(reqInfo);
        break;

      case SVC_EC_CLEAN_SHOPPING_CART:
        response = cleanShoppingCart();
        break;

      case SVC_EC_GET_DOCUMENTS:
        response = getDocuments(reqInfo);
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
    if (cb.isParameterTimer(timer, PRM_IMPORT_ERP_INV_CHANGES_TIME)) {
      getERPInvoiceChanges();
    }
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createBoolean(module, PRM_UPDATE_ITEMS_PRICES),
        BeeParameter.createNumber(module, PRM_CLEAR_RESERVATIONS_TIME),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_ITEMS_TIME),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_STOCKS_TIME),
        BeeParameter.createNumber(module, PRM_EXPORT_ERP_RESERVATIONS_TIME),
        BeeParameter.createRelation(module, PRM_DEFAULT_SALE_OPERATION, true,
            VIEW_TRADE_OPERATIONS, COL_OPERATION_NAME),
        BeeParameter.createNumber(module, PRM_MANAGER_DISCOUNT),
        BeeParameter.createRelation(module, PRM_MANAGER_WAREHOUSE, true, VIEW_WAREHOUSES,
            COL_WAREHOUSE_CODE),
        BeeParameter.createBoolean(module, PRM_CHECK_DEBT),
        BeeParameter.createBoolean(module, PRM_NOTIFY_ABOUT_DEBTS),
        BeeParameter.createNumber(module, PRM_IMPORT_ERP_INV_CHANGES_TIME),
        BeeParameter.createNumber(module, PRM_GET_INV_DAYS_BEFORE));

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
    cb.createIntervalTimer(this.getClass(), PRM_IMPORT_ERP_INV_CHANGES_TIME);

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

            Map<Long, Double> freeRemainders = getFreeRemainders(itemIds, order, null);
            Map<Long, Double> compInvoices = getCompletedInvoices(order);

            Totalizer totalizer = new Totalizer(rowSet.getColumns());

            for (BeeRow row : rowSet) {
              row.setProperty(PRP_FREE_REMAINDER, BeeUtils.toString(freeRemainders.get(row
                  .getLong(itemIndex))));

              Long key = Long.valueOf(row.getId());
              if (BeeUtils.isPositive(compInvoices.get(key))) {
                row.setProperty(PRP_COMPLETED_INVOICES, compInvoices.get(key));
              } else {
                row.setProperty(PRP_COMPLETED_INVOICES, BeeConst.DOUBLE_ZERO);
              }

              double total = BeeUtils.unbox(totalizer.getTotal(row));
              double vat = BeeUtils.unbox(totalizer.getVat(row));

              row.setProperty(PRP_AMOUNT_WO_VAT, total - vat);
            }
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillOrderNumber(DataEvent.ViewModifyEvent event) {
        if (event.isBefore()
            && Objects.equals(sys.getViewSource(event.getTargetName()), TBL_ORDERS)) {
          List<BeeColumn> cols;
          IsRow row;
          Long series = null;

          if (event instanceof ViewInsertEvent) {
            cols = ((ViewInsertEvent) event).getColumns();
            row = ((ViewInsertEvent) event).getRow();
          } else if (event instanceof DataEvent.ViewUpdateEvent) {
            cols = ((DataEvent.ViewUpdateEvent) event).getColumns();
            row = ((DataEvent.ViewUpdateEvent) event).getRow();
          } else {
            return;
          }

          int seriesIdx = DataUtils.getColumnIndex(COL_TA_SERIES, cols);

          if (!BeeConst.isUndef(seriesIdx)) {
            series = row.getLong(seriesIdx);
          }
          if (DataUtils.isId(series)) {
            int numberIdx = DataUtils.getColumnIndex(COL_TA_NUMBER, cols);

            if (BeeConst.isUndef(numberIdx)) {
              cols.add(new BeeColumn(COL_TA_NUMBER));
              row.addValue(null);
              numberIdx = row.getNumberOfCells() - 1;

            } else if (!BeeUtils.isEmpty(row.getString(numberIdx))) {
              return;
            }
            row.setValue(numberIdx, qs.getNextNumber(TBL_ORDERS, COL_TA_NUMBER, null, null));
          }
        }
      }
    });

    news.registerUsageQueryProvider(Feed.ORD_EC_ORDERS, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS,
            OrdersStatus.NEW.ordinal()));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_ORDERS, news.joinUsage(TBL_ORDERS));
      }
    });

    news.registerUsageQueryProvider(Feed.ORD_EC_ORDERS_MY, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS,
            OrdersStatus.NEW.ordinal()), SqlUtils.equals(TBL_ORDERS, COL_TRADE_MANAGER, userId)));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_ORDERS, news.joinUsage(TBL_ORDERS));
      }
    });

    news.registerUsageQueryProvider(Feed.ORDERS, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS,
            OrdersStatus.APPROVED.ordinal()));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_ORDERS, news.joinUsage(TBL_ORDERS));
      }
    });

    news.registerUsageQueryProvider(Feed.ORDERS_MY, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.and(SqlUtils.equals(TBL_ORDERS, COL_ORDERS_STATUS,
            OrdersStatus.APPROVED.ordinal()), SqlUtils.equals(TBL_ORDERS, COL_TRADE_MANAGER, userId)));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_ORDERS, news.joinUsage(TBL_ORDERS));
      }
    });
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    Long warehouse = reqInfo.getParameterLong(COL_WAREHOUSE);
    boolean remChecked = reqInfo.hasParameter(COL_WAREHOUSE_REMAINDER);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

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

    Map<Long, Double> freeRemainders = getFreeRemainders(items.getRowIds(), null, warehouse);
    Map<Long, Double> wrhRemainders = getWarehouseReminders(items.getRowIds(), warehouse);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
        .addFrom(TBL_WAREHOUSES)
        .setWhere(SqlUtils.equals(TBL_WAREHOUSES, sys.getIdName(TBL_WAREHOUSES), warehouse));

    String code = qs.getValue(query);
    Integer defaultVAT = prm.getInteger(PRM_VAT_PERCENT);

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

      Double free = freeRemainders.get(itemId);
      double wrhReminder = BeeConst.DOUBLE_ZERO;

      if (wrhRemainders.size() > 0) {
        wrhReminder = BeeUtils.unbox(wrhRemainders.get(itemId));
      }
      row.setValue(row.getNumberOfCells() - 5, defaultVAT);
      row.setValue(row.getNumberOfCells() - 4, code);
      row.setValue(row.getNumberOfCells() - 3, wrhReminder);
      row.setValue(row.getNumberOfCells() - 2, free);
      row.setValue(row.getNumberOfCells() - 1, wrhReminder - free);
    }

    return ResponseObject.response(items);
  }

  private ResponseObject getTmplItemsForSelection(RequestInfo reqInfo) {

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

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
    Map<String, String> map =
        Codec.deserializeLinkedHashMap(reqInfo.getParameter(Service.VAR_DATA));
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
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
        .setWhere(where);

    IsExpression vatExch =
        ExchangeUtils.exchangeFieldTo(query, SqlUtils.field(TBL_ORDER_ITEMS, COL_TRADE_VAT),
            SqlUtils.field(TBL_ORDER_ITEMS, COL_TRADE_CURRENCY), SqlUtils.field(TBL_ORDERS,
                COL_DATES_START_DATE), SqlUtils.constant(currency));

    String vatAlias = "Vat_" + SqlUtils.uniqueName();

    String priceAlias = "Price_" + SqlUtils.uniqueName();
    IsExpression priceExch =
        ExchangeUtils.exchangeFieldTo(query, SqlUtils.field(TBL_ORDER_ITEMS, COL_TRADE_ITEM_PRICE),
            SqlUtils.field(TBL_ORDER_ITEMS, COL_TRADE_CURRENCY), SqlUtils.field(TBL_ORDERS,
                COL_DATES_START_DATE), SqlUtils.constant(currency));

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

        SqlInsert si = new SqlInsert(VIEW_ORDER_CHILD_INVOICES)
            .addConstant(COL_SALE_ITEM, insResponse.getResponseAsLong())
            .addConstant(COL_ORDER_ITEM, row.getLong(sys.getIdName(TBL_ORDER_ITEMS)));

        qs.insertData(si);

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
            .addFields(ALS_RESERVATIONS, COL_ITEM_EXTERNAL_CODE, COL_WAREHOUSE_CODE)
            .addSum(ALS_RESERVATIONS, COL_RESERVED_REMAINDER, ALS_TOTAL_AMOUNT)
            .addGroup(ALS_RESERVATIONS, COL_ITEM_EXTERNAL_CODE)
            .addGroup(ALS_RESERVATIONS, COL_WAREHOUSE_CODE)
            .addFrom(new SqlSelect()
                    .setUnionAllMode(true)
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
                        OrdersStatus.APPROVED.ordinal())).addUnion(
                    new SqlSelect()
                        .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
                        .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
                        .addField(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY,
                            COL_RESERVED_REMAINDER)
                        .addFrom(VIEW_ORDER_CHILD_INVOICES)
                        .addFromLeft(TBL_ORDER_ITEMS, sys.joinTables(TBL_ORDER_ITEMS,
                            VIEW_ORDER_CHILD_INVOICES, COL_ORDER_ITEM))
                        .addFromLeft(TBL_ORDERS,
                            sys.joinTables(TBL_ORDERS, TBL_ORDER_ITEMS, COL_ORDER))
                        .addFromLeft(TBL_WAREHOUSES,
                            sys.joinTables(TBL_WAREHOUSES, TBL_ORDERS, COL_WAREHOUSE))
                        .addFromLeft(TBL_SALE_ITEMS, sys.joinTables(TBL_SALE_ITEMS,
                            VIEW_ORDER_CHILD_INVOICES, COL_SALE_ITEM))
                        .addFromLeft(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
                        .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
                        .setWhere(SqlUtils.isNull(TBL_SALES, COL_TRADE_EXPORTED))),
                ALS_RESERVATIONS);

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

  private ResponseObject getConfiguration(Long branchId) {
    Configuration configuration = new Configuration();

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_DIMENSIONS, COL_GROUP, COL_ORDINAL)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_DIMENSIONS)
        .addFromInner(TBL_CONF_GROUPS,
            sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_DIMENSIONS, COL_GROUP))
        .setWhere(SqlUtils.equals(TBL_CONF_DIMENSIONS, COL_BRANCH, branchId)));

    for (SimpleRow row : data) {
      configuration.addDimension(new Dimension(row.getLong(COL_GROUP),
              row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)),
          row.getInt(COL_ORDINAL));
    }

    data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_BRANCH_BUNDLES, COL_ITEM_PRICE, COL_BLOCKED)
        .addField(TBL_CONF_BRANCH_BUNDLES, OrdersConstants.COL_DESCRIPTION,
            COL_BUNDLE + OrdersConstants.COL_DESCRIPTION)
        .addFields(TBL_CONF_BUNDLE_OPTIONS, COL_BUNDLE, COL_OPTION)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE,
            OrdersConstants.COL_DESCRIPTION, COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_BRANCH_BUNDLES)
        .addFromInner(TBL_CONF_BUNDLE_OPTIONS,
            SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_BUNDLE_OPTIONS, COL_BUNDLE))
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_BUNDLE_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId)));

    Multimap<Long, Option> bundleOptions = HashMultimap.create();
    Map<Long, Pair<Bundle, Pair<Configuration.DataInfo, Boolean>>> bundles = new HashMap<>();

    for (SimpleRow row : data) {
      Long id = row.getLong(COL_BUNDLE);

      bundleOptions.put(id, new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(OrdersConstants.COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO)));

      if (!bundles.containsKey(id)) {
        bundles.put(id, Pair.of(null,
            Pair.of(Configuration.DataInfo.of(row.getValue(COL_ITEM_PRICE),
                row.getValue(COL_BUNDLE + OrdersConstants.COL_DESCRIPTION)),
                row.getBoolean(COL_BLOCKED))));
      }
    }
    for (Long bundleId : bundles.keySet()) {
      Bundle bundle = new Bundle(bundleOptions.get(bundleId));
      Pair<Bundle, Pair<Configuration.DataInfo, Boolean>> pair = bundles.get(bundleId);
      pair.setA(bundle);
      configuration.setBundleInfo(bundle, pair.getB().getA(), pair.getB().getB());
    }
    data = qs.getData(new SqlSelect()
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addFields(TBL_CONF_BRANCH_OPTIONS, COL_OPTION)
        .addField(TBL_CONF_BRANCH_OPTIONS, COL_ITEM_PRICE, COL_OPTION + COL_ITEM_PRICE)
        .addField(TBL_CONF_BRANCH_OPTIONS, OrdersConstants.COL_DESCRIPTION,
            COL_OPTION + OrdersConstants.COL_DESCRIPTION)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE,
            OrdersConstants.COL_DESCRIPTION, COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFields(TBL_CONF_RELATIONS, COL_ITEM_PRICE)
        .addField(TBL_CONF_RELATIONS, OrdersConstants.COL_DESCRIPTION,
            COL_RELATION + OrdersConstants.COL_DESCRIPTION)
        .addFields(TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE)
        .addFrom(TBL_CONF_BRANCH_OPTIONS)
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_BRANCH_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .addFromLeft(TBL_CONF_RELATIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RELATIONS, COL_BRANCH_OPTION))
        .addFromLeft(TBL_CONF_BRANCH_BUNDLES,
            sys.joinTables(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_RELATIONS, COL_BRANCH_BUNDLE))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId)));

    Map<Long, Option> branchOptions = new HashMap<>();

    for (SimpleRow row : data) {
      Long branchOption = row.getLong(COL_BRANCH_OPTION);

      if (!branchOptions.containsKey(branchOption)) {
        Option option = new Option(row.getLong(COL_OPTION),
            row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
            row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
            .setCode(row.getValue(COL_CODE))
            .setDescription(row.getValue(OrdersConstants.COL_DESCRIPTION))
            .setPhoto(row.getLong(COL_PHOTO));

        branchOptions.put(branchOption, option);
        configuration.setOptionInfo(option,
            Configuration.DataInfo.of(row.getValue(COL_OPTION + COL_ITEM_PRICE),
                row.getValue(COL_OPTION + OrdersConstants.COL_DESCRIPTION)));
      }
      if (DataUtils.isId(row.getLong(COL_BUNDLE))) {
        configuration.setRelationInfo(branchOptions.get(branchOption),
            bundles.get(row.getLong(COL_BUNDLE)).getA(),
            Configuration.DataInfo.of(row.getValue(COL_ITEM_PRICE),
                row.getValue(COL_RELATION + OrdersConstants.COL_DESCRIPTION)));
      }
    }
    data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION, COL_OPTION, COL_DENIED)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE,
            OrdersConstants.COL_DESCRIPTION, COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_RESTRICTIONS)
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_RESTRICTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .addFromInner(TBL_CONF_BRANCH_OPTIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId)));

    for (SimpleRow row : data) {
      Option option = new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(OrdersConstants.COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO));

      configuration.setRestriction(branchOptions.get(row.getLong(COL_BRANCH_OPTION)), option,
          BeeUtils.unbox(row.getBoolean(COL_DENIED)));
    }
    return ResponseObject.response(configuration);
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

  private void getERPInvoiceChanges() {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs;

    Integer days = prm.getInteger(PRM_GET_INV_DAYS_BEFORE);

    if (!BeeUtils.isPositive(days)) {
      return;
    }

    try {
      rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getERPInvChanges(days);

    } catch (BeeException e) {
      logger.error(e);
      sys.eventEnd(sys.eventStart(PRM_IMPORT_ERP_INV_CHANGES_TIME), "ERROR", e.getMessage());
      return;
    }

    if (rs.getNumberOfRows() > 0) {
      Table<Long, Long, Double> table = HashBasedTable.create();

      for (SimpleRow row : rs) {
        table.put(TradeModuleBean.decodeId(TBL_SALES, row.getLong("EXTERN_ID")),
            row.getLong("PREKE"), row.getDouble("KIEKIS"));
      }

      SqlSelect slcSaleItems = new SqlSelect()
          .addField(TBL_SALE_ITEMS, sys.getIdName(TBL_SALE_ITEMS), COL_SALE_ITEM)
          .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
          .addFields(TBL_SALE_ITEMS, COL_SALE, COL_TRADE_ITEM_QUANTITY)
          .addFrom(TBL_SALE_ITEMS)
          .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
          .setWhere(SqlUtils.inList(TBL_SALE_ITEMS, COL_SALE, table.rowKeySet()));

      SimpleRowSet saleItems = qs.getData(slcSaleItems);

      List<Long> removeList = new ArrayList<>();
      Map<Long, Double> update = new HashMap<>();

      for (SimpleRow saleItem : saleItems) {
        Long sale = saleItem.getLong(COL_SALE);
        Long externalCode = saleItem.getLong(COL_ITEM_EXTERNAL_CODE);
        Long saleItemId = saleItem.getLong(COL_SALE_ITEM);

        if (!table.contains(sale, externalCode)) {
          removeList.add(saleItemId);
        } else {
          Double qty = saleItem.getDouble(COL_TRADE_ITEM_QUANTITY);
          Double changedQty = table.get(sale, externalCode);

          if (!BeeUtils.isPositive(changedQty)) {
            removeList.add(saleItem.getLong(COL_SALE_ITEM));
            continue;
          }
          if (qty > changedQty) {
            update.put(saleItem.getLong(COL_SALE_ITEM), changedQty);
            table.put(sale, externalCode, 0.0);
          } else {
            table.put(sale, externalCode, changedQty - qty);
          }
        }
      }

      if (removeList.size() > 0) {
        qs.updateData(new SqlDelete(TBL_SALE_ITEMS).setWhere(sys.idInList(TBL_SALE_ITEMS,
            removeList)));
      }

      if (update.size() > 0) {
        for (Long id : update.keySet()) {
          qs.updateData(new SqlUpdate(TBL_SALE_ITEMS)
            .addConstant(COL_TRADE_ITEM_QUANTITY, update.get(id))
            .setWhere(sys.idEquals(TBL_SALE_ITEMS, id)));
        }
      }

      if (removeList.size() > 0 || update.size() > 0) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(removeList);
        ids.addAll(update.keySet());

        qs.updateData(new SqlUpdate(TBL_ORDERS)
          .addConstant(COL_ORDERS_STATUS, OrdersStatus.APPROVED.ordinal())
          .setWhere(SqlUtils.in(TBL_ORDERS, sys.getIdName(TBL_ORDERS), new SqlSelect()
              .addFields(TBL_ORDER_ITEMS, COL_ORDER)
              .addFrom(VIEW_ORDER_CHILD_INVOICES)
              .addFromLeft(TBL_ORDER_ITEMS, sys.joinTables(TBL_ORDER_ITEMS,
                  VIEW_ORDER_CHILD_INVOICES, COL_ORDER_ITEM))
              .setWhere(SqlUtils.inList(VIEW_ORDER_CHILD_INVOICES, COL_SALE_ITEM, ids)))));
      }
    }
  }

  private void getERPItems() {
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs;

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
          .addFields(TBL_ITEM_CATEGORY_TREE, ClassifierConstants.COL_CATEGORY_NAME)
          .addField(TBL_ITEM_CATEGORY_TREE, sys.getIdName(TBL_ITEM_CATEGORY_TREE), COL_CATEGORY)
          .addFrom(TBL_ITEM_CATEGORY_TREE))) {

        typesGroups.put(row.getValue(ClassifierConstants.COL_CATEGORY_NAME),
            row.getLong(COL_CATEGORY));
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
            typesGroups.put(type, qs.insertData(new SqlInsert(TBL_ITEM_CATEGORY_TREE)
                .addConstant(ClassifierConstants.COL_CATEGORY_NAME, type)));
          }

          if (!typesGroups.containsKey(group)) {
            typesGroups.put(group, qs.insertData(new SqlInsert(TBL_ITEM_CATEGORY_TREE)
                .addConstant(ClassifierConstants.COL_CATEGORY_NAME, group)));
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

    if (!BeeUtils.isEmpty(ids)) {
      select = new SqlSelect()
          .setDistinctMode(true)
          .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE)
          .addField(TBL_ITEMS, sys.getIdName(TBL_ITEMS), COL_ITEM)
          .addFrom(TBL_SALES)
          .addFromInner(TBL_SALE_ITEMS, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
          .setWhere(sys.idInList(TBL_SALES, ids));
    }

    try {

      if (!BeeUtils.isEmpty(ids)) {
        srs = qs.getData(select);
        String[] codeList = srs.getColumn(COL_ITEM_EXTERNAL_CODE);
        for (String code : codeList) {
          if (rs == null) {
            rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks(code);
          } else {
            rs.append(ButentWS.connect(remoteAddress, remoteLogin, remotePassword).getStocks(code));
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

      String tmp = SqlUtils.temporaryName();
      qs.updateData(new SqlCreate(tmp)
          .addLong(COL_ITEM, true)
          .addLong(COL_WAREHOUSE, true)
          .addDecimal(COL_WAREHOUSE_REMAINDER, 12, 3, false)
          .addLong(COL_ITEM_REMAINDER_ID, false));

      SqlInsert insert = new SqlInsert(tmp)
          .addFields(COL_ITEM, COL_WAREHOUSE, COL_WAREHOUSE_REMAINDER);
      int tot = 0;

      for (SimpleRow row : rs) {
        String exCode = row.getValue("PREKE");
        String warehouse = row.getValue("SANDELIS");
        String stock = row.getValue("LIKUTIS");

        if (externalCodes.containsKey(exCode) && warehouses.containsKey(warehouse)) {
          insert.addValues(externalCodes.get(exCode), warehouses.get(warehouse), stock);

          if (++tot % 1e4 == 0) {
            qs.insertData(insert);
            insert.resetValues();
          }
        }
      }

      if (tot % 1e4 > 0) {
        if (!insert.isEmpty()) {
          qs.insertData(insert);
        }
      }

      SqlUpdate updateTmp = new SqlUpdate(tmp)
              .addExpression(COL_ITEM_REMAINDER_ID,
                  SqlUtils.field(VIEW_ITEM_REMAINDERS, sys.getIdName(VIEW_ITEM_REMAINDERS)))
              .setFrom(VIEW_ITEM_REMAINDERS, SqlUtils.joinUsing(VIEW_ITEM_REMAINDERS, tmp, COL_ITEM,
                  COL_WAREHOUSE));

      qs.updateData(updateTmp);

      SqlUpdate updateRem =
          new SqlUpdate(VIEW_ITEM_REMAINDERS)
              .addExpression(COL_WAREHOUSE_REMAINDER,
                  SqlUtils.field(tmp, COL_WAREHOUSE_REMAINDER))
              .setFrom(tmp, sys.joinTables(VIEW_ITEM_REMAINDERS, tmp, COL_ITEM_REMAINDER_ID))
              .setWhere(SqlUtils.or(SqlUtils.notEqual(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER,
                  SqlUtils.field(tmp, COL_WAREHOUSE_REMAINDER)), SqlUtils.isNull(
                  VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)));

      qs.updateData(updateRem);

      SqlUpdate updRem = new SqlUpdate(VIEW_ITEM_REMAINDERS)
          .addConstant(COL_WAREHOUSE_REMAINDER, null);

      IsCondition whereCondition;
      if (BeeUtils.isEmpty(ids)) {
        whereCondition =
            SqlUtils.not(SqlUtils.in(VIEW_ITEM_REMAINDERS, sys.getIdName(VIEW_ITEM_REMAINDERS), new
                SqlSelect().addFields(tmp, COL_ITEM_REMAINDER_ID)
                .addFrom(tmp)));
      } else {
        whereCondition =
            SqlUtils.and(SqlUtils.not(SqlUtils.in(VIEW_ITEM_REMAINDERS, sys
                    .getIdName(VIEW_ITEM_REMAINDERS), new SqlSelect().addFields(tmp,
                COL_ITEM_REMAINDER_ID).addFrom(tmp))),
                SqlUtils.inList(VIEW_ITEM_REMAINDERS, COL_ITEM,
                    Lists.newArrayList(srs.getLongColumn(COL_ITEM))));
      }
      updRem.setWhere(whereCondition);
      qs.updateData(updRem);

      qs.loadData(VIEW_ITEM_REMAINDERS, new SqlSelect().setLimit(10000).addFields(
          tmp, COL_ITEM, COL_WAREHOUSE, COL_WAREHOUSE_REMAINDER)
          .addFrom(tmp).setWhere(SqlUtils.isNull(tmp,
              COL_ITEM_REMAINDER_ID)).addOrder(tmp, COL_ITEM, COL_WAREHOUSE));

      qs.sqlDropTemp(tmp);

    }
  }

  private ResponseObject getObject(Long objectId) {
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_OBJECTS, COL_BRANCH, OrdersConstants.COL_BRANCH_NAME)
        .addField(TBL_CONF_OBJECTS, OrdersConstants.COL_DESCRIPTION,
            COL_BUNDLE + OrdersConstants.COL_DESCRIPTION)
        .addField(TBL_CONF_OBJECTS, COL_ITEM_PRICE, COL_BUNDLE + COL_ITEM_PRICE)
        .addFields(TBL_CONF_OBJECT_OPTIONS, COL_OPTION, COL_ITEM_PRICE)
        .addFields(TBL_CONF_OPTIONS, COL_GROUP, COL_OPTION_NAME, COL_CODE,
            OrdersConstants.COL_DESCRIPTION, COL_PHOTO)
        .addFields(TBL_CONF_GROUPS, COL_GROUP_NAME, COL_REQUIRED)
        .addFrom(TBL_CONF_OBJECTS)
        .addFromInner(TBL_CONF_OBJECT_OPTIONS,
            sys.joinTables(TBL_CONF_OBJECTS, TBL_CONF_OBJECT_OPTIONS, COL_OBJECT))
        .addFromInner(TBL_CONF_OPTIONS,
            sys.joinTables(TBL_CONF_OPTIONS, TBL_CONF_OBJECT_OPTIONS, COL_OPTION))
        .addFromInner(TBL_CONF_GROUPS, sys.joinTables(TBL_CONF_GROUPS, TBL_CONF_OPTIONS, COL_GROUP))
        .setWhere(sys.idEquals(TBL_CONF_OBJECTS, objectId))
        .addOrder(TBL_CONF_OBJECT_OPTIONS, sys.getIdName(TBL_CONF_OBJECT_OPTIONS)));

    Specification specification = null;
    Integer bundlePrice = null;
    List<Option> bundleOptions = new ArrayList<>();

    for (SimpleRow row : rs) {
      if (Objects.isNull(specification)) {
        specification = new Specification();
        specification.setId(objectId);
        specification.setBranch(row.getLong(COL_BRANCH),
            row.getValue(OrdersConstants.COL_BRANCH_NAME));
        specification.setDescription(row.getValue(COL_BUNDLE + OrdersConstants.COL_DESCRIPTION));
        bundlePrice = row.getInt(COL_BUNDLE + COL_ITEM_PRICE);
      }
      Integer price = row.getInt(COL_ITEM_PRICE);
      Option option = new Option(row.getLong(COL_OPTION),
          row.getValue(COL_OPTION_NAME), new Dimension(row.getLong(COL_GROUP),
          row.getValue(COL_GROUP_NAME)).setRequired(row.getBoolean(COL_REQUIRED)))
          .setCode(row.getValue(COL_CODE))
          .setDescription(row.getValue(OrdersConstants.COL_DESCRIPTION))
          .setPhoto(row.getLong(COL_PHOTO));

      if (Objects.isNull(price)) {
        bundleOptions.add(option);
      } else {
        specification.addOption(option, price);
      }
    }
    if (Objects.nonNull(specification)) {
      if (!BeeUtils.isEmpty(bundleOptions)) {
        specification.setBundle(new Bundle(bundleOptions), bundlePrice);
      }
      if (DataUtils.isId(specification.getBranchId())) {
        String idName = sys.getIdName(TBL_CONF_PRICELIST);

        rs = qs.getData(new SqlSelect()
            .addFields(TBL_CONF_PRICELIST, idName, COL_BRANCH, COL_PHOTO)
            .addFrom(TBL_CONF_PRICELIST));

        SimpleRow row = rs.getRowByKey(idName, BeeUtils.toString(specification.getBranchId()));

        while (Objects.nonNull(row)) {
          specification.getPhotos().add(0, row.getLong(COL_PHOTO));
          String id = row.getValue(COL_BRANCH);
          row = DataUtils.isId(id) ? rs.getRowByKey(idName, id) : null;
        }
      }
    }
    return ResponseObject.response(specification);
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

    SqlSelect select = new SqlSelect()
            .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
            .addFields(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS))
            .addFrom(VIEW_ORDER_CHILD_INVOICES)
            .addFromInner(TBL_ORDER_ITEMS, sys.joinTables(TBL_ORDER_ITEMS,
                VIEW_ORDER_CHILD_INVOICES, COL_ORDER_ITEM))
            .addFromInner(TBL_SALE_ITEMS,
                sys.joinTables(TBL_SALE_ITEMS, VIEW_ORDER_CHILD_INVOICES, COL_SALE_ITEM))
            .setWhere(
                SqlUtils.and(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, order), SqlUtils
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

  private Map<Long, Double> getAllRemainders(List<Long> ids) {

    Map<Long, Double> reminders = new HashMap<>();
    Map<Long, Double> resRemainders = new HashMap<>();
    Map<Long, Double> invoices = new HashMap<>();
    Map<Long, Double> wrhRemainders = getWarehouseReminders(ids, null);

    if (!BeeUtils.isEmpty(ids)) {
      SqlSelect selectReminders = new SqlSelect()
          .addFields(TBL_ORDER_ITEMS, COL_ITEM)
          .addSum(TBL_ORDER_ITEMS, COL_RESERVED_REMAINDER)
          .addFrom(TBL_ORDER_ITEMS)
          .setWhere(SqlUtils.inList(TBL_ORDER_ITEMS, COL_ITEM, ids))
          .addGroup(TBL_ORDER_ITEMS, COL_ITEM);

      SqlSelect slcInvoices = new SqlSelect()
          .addFields(TBL_SALE_ITEMS, COL_ITEM)
          .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
          .addFrom(TBL_SALE_ITEMS)
          .addFromLeft(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .setWhere(SqlUtils.and(SqlUtils.inList(TBL_SALE_ITEMS, COL_ITEM, ids), SqlUtils.isNull(
              TBL_SALES, COL_TRADE_EXPORTED)))
          .addGroup(TBL_SALE_ITEMS, COL_ITEM);

      for (SimpleRow row : qs.getData(slcInvoices)) {
        invoices.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getDouble(COL_TRADE_ITEM_QUANTITY)));
      }

      for (SimpleRow row : qs.getData(selectReminders)) {
        resRemainders.put(row.getLong(COL_ITEM),
            BeeUtils.unbox(row.getDouble(COL_RESERVED_REMAINDER)));
      }

      for (Long itemId : ids) {
        double wrhRemainder = BeeUtils.unbox(wrhRemainders.get(itemId));
        double remainder = BeeUtils.unbox(resRemainders.get(itemId));
        double invoice = BeeUtils.unbox(invoices.get(itemId));

        reminders.put(itemId, wrhRemainder - remainder - invoice);
      }
    }

    return reminders;
  }

  private Map<Long, Double> getFreeRemainders(List<Long> itemIds, Long order, Long whId) {
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

    if (warehouseId == null) {
      return getAllRemainders(itemIds);
    }

    for (Long itemId : itemIds) {
      SqlSelect qry = new SqlSelect()
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

      SqlSelect invoiceQry = new SqlSelect()
          .addSum(TBL_SALE_ITEMS, COL_TRADE_ITEM_QUANTITY)
          .addFrom(VIEW_ORDER_CHILD_INVOICES)
          .addFromLeft(TBL_SALE_ITEMS, sys.joinTables(TBL_SALE_ITEMS, VIEW_ORDER_CHILD_INVOICES,
              COL_SALE_ITEM))
          .addFromLeft(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .setWhere(SqlUtils.and(SqlUtils.equals(TBL_SALES, COL_TRADE_WAREHOUSE_FROM, warehouseId),
              SqlUtils.equals(TBL_SALE_ITEMS, COL_ITEM, itemId), SqlUtils.isNull(TBL_SALES,
                  COL_TRADE_EXPORTED)))
          .addGroup(TBL_SALE_ITEMS, COL_ITEM);

      Double totInvc = qs.getDouble(invoiceQry);

      if (totInvc == null) {
        totInvc = BeeConst.DOUBLE_ZERO;
      }

      SqlSelect q = new SqlSelect()
          .addFields(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)
          .addFrom(VIEW_ITEM_REMAINDERS)
          .setWhere(SqlUtils.and(SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_ITEM, itemId),
              SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE, warehouseId), SqlUtils.notNull(
                  VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)));

      if (BeeUtils.isDouble(qs.getDouble(q))) {
        Double rem = qs.getDouble(q);
        totRemainders.put(itemId, rem - totRes - totInvc);
      } else {
        totRemainders.put(itemId, BeeConst.DOUBLE_ZERO);
      }
    }

    return totRemainders;
  }

  private Map<Long, Double> getWarehouseReminders(List<Long> ids, Long warehouse) {
    Map<Long, Double> result = new HashMap<>();

    SqlSelect selectWrhReminders = new SqlSelect();

    if (DataUtils.isId(warehouse)) {
      selectWrhReminders
          .addFields(VIEW_ITEM_REMAINDERS, COL_ITEM, COL_WAREHOUSE_REMAINDER)
          .addFrom(VIEW_ITEM_REMAINDERS)
          .setWhere(SqlUtils.and(SqlUtils.inList(VIEW_ITEM_REMAINDERS, COL_ITEM, ids),
              SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE, warehouse)));
    } else {
      selectWrhReminders
          .addFields(VIEW_ITEM_REMAINDERS, COL_ITEM)
          .addSum(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE_REMAINDER)
          .addFrom(VIEW_ITEM_REMAINDERS)
          .setWhere(SqlUtils.inList(VIEW_ITEM_REMAINDERS, COL_ITEM, ids))
          .addGroup(VIEW_ITEM_REMAINDERS, COL_ITEM);
    }

    for (SimpleRow row : qs.getData(selectWrhReminders)) {
      result.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getDouble(COL_WAREHOUSE_REMAINDER)));
    }

    return result;
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
        getFreeRemainders(Arrays.asList(srs.getLongColumn(COL_ITEM)), null, warehouseId);

    for (SimpleRow sr : srs) {
      Double resRemainder;
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

  private ResponseObject saveDimensions(Long branchId, List<Long> rows, List<Long> cols) {
    String idName = sys.getIdName(TBL_CONF_DIMENSIONS);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_DIMENSIONS, idName, COL_GROUP, COL_ORDINAL)
        .addFrom(TBL_CONF_DIMENSIONS)
        .setWhere(SqlUtils.equals(TBL_CONF_DIMENSIONS, COL_BRANCH, branchId)));

    Set<Long> usedIds = new HashSet<>();
    List<Pair<Long, Integer>> list = new ArrayList<>();

    for (int i = 0; i < rows.size(); i++) {
      list.add(Pair.of(rows.get(i), i));
    }
    for (int i = 0; i < cols.size(); i++) {
      list.add(Pair.of(cols.get(i), (i + 1) * (-1)));
    }

    for (Pair<Long, Integer> pair : list) {
      boolean found = false;

      for (SimpleRow row : data) {
        Long id = row.getLong(idName);
        found = !usedIds.contains(id) && Objects.equals(pair.getA(), row.getLong(COL_GROUP));

        if (found) {
          if (!Objects.equals(row.getInt(COL_ORDINAL), pair.getB())) {
            qs.updateData(new SqlUpdate(TBL_CONF_DIMENSIONS)
                .addConstant(COL_ORDINAL, pair.getB())
                .setWhere(sys.idEquals(TBL_CONF_DIMENSIONS, id)));
          }
          usedIds.add(id);
          break;
        }
      }
      if (!found) {
        qs.insertData(new SqlInsert(TBL_CONF_DIMENSIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_GROUP, pair.getA())
            .addConstant(COL_ORDINAL, pair.getB()));
      }
    }
    List<Long> unusedIds = Arrays.stream(data.getLongColumn(idName))
        .filter(id -> !usedIds.contains(id))
        .collect(Collectors.toList());

    if (!BeeUtils.isEmpty(unusedIds)) {
      qs.updateData(new SqlDelete(TBL_CONF_DIMENSIONS)
          .setWhere(sys.idInList(TBL_CONF_DIMENSIONS, unusedIds)));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject saveObject(Specification specification) {
    long objectId = qs.insertData(new SqlInsert(TBL_CONF_OBJECTS)
        .addConstant(COL_BRANCH, specification.getBranchId())
        .addConstant(OrdersConstants.COL_BRANCH_NAME, specification.getBranchName())
        .addConstant(OrdersConstants.COL_DESCRIPTION, specification.getDescription())
        .addConstant(COL_ITEM_PRICE, specification.getBundlePrice()));

    if (specification.getBundle() != null) {
      for (Option option : specification.getBundle().getOptions()) {
        qs.insertData(new SqlInsert(TBL_CONF_OBJECT_OPTIONS)
            .addConstant(COL_OBJECT, objectId)
            .addConstant(COL_OPTION, option.getId()));
      }
    }
    for (Option option : specification.getOptions()) {
      qs.insertData(new SqlInsert(TBL_CONF_OBJECT_OPTIONS)
          .addConstant(COL_OBJECT, objectId)
          .addConstant(COL_OPTION, option.getId())
          .addConstant(COL_ITEM_PRICE, specification.getOptionPrice(option)));
    }
    return ResponseObject.response(objectId);
  }

  private ResponseObject setBundle(Long branchId, Bundle bundle, Configuration.DataInfo info,
      boolean blocked) {
    int c = 0;
    Long bundleId = qs.getLong(new SqlSelect()
        .addFields(TBL_CONF_BUNDLES, sys.getIdName(TBL_CONF_BUNDLES))
        .addFrom(TBL_CONF_BUNDLES)
        .setWhere(SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY, bundle.getKey())));

    if (!DataUtils.isId(bundleId)) {
      bundleId = qs.insertData(new SqlInsert(TBL_CONF_BUNDLES)
          .addConstant(COL_KEY, bundle.getKey()));

      for (Option option : bundle.getOptions()) {
        qs.insertData(new SqlInsert(TBL_CONF_BUNDLE_OPTIONS)
            .addConstant(COL_BUNDLE, bundleId)
            .addConstant(COL_OPTION, option.getId()));
      }
    } else {
      c = qs.updateData(new SqlUpdate(TBL_CONF_BRANCH_BUNDLES)
          .addConstant(COL_ITEM_PRICE, info.getPrice())
          .addConstant(OrdersConstants.COL_DESCRIPTION, info.getDescription())
          .addConstant(COL_BLOCKED, blocked)
          .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId,
              COL_BUNDLE, bundleId)));
    }
    if (!BeeUtils.isPositive(c)) {
      qs.insertData(new SqlInsert(TBL_CONF_BRANCH_BUNDLES)
          .addConstant(COL_BRANCH, branchId)
          .addConstant(COL_BUNDLE, bundleId)
          .addNotEmpty(COL_ITEM_PRICE, info.getPrice())
          .addNotEmpty(OrdersConstants.COL_DESCRIPTION, info.getDescription())
          .addConstant(COL_BLOCKED, blocked));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setOption(Long branchId, Long optionId, Configuration.DataInfo info) {
    int c = qs.updateData(new SqlUpdate(TBL_CONF_BRANCH_OPTIONS)
        .addConstant(COL_ITEM_PRICE, info.getPrice())
        .addConstant(OrdersConstants.COL_DESCRIPTION, info.getDescription())
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId, COL_OPTION,
            optionId)));

    if (!BeeUtils.isPositive(c)) {
      qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
          .addConstant(COL_BRANCH, branchId)
          .addConstant(COL_OPTION, optionId)
          .addNotEmpty(COL_ITEM_PRICE, info.getPrice())
          .addNotEmpty(OrdersConstants.COL_DESCRIPTION, info.getDescription()));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setRelation(Long branchId, String key, Long optionId,
      Configuration.DataInfo info) {
    SimpleRow row = qs.getRow(new SqlSelect()
        .addField(TBL_CONF_BRANCH_BUNDLES, sys.getIdName(TBL_CONF_BRANCH_BUNDLES),
            COL_BRANCH_BUNDLE)
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addFields(TBL_CONF_RELATIONS, sys.getIdName(TBL_CONF_RELATIONS))
        .addFrom(TBL_CONF_BRANCH_BUNDLES)
        .addFromInner(TBL_CONF_BUNDLES,
            SqlUtils.and(sys.joinTables(TBL_CONF_BUNDLES, TBL_CONF_BRANCH_BUNDLES, COL_BUNDLE),
                SqlUtils.equals(TBL_CONF_BUNDLES, COL_KEY, key)))
        .addFromLeft(TBL_CONF_BRANCH_OPTIONS,
            SqlUtils.and(SqlUtils.joinUsing(TBL_CONF_BRANCH_BUNDLES, TBL_CONF_BRANCH_OPTIONS,
                COL_BRANCH), SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_OPTION, optionId)))
        .addFromLeft(TBL_CONF_RELATIONS, SqlUtils.and(sys.joinTables(TBL_CONF_BRANCH_BUNDLES,
            TBL_CONF_RELATIONS, COL_BRANCH_BUNDLE), sys.joinTables(TBL_CONF_BRANCH_OPTIONS,
            TBL_CONF_RELATIONS, COL_BRANCH_OPTION)))
        .setWhere(SqlUtils.equals(TBL_CONF_BRANCH_BUNDLES, COL_BRANCH, branchId)));

    Assert.notNull(row);

    Long relationId = row.getLong(sys.getIdName(TBL_CONF_RELATIONS));

    if (DataUtils.isId(relationId)) {
      qs.updateData(new SqlUpdate(TBL_CONF_RELATIONS)
          .addConstant(COL_ITEM_PRICE, info.getPrice())
          .addConstant(OrdersConstants.COL_DESCRIPTION, info.getDescription())
          .setWhere(sys.idEquals(TBL_CONF_RELATIONS, relationId)));
    } else {
      Long branchOptionId = row.getLong(COL_BRANCH_OPTION);

      if (!DataUtils.isId(branchOptionId)) {
        branchOptionId = qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_OPTION, optionId));
      }
      qs.insertData(new SqlInsert(TBL_CONF_RELATIONS)
          .addConstant(COL_BRANCH_BUNDLE, row.getLong(COL_BRANCH_BUNDLE))
          .addConstant(COL_BRANCH_OPTION, branchOptionId)
          .addNotEmpty(COL_ITEM_PRICE, info.getPrice())
          .addNotEmpty(OrdersConstants.COL_DESCRIPTION, info.getDescription()));
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject setRestrictions(Long branchId, Map<Long, Map<Long, Boolean>> data) {
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_CONF_BRANCH_OPTIONS, COL_OPTION)
        .addField(TBL_CONF_BRANCH_OPTIONS, sys.getIdName(TBL_CONF_BRANCH_OPTIONS),
            COL_BRANCH_OPTION)
        .addField(TBL_CONF_RESTRICTIONS, COL_OPTION, COL_RELATION + COL_OPTION)
        .addFields(TBL_CONF_RESTRICTIONS, COL_DENIED)
        .addFrom(TBL_CONF_BRANCH_OPTIONS)
        .addFromLeft(TBL_CONF_RESTRICTIONS,
            sys.joinTables(TBL_CONF_BRANCH_OPTIONS, TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CONF_BRANCH_OPTIONS, COL_BRANCH, branchId),
            SqlUtils.inList(TBL_CONF_BRANCH_OPTIONS, COL_OPTION, data.keySet()))));

    Map<Long, Pair<Long, Map<Long, Boolean>>> map = new HashMap<>();

    for (SimpleRow row : rs) {
      Long option = row.getLong(COL_OPTION);

      if (!map.containsKey(option)) {
        map.put(option, Pair.of(row.getLong(COL_BRANCH_OPTION), new HashMap<>()));
      }
      Long relatedOption = row.getLong(COL_RELATION + COL_OPTION);

      if (DataUtils.isId(relatedOption)) {
        map.get(option).getB().put(relatedOption, BeeUtils.unbox(row.getBoolean(COL_DENIED)));
      }
    }
    for (Long option : map.keySet()) {
      Map<Long, Boolean> restrictions = data.remove(option);
      Long branchOption = map.get(option).getA();

      for (Map.Entry<Long, Boolean> entry : map.get(option).getB().entrySet()) {
        Long opt = entry.getKey();
        Boolean denied = restrictions.remove(opt);

        if (!Objects.equals(denied, entry.getValue())) {
          IsQuery query;
          IsCondition clause = SqlUtils.equals(TBL_CONF_RESTRICTIONS, COL_BRANCH_OPTION,
              branchOption, COL_OPTION, opt);

          if (denied == null) {
            query = new SqlDelete(TBL_CONF_RESTRICTIONS)
                .setWhere(clause);
          } else {
            query = new SqlUpdate(TBL_CONF_RESTRICTIONS)
                .addConstant(COL_DENIED, denied)
                .setWhere(clause);
          }
          qs.updateData(query);
        }
      }
      for (Long opt : restrictions.keySet()) {
        qs.insertData(new SqlInsert(TBL_CONF_RESTRICTIONS)
            .addConstant(COL_BRANCH_OPTION, branchOption)
            .addConstant(COL_OPTION, opt)
            .addConstant(COL_DENIED, restrictions.get(opt)));
      }
    }
    for (Long option : data.keySet()) {
      Map<Long, Boolean> restrictions = data.get(option);

      if (!BeeUtils.isEmpty(restrictions)) {
        Long branchOption = qs.insertData(new SqlInsert(TBL_CONF_BRANCH_OPTIONS)
            .addConstant(COL_BRANCH, branchId)
            .addConstant(COL_OPTION, option));

        for (Long opt : restrictions.keySet()) {
          qs.insertData(new SqlInsert(TBL_CONF_RESTRICTIONS)
              .addConstant(COL_BRANCH_OPTION, branchOption)
              .addConstant(COL_OPTION, opt)
              .addConstant(COL_DENIED, restrictions.get(opt)));
        }
      }
    }
    return ResponseObject.emptyResponse();
  }

  // E-Commerce

  private ResponseObject clearConfiguration(RequestInfo reqInfo) {
    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(SVC_EC_CLEAR_CONFIGURATION, Service.VAR_COLUMN);
    }

    if (updateConfiguration(column, null)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_EC_CLEAR_CONFIGURATION, column,
          "cannot clear configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private ResponseObject cleanShoppingCart() {

    Long user = usr.getCurrentUserId();

    return qs.updateDataWithResponse(new SqlDelete(TBL_ORD_EC_SHOPPING_CARTS)
        .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, user)));
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

    return SqlUtils.or(SqlUtils.compare(TBL_ITEMS, COL_ITEM_ARTICLE, operator, value),
        SqlUtils.compare(TBL_ITEMS, COL_ITEM_ARTICLE_2, operator, value),
        SqlUtils.compare(TBL_ITEMS, COL_ITEM_ARTICLE_3, operator, value),
        SqlUtils.compare(TBL_ITEMS, COL_ITEM_ARTICLE_4, operator, value));
  }

  private ResponseObject getFinancialInformation(Long companyId) {

    OrdEcFinInfo finInfo = new OrdEcFinInfo();
    if (!DataUtils.isId(companyId)) {
      return ResponseObject.response(finInfo);
    }

    if (DataUtils.isId(companyId)) {
      ResponseObject response = trd.getCreditInfo(companyId);

      if (!response.hasErrors()) {

        Map<String, Object> creditInfo = (Map<String, Object>) response.getResponse();

        if (creditInfo.size() > 0) {

          double limit = (double) creditInfo.get(COL_COMPANY_CREDIT_LIMIT);
          finInfo.setCreditLimit(limit);

          double debt = (double) creditInfo.get(VAR_DEBT);
          finInfo.setDebt(debt);

          double maxedOut = (double) creditInfo.get(VAR_OVERDUE);
          finInfo.setOverdue(maxedOut);
        }

        BeeRowSet orderData = qs.getViewData(VIEW_ORDERS, Filter.equals(COL_COMPANY, companyId),
            new Order(COL_START_DATE, false));

        if (!DataUtils.isEmpty(orderData)) {
          int startDateIndex = orderData.getColumnIndex(COL_START_DATE);
          int statusIndex = orderData.getColumnIndex(COL_ORDERS_STATUS);

          int mfIndex = orderData.getColumnIndex(EcConstants.ALS_ORDER_MANAGER_FIRST_NAME);
          int mlIndex = orderData.getColumnIndex(EcConstants.ALS_ORDER_MANAGER_LAST_NAME);

          int commentIndex = orderData.getColumnIndex(ClassifierConstants.COL_NOTES);
          String unitName = "UnitName";

          SqlSelect itemQuery = new SqlSelect()
              .addFields(TBL_ORDER_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_ITEM_PRICE)
              .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_ARTICLE)
              .addField(TBL_UNITS, COL_UNIT_NAME, unitName)
              .addFrom(TBL_ORDER_ITEMS)
              .addFromInner(TBL_ITEMS,
                  sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
              .addFromLeft(TBL_UNITS,
                  sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
              .addOrder(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS));

          for (BeeRow orderRow : orderData) {
            OrdEcOrder order = new OrdEcOrder();

            order.setOrderId(orderRow.getId());
            order.setDate(orderRow.getDateTime(startDateIndex));
            order.setStatus(orderRow.getInteger(statusIndex));
            order.setManager(BeeUtils.joinWords(orderRow.getString(mfIndex),
                orderRow.getString(mlIndex)));
            order.setComment(orderRow.getString(commentIndex));

            itemQuery.setWhere(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, orderRow.getId()));
            SimpleRowSet itemData = qs.getData(itemQuery);

            if (!DataUtils.isEmpty(itemData)) {
              for (SimpleRow itemRow : itemData) {
                OrdEcOrderItem item = new OrdEcOrderItem();

                item.setItemId(itemRow.getLong(COL_ITEM));
                item.setName(itemRow.getValue(COL_ITEM_NAME));
                item.setArticle(itemRow.getValue(COL_ITEM_ARTICLE));
                item.setQuantity(itemRow.getInt(COL_TRADE_ITEM_QUANTITY));
                item.setPrice(itemRow.getDouble(COL_ITEM_PRICE));
                item.setUnit(itemRow.getValue(COL_UNIT_NAME));

                order.getItems().add(item);
              }
            }
            finInfo.getOrders().add(order);
          }
        }

        BeeRowSet invoiceData = qs.getViewData(VIEW_SALES, Filter.equals(COL_TRADE_CUSTOMER,
            companyId), new Order(COL_TRADE_DATE, false));

        if (!DataUtils.isEmpty(invoiceData)) {

          int dateIndex = invoiceData.getColumnIndex(COL_TRADE_DATE);
          int seriesIndex = invoiceData.getColumnIndex(COL_TRADE_INVOICE_PREFIX);
          int numberIndex = invoiceData.getColumnIndex(COL_TRADE_INVOICE_NO);
          int amountIndex = invoiceData.getColumnIndex(COL_TRADE_AMOUNT);
          int debtIndex = invoiceData.getColumnIndex(VAR_DEBT);
          int termIndex = invoiceData.getColumnIndex(COL_TRADE_TERM);
          int paidIndex = invoiceData.getColumnIndex(COL_TRADE_PAID);
          int paymentIndex = invoiceData.getColumnIndex(COL_TRADE_PAYMENT_TIME);
          int currencyIndex = invoiceData.getColumnIndex(ALS_CURRENCY_NAME);
          int mfIndex = invoiceData.getColumnIndex(EcConstants.ALS_ORDER_MANAGER_FIRST_NAME);
          int mlIndex = invoiceData.getColumnIndex(EcConstants.ALS_ORDER_MANAGER_LAST_NAME);

          String unitName = "UnitName";

          SqlSelect saleItemQuery = new SqlSelect()
              .addFields(TBL_SALE_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_ITEM_PRICE)
              .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_ARTICLE)
              .addField(TBL_UNITS, COL_UNIT_NAME, unitName)
              .addFrom(TBL_SALE_ITEMS)
              .addFromInner(TBL_ITEMS,
                  sys.joinTables(TBL_ITEMS, TBL_SALE_ITEMS, COL_ITEM))
              .addFromLeft(TBL_UNITS,
                  sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
              .addOrder(TBL_SALE_ITEMS, sys.getIdName(TBL_SALE_ITEMS));

          for (BeeRow row : invoiceData) {
            OrdEcInvoice invoice = new OrdEcInvoice();

            invoice.setInvoiceId(row.getId());
            invoice.setDate(row.getDateTime(dateIndex));
            invoice.setSeries(row.getString(seriesIndex));
            invoice.setNumber(row.getString(numberIndex));
            invoice.setAmount(row.getDouble(amountIndex));
            invoice.setDebt(row.getDouble(debtIndex));
            invoice.setTerm(row.getDate(termIndex));
            invoice.setPaid(row.getDouble(paidIndex));
            invoice.setPaymentTime(row.getDateTime(paymentIndex));
            invoice.setCurrency(row.getString(currencyIndex));
            invoice.setManager(BeeUtils.joinWords(row.getString(mfIndex), row.getString(mlIndex)));

            saleItemQuery.setWhere(SqlUtils.equals(TBL_SALE_ITEMS, COL_SALE, row.getId()));
            SimpleRowSet saleItemData = qs.getData(saleItemQuery);

            if (!DataUtils.isEmpty(saleItemData)) {
              for (SimpleRow itemRow : saleItemData) {
                OrdEcInvoiceItem item = new OrdEcInvoiceItem();

                item.setItemId(itemRow.getLong(COL_ITEM));
                item.setName(itemRow.getValue(COL_ITEM_NAME));
                item.setArticle(itemRow.getValue(COL_ITEM_ARTICLE));
                item.setQuantity(itemRow.getInt(COL_TRADE_ITEM_QUANTITY));
                item.setPrice(itemRow.getDouble(COL_ITEM_PRICE));
                item.setUnit(itemRow.getValue(COL_UNIT_NAME));

                invoice.getItems().add(item);
              }
            }
            finInfo.getInvoices().add(invoice);
          }
        }
      }
    }
    int size = finInfo.getOrders().size() + finInfo.getInvoices().size();

    return ResponseObject.response(finInfo).setSize(size);
  }

  private List<OrdEcItem> getItems(IsCondition condition, Long companyId, Integer clicked) {

    List<OrdEcItem> items = new ArrayList<>();

    String unitName = "UnitName";

    SqlSelect categoryQuery = new SqlSelect()
        .addFields(TBL_ITEM_CATEGORY_TREE, sys.getIdName(TBL_ITEM_CATEGORY_TREE))
        .addFrom(TBL_ITEM_CATEGORY_TREE)
        .setWhere(SqlUtils.notNull(TBL_ITEM_CATEGORY_TREE, COL_CATEGORY_INCLUDED));

    Set<Long> categories = qs.getLongSet(categoryQuery);

    IsCondition categoryCondition = SqlUtils.or(SqlUtils.inList(TBL_ITEMS, COL_ITEM_TYPE,
        categories), SqlUtils.inList(TBL_ITEMS, COL_ITEM_GROUP, categories), SqlUtils.in(TBL_ITEMS,
            sys.getIdName(TBL_ITEMS), VIEW_ITEM_CATEGORIES, COL_ITEM,
        SqlUtils.inList(VIEW_ITEM_CATEGORIES, COL_CATEGORY, categories)));

    SqlSelect itemsQuery = new SqlSelect()
            .addFields(TBL_ITEMS, sys.getIdName(TBL_ITEMS), COL_ITEM_ARTICLE, COL_ITEM_NAME,
                COL_ITEM_PRICE, COL_ITEM_DESCRIPTION, COL_ITEM_LINK, COL_ITEM_MIN_QUANTITY)
            .addField(TBL_UNITS, COL_UNIT_NAME, unitName)
            .addFrom(TBL_ITEMS)
            .addFromLeft(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
            .setWhere(SqlUtils.and(condition, categoryCondition, SqlUtils.isNull(TBL_ITEMS,
                COL_ITEM_NOT_INCLUDED)));

    if (clicked != null) {
      int offset = clicked * 50;
      int limit = 50;

      itemsQuery.setOffset(offset);
      itemsQuery.setLimit(limit);
    }


    SimpleRowSet itemData = qs.getData(itemsQuery);
    Pair<Long, Boolean> warehouse = getClientWarehouseId(companyId);

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

        Double price = row.getDouble(COL_ITEM_PRICE);

        Pair<Double, Double> prices = (Pair<Double, Double>) cmb.getPriceAndDiscount(companyId,
            item.getId(), null, warehouse.getA(), null, 1.0, null, null, null, 0).getResponse();

        if (prices != null && BeeUtils.isDouble(prices.getA())
            && BeeUtils.isDouble(prices.getB())) {
          price = prices.getA();
          Double discount = prices.getB();

          item.setPrice(price - price * discount / 100.0);
          item.setDefPrice(price - price * discount / 100.0);
        } else if (BeeUtils.isPositive(price)) {
          item.setPrice(price);
          item.setDefPrice(price);
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
      return ResponseObject.parameterNotFound(SVC_EC_SAVE_CONFIGURATION, Service.VAR_COLUMN);
    }

    String value = reqInfo.getParameter(Service.VAR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.parameterNotFound(SVC_EC_SAVE_CONFIGURATION, Service.VAR_VALUE);
    }

    if (updateConfiguration(column, value)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_EC_SAVE_CONFIGURATION, column,
          "cannot save configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private ResponseObject searchByItemArticle(Operator defOperator, RequestInfo reqInfo) {
    String article = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);
    Integer clicked = reqInfo.getParameterInt("Clicked");

    if (BeeUtils.isEmpty(article)) {
      return ResponseObject.parameterNotFound(SVC_EC_SEARCH_BY_ITEM_ARTICLE, VAR_QUERY);
    }

    IsCondition articleCondition = getArticleCondition(article, defOperator);

    if (articleCondition == null) {
      return ResponseObject.error(EcUtils.normalizeCode(article), usr.getDictionary()
          .searchQueryRestriction(MIN_SEARCH_QUERY_LENGTH));
    }

    List<OrdEcItem> items = getItems(articleCondition, companyId, clicked);
    if (items.isEmpty()) {
      return didNotMatch(article);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject searchByCategory(RequestInfo reqInfo) {
    String category = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);
    IsCondition categoryCondition = null;
    Integer clicked = reqInfo.getParameterInt("Clicked");

    if (BeeUtils.isEmpty(category)) {
      return ResponseObject.parameterNotFound(SVC_EC_SEARCH_BY_ITEM_CATEGORY, VAR_QUERY);
    }

    if (BeeUtils.isLong(category)) {
      categoryCondition = SqlUtils.or(SqlUtils.equals(TBL_ITEMS, COL_ITEM_TYPE, category),
          SqlUtils.equals(TBL_ITEMS, COL_ITEM_GROUP, category), SqlUtils.in(TBL_ITEMS,
              sys.getIdName(TBL_ITEMS), VIEW_ITEM_CATEGORIES, COL_ITEM,
              SqlUtils.equals(VIEW_ITEM_CATEGORIES, COL_CATEGORY, category)));
    }

    List<OrdEcItem> items = getItems(categoryCondition, companyId, clicked);
    if (items.isEmpty()) {
      return didNotMatch(category);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private BeeRowSet getBanners(List<RowInfo> cachedBanners) {
    DateTimeValue now = new DateTimeValue(TimeUtils.nowMinutes());

    Filter filter = Filter.and(Filter.or(Filter.isNull(EcConstants.COL_BANNER_SHOW_AFTER),
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
            ClassifierConstants.COL_CATEGORY_NAME)
        .addFrom(TBL_ITEM_CATEGORY_TREE)
        .setWhere(SqlUtils.notNull(TBL_ITEM_CATEGORY_TREE, COL_CATEGORY_INCLUDED))
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

  private ResponseObject doGlobalSearch(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    Long companyId = reqInfo.getParameterLong(COL_COMAPNY);
    Integer clicked = reqInfo.getParameterInt("Clicked");

    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    IsCondition condition;

    if (BeeUtils.isEmpty(BeeUtils.parseDigits(query))) {
      condition = SqlUtils.or(
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME_2, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME_3, query));

    } else {
      condition = SqlUtils.or(
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME_2, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_NAME_3, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_ARTICLE, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_ARTICLE_2, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_ARTICLE_3, query),
          SqlUtils.contains(TBL_ITEMS, COL_ITEM_ARTICLE_4, query));
    }

    List<OrdEcItem> items = getItems(condition, companyId, clicked);
    if (items.isEmpty()) {
      return didNotMatch(query);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject mailOrder(Long orderId) {
    if (!DataUtils.isId(orderId)) {
      return ResponseObject.parameterNotFound(SVC_MAIL_ORDER, COL_ORDER);
    }

    BeeRowSet orderData = qs.getViewData(VIEW_ORDERS, Filter.compareId(orderId));
    if (DataUtils.isEmpty(orderData)) {
      String msg = BeeUtils.joinWords(SVC_MAIL_ORDER, "order not found:", orderId);
      logger.severe(msg);
      return ResponseObject.error(msg);
    }

    BeeRow orderRow = orderData.getRow(0);

    OrdersStatus status = OrdersStatus.get(DataUtils.getInteger(orderData, orderRow,
        COL_ORDERS_STATUS));
    Assert.notNull(status);

    Long clientUser = usr.getCurrentUserId();

    Long manager = DataUtils.getLong(orderData, orderRow, COL_TRADE_MANAGER);

    ResponseObject response = ResponseObject.emptyResponse();

    String clientEmail = usr.getUserEmail(clientUser, true);

    if (BeeUtils.isEmpty(clientEmail)) {
      response.addWarning(usr.getDictionary().ecMailClientAddressNotFound());
      return response;
    }

    Long account = getSenderAccountId(manager);
    if (!DataUtils.isId(account)) {
      return ResponseObject.warning(usr.getDictionary().ecMailAccountNotFound());
    }

    Dictionary constants = usr.getDictionary(clientUser);
    Assert.notNull(constants);

    Document document = orderToHtml(orderData.getColumns(), orderRow, constants);
    String content = document.buildLines();

    ResponseObject mailResponse = mail.sendMail(account, clientEmail,
        constants.ecOrderStatusNewSubject(), content);
    if (mailResponse.hasErrors()) {
      return ResponseObject.warning(usr.getDictionary().ecMailFailed());
    }

    logger.info(SVC_MAIL_ORDER, orderId, "sent to", clientEmail);

    if (BeeUtils.isEmpty(clientEmail)) {
      return response;
    }

    response.addInfo(usr.getDictionary().ecMailSent());
    if (!BeeUtils.isEmpty(clientEmail)) {
      response.addInfo(clientEmail);
    }

    return response;
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

  private Long getCurrentUserCompany() {

    Long id = qs.getLong(new SqlSelect().addFrom(TBL_COMPANY_PERSONS)
        .addFields(TBL_COMPANY_PERSONS, COL_COMAPNY)
        .setWhere(SqlUtils.equals(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS),
            usr.getCompanyPerson(usr.getCurrentUserId()))));

    if (!DataUtils.isId(id)) {
      logger.severe("client not available for user", usr.getCurrentUser());
    }
    return id;
  }

  private SimpleRow getCurrentClientInfo(String... fields) {
    return qs.getRow(new SqlSelect().addFrom(TBL_COMPANY_PERSONS).addFields(TBL_COMPANIES, fields)
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_COMPANY_PERSONS, COL_COMPANY))
        .setWhere(SqlUtils.equals(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS),
            usr.getCompanyPerson(usr.getCurrentUserId()))));
  }

  private ResponseObject getDocuments(RequestInfo reqInfo) {
    Long itemId = reqInfo.getParameterLong(COL_ITEM);
    if (!DataUtils.isId(itemId)) {
      return ResponseObject.parameterNotFound(SVC_EC_GET_DOCUMENTS, COL_ITEM);
    }

    SqlSelect select = new SqlSelect()
        .addFields(VIEW_DOCUMENT_TYPES, COL_DOCUMENT_NAME, "EN", "LV", "FI", "RU", "DE")
        .addFields(VIEW_DOCUMENT_FILES, COL_CAPTION, COL_FILE)
        .addFrom(TBL_DOCUMENTS)
        .addFromLeft(VIEW_DOCUMENT_TYPES, sys.joinTables(VIEW_DOCUMENT_TYPES, TBL_DOCUMENTS,
            DocumentConstants.COL_DOCUMENT_TYPE))
        .addFromLeft(VIEW_DOCUMENT_FILES, sys.joinTables(TBL_DOCUMENTS, VIEW_DOCUMENT_FILES,
            COL_DOCUMENT))
        .setWhere(SqlUtils.and(SqlUtils.in(TBL_DOCUMENTS, sys.getIdName(TBL_DOCUMENTS),
            new SqlSelect()
                .addFields(TBL_RELATIONS, COL_DOCUMENT)
                .addFrom(TBL_RELATIONS)
                .setWhere(SqlUtils.equals(TBL_RELATIONS, COL_ITEM, itemId))),
                  SqlUtils.notNull(TBL_DOCUMENTS, DocumentConstants.COL_DOCUMENT_TYPE),
                  SqlUtils.notNull(VIEW_DOCUMENT_FILES, COL_FILE)));

    Multimap<String, Pair<String, String>> documents = HashMultimap.create();

    String locale = usr.getLocale().toString().toUpperCase();
    if (Objects.equals(locale, "LT")) {
      locale = "Name";
    }

    for (SimpleRow row : qs.getData(select)) {
      String type = row.getValue(locale);

      if (BeeUtils.isEmpty(type)) {
        type = row.getValue("Name");
      }

      documents.put(type, Pair.of(row.getValue(COL_CAPTION), row.getValue(COL_FILE)));
    }

    return ResponseObject.response(documents.asMap());
  }

  private ResponseObject getNotSubmittedOrders() {

    List<NotSubmittedOrdersInfo> carts = new ArrayList<>();
    Long user = usr.getCurrentUserId();

    SqlSelect select = new SqlSelect()
        .addFields(TBL_NOT_SUBMITTED_ORDERS, COL_SHOPPING_CART_NAME, COL_TRADE_DATE,
            COL_SHOPPING_CART_COMMENT)
        .addFrom(TBL_NOT_SUBMITTED_ORDERS)
        .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_NOT_SUBMITTED_ORDERS, COL_ITEM))
        .setWhere(SqlUtils.equals(TBL_NOT_SUBMITTED_ORDERS, COL_SHOPPING_CART_CLIENT, user))
        .addGroup(TBL_NOT_SUBMITTED_ORDERS, COL_SHOPPING_CART_NAME, COL_TRADE_DATE,
            COL_SHOPPING_CART_COMMENT);

    for (SimpleRow row : qs.getData(select)) {
      NotSubmittedOrdersInfo info = new NotSubmittedOrdersInfo();

      info.setName(row.getValue(COL_SHOPPING_CART_NAME));
      info.setDate(row.getDateTime(COL_TRADE_DATE));
      info.setComment(row.getValue(COL_SHOPPING_CART_COMMENT));

      carts.add(info);
    }

    return ResponseObject.response(carts);
  }

  private ResponseObject getPromo(RequestInfo reqInfo) {
    List<RowInfo> cachedBanners = new ArrayList<>();

    String param = reqInfo.getParameter(EcConstants.VAR_BANNERS);
    if (!BeeUtils.isEmpty(param)) {
      String[] arr = Codec.beeDeserializeCollection(param);
      if (arr != null) {
        for (String anArr : arr) {
          cachedBanners.add(RowInfo.restore(anArr));
        }
      }
    }

    BeeRowSet banners = getBanners(cachedBanners);

    String banner = (banners == null) ? null : Codec.beeSerialize(banners);
    return ResponseObject.response(banner);
  }

  private Long getSenderAccountId(Long manager) {
    Long accountId = null;

    if (DataUtils.isId(manager)) {
      accountId = qs.getLong(new SqlSelect()
          .addFields(MailConstants.TBL_ACCOUNTS, sys.getIdName(MailConstants.TBL_ACCOUNTS))
          .addFrom(MailConstants.TBL_ACCOUNTS)
          .setWhere(SqlUtils.equals(MailConstants.TBL_ACCOUNTS, COL_USER, manager)));
    }
    if (!DataUtils.isId(accountId)) {
      accountId = qs.getLong(new SqlSelect()
          .addFields(MailConstants.TBL_ACCOUNTS, sys.getIdName(MailConstants.TBL_ACCOUNTS))
          .addFrom(MailConstants.TBL_ACCOUNTS)
          .addFromInner(VIEW_ORD_EC_CONFIGURATION, sys.joinTables(MailConstants.TBL_ACCOUNTS,
              VIEW_ORD_EC_CONFIGURATION, EcConstants.COL_CONFIG_MAIL_ACCOUNT)));
    }
    return accountId;
  }

  private ResponseObject getShoppingCarts() {
    Long user = usr.getCurrentUserId();
    if (user == null) {
      return ResponseObject.emptyResponse();
    }

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CREATED, COL_SHOPPING_CART_ITEM,
            COL_SHOPPING_CART_QUANTITY)
        .addFrom(TBL_ORD_EC_SHOPPING_CARTS)
        .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, user))
        .addOrder(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CREATED));

    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();
    }

    Pair<Long, Boolean> warehouse = getClientWarehouseId(getCurrentUserCompany());

    Set<Long> items = Sets.newHashSet(data.getLongColumn(COL_SHOPPING_CART_ITEM));

    IsCondition condition = sys.idInList(TBL_ITEMS, items);

    List<OrdEcItem> ecItems = getItems(condition, getCurrentUserCompany(), null);
    if (ecItems.isEmpty()) {
      return ResponseObject.emptyResponse();
    }

    List<OrdEcCartItem> result = new ArrayList<>();

    for (SimpleRow row : data) {
      Long itemId = row.getLong(COL_SHOPPING_CART_ITEM);

      for (OrdEcItem ecItem : ecItems) {
        if (Objects.equals(itemId, ecItem.getId())) {
          Pair<Double, Double> prices = (Pair<Double, Double>) cmb.getPriceAndDiscount(
              getCurrentUserCompany(), itemId, null, warehouse.getA(), null, (double)
                  row.getInt(COL_SHOPPING_CART_QUANTITY), null, null, null, 0).getResponse();

          if (prices != null && BeeUtils.isDouble(prices.getA())
              && BeeUtils.isDouble(prices.getB())) {
            Double discount = prices.getB();
            ecItem.setPrice(prices.getA() - prices.getA() * discount / 100.0);
          }

          OrdEcCartItem cartItem = new OrdEcCartItem(ecItem,
              row.getInt(COL_SHOPPING_CART_QUANTITY));
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
          .setWhere(SqlUtils.and(SqlUtils.inList(VIEW_ITEM_REMAINDERS, COL_ITEM,
              Arrays.asList(itemIds)), SqlUtils.equals(VIEW_ITEM_REMAINDERS, COL_WAREHOUSE,
              warehouseData.getA())));

      for (SimpleRow row : qs.getData(select)) {
        stocks.put(row.getLong(COL_ITEM), BeeUtils.unbox(row.getInt(COL_WAREHOUSE_REMAINDER)));
      }
    }
    return Pair.of(stocks, warehouseData.getB());
  }

  private ResponseObject openShoppingCart(RequestInfo reqInfo) {
    String cartName = reqInfo.getParameter(COL_SHOPPING_CART_NAME);
    Long user = usr.getCurrentUserId();

    SqlSelect select = new SqlSelect()
            .addFields(TBL_NOT_SUBMITTED_ORDERS, COL_SHOPPING_CART_CREATED,
                COL_SHOPPING_CART_CLIENT, COL_SHOPPING_CART_ITEM, COL_SHOPPING_CART_QUANTITY)
            .addFrom(TBL_NOT_SUBMITTED_ORDERS)
            .setWhere(SqlUtils.equals(TBL_NOT_SUBMITTED_ORDERS, COL_SHOPPING_CART_CLIENT, user,
                COL_SHOPPING_CART_NAME, cartName));

    for (SimpleRow row : qs.getData(select)) {
      SqlInsert insert = new SqlInsert(TBL_ORD_EC_SHOPPING_CARTS)
          .addConstant(COL_SHOPPING_CART_CREATED, row.getDateTime(COL_SHOPPING_CART_CREATED))
          .addConstant(COL_SHOPPING_CART_ITEM, row.getLong(COL_SHOPPING_CART_ITEM))
          .addConstant(COL_SHOPPING_CART_QUANTITY, row.getInt(COL_SHOPPING_CART_QUANTITY))
          .addConstant(COL_SHOPPING_CART_CLIENT, row.getLong(COL_SHOPPING_CART_CLIENT));

      ResponseObject insertResponse = qs.insertDataWithResponse(insert);
      if (insertResponse.hasErrors()) {
        return insertResponse;
      }
    }

    ResponseObject updateResponse = qs.updateDataWithResponse(
        new SqlDelete(TBL_NOT_SUBMITTED_ORDERS).setWhere(SqlUtils.equals(TBL_NOT_SUBMITTED_ORDERS,
            COL_SHOPPING_CART_CLIENT, user, COL_SHOPPING_CART_NAME, cartName)));

    if (updateResponse.hasErrors()) {
      return updateResponse;
    }

    return ResponseObject.emptyResponse();
  }

  private Document orderToHtml(List<BeeColumn> orderColumns, BeeRow orderRow,
      Dictionary constants) {

    String clientFirstName = orderRow.getString(DataUtils.getColumnIndex(ALS_CONTACT_FIRST_NAME,
        orderColumns));
    String clientLastName = orderRow.getString(DataUtils.getColumnIndex(ALS_CONTACT_LAST_NAME,
        orderColumns));
    String clientCompanyName = BeeUtils.joinWords(orderRow.getString(DataUtils.getColumnIndex(
        ALS_COMPANY_NAME, orderColumns)), orderRow.getString(DataUtils.getColumnIndex(
        ProjectConstants.ALS_COMPANY_TYPE_NAME, orderColumns)));

    DateTime date = orderRow.getDateTime(DataUtils.getColumnIndex(COL_START_DATE, orderColumns));
    OrdersStatus status = OrdersStatus.get(orderRow.getInteger(DataUtils.getColumnIndex(
        COL_ORDERS_STATUS, orderColumns)));

    String managerFirstName = orderRow.getString(DataUtils.getColumnIndex(
        EcConstants.ALS_ORDER_MANAGER_FIRST_NAME, orderColumns));
    String managerLastName = orderRow.getString(DataUtils.getColumnIndex(
        EcConstants.ALS_ORDER_MANAGER_LAST_NAME, orderColumns));

    String clientComment = orderRow.getString(DataUtils.getColumnIndex(
        ClassifierConstants.COL_NOTES, orderColumns));

    SqlSelect itemQuery = new SqlSelect()
        .addFields(TBL_ORDER_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_ITEM_PRICE)
        .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_ARTICLE)
        .addField(TBL_UNITS, COL_UNIT_NAME, "UnitName")
        .addFrom(TBL_ORDER_ITEMS)
        .addFromInner(TBL_ITEMS,
            sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
        .addFromLeft(TBL_UNITS,
            sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
        .setWhere(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, orderRow.getId()))
        .addOrder(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS));

    SimpleRowSet itemData = qs.getData(itemQuery);

    double totalAmount = BeeConst.DOUBLE_ZERO;

    if (!DataUtils.isEmpty(itemData)) {
      for (SimpleRow itemRow : itemData) {
        Integer quantity = itemRow.getInt(COL_TRADE_ITEM_QUANTITY);
        Double price = itemRow.getDouble(COL_ITEM_PRICE);

        totalAmount += BeeUtils.unbox(quantity) * BeeUtils.unbox(price);
      }
    }

    Document doc = new Document();

    doc.getHead().append(meta().encodingDeclarationUtf8(), title().text(constants.ecOrder()));

    Div panel = div().backgroundColor(Colors.WHITESMOKE);
    doc.getBody().append(panel);

    String customer = EcUtils.formatPerson(clientFirstName, clientLastName, clientCompanyName);
    panel.append(h3().text(customer));

    Tbody fields = tbody().append(
        tr().append(
            td().text(constants.ecOrderSubmissionDate()),
            td().text(TimeUtils.renderCompact(date)),
            td().text(constants.order()),
            td().text(BeeUtils.toString(orderRow.getId()))));

    Tr tr = tr().append(
        td().text(constants.ecOrderStatus()),
        td().text((status == null) ? null : status.getCaption(constants)),
        td().text(constants.comment()),
        td().text(clientComment));
    fields.append(tr);

    fields.append(
        tr().append(
            td().text(constants.ecManager()),
            td().text(BeeUtils.joinWords(managerFirstName, managerLastName)),
            td().text(BeeUtils.joinWords(constants.ecOrderAmount(), EcConstants.CURRENCY)),
            td().text(EcUtils.formatCents(EcUtils.toCents(totalAmount))).alignRight()
                .fontWeight(FontWeight.BOLD).fontSize(FontSize.LARGER)));

    List<Element> cells = fields.queryTag(Tags.TD);
    for (Element cell : cells) {
      cell.setPaddingLeft(10, CssUnit.PX);
      cell.setPaddingRight(10, CssUnit.PX);

      cell.setPaddingTop(3, CssUnit.PX);
      cell.setPaddingBottom(3, CssUnit.PX);

      int index = cell.index();
      if (index % 2 == 0) {
        cell.setTextAlign(TextAlign.RIGHT);

      } else {
        cell.setMinWidth(120, CssUnit.PX);
        cell.setMaxWidth(200, CssUnit.PX);

        cell.setBorderWidth(1, CssUnit.PX);
        cell.setBorderStyle(BorderStyle.SOLID);
        cell.setBorderColor("#ccc");

        cell.setWhiteSpace(WhiteSpace.PRE_LINE);
        cell.setBackground(Colors.WHITE);

        Td td = (Td) cell;
        if (td.size() == 1 && td.hasText()) {
          Text textNode = (Text) td.getFirstChild();
          if (textNode.isEmpty()) {
            textNode.setText(BeeConst.STRING_MINUS);
          }
        }
      }
    }

    panel.append(table().append(fields));

    if (!DataUtils.isEmpty(itemData)) {
      panel.append(div()
          .fontWeight(FontWeight.BOLDER)
          .marginTop(2, CssUnit.EX)
          .marginBottom(1, CssUnit.EX)
          .text(BeeUtils.joinWords(constants.ecOrderItems(),
              BeeUtils.bracket(itemData.getNumberOfRows()))));

      Tbody items = tbody().append(tr().textAlign(TextAlign.CENTER).fontWeight(FontWeight.BOLDER)
          .append(
              td().text(constants.ecItemName()),
              td().text(constants.article()),
              td().text(constants.ecItemQuantity()),
              td().text(constants.ecItemPrice()),
              td().text(constants.total())));

      int i = 0;
      for (SimpleRow itemRow : itemData) {
        String rowBackground = (i % 2 == 1) ? "#f5f5f5" : "#ebebeb";

        int quantity = BeeUtils.unbox(itemRow.getInt(COL_TRADE_ITEM_QUANTITY));
        double price = BeeUtils.unbox(itemRow.getDouble(COL_ITEM_PRICE));

        String quantityColor = Colors.GREEN;

        items.append(tr().backgroundColor(rowBackground).append(
            td().text(itemRow.getValue(COL_ITEM_NAME)),
            td().text(itemRow.getValue(COL_ITEM_ARTICLE)),
            td().text(EcUtils.format(quantity)).alignRight()
                .fontWeight(FontWeight.BOLDER).color(quantityColor),
            td().text(EcUtils.formatCents(EcUtils.toCents(price))).alignRight(),
            td().text(EcUtils.formatCents(EcUtils.toCents(quantity * price))).alignRight()));

        i++;
      }

      cells = items.queryTag(Tags.TD);
      for (Element cell : cells) {
        cell.setPaddingLeft(10, CssUnit.PX);
        cell.setPaddingRight(10, CssUnit.PX);

        cell.setPaddingTop(3, CssUnit.PX);
        cell.setPaddingBottom(3, CssUnit.PX);

        cell.setBorderWidth(1, CssUnit.PX);
        cell.setBorderStyle(BorderStyle.SOLID);
        cell.setBorderColor("#ddd");
      }

      panel.append(table().borderCollapse().marginLeft(1, CssUnit.EM).append(items));
    }

    return doc;
  }

  private ResponseObject saveOrder(RequestInfo reqInfo) {
    String cartName = reqInfo.getParameter(COL_SHOPPING_CART_NAME);
    String comment = reqInfo.getParameter(COL_SHOPPING_CART_COMMENT);
    Long user = usr.getCurrentUserId();

    SqlSelect select = new SqlSelect()
            .addFields(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CREATED, COL_SHOPPING_CART_ITEM,
                COL_SHOPPING_CART_QUANTITY)
            .addFrom(TBL_ORD_EC_SHOPPING_CARTS)
            .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, user));

    DateTime time = TimeUtils.nowMillis();

    for (SimpleRow row : qs.getData(select)) {
      SqlInsert insert = new SqlInsert(TBL_NOT_SUBMITTED_ORDERS)
          .addConstant(COL_TRADE_DATE, time)
          .addConstant(COL_SHOPPING_CART_CREATED, row.getDateTime(COL_SHOPPING_CART_CREATED))
          .addConstant(COL_SHOPPING_CART_NAME, cartName)
          .addConstant(COL_SHOPPING_CART_ITEM, row.getLong(COL_SHOPPING_CART_ITEM))
          .addConstant(COL_SHOPPING_CART_QUANTITY, row.getInt(COL_SHOPPING_CART_QUANTITY))
          .addConstant(COL_SHOPPING_CART_COMMENT, comment)
          .addConstant(COL_SHOPPING_CART_CLIENT, user);

      ResponseObject insertResponse = qs.insertDataWithResponse(insert);
      if (insertResponse.hasErrors()) {
        return insertResponse;
      }
    }

    ResponseObject updateResponse =
        qs.updateDataWithResponse(new SqlDelete(TBL_ORD_EC_SHOPPING_CARTS)
            .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, user)));

    if (updateResponse.hasErrors()) {
      return updateResponse;
    }

    return ResponseObject.emptyResponse();
  }

  private ResponseObject submitOrder(RequestInfo reqInfo) {
    String serializedCart = reqInfo.getParameter(EcConstants.VAR_CART);
    if (BeeUtils.isEmpty(serializedCart)) {
      return ResponseObject.parameterNotFound(SVC_SUBMIT_ORDER, EcConstants.VAR_CART);
    }

    boolean copyByMail = reqInfo.hasParameter(EcConstants.VAR_MAIL);

    Long currency = prm.getRelation(PRM_CURRENCY);
    if (!DataUtils.isId(currency)) {
      return ResponseObject.parameterNotFound(SVC_SUBMIT_ORDER, PRM_CURRENCY);
    }

    OrdEcCart cart = OrdEcCart.restore(serializedCart);
    if (cart == null || cart.isEmpty()) {
      String message = BeeUtils.joinWords(SVC_SUBMIT_ORDER, "cart deserialization failed");
      logger.severe(message);
      return ResponseObject.error(message);
    }

    String colClientId = sys.getIdName(TBL_COMPANIES);
    SimpleRow clientInfo = getCurrentClientInfo(colClientId, COL_EC_MANAGER, COL_EC_WAREHOUSE);
    if (clientInfo == null) {
      String message = BeeUtils.joinWords(SVC_SUBMIT_ORDER, "client not found for user",
          usr.getCurrentUserId());
      logger.severe(message);
      return ResponseObject.error(message);
    }

    SqlInsert insOrder = new SqlInsert(TBL_ORDERS);

    insOrder.addConstant(COL_ORDERS_STATUS, OrdersStatus.NEW.ordinal());
    insOrder.addConstant(COL_START_DATE, TimeUtils.nowMinutes());
    insOrder.addConstant(COL_COMPANY, clientInfo.getLong(colClientId));
    insOrder.addConstant(COL_CURRENCY, currency);

    Long contact = usr.getCompanyPerson(usr.getCurrentUserId());
    if (contact != null) {
      insOrder.addConstant(COL_CONTACT, contact);
    }

    Long manager = clientInfo.getLong(COL_EC_MANAGER);
    if (manager != null) {
      insOrder.addConstant(COL_TRADE_MANAGER, manager);
    }

    Long warehouse = clientInfo.getLong(COL_EC_WAREHOUSE);
    if (warehouse != null) {
      insOrder.addConstant(COL_WAREHOUSE, warehouse);
    }

    if (!BeeUtils.isEmpty(cart.getComment())) {
      insOrder.addConstant(ClassifierConstants.COL_NOTES, cart.getComment());
    }

    ResponseObject response = qs.insertDataWithResponse(insOrder);
    if (response.hasErrors() || !response.hasResponse(Long.class)) {
      return response;
    }

    Long orderId = (Long) response.getResponse();

    SqlInsert insUsage = new SqlInsert(TBL_ORD_EC_USAGE)
        .addConstant(COL_ORDER, orderId)
        .addConstant(COL_USER, usr.getCurrentUserId())
        .addConstant(NewsConstants.COL_USAGE_UPDATE, TimeUtils.nowMillis());

    qs.insertData(insUsage);

    for (OrdEcCartItem cartItem : cart.getItems()) {
      SqlInsert insItem = new SqlInsert(TBL_ORDER_ITEMS);

      insItem.addConstant(COL_ORDER, orderId);
      insItem.addConstant(COL_ITEM, cartItem.getEcItem().getId());
      insItem.addConstant(COL_TRADE_ITEM_QUANTITY, cartItem.getQuantity());
      insItem.addConstant(COL_ITEM_PRICE, cartItem.getEcItem().getPrice() / 100d);
      insItem.addConstant(COL_ITEM_CURRENCY, currency);

      ResponseObject itemResponse = qs.insertDataWithResponse(insItem);
      if (itemResponse.hasErrors()) {
        return itemResponse;
      }
    }

    qs.updateData(new SqlDelete(TBL_ORD_EC_SHOPPING_CARTS)
        .setWhere(SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS,
            COL_SHOPPING_CART_CLIENT, clientInfo.getLong(colClientId))));

    if (copyByMail) {
      ResponseObject mailResponse = mailOrder(orderId);
      response.addMessagesFrom(mailResponse);
    }

    return response;
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
        SqlUpdate upd = new SqlUpdate(VIEW_ORD_EC_CONFIGURATION)
            .addConstant(column, value)
            .setWhere(SqlUtils.equals(VIEW_ORD_EC_CONFIGURATION,
                sys.getIdName(VIEW_ORD_EC_CONFIGURATION), rowSet.getRow(0).getId()));

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

    Long user = usr.getCurrentUserId();
    if (!DataUtils.isId(user)) {
      return ResponseObject.emptyResponse();
    }

    IsCondition where = SqlUtils.equals(TBL_ORD_EC_SHOPPING_CARTS, COL_SHOPPING_CART_CLIENT, user,
        COL_SHOPPING_CART_ITEM, itemId);

    if (BeeUtils.isPositive(quantity)) {
      if (qs.sqlExists(TBL_ORD_EC_SHOPPING_CARTS, where)) {
        qs.updateData(new SqlUpdate(TBL_ORD_EC_SHOPPING_CARTS)
            .addConstant(COL_SHOPPING_CART_QUANTITY, quantity)
            .setWhere(where));
      } else {
        qs.insertData(new SqlInsert(TBL_ORD_EC_SHOPPING_CARTS)
            .addConstant(COL_SHOPPING_CART_CREATED, System.currentTimeMillis())
            .addConstant(COL_SHOPPING_CART_CLIENT, user)
            .addConstant(COL_SHOPPING_CART_ITEM, itemId)
            .addConstant(COL_SHOPPING_CART_QUANTITY, quantity));
      }

    } else {
      qs.updateData(new SqlDelete(TBL_ORD_EC_SHOPPING_CARTS).setWhere(where));
    }

    return ResponseObject.response(itemId);
  }

  private ResponseObject uploadBanners(RequestInfo reqInfo) {
    String picture = reqInfo.getParameter(COL_BANNER_PICTURE);
    if (BeeUtils.isEmpty(picture)) {
      return ResponseObject.parameterNotFound(SVC_UPLOAD_BANNERS, COL_BANNER_PICTURE);
    }

    return qs.insertDataWithResponse(new SqlInsert(TBL_ORD_EC_BANNERS)
        .addConstant(COL_BANNER_PICTURE, picture));
  }
}