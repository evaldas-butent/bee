package com.butent.bee.server.modules.transport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.concurrency.ConcurrencyBean.HasTimerService;
import com.butent.bee.server.data.DataEditorBean;
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
import com.butent.bee.server.modules.administration.ExtensionIcons;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.news.ExtendedUsageQueryProvider;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.server.websocket.Endpoint;
import com.butent.bee.shared.Assert;
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
import com.butent.bee.shared.data.SqlConstants.SqlFunction;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.modules.transport.TransportUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.HeadlineProducer;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.websocket.messages.ModificationMessage;
import com.butent.webservice.ButentWS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;

@Stateless
@LocalBean
public class TransportModuleBean implements BeeModule, HasTimerService {

  private static BeeLogger logger = LogUtils.getLogger(TransportModuleBean.class);

  @EJB
  DataEditorBean deb;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  ParamHolderBean prm;
  @EJB
  TradeModuleBean trd;
  @EJB
  NewsBean news;
  @EJB
  TransportReportsBean rep;
  @EJB
  MailModuleBean mail;
  @EJB
  ConcurrencyBean cb;

  @Resource
  TimerService timerService;
  @Resource
  EJBContext ctx;

  private static IsExpression getAssessmentTurnoverExpression(SqlSelect query, String source,
      String defDateSource, String defDateAlias, Long currency, boolean woVat) {

    IsExpression amountExpr = SqlUtils.field(source, COL_AMOUNT);

    if (woVat) {
      amountExpr = TradeModuleBean.getWithoutVatExpression(source, amountExpr);
    } else {
      amountExpr = TradeModuleBean.getTotalExpression(source, amountExpr);
    }
    if (DataUtils.isId(currency)) {
      return ExchangeUtils.exchangeFieldTo(query, amountExpr, SqlUtils.field(source, COL_CURRENCY),
          SqlUtils.nvl(SqlUtils.field(source, COL_DATE),
              SqlUtils.field(defDateSource, defDateAlias)),
          SqlUtils.constant(currency));

    } else {
      return ExchangeUtils.exchangeField(query, amountExpr, SqlUtils.field(source, COL_CURRENCY),
          SqlUtils.nvl(SqlUtils.field(source, COL_DATE),
              SqlUtils.field(defDateSource, defDateAlias)));
    }
  }

  private static IsCondition getChartTripCondition(Range<?> period, Boolean completed) {
    HasConditions conditions = SqlUtils.and();

    if (period != null) {
      conditions.add(SqlUtils.anyIntersects(TBL_TRIPS, TRIP_DATE_COLUMNS, period));
    }

    if (!BeeUtils.isTrue(completed)) {
      conditions.add(SqlUtils.notEqual(TBL_TRIPS, COL_TRIP_STATUS, TripStatus.COMPLETED));
    }

    return conditions.isEmpty() ? null : conditions;
  }

  private static Filter getChartTripFilter(Range<Value> period, Boolean completed) {
    CompoundFilter filter = Filter.and();

    if (period != null) {
      filter.add(Filter.anyIntersects(TRIP_DATE_COLUMNS, period));
    }

    if (!BeeUtils.isTrue(completed)) {
      filter.add(Filter.notEquals(COL_TRIP_STATUS, TripStatus.COMPLETED));
    }

    return filter.isEmpty() ? null : filter;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();

    List<SearchResult> vehiclesResult = qs.getSearchResults(VIEW_VEHICLES,
        Filter.anyContains(Sets.newHashSet(COL_NUMBER, COL_PARENT_MODEL_NAME, COL_MODEL_NAME,
            COL_OWNER_NAME), query));

    List<SearchResult> orderCargoResult = qs.getSearchResults(VIEW_ORDER_CARGO,
        Filter.anyContains(Sets.newHashSet(COL_CARGO_DESCRIPTION,
            COL_NUMBER, ALS_CARGO_CMR_NUMBER, ALS_CARGO_NOTES, COL_CARGO_DIRECTIONS,
            ALS_LOADING_NUMBER, ALS_LOADING_CONTACT, ALS_LOADING_COMPANY, ALS_LOADING_ADDRESS,
            ALS_LOADING_POST_INDEX, ALS_LOADING_CITY_NAME, ALS_LOADING_COUNTRY_NAME,
            ALS_LOADING_COUNTRY_CODE, ALS_UNLOADING_NUMBER, ALS_UNLOADING_CONTACT,
            ALS_UNLOADING_COMPANY, ALS_UNLOADING_ADDRESS, ALS_UNLOADING_POST_INDEX,
            ALS_UNLOADING_CITY_NAME, ALS_UNLOADING_COUNTRY_NAME, ALS_UNLOADING_COUNTRY_CODE),
            query));

    result.addAll(vehiclesResult);
    result.addAll(orderCargoResult);

    if (usr.isModuleVisible(ModuleAndSub.of(Module.TRANSPORT, SubModule.LOGISTICS))) {
      result.addAll(qs.getSearchResults(VIEW_ASSESSMENTS,
          Filter.compareId(BeeUtils.toLong(query))));
    }

    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response;

    if (BeeUtils.same(svc, SVC_GET_BEFORE)) {
      response = getTripBeforeData(BeeUtils.toLong(reqInfo.getParameter(COL_VEHICLE)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_DATE)));

    } else if (BeeUtils.same(svc, SVC_GET_UNASSIGNED_CARGOS)) {
      response = getUnassignedCargos(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ROUTE)) {
      String tmp = rep.getTripRoutes(new SqlSelect()
          .addExpr(SqlUtils.constant(BeeUtils.toLong(reqInfo.getParameter(COL_TRIP))), COL_TRIP));

      response = ResponseObject.response(qs.getValue(new SqlSelect()
          .addFields(tmp, COL_TRIP_ROUTE)
          .addFrom(tmp)));

      qs.sqlDropTemp(tmp);

    } else if (BeeUtils.same(svc, SVC_GENERATE_DAILY_COSTS)) {
      response = generateDailyCosts(BeeUtils.toLong(reqInfo.getParameter(COL_TRIP)));

    } else if (BeeUtils.same(svc, SVC_GENERATE_ROUTE)) {
      response = generateTripRoute(BeeUtils.toLong(reqInfo.getParameter(COL_TRIP)));

    } else if (BeeUtils.same(svc, SVC_GET_FX_DATA)) {
      response = getFxData();

    } else if (BeeUtils.same(svc, SVC_GET_SS_DATA)) {
      response = getVehicleTbData(svc, null, true, VehicleType.TRUCK,
          COL_SS_THEME, COL_SS_MIN_DATE, COL_SS_MAX_DATE,
          COL_SS_TRANSPORT_GROUPS, COL_SS_COMPLETED_TRIPS);

    } else if (BeeUtils.same(svc, SVC_GET_DTB_DATA)) {
      response = getDtbData();

    } else if (BeeUtils.same(svc, SVC_GET_TRUCK_TB_DATA)) {
      response = getVehicleTbData(svc, Filter.notNull(COL_IS_TRUCK), false, VehicleType.TRUCK,
          COL_TRUCK_THEME, COL_TRUCK_MIN_DATE, COL_TRUCK_MAX_DATE,
          COL_TRUCK_TRANSPORT_GROUPS, COL_TRUCK_COMPLETED_TRIPS);

    } else if (BeeUtils.same(svc, SVC_GET_TRAILER_TB_DATA)) {
      response = getVehicleTbData(svc, Filter.notNull(COL_IS_TRAILER), false, VehicleType.TRAILER,
          COL_TRAILER_THEME, COL_TRAILER_MIN_DATE, COL_TRAILER_MAX_DATE,
          COL_TRAILER_TRANSPORT_GROUPS, COL_TRAILER_COMPLETED_TRIPS);

    } else if (BeeUtils.same(svc, SVC_GET_COLORS)) {
      response = getColors(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CARGO_USAGE)) {
      response = getCargoUsage(reqInfo.getParameter(Service.VAR_VIEW_NAME),
          DataUtils.parseIdList(reqInfo.getParameter(Service.VAR_VIEW_ROW_ID)),
          reqInfo.getParameter(Service.VAR_COLUMN));

    } else if (BeeUtils.same(svc, SVC_GET_ASSESSMENT_TOTALS)) {
      response = getAssessmentTotals(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ASSESSMENT)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CURRENCY)),
          BeeUtils.toBoolean(reqInfo.getParameter("isPrimary")));

    } else if (BeeUtils.same(svc, SVC_GET_ASSESSMENT_QUANTITY_REPORT)) {
      response = getAssessmentQuantityReport(reqInfo);
    } else if (BeeUtils.same(svc, SVC_GET_ASSESSMENT_TURNOVER_REPORT)) {
      response = getAssessmentTurnoverReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_CREATE_INVOICE_ITEMS)) {
      Long saleId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SALE));
      Long purchaseId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_PURCHASE));
      Long currency = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CURRENCY));
      Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(Service.VAR_DATA));
      Long item = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ITEM));
      Double creditAmount = BeeUtils.toDoubleOrNull(reqInfo.getParameter(COL_TRADE_AMOUNT));

      if (DataUtils.isId(saleId)) {
        response = createInvoiceItems(saleId, currency, ids, item);
      } else if (BeeUtils.isPositive(creditAmount)) {
        response = createCreditInvoiceItems(purchaseId, currency, ids, item, creditAmount);

      } else if (BeeUtils.same(reqInfo.getParameter(Service.VAR_TABLE), TBL_TRIP_COSTS)) {
        response = createTripInvoiceItems(purchaseId, currency, ids, item);
      } else {
        response = createPurchaseInvoiceItems(purchaseId, currency, ids, item);
      }
    } else if (BeeUtils.same(svc, SVC_SEND_MESSAGE)) {
      response = sendMessage(reqInfo.getParameter(COL_DESCRIPTION),
          Codec.beeDeserializeCollection(reqInfo.getParameter(COL_MOBILE)));

    } else if (BeeUtils.same(svc, SVC_GET_CREDIT_INFO)) {
      response = getCreditInfo(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CARGO_TOTAL)) {
      response = getCargoTotal(BeeUtils.toLong(reqInfo.getParameter(COL_CARGO)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CURRENCY)));

    } else if (BeeUtils.same(svc, SVC_UPDATE_PERCENT)) {
      response = updatePercent(reqInfo.getParameterLong(COL_CARGO_TRIP),
          reqInfo.getParameter(Service.VAR_COLUMN),
          reqInfo.getParameterDouble(COL_AMOUNT),
          BeeUtils.toBoolean(reqInfo.getParameter(Service.VAR_CHECK)));

    } else if (BeeUtils.same(svc, SVC_TRIP_PROFIT_REPORT)) {
      response = rep.getTripProfitReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_FUEL_USAGE_REPORT)) {
      response = rep.getFuelUsageReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_REPAIRS)) {
      response = getVehicleRepairs(reqInfo.getParameter(COL_ITEM_EXTERNAL_CODE));

    } else if (BeeUtils.same(svc, SVC_CHECK_CARGO_HANDLING)) {
      response = checkCargoHandling(reqInfo.getParameter("column"), reqInfo.getParameter("value"));

    } else if (BeeUtils.same(svc, SVC_COSTS_TO_ERP)) {
      response = costsToERP(DataUtils.parseIdSet(reqInfo.getParameter(VAR_ID_LIST)));

    } else if (BeeUtils.same(svc, SVC_GET_VEHICLE_BUSY_DATES)) {
      response = getVehicleBusyDates(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_VEHICLE)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRAILER)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRIP_DATE_FROM)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRIP_DATE_TO)));

    } else if (BeeUtils.same(svc, SVC_GET_DRIVER_BUSY_DATES)) {
      response = getDriverBusyDates(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_DRIVER)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRIP_DATE_FROM)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRIP_DATE_TO)));

    } else if (BeeUtils.same(svc, SVC_CREATE_USER)) {
      response = createUser(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_TRIP_INFO)) {
      response = rep.getTripInfo(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_TEXT_CONSTANT)) {
      response = getTextConstant(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Transport service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  private ResponseObject checkCargoHandling(String column, String value) {
    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_CARGO_TRIPS, COL_CARGO)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_CARGO_TRIPS, COL_TRIP))
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .setWhere(SqlUtils.equals(SqlUtils.name(column), value));

    SimpleRowSet rs = qs.getData(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_CARGO_TRIPS, COL_CARGO)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(new SqlSelect()
                .addFields(TBL_CARGO_TRIPS, COL_CARGO)
                .addFrom(TBL_CARGO_TRIPS)
                .addFromInner(query, "qq", SqlUtils.joinUsing(TBL_CARGO_TRIPS, "qq", COL_CARGO))
                .addGroup(TBL_CARGO_TRIPS, COL_CARGO)
                .setHaving(SqlUtils.more(SqlUtils.aggregate(SqlFunction.COUNT,
                    SqlUtils.field(TBL_CARGO_TRIPS, COL_TRIP)), 1)), "subq",
            SqlUtils.joinUsing(TBL_CARGO_TRIPS, "subq", COL_CARGO))
        .addFromLeft(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_TRIPS, TBL_CARGO_HANDLING, COL_CARGO_TRIP))
        .setWhere(SqlUtils.isNull(TBL_CARGO_HANDLING, COL_CARGO_TRIP)));

    return ResponseObject.response(rs.getColumn(COL_CARGO));
  }

  @Override
  public void ejbTimeout(Timer timer) {
    if (cb.isParameterTimer(timer, PRM_SYNC_ERP_VEHICLES)) {
      importVehicles();
    } else if (cb.isParameterTimer(timer, PRM_SYNC_ERP_EMPLOYEES)) {
      importEmployees();
    }
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    return Lists.newArrayList(
        BeeParameter.createBoolean(module, PRM_EXCLUDE_VAT),
        BeeParameter.createText(module, PRM_TRIP_PREFIX, true, null),
        BeeParameter.createRelation(module, PRM_INVOICE_PREFIX, true, TBL_SALES_SERIES,
            COL_SERIES_NAME),
        BeeParameter.createMap(module, PRM_MESSAGE_TEMPLATE, true, null),
        BeeParameter.createText(module, "SmsServiceAddress"),
        BeeParameter.createText(module, "SmsUserName"),
        BeeParameter.createText(module, "SmsPassword"),
        BeeParameter.createText(module, "SmsServiceId"),
        BeeParameter.createText(module, "SmsDisplayText"),
        BeeParameter.createRelation(module, PRM_SELF_SERVICE_ROLE, TBL_ROLES, COL_ROLE_NAME),
        BeeParameter.createRelation(module, PRM_CARGO_TYPE, true, TBL_CARGO_TYPES,
            COL_CARGO_TYPE_NAME),
        BeeParameter.createRelation(module, PRM_CARGO_SERVICE, TBL_SERVICES, COL_SERVICE_NAME),
        BeeParameter.createText(module, PRM_SYNC_ERP_VEHICLES),
        BeeParameter.createText(module, PRM_SYNC_ERP_EMPLOYEES),
        BeeParameter.createBoolean(module, PRM_BIND_EXPENSES_TO_INCOMES, false, true));
  }

  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  @Override
  public Module getModule() {
    return Module.TRANSPORT;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    cb.createCalendarTimer(this.getClass(), PRM_SYNC_ERP_VEHICLES);
    cb.createCalendarTimer(this.getClass(), PRM_SYNC_ERP_EMPLOYEES);

    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      @AllowConcurrentEvents
      public void calcAssessmentAmounts(ViewQueryEvent event) {
        if (event.isAfter(VIEW_CHILD_ASSESSMENTS) && event.hasData()) {
          for (String tbl : new String[] {TBL_CARGO_INCOMES, TBL_CARGO_EXPENSES}) {
            SqlSelect query = new SqlSelect()
                .addField(TBL_ASSESSMENTS, sys.getIdName(TBL_ASSESSMENTS), COL_ASSESSMENT)
                .addFrom(TBL_ASSESSMENTS)
                .addFromInner(TBL_ORDER_CARGO,
                    sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS, COL_CARGO))
                .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
                .addFromInner(tbl, SqlUtils.joinUsing(TBL_ASSESSMENTS, tbl, COL_CARGO))
                .setWhere(sys.idInList(TBL_ASSESSMENTS, event.getRowset().getRowIds()))
                .addGroup(TBL_ASSESSMENTS, sys.getIdName(TBL_ASSESSMENTS));

            IsExpression amountExpr = SqlUtils.field(tbl, COL_AMOUNT);

            if (BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT))) {
              amountExpr = TradeModuleBean.getWithoutVatExpression(tbl, amountExpr);
            } else {
              amountExpr = TradeModuleBean.getTotalExpression(tbl, amountExpr);
            }
            IsExpression xpr = ExchangeUtils.exchangeFieldTo(query, amountExpr,
                SqlUtils.field(tbl, COL_CURRENCY),
                SqlUtils.nvl(SqlUtils.field(tbl, COL_DATE), SqlUtils.field(TBL_ORDERS, COL_DATE)),
                SqlUtils.field(TBL_ORDER_CARGO, COL_CURRENCY));

            SimpleRowSet rs = qs.getData(query.addSum(xpr, VAR_TOTAL));

            for (BeeRow row : event.getRowset().getRows()) {
              row.setProperty((BeeUtils.same(tbl, TBL_CARGO_INCOMES)) ? VAR_INCOME : VAR_EXPENSE,
                  rs.getValueByKey(COL_ASSESSMENT, BeeUtils.toString(row.getId()), VAR_TOTAL));
            }
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillCargoIncomes(ViewQueryEvent event) {
        if (event.isAfter(VIEW_ORDER_CARGO, VIEW_ALL_CARGO) && event.hasData()) {
          BeeRowSet rowSet = event.getRowset();
          Collection<Long> cargoIds;
          Function<BeeRow, Long> valueSupplier;

          switch (event.getTargetName()) {
            case VIEW_ORDER_CARGO:
              cargoIds = rowSet.getRowIds();
              valueSupplier = BeeRow::getId;
              break;
            case VIEW_ALL_CARGO:
              int idx = rowSet.getColumnIndex(COL_CARGO);
              cargoIds = rowSet.getDistinctLongs(idx);
              valueSupplier = row -> row.getLong(idx);
              break;
            default:
              return;
          }
          SimpleRowSet rs = qs.getData(rep.getCargoIncomeQuery(new SqlSelect()
                  .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
                  .addFrom(TBL_ORDER_CARGO)
                  .setWhere(sys.idInList(TBL_ORDER_CARGO, cargoIds)),
              prm.getRelation(PRM_CURRENCY), BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT))));

          for (BeeRow row : rowSet.getRows()) {
            SimpleRow r = rs.getRowByKey(COL_CARGO, BeeUtils.toString(valueSupplier.apply(row)));

            row.setProperty(VAR_INCOME, BeeUtils.toString(BeeUtils.unbox(r.getDouble("CargoIncome"))
                + BeeUtils.unbox(r.getDouble("ServicesIncome"))));
            row.setProperty(COL_TRADE_VAT, BeeUtils.toString(BeeUtils.unbox(r.getDouble("CargoVat"))
                + BeeUtils.unbox(r.getDouble("ServicesVat"))));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillFuelConsumptions(ViewQueryEvent event) {
        if (event.isAfter(TBL_TRIP_ROUTES) && event.hasData()) {
          BeeRowSet rowset = event.getRowset();
          int colIndex = DataUtils.getColumnIndex(COL_ROUTE_CONSUMPTION, rowset.getColumns());

          if (BeeConst.isUndef(colIndex)) {
            return;
          }
          SimpleRowSet rs = qs.getData(rep.getFuelConsumptionsQuery(event.getQuery()
              .resetFields().resetOrder().resetGroup()
              .addFields(TBL_TRIP_ROUTES, sys.getIdName(TBL_TRIP_ROUTES))
              .addGroup(TBL_TRIP_ROUTES, sys.getIdName(TBL_TRIP_ROUTES)), true));

          for (BeeRow row : rowset.getRows()) {
            row.setValue(colIndex, rs.getValueByKey(sys.getIdName(TBL_TRIP_ROUTES),
                BeeUtils.toString(row.getId()), COL_ROUTE_CONSUMPTION));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillPercents(ViewQueryEvent event) {
        if (event.isAfter(VIEW_TRIP_CARGO, VIEW_CARGO_TRIPS) && event.hasData()) {
          BeeRowSet rowset = event.getRowset();
          int cargoIndex = rowset.getColumnIndex(COL_CARGO);
          int tripIndex = rowset.getColumnIndex(COL_TRIP);

          if (BeeConst.isUndef(cargoIndex) || BeeConst.isUndef(tripIndex)) {
            return;
          }
          SqlSelect trips = null;

          for (Long tripId : rowset.getDistinctLongs(tripIndex)) {
            SqlSelect subQuery = new SqlSelect().addConstant(tripId, COL_TRIP);

            if (Objects.isNull(trips)) {
              trips = subQuery;
            } else {
              trips.addUnion(subQuery);
            }
          }
          Table<Long, Long, Pair<String, String>> amounts = HashBasedTable.create();

          String crs = rep.getTripIncomes(trips, prm.getRelation(PRM_CURRENCY),
              BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT)));
          SimpleRowSet rs = qs.getData(new SqlSelect().addAllFields(crs).addFrom(crs));
          qs.sqlDropTemp(crs);

          for (SimpleRow row : rs) {
            amounts.put(row.getLong(COL_TRIP), row.getLong(COL_CARGO),
                Pair.of(BeeUtils.round(BeeUtils.nvl(row.getValue("TripIncome"), "0"), 2) + " ("
                    + BeeUtils.removeTrailingZeros(BeeUtils.round(row.getValue(COL_TRIP_PERCENT),
                    2)) + "%)", null));
          }
          crs = rep.getCargoTripPercents(COL_TRIP, trips);
          rs = qs.getData(new SqlSelect().addAllFields(crs).addFrom(crs));
          qs.sqlDropTemp(crs);

          for (SimpleRow row : rs) {
            if (!amounts.contains(row.getLong(COL_TRIP), row.getLong(COL_CARGO))) {
              amounts.put(row.getLong(COL_TRIP), row.getLong(COL_CARGO), Pair.empty());
            }
            amounts.get(row.getLong(COL_TRIP), row.getLong(COL_CARGO))
                .setB(BeeUtils.removeTrailingZeros(BeeUtils.round(row.getValue(COL_CARGO_PERCENT),
                    2)) + "%");
          }
          for (BeeRow row : rowset.getRows()) {
            Pair<String, String> p = amounts.get(row.getLong(tripIndex), row.getLong(cargoIndex));
            row.setProperty(VAR_INCOME, p.getA());
            row.setProperty(VAR_EXPENSE, p.getB());
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void getFileIcons(ViewQueryEvent event) {
        if (event.isAfter(VIEW_CARGO_FILES)) {
          ExtensionIcons.setIcons(event.getRowset(), ALS_FILE_NAME, PROP_ICON);
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillTripNumber(ViewInsertEvent event) {
        if (event.isBefore(VIEW_TRIPS, VIEW_EXPEDITION_TRIPS)
            && BeeConst.isUndef(DataUtils.getColumnIndex(COL_TRIP_NO, event.getColumns()))) {

          BeeParameter parameter = prm.getParameter(PRM_TRIP_PREFIX);
          String prefix;

          if (parameter.supportsUsers()) {
            Long userId;

            if (!BeeConst.isUndef(DataUtils.getColumnIndex(COL_TRIP_MANAGER, event.getColumns()))) {
              userId = DataUtils.getLong(event.getColumns(), event.getRow(), COL_TRIP_MANAGER);
            } else {
              userId = usr.getCurrentUserId();
            }
            prefix = parameter.getText(userId);
          } else {
            prefix = parameter.getText();
          }
          /* @since Hoptransa TaskID 17242 trip number elementing under braces {*/
          String nextNumber = qs.getNextNumber(TBL_TRIPS, COL_TRIP_NO,
              BeeUtils.isEmpty(prefix) ? null : prefix, null);

          String actualNumber = BeeUtils.isEmpty(prefix) ? BeeUtils.removePrefix(nextNumber, prefix)
              : nextNumber;

          if (BeeUtils.contains(actualNumber, BeeConst.CHAR_UNDER)) {
            IsCondition con;

            if (BeeUtils.isEmpty(prefix)) {
              con = SqlUtils.not(SqlUtils.contains(TBL_TRIPS, COL_TRIP_NO, BeeConst.STRING_UNDER));
            } else {
              IsExpression expr = SqlUtils.substring(TBL_TRIPS, COL_TRIP_NO, prefix.length()
                  + 1);
              con = SqlUtils.and(SqlUtils.startsWith(TBL_TRIPS, COL_TRIP_NO, prefix),
                  SqlUtils.not(SqlUtils.contains(expr, BeeConst.STRING_UNDER)));
            }

            String normaliseNumbers = qs.sqlCreateTemp(new SqlSelect()
                .addFields(TBL_TRIPS, COL_TRIP_NO)
                .addFrom(TBL_TRIPS)
                .setWhere(con));

            nextNumber = qs.getNextNumber(normaliseNumbers, COL_TRIP_NO,
                BeeUtils.isEmpty(prefix) ? null : prefix, null);

            int count = 0;
            while (BeeUtils.isPositive(
                qs.getInt(new SqlSelect()
                    .addCount(TBL_TRIPS, COL_TRIP_NO)
                    .addFrom(TBL_TRIPS)
                    .setWhere(SqlUtils.startsWith(TBL_TRIPS, COL_TRIP_NO, nextNumber
                        + BeeConst.STRING_UNDER))))) {
              nextNumber = BeeUtils.nextString(nextNumber);

              if (count++ > 100) {
                break;
              }
            }

            qs.sqlDropTemp(normaliseNumbers);
          }

          event.addValue(new BeeColumn(COL_TRIP_NO),
              Value.getValue(nextNumber));
          /* } */
        }

        /* @since Hoptransa TaskID 17242 trip number_template {*/
        if (event.isBefore(VIEW_TRIPS)
            && !BeeConst.isUndef(DataUtils.getColumnIndex(COL_TRIP_NO, event.getColumns()))) {
          BeeRow row = event.getRow();
          int idxTripNo = DataUtils.getColumnIndex(COL_TRIP_NO, event.getColumns());
          String tripNo = row.getString(idxTripNo);

          if (BeeUtils.isSuffix(tripNo, VAR_AUTO_NUMBER_SUFFIX)) {

            String prefix = BeeUtils.removeSuffix(tripNo, VAR_AUTO_NUMBER_SUFFIX) + "_";

            if (!BeeUtils.isEmpty(prefix)) {

              row.setValue(idxTripNo,
                  Value.getValue(qs.getNextNumber(TBL_TRIPS, COL_TRIP_NO, prefix, null)));
            }
          }
        }
        /* } */
      }

      @Subscribe
      @AllowConcurrentEvents
      public void informShipmentRequestCustomer(DataEvent.ViewUpdateEvent event) {
        if (event.isAfter(VIEW_SHIPMENT_REQUESTS)) {
          int col = DataUtils.getColumnIndex(COL_QUERY_STATUS, event.getColumns());

          if (!BeeConst.isUndef(col)) {
            Integer status = event.getRow().getInteger(col);
            TextConstant constant = null;

            if (ShipmentRequestStatus.CONFIRMED.is(status)) {
              constant = TextConstant.REQUEST_CONFIRMED_MAIL_CONTENT;
            } else if (ShipmentRequestStatus.LOST.is(status)) {
              constant = TextConstant.REQUEST_LOST_MAIL_CONTENT;
            }
            if (Objects.isNull(constant)) {
              return;
            }
            BeeRowSet info = qs.getViewData(VIEW_SHIPMENT_REQUESTS,
                Filter.compareId(event.getRow().getId()));

            String email = BeeUtils.notEmpty(info.getString(0, COL_PERSON + COL_EMAIL),
                info.getString(0, COL_QUERY_CUSTOMER_EMAIL));

            if (BeeUtils.isEmpty(email)) {
              return;
            }
            Long accountId = prm.getRelation(MailConstants.PRM_DEFAULT_ACCOUNT);

            if (!DataUtils.isId(accountId)) {
              logger.warning("Default mail account not found");
              return;
            }

            BeeRowSet data = qs.getViewData(VIEW_TEXT_CONSTANTS,
                Filter.equals(COL_TEXT_CONSTANT, constant));

            String localizedContent = Localized.column(COL_TEXT_CONTENT,
                EnumUtils.getEnumByIndex(SupportedLocale.class, info.getInteger(0, COL_USER_LOCALE))
                    .getLanguage());
            String text;

            if (DataUtils.isEmpty(data)) {
              text = constant.getDefaultContent();
            } else if (BeeConst.isUndef(DataUtils.getColumnIndex(localizedContent,
                data.getColumns()))) {
              text = data.getString(0, COL_TEXT_CONTENT);
            } else {
              text = BeeUtils.notEmpty(data.getString(0, localizedContent),
                  data.getString(0, COL_TEXT_CONTENT));
            }
            cb.asynchronousCall(() -> mail.sendMail(accountId, email, null,
                text.replace("{CONTRACT_ID}", BeeUtils.toString(event.getRow().getId()))));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void updateAssessmentRelations(ViewInsertEvent event) {
        String tbl = sys.getViewSource(event.getTargetName());

        if (BeeUtils.inList(tbl, TBL_ASSESSMENTS, TBL_ASSESSMENT_FORWARDERS) && event.isAfter()) {
          String fld;
          String tblFrom;
          String joinFrom;

          if (BeeUtils.same(tbl, TBL_ASSESSMENTS)) {
            fld = COL_ORDER;
            tblFrom = TBL_ORDER_CARGO;
            joinFrom = COL_CARGO;
          } else {
            fld = COL_TRIP;
            tblFrom = TBL_CARGO_TRIPS;
            joinFrom = COL_CARGO_TRIP;
          }
          IsCondition clause = sys.idEquals(tbl, event.getRow().getId());

          qs.updateData(new SqlUpdate(tbl)
              .addExpression(fld, new SqlSelect()
                  .addFields(tblFrom, fld)
                  .addFrom(tbl)
                  .addFromInner(tblFrom, sys.joinTables(tblFrom, tbl, joinFrom))
                  .setWhere(clause))
              .setWhere(clause));
        }
      }
    });

    HeadlineProducer assessmentsHeadlineProducer =
        (feed, userId, rowSet, row, isNew, constants) -> {
          String caption = "";
          String pid = DataUtils.getString(rowSet, row, COL_ASSESSMENT);

          if (!BeeUtils.isEmpty(pid)) {
            caption = BeeUtils.joinWords(caption, constants.captionPid() + BeeConst.STRING_COLON,
                pid);
          }

          String id = BeeUtils.toString(row.getId());

          caption = BeeUtils.joinWords(caption, constants.captionId() + BeeConst.STRING_COLON, id);

          AssessmentStatus status =
              EnumUtils.getEnumByIndex(AssessmentStatus.class,
                  DataUtils.getInteger(rowSet, row, COL_STATUS));

          if (status != null) {
            caption = BeeUtils.joinWords(caption, status.getCaption(constants));
          }

          String notes = DataUtils.getString(rowSet, row, ALS_ORDER_NOTES);
          String customer = DataUtils.getString(rowSet, row, TransportConstants.ALS_CUSTOMER_NAME);

          caption = BeeUtils.joinWords(caption, notes, customer);

          return Headline.create(row.getId(), caption, isNew);
        };

    news.registerUsageQueryProvider(Feed.ORDER_CARGO, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.notNull(TBL_ORDER_CARGO, COL_ORDER));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_ORDER_CARGO, news.joinUsage(TBL_ORDER_CARGO));
      }
    });

    news.registerUsageQueryProvider(Feed.TRANSPORTATION_ORDERS_MY,
        new ExtendedUsageQueryProvider() {
          @Override
          protected List<IsCondition> getConditions(long userId) {
            return NewsHelper.buildConditions(SqlUtils.equals(TBL_ORDERS, COL_ORDER_MANAGER,
                userId));
          }

          @Override
          protected List<Pair<String, IsCondition>> getJoins() {
            return NewsHelper.buildJoin(TBL_ORDERS, news.joinUsage(TBL_ORDERS));
          }
        });

    news.registerUsageQueryProvider(Feed.TRIPS, new ExtendedUsageQueryProvider() {
      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_TRIPS, news.joinUsage(TBL_TRIPS));
      }

      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION));
      }
    });

    news.registerUsageQueryProvider(Feed.TRIPS_MY, new ExtendedUsageQueryProvider() {
      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_TRIPS, news.joinUsage(TBL_TRIPS));
      }

      @Override
      protected List<IsCondition> getConditions(long userId) {
        SqlSelect vehicleQuery = new SqlSelect().setDistinctMode(true)
            .addFields(TBL_VEHICLE_GROUPS, COL_VEHICLE)
            .addFrom(TBL_VEHICLE_GROUPS)
            .addFromInner(TBL_TRANSPORT_GROUPS,
                sys.joinTables(TBL_TRANSPORT_GROUPS, TBL_VEHICLE_GROUPS, COL_GROUP))
            .setWhere(SqlUtils.equals(TBL_TRANSPORT_GROUPS, COL_GROUP_MANAGER, userId));

        IsCondition userCondition = SqlUtils.or(
            SqlUtils.equals(TBL_TRIPS, COL_TRIP_MANAGER, userId),
            SqlUtils.in(TBL_TRIPS, COL_VEHICLE, vehicleQuery));

        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION),
            userCondition);
      }
    });

    news.registerUsageQueryProvider(Feed.SHIPMENT_REQUESTS_UNREGISTERED_MY,
        new ShipmentRequestsUsageQueryProvider());

    news.registerUsageQueryProvider(Feed.SHIPMENT_REQUESTS_MY,
        new ShipmentRequestsUsageQueryProvider());

    news.registerUsageQueryProvider(Feed.SHIPMENT_REQUESTS_UNREGISTERED_ALL,
        new ExtendedUsageQueryProvider() {
          @Override
          protected List<IsCondition> getConditions(long userId) {
            return NewsHelper.buildConditions(SqlUtils.isNull(TBL_SHIPMENT_REQUESTS,
                COL_COMPANY_PERSON));
          }

          @Override
          protected List<Pair<String, IsCondition>> getJoins() {
            return NewsHelper.buildJoin(TBL_SHIPMENT_REQUESTS,
                news.joinUsage(TBL_SHIPMENT_REQUESTS));
          }
        });

    news.registerUsageQueryProvider(Feed.SHIPMENT_REQUESTS_ALL, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.notNull(TBL_SHIPMENT_REQUESTS,
            COL_COMPANY_PERSON));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_SHIPMENT_REQUESTS, news.joinUsage(TBL_SHIPMENT_REQUESTS));
      }
    });

    news.registerHeadlineProducer(Feed.ASSESSMENT_REQUESTS_ALL, assessmentsHeadlineProducer);

    news.registerUsageQueryProvider(Feed.ASSESSMENT_REQUESTS_ALL,
        new AssesmentRequestsUsageQueryProvider(false));

    news.registerHeadlineProducer(Feed.ASSESSMENT_REQUESTS_MY, assessmentsHeadlineProducer);

    news.registerUsageQueryProvider(Feed.ASSESSMENT_REQUESTS_MY,
        new AssesmentRequestsUsageQueryProvider(true));

    news.registerHeadlineProducer(Feed.ASSESSMENT_ORDERS_ALL, assessmentsHeadlineProducer);

    news.registerUsageQueryProvider(Feed.ASSESSMENT_ORDERS_ALL,
        new AssesmentRequestsUsageQueryProvider(false, true));

    news.registerHeadlineProducer(Feed.ASSESSMENT_ORDERS_MY, assessmentsHeadlineProducer);

    news.registerUsageQueryProvider(Feed.ASSESSMENT_ORDERS_MY,
        new AssesmentRequestsUsageQueryProvider(true, true));

    news.registerUsageQueryProvider(Feed.CARGO_SALES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_CARGO_INCOMES, COL_SALE));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_CARGO_INCOMES, news.joinUsage(TBL_CARGO_INCOMES));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_CREDIT_SALES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_CARGO_INCOMES, COL_PURCHASE),
            SqlUtils.isNull(TBL_SALES, COL_SALE_PROFORMA),
            SqlUtils.notNull(TBL_CARGO_INCOMES, COL_SALE));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoins(TBL_CARGO_INCOMES, news.joinUsage(TBL_CARGO_INCOMES),
            TBL_SALES, sys.joinTables(TBL_SALES, TBL_CARGO_INCOMES, COL_SALE));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_PURCHASES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_CARGO_EXPENSES, COL_PURCHASE));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoin(TBL_CARGO_EXPENSES, news.joinUsage(TBL_CARGO_EXPENSES));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_INVOICES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.isNull(TBL_SALES, COL_SALE_PROFORMA));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoins(TBL_SALES, news.joinUsage(TBL_SALES),
            TBL_CARGO_INCOMES, sys.joinTables(TBL_SALES, TBL_CARGO_INCOMES, COL_SALE));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_PROFORMA_INVOICES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return NewsHelper.buildConditions(SqlUtils.notNull(TBL_SALES, COL_SALE_PROFORMA));
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoins(TBL_SALES, news.joinUsage(TBL_SALES),
            TBL_CARGO_INCOMES, sys.joinTables(TBL_SALES, TBL_CARGO_INCOMES, COL_SALE));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_CREDIT_INVOICES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return null;
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoins(TBL_PURCHASES, news.joinUsage(TBL_PURCHASES),
            TBL_CARGO_INCOMES, sys.joinTables(TBL_PURCHASES, TBL_CARGO_INCOMES, COL_PURCHASE));
      }
    });

    news.registerUsageQueryProvider(Feed.CARGO_PURCHASE_INVOICES, new ExtendedUsageQueryProvider() {
      @Override
      protected List<IsCondition> getConditions(long userId) {
        return null;
      }

      @Override
      protected List<Pair<String, IsCondition>> getJoins() {
        return NewsHelper.buildJoins(TBL_PURCHASES, news.joinUsage(TBL_PURCHASES),
            TBL_CARGO_EXPENSES, sys.joinTables(TBL_PURCHASES, TBL_CARGO_EXPENSES, COL_PURCHASE));
      }
    });

    news.registerUsageQueryProvider(Feed.ASSESSMENT_TRANSPORTATIONS, new UsageQueryProvider() {
      @Override
      public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
          DateTime startDate) {
        return new SqlSelect()
            .addFields(TBL_TRIP_USAGE, COL_TRIP)
            .addMax(TBL_TRIP_USAGE, NewsConstants.COL_USAGE_ACCESS)
            .addFrom(TBL_TRIP_USAGE)
            .addFromInner(TBL_TRIPS, SqlUtils.join(TBL_TRIPS, sys.getIdName(TBL_TRIPS),
                TBL_TRIP_USAGE, COL_TRIP))
            .addFromLeft(
                TBL_ASSESSMENT_FORWARDERS,
                SqlUtils.join(TBL_ASSESSMENT_FORWARDERS, COL_TRIP, TBL_TRIPS, sys
                    .getIdName(TBL_TRIPS)))
            .addFromLeft(
                TBL_CARGO_TRIPS,
                SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, sys
                    .getIdName(TBL_TRIPS)))
            .addFromLeft(TBL_ASSESSMENTS,
                SqlUtils.join(TBL_ASSESSMENTS, COL_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
            .setWhere(SqlUtils.and(
                SqlUtils.isNull(TBL_ASSESSMENT_FORWARDERS, COL_TRIP),
                SqlUtils.notNull(TBL_ASSESSMENTS, COL_CARGO),
                SqlUtils.equals(TBL_TRIP_USAGE, NewsConstants.COL_UF_USER, userId),
                SqlUtils.notNull(TBL_TRIP_USAGE, NewsConstants.COL_USAGE_ACCESS)))
            .addGroup(TBL_TRIP_USAGE, COL_TRIP);
      }

      @Override
      public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
          DateTime startDate) {
        return new SqlSelect()
            .addFields(TBL_TRIP_USAGE, COL_TRIP)
            .addMax(TBL_TRIP_USAGE, NewsConstants.COL_USAGE_UPDATE)
            .addFrom(TBL_TRIP_USAGE)
            .addFromInner(TBL_TRIPS, SqlUtils.join(TBL_TRIPS, sys.getIdName(TBL_TRIPS),
                TBL_TRIP_USAGE, COL_TRIP))
            .addFromLeft(
                TBL_ASSESSMENT_FORWARDERS,
                SqlUtils.join(TBL_ASSESSMENT_FORWARDERS, COL_TRIP, TBL_TRIPS, sys
                    .getIdName(TBL_TRIPS)))
            .addFromLeft(
                TBL_CARGO_TRIPS,
                SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, sys
                    .getIdName(TBL_TRIPS)))
            .addFromLeft(TBL_ASSESSMENTS,
                SqlUtils.join(TBL_ASSESSMENTS, COL_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
            .setWhere(SqlUtils.and(
                SqlUtils.isNull(TBL_ASSESSMENT_FORWARDERS, COL_TRIP),
                SqlUtils.notNull(TBL_ASSESSMENTS, COL_CARGO),
                SqlUtils.notEqual(TBL_TRIP_USAGE, NewsConstants.COL_UF_USER, userId),
                SqlUtils.more(TBL_TRIP_USAGE, NewsConstants.COL_USAGE_UPDATE,
                    NewsHelper.getStartTime(startDate))))
            .addGroup(TBL_TRIP_USAGE, COL_TRIP);
      }
    });
  }

  @Schedule(persistent = false)
  private void checkRequestStatus() {
    SqlSelect query = new SqlSelect()
        .addField(TBL_SHIPMENT_REQUESTS, sys.getIdName(TBL_SHIPMENT_REQUESTS), "id")
        .addField(TBL_SHIPMENT_REQUESTS, sys.getVersionName(TBL_SHIPMENT_REQUESTS), "version")
        .addFields(TBL_SHIPMENT_REQUESTS, COL_QUERY_STATUS)
        .addFrom(TBL_SHIPMENT_REQUESTS)
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_SHIPMENT_REQUESTS, COL_CARGO))
        .setWhere(SqlUtils.and(SqlUtils.not(SqlUtils.inList(TBL_SHIPMENT_REQUESTS, COL_QUERY_STATUS,
            ShipmentRequestStatus.CONFIRMED, ShipmentRequestStatus.LOST)),
            SqlUtils.less(TBL_CARGO_PLACES, COL_PLACE_DATE, TimeUtils.startOfDay(1))));

    SimpleRowSet expired = qs.getData(query.copyOf()
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromInner(TBL_CARGO_PLACES,
            sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .setUnionAllMode(false)
        .addUnion(query.copyOf()
            .addFromInner(TBL_CARGO_HANDLING,
                sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))
            .addFromInner(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_LOADING_PLACE))));

    if (!DataUtils.isEmpty(expired)) {
      BeeColumn col = DataUtils.getColumn(COL_QUERY_STATUS,
          sys.getView(VIEW_SHIPMENT_REQUESTS).getRowSetColumns());

      for (SimpleRow row : expired) {
        BeeRowSet rs = DataUtils.getUpdated(VIEW_SHIPMENT_REQUESTS, row.getLong("id"),
            row.getLong("version"), col, row.getValue(COL_QUERY_STATUS),
            BeeUtils.toString(ShipmentRequestStatus.LOST.ordinal()));

        deb.commitRow(rs, RowInfo.class);
      }
      DataChangeEvent.fireRefresh(
          (event, locality) -> Endpoint.sendToAll(new ModificationMessage(event)),
          VIEW_SHIPMENT_REQUESTS);
    }
  }

  private ResponseObject costsToERP(Set<Long> ids) {
    String idName = sys.getIdName(TBL_TRIP_COSTS);

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addEmptyText("KLAIDA")
        .addField(TBL_TRIPS, COL_TRIP_NO, "reisas")
        .addField(TBL_TRIPS, COL_TRIP_DATE_TO, "pab_data")
        .addField(TBL_TRIP_COSTS, idName, "id")
        .addField(TBL_TRIP_COSTS, COL_COSTS_DATE, "data")
        .addField(TBL_TRIP_COSTS, COL_COSTS_QUANTITY, "kiekis")
        .addField(TBL_TRIP_COSTS, "Prefix", "serija")
        .addField(TBL_TRIP_COSTS, COL_NUMBER, "numeris")
        .addField(TBL_TRIP_COSTS, COL_COSTS_NOTE, "pastaba")
        .addField(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE, "preke")
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, "valiuta")
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, "tiekejas")
        .addField(TBL_COMPANIES, COL_COMPANY_CODE, "tiek_kod")
        .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, "salis")
        .addField(TBL_PAYMENT_TYPES, "PaymentName", "atsiskaitymas")
        .addExpr(TradeModuleBean.getVatExpression(TBL_TRIP_COSTS), "pvm_suma")
        .addExpr(TradeModuleBean.getWithoutVatExpression(TBL_TRIP_COSTS), "suma_be_pvm")
        .addField(TBL_VEHICLES, "ExternalCode", "car_id")
        .addExpr(SqlUtils.nvl(SqlUtils.field("alsEmployees", PayrollConstants.COL_TAB_NUMBER),
            SqlUtils.field(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)),
            "tab_nr")
        .addFields("subq", "dienpinigiai")
        .addFrom(TBL_TRIP_COSTS)
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_TRIP_COSTS, COL_TRIP))
        .addFromLeft(TBL_VEHICLES, sys.joinTables(TBL_VEHICLES, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_TRIP_DRIVERS, sys.joinTables(TBL_TRIP_DRIVERS, TBL_TRIPS, COL_MAIN_DRIVER))
        .addFromLeft(TBL_DRIVERS, sys.joinTables(TBL_DRIVERS, TBL_TRIP_DRIVERS, COL_DRIVER))
        .addFromLeft(PayrollConstants.TBL_EMPLOYEES,
            SqlUtils.joinUsing(TBL_DRIVERS, PayrollConstants.TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .addFromLeft(TBL_TRIP_DRIVERS, "alsTripDrivers",
            sys.joinTables(TBL_TRIP_DRIVERS, "alsTripDrivers", TBL_TRIP_COSTS, COL_DRIVER))
        .addFromLeft(TBL_DRIVERS, "alsDrivers",
            sys.joinTables(TBL_DRIVERS, "alsDrivers", "alsTripDrivers", COL_DRIVER))
        .addFromLeft(PayrollConstants.TBL_EMPLOYEES, "alsEmployees",
            SqlUtils.joinUsing("alsEmployees", "alsDrivers", COL_COMPANY_PERSON))
        .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRIP_COSTS, COL_COSTS_ITEM))
        .addFromInner(TBL_CURRENCIES,
            sys.joinTables(TBL_CURRENCIES, TBL_TRIP_COSTS, COL_COSTS_CURRENCY))
        .addFromLeft(TBL_COMPANIES,
            sys.joinTables(TBL_COMPANIES, TBL_TRIP_COSTS, COL_COSTS_SUPPLIER))
        .addFromLeft(TBL_COUNTRIES,
            sys.joinTables(TBL_COUNTRIES, TBL_TRIP_COSTS, COL_COSTS_COUNTRY))
        .addFromLeft(TBL_PAYMENT_TYPES,
            sys.joinTables(TBL_PAYMENT_TYPES, TBL_TRIP_COSTS, "PaymentType"))
        .addFromLeft(new SqlSelect()
                .addFields(TBL_TRIP_COSTS, idName)
                .addConstant(1, "dienpinigiai")
                .addFrom(TBL_TRIP_COSTS)
                .addFromInner(TBL_COUNTRY_NORMS, SqlUtils.join(TBL_TRIP_COSTS, COL_COSTS_ITEM,
                    TBL_COUNTRY_NORMS, COL_DAILY_COSTS_ITEM))
                .setWhere(sys.idInList(TBL_TRIP_COSTS, ids))
                .addGroup(TBL_TRIP_COSTS, idName),
            "subq", SqlUtils.joinUsing(TBL_TRIP_COSTS, "subq", idName))
        .setWhere(sys.idInList(TBL_TRIP_COSTS, ids)));

    StringBuilder sb = new StringBuilder("<table>");

    for (SimpleRow row : rs) {
      sb.append("<row>");

      for (String col : rs.getColumnNames()) {
        Object value;

        switch (col) {
          case "id":
            value = TBL_TRIP_COSTS + row.getValue(col);
            break;
          case "data":
          case "pab_data":
            value = row.getDateTime(col);
            break;
          default:
            value = row.getValue(col);
            break;
        }
        sb.append(XmlUtils.tag(col, value));
      }
      sb.append("</row>");
    }
    sb.append("</table>");

    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet res;

    try {
      res = ButentWS.connect(remoteAddress, remoteLogin, remotePassword).importFin(sb.toString());
    } catch (BeeException e) {
      logger.error(e);
      return ResponseObject.error(e);
    }
    SimpleRowSet answer = new SimpleRowSet(rs.getColumnNames());
    List<Long> exported = new ArrayList<>();

    for (SimpleRow row : res) {
      String id = BeeUtils.removePrefix(row.getValue("id"), TBL_TRIP_COSTS);
      String result = row.getValue("result");

      if (BeeUtils.same(result, "OK")) {
        exported.add(BeeUtils.toLong(id));
      } else {
        SimpleRow r = rs.getRowByKey("id", id);

        if (r != null) {
          r.setValue("KLAIDA", result);
          answer.addRow(r.getValues());
        }
      }
    }
    if (!exported.isEmpty()) {
      qs.updateData(new SqlUpdate(TBL_TRIP_COSTS)
          .addConstant(COL_TRADE_EXPORTED, TimeUtils.nowSeconds())
          .setWhere(sys.idInList(TBL_TRIP_COSTS, exported)));
    }
    return ResponseObject.response(answer);
  }

  private ResponseObject createCreditInvoiceItems(Long purchaseId, Long currency, Set<Long> idList,
      Long mainItem, Double amount) {

    if (!DataUtils.isId(purchaseId)) {
      return ResponseObject.error("Wrong account ID");
    }
    if (!BeeUtils.isPositive(amount)) {
      return ResponseObject.error("Wrong amount");
    }
    if (!DataUtils.isId(currency)) {
      return ResponseObject.error("Wrong currency ID");
    }
    if (BeeUtils.isEmpty(idList)) {
      return ResponseObject.error("Empty ID list");
    }
    IsCondition wh = sys.idInList(TBL_CARGO_INCOMES, idList);

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_ORDERS, COL_ORDER_NO)
        .addFields(TBL_SALES_SERIES, COL_SERIES_NAME)
        .addFields(TBL_SALES, COL_TRADE_INVOICE_NO)
        .addFields(TBL_CARGO_INCOMES, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)
        .addFrom(TBL_CARGO_INCOMES)
        .addFromInner(TBL_SERVICES,
            sys.joinTables(TBL_SERVICES, TBL_CARGO_INCOMES, COL_SERVICE))
        .addFromInner(TBL_SALES,
            sys.joinTables(TBL_SALES, TBL_CARGO_INCOMES, COL_SALE))
        .addFromLeft(TBL_SALES_SERIES,
            sys.joinTables(TBL_SALES_SERIES, TBL_SALES, COL_TRADE_SALE_SERIES))
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .setWhere(SqlUtils.and(wh, SqlUtils.positive(TBL_CARGO_INCOMES, COL_AMOUNT)))
        .addGroup(TBL_ORDERS, COL_ORDER_NO)
        .addGroup(TBL_SALES_SERIES, COL_SERIES_NAME)
        .addGroup(TBL_SALES, COL_TRADE_INVOICE_NO)
        .addGroup(TBL_CARGO_INCOMES, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC);

    if (DataUtils.isId(mainItem)) {
      ss.addConstant(mainItem, COL_ITEM);
    } else {
      ss.addFields(TBL_SERVICES, COL_ITEM)
          .addGroup(TBL_SERVICES, COL_ITEM);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_INCOMES, COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE)), SqlUtils.constant(currency));

    SimpleRowSet rs = qs.getData(ss.addSum(xpr, COL_AMOUNT));
    ResponseObject response = new ResponseObject();

    double totalAmount = 0;

    for (Double n : rs.getDoubleColumn(COL_AMOUNT)) {
      totalAmount += BeeUtils.unbox(n);
    }
    for (SimpleRow row : rs) {
      String xml = XmlUtils.createString("CreditInfo",
          COL_ORDER_NO, row.getValue(COL_ORDER_NO),
          COL_TRADE_INVOICE_NO, BeeUtils.joinWords(row.getValue(COL_SERIES_NAME),
              row.getValue(COL_TRADE_INVOICE_NO)));

      SqlInsert insert = new SqlInsert(TBL_PURCHASE_ITEMS)
          .addConstant(COL_PURCHASE, purchaseId)
          .addConstant(COL_ITEM, row.getLong(COL_ITEM))
          .addConstant(COL_TRADE_ITEM_QUANTITY, 1)
          .addConstant(COL_TRADE_ITEM_PRICE,
              BeeUtils.round(amount * row.getDouble(COL_AMOUNT) / totalAmount, 2))
          .addConstant(COL_TRADE_VAT_PLUS, row.getBoolean(COL_TRADE_VAT_PLUS))
          .addConstant(COL_TRADE_VAT, row.getDouble(COL_TRADE_VAT))
          .addConstant(COL_TRADE_VAT_PERC, row.getBoolean(COL_TRADE_VAT_PERC))
          .addConstant(COL_TRADE_ITEM_NOTE, xml);

      qs.insertData(insert);
    }
    return response.addErrorsFrom(qs.updateDataWithResponse(new SqlUpdate(TBL_CARGO_INCOMES)
        .addConstant(COL_PURCHASE, purchaseId)
        .setWhere(wh)));
  }

  private ResponseObject createInvoiceItems(Long saleId, Long currency, Set<Long> idList,
      Long mainItem) {
    if (!DataUtils.isId(saleId)) {
      return ResponseObject.error("Wrong account ID");
    }
    if (!DataUtils.isId(currency)) {
      return ResponseObject.error("Wrong currency ID");
    }
    if (BeeUtils.isEmpty(idList)) {
      return ResponseObject.error("Empty ID list");
    }
    IsCondition wh = sys.idInList(TBL_CARGO_INCOMES, idList);

    String loadPlace = SqlUtils.uniqueName();
    String unloadPlace = SqlUtils.uniqueName();
    String loadCountry = SqlUtils.uniqueName();
    String unloadCountry = SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_ORDERS, COL_ORDER_NO, COL_ORDER_NOTES)
        .addFields(TBL_ORDER_CARGO, COL_NUMBER)
        .addFields(TBL_CARGO_INCOMES,
            COL_CARGO, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)
        .addField(loadPlace, COL_DATE, ALS_LOADING_DATE)
        .addField(loadPlace, COL_POST_INDEX, ALS_LOADING_POST_INDEX)
        .addField(unloadPlace, COL_DATE, ALS_UNLOADING_DATE)
        .addField(unloadPlace, COL_POST_INDEX, ALS_UNLOADING_POST_INDEX)
        .addField(loadCountry, COL_COUNTRY_CODE, ALS_LOADING_COUNTRY_CODE)
        .addField(loadCountry, COL_COUNTRY_NAME, ALS_LOADING_COUNTRY_NAME)
        .addField(unloadCountry, COL_COUNTRY_CODE, ALS_UNLOADING_COUNTRY_CODE)
        .addField(unloadCountry, COL_COUNTRY_NAME, ALS_UNLOADING_COUNTRY_NAME)
        .addField(TBL_ASSESSMENTS, sys.getIdName(TBL_ASSESSMENTS), COL_ASSESSMENT)
        .addField(DocumentConstants.TBL_DOCUMENTS, DocumentConstants.COL_DOCUMENT_NUMBER,
            ALS_CARGO_CMR_NUMBER)
        .addField(TBL_CARGO_INCOMES, COL_NOTE, ALS_CARGO_NOTES)
        .addFrom(TBL_CARGO_INCOMES)
        .addFromInner(TBL_SERVICES,
            sys.joinTables(TBL_SERVICES, TBL_CARGO_INCOMES, COL_SERVICE))
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromLeft(TBL_CARGO_PLACES, loadPlace,
            sys.joinTables(TBL_CARGO_PLACES, loadPlace, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_COUNTRIES, loadCountry,
            sys.joinTables(TBL_COUNTRIES, loadCountry, loadPlace, COL_COUNTRY))
        .addFromLeft(TBL_CARGO_PLACES, unloadPlace,
            sys.joinTables(TBL_CARGO_PLACES, unloadPlace, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_COUNTRIES, unloadCountry,
            sys.joinTables(TBL_COUNTRIES, unloadCountry, unloadPlace, COL_COUNTRY))
        .addFromLeft(TBL_ASSESSMENTS,
            sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS, COL_CARGO))
        .addFromLeft(DocumentConstants.TBL_DOCUMENTS,
            sys.joinTables(DocumentConstants.TBL_DOCUMENTS, TBL_ORDER_CARGO, COL_CARGO_CMR))
        .setWhere(wh);

    if (DataUtils.isId(mainItem)) {
      ss.addConstant(mainItem, COL_ITEM)
          .addConstant(true, COL_TRANSPORTATION);
    } else {
      ss.addFields(TBL_SERVICES, COL_ITEM, COL_TRANSPORTATION);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_INCOMES, COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE)), SqlUtils.constant(currency));

    SimpleRowSet rs = qs.getData(ss.addExpr(xpr, COL_AMOUNT));

    SqlSelect additionalHandling = new SqlSelect()
        .addFields(TBL_CARGO_HANDLING, COL_CARGO)
        .addField(loadPlace, COL_DATE, ALS_LOADING_DATE)
        .addField(loadPlace, COL_POST_INDEX, ALS_LOADING_POST_INDEX)
        .addField(unloadPlace, COL_DATE, ALS_UNLOADING_DATE)
        .addField(unloadPlace, COL_POST_INDEX, ALS_UNLOADING_POST_INDEX)
        .addField(loadCountry, COL_COUNTRY_CODE, ALS_LOADING_COUNTRY_CODE)
        .addField(loadCountry, COL_COUNTRY_NAME, ALS_LOADING_COUNTRY_NAME)
        .addField(unloadCountry, COL_COUNTRY_CODE, ALS_UNLOADING_COUNTRY_CODE)
        .addField(unloadCountry, COL_COUNTRY_NAME, ALS_UNLOADING_COUNTRY_NAME)
        .addFrom(TBL_CARGO_HANDLING)
        .addFromLeft(TBL_CARGO_PLACES, loadPlace,
            sys.joinTables(TBL_CARGO_PLACES, loadPlace, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_COUNTRIES, loadCountry,
            sys.joinTables(TBL_COUNTRIES, loadCountry, loadPlace, COL_COUNTRY))
        .addFromLeft(TBL_CARGO_PLACES, unloadPlace,
            sys.joinTables(TBL_CARGO_PLACES, unloadPlace, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_COUNTRIES, unloadCountry,
            sys.joinTables(TBL_COUNTRIES, unloadCountry, unloadPlace, COL_COUNTRY))
        .setWhere(SqlUtils.inList(TBL_CARGO_HANDLING, COL_CARGO, Sets.newHashSet(rs.getColumn(
            COL_CARGO))))
        .addOrder(TBL_CARGO_HANDLING, sys.getIdName(TBL_CARGO_HANDLING));

    SimpleRowSet rsHandling = qs.getData(additionalHandling);

    ResponseObject response = new ResponseObject();

    Multimap<Long, String> drivers = HashMultimap.create();
    Multimap<Long, String> vehicles = HashMultimap.create();

    String vehicle = SqlUtils.uniqueName();
    String trailer = SqlUtils.uniqueName();

    IsCondition clause = SqlUtils.and(SqlUtils.inList(TBL_CARGO_TRIPS, COL_CARGO,
        Sets.newHashSet(rs.getLongColumn(COL_CARGO))));

    SimpleRowSet tripData = qs.getData(new SqlSelect()
        .addFields(TBL_CARGO_TRIPS, COL_CARGO)
        .addField(vehicle, COL_VEHICLE_NUMBER, vehicle)
        .addField(trailer, COL_VEHICLE_NUMBER, trailer)
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_CARGO_TRIPS, COL_TRIP))
        .addFromLeft(TBL_VEHICLES, vehicle,
            sys.joinTables(TBL_VEHICLES, vehicle, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLES, trailer,
            sys.joinTables(TBL_VEHICLES, trailer, TBL_TRIPS, COL_TRAILER))
        .addFromLeft(TBL_TRIP_DRIVERS, sys.joinTables(TBL_TRIPS, TBL_TRIP_DRIVERS, COL_TRIP))
        .addFromLeft(TBL_DRIVERS, sys.joinTables(TBL_DRIVERS, TBL_TRIP_DRIVERS, COL_DRIVER))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_DRIVERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS,
            sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .setWhere(SqlUtils.and(clause, SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION))));

    for (SimpleRow trip : tripData) {
      Long cargo = trip.getLong(COL_CARGO);
      String txt = BeeUtils.joinWords(trip.getValue(COL_FIRST_NAME), trip.getValue(COL_LAST_NAME));

      if (!BeeUtils.isEmpty(txt)) {
        drivers.put(cargo, txt);
      }
      txt = BeeUtils.join("/", trip.getValue(vehicle), trip.getValue(trailer));

      if (!BeeUtils.isEmpty(txt)) {
        vehicles.put(cargo, txt);
      }
    }
    tripData = qs.getData(new SqlSelect()
        .addFields(TBL_CARGO_TRIPS, COL_CARGO)
        .addFields(TBL_TRIPS, COL_FORWARDER_VEHICLE, COL_FORWARDER_DRIVER)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_CARGO_TRIPS, COL_TRIP))
        .setWhere(SqlUtils.and(clause, SqlUtils.notNull(TBL_TRIPS, COL_EXPEDITION))));

    for (SimpleRow trip : tripData) {
      Long cargo = trip.getLong(COL_CARGO);
      String txt = trip.getValue(COL_FORWARDER_DRIVER);

      if (!BeeUtils.isEmpty(txt)) {
        drivers.put(cargo, txt);
      }
      txt = trip.getValue(COL_FORWARDER_VEHICLE);

      if (!BeeUtils.isEmpty(txt)) {
        vehicles.put(cargo, txt);
      }
    }
    String[] tableFields = new String[] {
        COL_ITEM, COL_TRADE_VAT_PLUS, COL_TRADE_VAT,
        COL_TRADE_VAT_PERC};

    String[] group = DataUtils.isId(mainItem) ? tableFields : rs.getColumnNames();
    Map<String, Multimap<String, String>> map = new HashMap<>();
    Map<String, Double> amounts = new HashMap<>();

    for (SimpleRow row : rs) {
      String key = "";

      for (String fld : group) {
        if (!fld.equals(COL_AMOUNT)) {
          key += fld + row.getValue(fld);
        }
      }
      if (!map.containsKey(key)) {
        map.put(key, TreeMultimap.create());
      }
      Multimap<String, String> valueMap = map.get(key);

      for (String fld : tableFields) {
        String value = row.getValue(fld);

        if (!BeeUtils.isEmpty(value)) {
          valueMap.put(fld, value);
        }
      }
      for (String fld : new String[] {
          COL_ORDER_NO, COL_ASSESSMENT, ALS_CARGO_CMR_NUMBER, COL_NUMBER, ALS_CARGO_NOTES}) {
        String value = row.getValue(fld);

        if (!BeeUtils.isEmpty(value)) {
          valueMap.put(fld, value);
        }
      }
      if (BeeUtils.unbox(row.getBoolean(COL_TRANSPORTATION))) {
        String value = BeeUtils.join("\n", BeeUtils.nvl(row.getValue(COL_ORDER_NOTES), ""),
            BeeUtils.join("-", BeeUtils.nvl(row.getValue(ALS_LOADING_COUNTRY_CODE), ""),
                BeeUtils.nvl(row.getValue(ALS_LOADING_POST_INDEX), "") + "\n"
                    + BeeUtils.nvl(row.getValue(ALS_UNLOADING_COUNTRY_CODE), ""),
                BeeUtils.nvl(row.getValue(ALS_UNLOADING_POST_INDEX), "")));

        if (!BeeUtils.isEmpty(value)) {
          valueMap.put(COL_ORDER_NOTES, value);
        }
        long cargoId = BeeUtils.unbox(row.getLong(COL_CARGO));
        String handlingValue = "";
        String handlingDates = "";

        for (SimpleRow addH : rsHandling) {
          long addCargoId = BeeUtils.unbox(addH.getLong(COL_CARGO));

          if (addCargoId == cargoId) {
            String postIndex = BeeUtils.join("\n",
                BeeUtils.join("-", BeeUtils.nvl(addH.getValue(ALS_LOADING_COUNTRY_CODE), ""),
                    BeeUtils.nvl(addH.getValue(ALS_LOADING_POST_INDEX), "")),
                BeeUtils.join("-", BeeUtils.nvl(addH.getValue(ALS_UNLOADING_COUNTRY_CODE), ""),
                    BeeUtils.nvl(addH.getValue(ALS_UNLOADING_POST_INDEX), "")));

            String ldate = "";
            String udate = "";

            DateTime time = addH.getDateTime(ALS_LOADING_DATE);

            if (time != null && time.hasTimePart()) {
              ldate = time.getDateTime().toString();
            } else if (time != null) {
              ldate = time.getDate().toString();
            }

            time = addH.getDateTime(ALS_UNLOADING_DATE);

            if (time != null && time.hasTimePart()) {
              udate = time.getDateTime().toString();
            } else if (time != null) {
              udate = time.getDate().toString();
            }

            String dates = BeeUtils.join("\n", ldate, udate);

            if (!BeeUtils.isEmpty(postIndex)) {
              handlingValue = BeeUtils.join("\n\n", handlingValue, postIndex);
            }

            if (!BeeUtils.isEmpty(dates)) {
              handlingDates = BeeUtils.join("\n\n", handlingDates, dates);
            }
          }
        }

        if (!BeeUtils.isEmpty(handlingValue)) {
          valueMap.put(COL_ORDER_NOTES2, handlingValue);
        }

        if (!BeeUtils.isEmpty(handlingDates)) {
          valueMap.put(COL_ORDER_LOADING_NOTES, handlingDates);
        }

        for (String fld : new String[] {ALS_LOADING_DATE, ALS_UNLOADING_DATE}) {
          DateTime time = row.getDateTime(fld);

          if (time != null && time.hasTimePart()) {
            valueMap.put(fld, time.getDateTime().toString());
          } else if (time != null) {
            valueMap.put(fld, time.getDate().toString());
          }
        }
        Long cargo = row.getLong(COL_CARGO);

        if (drivers.containsKey(cargo)) {
          valueMap.putAll(COL_DRIVER, drivers.get(cargo));
        }
        if (vehicles.containsKey(cargo)) {
          valueMap.putAll(COL_VEHICLE, vehicles.get(cargo));
        }
      }
      amounts.put(key, BeeUtils.unbox(amounts.get(key))
          + BeeUtils.unbox(row.getDouble(COL_AMOUNT)));
    }
    for (Entry<String, Multimap<String, String>> entry : map.entrySet()) {
      Multimap<String, String> values = entry.getValue();

      SqlInsert insert = new SqlInsert(TBL_SALE_ITEMS)
          .addConstant(COL_SALE, saleId)
          .addConstant(COL_TRADE_ITEM_QUANTITY, 1)
          .addConstant(COL_TRADE_ITEM_PRICE, BeeUtils.round(amounts.get(entry.getKey()), 2));

      List<String> nodes = new ArrayList<>();

      for (String fld : values.keySet()) {
        if (ArrayUtils.contains(tableFields, fld)) {
          insert.addConstant(fld, BeeUtils.peek(values.get(fld)));
        } else {
          nodes.add(fld);
          nodes.add(BeeUtils.joinItems(values.get(fld)));
        }
      }
      qs.insertData(insert.addConstant(COL_TRADE_ITEM_NOTE,
          XmlUtils.createString("CargoInfo", nodes.toArray(new String[0]))));
    }
    return response.addErrorsFrom(qs.updateDataWithResponse(new SqlUpdate(TBL_CARGO_INCOMES)
        .addConstant(COL_SALE, saleId)
        .setWhere(wh)));
  }

  private ResponseObject createPurchaseInvoiceItems(Long purchaseId, Long currency,
      Set<Long> idList, Long mainItem) {

    if (!DataUtils.isId(purchaseId)) {
      return ResponseObject.error("Wrong account ID");
    }
    if (!DataUtils.isId(currency)) {
      return ResponseObject.error("Wrong currency ID");
    }
    if (BeeUtils.isEmpty(idList)) {
      return ResponseObject.error("Empty ID list");
    }
    IsCondition wh = sys.idInList(TBL_CARGO_EXPENSES, idList);

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_CARGO_EXPENSES, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)
        .addFrom(TBL_CARGO_EXPENSES)
        .addFromInner(TBL_SERVICES,
            sys.joinTables(TBL_SERVICES, TBL_CARGO_EXPENSES, COL_SERVICE))
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_EXPENSES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .setWhere(wh)
        .addGroup(TBL_CARGO_EXPENSES, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC);

    if (DataUtils.isId(mainItem)) {
      ss.addConstant(mainItem, COL_ITEM);
    } else {
      ss.addFields(TBL_SERVICES, COL_ITEM)
          .addGroup(TBL_SERVICES, COL_ITEM);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_CARGO_EXPENSES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_EXPENSES, COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_EXPENSES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE)), SqlUtils.constant(currency));

    SimpleRowSet rs = qs.getData(ss.addSum(xpr, COL_AMOUNT));
    ResponseObject response = new ResponseObject();

    for (SimpleRow row : rs) {
      SqlInsert insert = new SqlInsert(TBL_PURCHASE_ITEMS)
          .addConstant(COL_PURCHASE, purchaseId)
          .addConstant(COL_ITEM, row.getLong(COL_ITEM))
          .addConstant(COL_TRADE_ITEM_QUANTITY, 1)
          .addConstant(COL_TRADE_ITEM_PRICE, BeeUtils.round(row.getDouble(COL_AMOUNT), 2))
          .addConstant(COL_TRADE_VAT_PLUS, row.getBoolean(COL_TRADE_VAT_PLUS))
          .addConstant(COL_TRADE_VAT, row.getDouble(COL_TRADE_VAT))
          .addConstant(COL_TRADE_VAT_PERC, row.getBoolean(COL_TRADE_VAT_PERC));

      qs.insertData(insert);
    }
    return response.addErrorsFrom(qs.updateDataWithResponse(new SqlUpdate(TBL_CARGO_EXPENSES)
        .addConstant(COL_PURCHASE, purchaseId)
        .setWhere(wh)));
  }

  private ResponseObject createTripInvoiceItems(Long purchaseId, Long currency, Set<Long> idList,
      Long mainItem) {

    IsCondition wh = sys.idInList(TBL_TRIP_COSTS, idList);

    SqlSelect ss = new SqlSelect()
        .addConstant(purchaseId, COL_PURCHASE)
        .addFields(TBL_TRIP_COSTS, COL_TRADE_VAT_PLUS, COL_TRADE_VAT_PERC)
        .addField(TBL_TRIP_COSTS, COL_COSTS_NOTE, COL_TRADE_ITEM_NOTE)
        .addField(TBL_TRIPS, COL_TRIP_NO, COL_TRADE_ITEM_ARTICLE)
        .addFrom(TBL_TRIP_COSTS)
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_TRIP_COSTS, COL_TRIP))
        .setWhere(wh)
        .addGroup(TBL_TRIP_COSTS, COL_TRADE_VAT_PLUS, COL_COSTS_VAT, COL_TRADE_VAT_PERC,
            COL_COSTS_NOTE)
        .addGroup(TBL_TRIPS, COL_TRIP_NO);

    if (DataUtils.isId(mainItem)) {
      IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
          TradeModuleBean.getAmountExpression(TBL_TRIP_COSTS),
          SqlUtils.field(TBL_TRIP_COSTS, COL_COSTS_CURRENCY),
          SqlUtils.field(TBL_TRIP_COSTS, COL_DATE), SqlUtils.constant(currency));

      ss.addConstant(mainItem, COL_ITEM)
          .addConstant(1, COL_TRADE_ITEM_QUANTITY)
          .addSum(xpr, COL_TRADE_ITEM_PRICE);
    } else {
      IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
          SqlUtils.field(TBL_TRIP_COSTS, COL_COSTS_PRICE),
          SqlUtils.field(TBL_TRIP_COSTS, COL_COSTS_CURRENCY),
          SqlUtils.field(TBL_TRIP_COSTS, COL_DATE), SqlUtils.constant(currency));

      ss.addFields(TBL_TRIP_COSTS, COL_ITEM)
          .addSum(TBL_TRIP_COSTS, COL_COSTS_QUANTITY, COL_TRADE_ITEM_QUANTITY)
          .addMax(xpr, COL_TRADE_ITEM_PRICE)
          .addGroup(TBL_TRIP_COSTS, COL_ITEM, COL_COSTS_PRICE);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_TRIP_COSTS, COL_TRADE_VAT),
        SqlUtils.field(TBL_TRIP_COSTS, COL_COSTS_CURRENCY),
        SqlUtils.field(TBL_TRIP_COSTS, COL_DATE), SqlUtils.constant(currency));

    ss.addMax(SqlUtils.sqlIf(SqlUtils.isNull(TBL_TRIP_COSTS, COL_TRADE_VAT_PERC), xpr,
        SqlUtils.field(TBL_TRIP_COSTS, COL_COSTS_VAT)), COL_TRADE_VAT);

    qs.loadData(TBL_PURCHASE_ITEMS, ss);

    ResponseObject response = new ResponseObject();

    return response.addErrorsFrom(qs.updateDataWithResponse(new SqlUpdate(TBL_TRIP_COSTS)
        .addConstant(COL_PURCHASE, purchaseId)
        .setWhere(wh)));
  }

  private ResponseObject createUser(RequestInfo reqInfo) {
    String login = reqInfo.getParameter(COL_LOGIN);
    if (BeeUtils.isEmpty(login)) {
      return ResponseObject.parameterNotFound(SVC_CREATE_USER, COL_LOGIN);
    }
    String password = reqInfo.getParameter(COL_PASSWORD);
    if (BeeUtils.isEmpty(password)) {
      return ResponseObject.parameterNotFound(SVC_CREATE_USER, COL_PASSWORD);
    }
    if (usr.isUser(login)) {
      return ResponseObject.warning(usr.getDictionary()
          .valueExists(BeeUtils.joinWords(usr.getDictionary().user(), login)));
    }
    Long role = prm.getRelation(PRM_SELF_SERVICE_ROLE);

    if (!DataUtils.isId(role)) {
      return ResponseObject.parameterNotFound(SVC_CREATE_USER, PRM_SELF_SERVICE_ROLE);
    }
    ResponseObject resp = ResponseObject.info(usr.getDictionary().newUser(), login);
    String email;

    try {
      email = new InternetAddress(reqInfo.getParameter(COL_EMAIL), true).getAddress();
    } catch (AddressException e) {
      return ResponseObject.error(e);
    }
    Long accountId = prm.getRelation(MailConstants.PRM_DEFAULT_ACCOUNT);

    if (!DataUtils.isId(accountId)) {
      return ResponseObject.parameterNotFound(SVC_CREATE_USER, MailConstants.PRM_DEFAULT_ACCOUNT);
    }
    Long user = qs.insertData(new SqlInsert(TBL_USERS)
        .addConstant(COL_LOGIN, login)
        .addConstant(COL_PASSWORD, Codec.encodePassword(password))
        .addConstant(COL_COMPANY_PERSON, reqInfo.getParameter(COL_COMPANY_PERSON))
        .addConstant(COL_USER_INTERFACE, UserInterface.SELF_SERVICE));

    qs.insertData(new SqlInsert(TBL_USER_ROLES)
        .addConstant(COL_USER, user)
        .addConstant(COL_ROLE, role));

    TextConstant constant = TextConstant.REGISTRATION_MAIL_CONTENT;

    BeeRowSet data = qs.getViewData(VIEW_TEXT_CONSTANTS,
        Filter.equals(COL_TEXT_CONSTANT, constant));

    String localizedContent = Localized.column(COL_TEXT_CONTENT,
        EnumUtils.getEnumByIndex(SupportedLocale.class, reqInfo.getParameterInt(COL_USER_LOCALE))
            .getLanguage());
    String text;

    if (DataUtils.isEmpty(data)) {
      text = constant.getDefaultContent();
    } else if (BeeConst.isUndef(DataUtils.getColumnIndex(localizedContent, data.getColumns()))) {
      text = data.getString(0, COL_TEXT_CONTENT);
    } else {
      text = BeeUtils.notEmpty(data.getString(0, localizedContent),
          data.getString(0, COL_TEXT_CONTENT));
    }
    if (!BeeUtils.isEmpty(text)) {
      cb.asynchronousCall(() -> mail.sendMail(accountId, email, null,
          text.replace("{LOGIN}", login).replace("{PASSWORD}", password)));
    }
    return resp;
  }

  private ResponseObject generateDailyCosts(long tripId) {
    Long mainCountry = prm.getRelation(PRM_COUNTRY);

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_COUNTRY_NORMS, COL_COUNTRY, COL_DAILY_COSTS_ITEM)
        .addFields(TBL_COUNTRY_DAILY_COSTS, COL_AMOUNT, COL_CURRENCY)
        .addExpr(SqlUtils.sqlIf(SqlUtils.or(
            SqlUtils.isNull(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_FROM),
            SqlUtils.joinLess(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_FROM,
                TBL_TRIP_ROUTES, COL_ROUTE_DEPARTURE_DATE)),
            SqlUtils.field(TBL_TRIP_ROUTES, COL_ROUTE_DEPARTURE_DATE),
            SqlUtils.field(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_FROM)), COL_ROUTE_DEPARTURE_DATE)
        .addExpr(SqlUtils.sqlIf(
            SqlUtils.or(SqlUtils.isNull(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_TO),
                SqlUtils.joinMore(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_TO,
                    TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_DATE)),
            SqlUtils.field(TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_DATE),
            SqlUtils.field(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_TO)), COL_ROUTE_ARRIVAL_DATE)
        .addFrom(TBL_TRIP_ROUTES)
        .addFromInner(TBL_COUNTRY_NORMS,
            SqlUtils.compare(SqlUtils.sqlIf(SqlUtils.equals(TBL_TRIP_ROUTES,
                COL_ROUTE_ARRIVAL_COUNTRY, mainCountry),
                SqlUtils.field(TBL_TRIP_ROUTES, COL_ROUTE_DEPARTURE_COUNTRY),
                SqlUtils.field(TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_COUNTRY)), Operator.EQ,
                SqlUtils.field(TBL_COUNTRY_NORMS, COL_COUNTRY)))
        .addFromInner(TBL_COUNTRY_DAILY_COSTS, SqlUtils.and(
            sys.joinTables(TBL_COUNTRY_NORMS, TBL_COUNTRY_DAILY_COSTS, COL_COUNTRY_NORM),
            SqlUtils.notNull(TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_DATE),
            SqlUtils.joinMoreEqual(TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_DATE, TBL_TRIP_ROUTES,
                COL_ROUTE_DEPARTURE_DATE),
            SqlUtils.or(SqlUtils.isNull(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_FROM),
                SqlUtils.joinLess(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_FROM,
                    TBL_TRIP_ROUTES, COL_ROUTE_ARRIVAL_DATE)),
            SqlUtils.or(SqlUtils.isNull(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_TO),
                SqlUtils.joinMore(TBL_COUNTRY_DAILY_COSTS, COL_TRIP_DATE_TO,
                    TBL_TRIP_ROUTES, COL_ROUTE_DEPARTURE_DATE))))
        .setWhere(SqlUtils.equals(TBL_TRIP_ROUTES, COL_TRIP, tripId))
        .addOrderDesc(null, COL_ROUTE_DEPARTURE_DATE, COL_ROUTE_ARRIVAL_DATE,
            sys.getIdName(TBL_TRIP_ROUTES)));

    qs.updateData(new SqlDelete(TBL_TRIP_COSTS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TRIP_COSTS, COL_TRIP, tripId),
            SqlUtils.in(TBL_TRIP_COSTS, COL_COSTS_ITEM, TBL_COUNTRY_NORMS, COL_DAILY_COSTS_ITEM))));

    Map<String, Map<String, String>> map = new HashMap<>();
    JustDate lastArrivalTime = null;
    String lastKey = null;

    for (SimpleRow row : rs) {
      JustDate arrivalTime = row.getDateTime(COL_ROUTE_ARRIVAL_DATE).getDate();
      String key;

      if (TimeUtils.sameDate(arrivalTime, lastArrivalTime)) {
        key = lastKey;
      } else {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(COL_COSTS_COUNTRY, row.getValue(COL_COUNTRY));
        values.put(COL_COSTS_ITEM, row.getValue(COL_DAILY_COSTS_ITEM));
        values.put(COL_COSTS_PRICE, row.getValue(COL_AMOUNT));
        values.put(COL_COSTS_CURRENCY, row.getValue(COL_CURRENCY));
        values.put("Old" + COL_COSTS_PRICE, row.getValue(COL_AMOUNT));

        key = Codec.md5(BeeUtils.joinItems(arrivalTime, BeeUtils.joinItems(values.values())));

        if (!map.containsKey(key)) {
          map.put(key, values);
        }
        lastArrivalTime = arrivalTime;
        lastKey = key;
      }
      Map<String, String> values = map.get(key);

      values.put(COL_COSTS_QUANTITY,
          BeeUtils.toString(BeeUtils.toInt(values.get(COL_COSTS_QUANTITY))
              + TimeUtils.dayDiff(row.getDateTime(COL_ROUTE_DEPARTURE_DATE), arrivalTime)));
    }
    if (!BeeUtils.isEmpty(lastKey)) {
      Map<String, String> values = map.get(lastKey);
      values.put(COL_COSTS_QUANTITY,
          BeeUtils.toString(BeeUtils.toInt(values.get(COL_COSTS_QUANTITY)) + 1));
    }
    Map<String, Map<String, String>> newMap = new HashMap<>();

    for (Map<String, String> values : map.values()) {
      int qty = Math.max(BeeUtils.toInt(values.get(COL_COSTS_QUANTITY)), 1);
      values.remove(COL_COSTS_QUANTITY);
      String key = Codec.md5(BeeUtils.joinItems(values.values()));

      if (newMap.containsKey(key)) {
        qty += BeeUtils.toInt(newMap.get(key).get(COL_COSTS_QUANTITY));
      } else {
        newMap.put(key, values);
      }
      newMap.get(key).put(COL_COSTS_QUANTITY, BeeUtils.toString(qty));
    }
    SimpleRow dateRow = qs.getRow(new SqlSelect()
        .addFields(TBL_TRIPS, COL_TRIP_DATE, COL_TRIP_DATE_TO)
        .addFrom(TBL_TRIPS)
        .setWhere(sys.idEquals(TBL_TRIPS, tripId)));

    Long payment = ArrayUtils.getQuietly(qs.getLongColumn(new SqlSelect()
        .addFields(TBL_PAYMENT_TYPES, sys.getIdName(TBL_PAYMENT_TYPES))
        .addFrom(TBL_PAYMENT_TYPES)
        .setWhere(SqlUtils.notNull(TBL_PAYMENT_TYPES, COL_PAYMENT_CASH))), 0);

    Long[] drivers = qs.getLongColumn(new SqlSelect()
        .addFields(TBL_TRIP_DRIVERS, sys.getIdName(TBL_TRIP_DRIVERS))
        .addFrom(TBL_TRIP_DRIVERS)
        .setWhere(SqlUtils.equals(TBL_TRIP_DRIVERS, COL_TRIP, tripId)));

    for (Long driver : drivers) {
      for (Map<String, String> values : newMap.values()) {
        SqlInsert insert = new SqlInsert(TBL_TRIP_COSTS)
            .addConstant(COL_TRIP, tripId)
            .addConstant(COL_COSTS_DATE, BeeUtils.nvl(dateRow.getDate(COL_TRIP_DATE_TO),
                dateRow.getDateTime(COL_TRIP_DATE).getDate()))
            .addConstant(COL_DRIVER, driver)
            .addNotNull(COL_PAYMENT_TYPE, payment);

        for (Entry<String, String> entry : values.entrySet()) {
          insert.addConstant(entry.getKey(), entry.getValue());
        }
        qs.insertData(insert);
      }
    }
    return ResponseObject.info(Localized.dictionary().createdRows(drivers.length * newMap.size()));
  }

  private ResponseObject generateTripRoute(long tripId) {
    List<Long> ids = qs.getLongList(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_CARGO_HANDLING, COL_CARGO_TRIP)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_TRIPS, TBL_CARGO_HANDLING, COL_CARGO_TRIP))
        .setWhere(SqlUtils.equals(TBL_CARGO_TRIPS, COL_TRIP, tripId)));

    SqlSelect query = new SqlSelect()
        .addField(TBL_CARGO_TRIPS, sys.getIdName(TBL_CARGO_TRIPS), COL_ROUTE_CARGO)
        .addFields(TBL_CARGO_HANDLING, COL_CARGO_WEIGHT, COL_LOADED_KILOMETERS,
            COL_EMPTY_KILOMETERS, COL_UNPLANNED_KILOMETERS)
        .addFields(TBL_CARGO_PLACES, COL_PLACE_DATE, COL_PLACE_COUNTRY, COL_PLACE_CITY)
        .addFields(TBL_ORDER_CARGO, COL_CARGO_PARTIAL)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CARGO_TRIPS, COL_TRIP, tripId),
            SqlUtils.notNull(TBL_CARGO_PLACES, COL_PLACE_DATE)));

    SqlSelect union = query.copyOf()
        .addConstant(0, VAR_UNLOADING)
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_TRIPS, TBL_CARGO_HANDLING, COL_CARGO_TRIP))
        .addFromLeft(TBL_CARGO_PLACES,
            sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_LOADING_PLACE))

        .addUnion(query.copyOf()
            .addConstant(1, VAR_UNLOADING)
            .addFromInner(TBL_CARGO_HANDLING,
                sys.joinTables(TBL_CARGO_TRIPS, TBL_CARGO_HANDLING, COL_CARGO_TRIP))
            .addFromLeft(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE)));

    if (!BeeUtils.isEmpty(ids)) {
      query.setWhere(SqlUtils.and(query.getWhere(),
          SqlUtils.not(sys.idInList(TBL_CARGO_TRIPS, ids))));
    }
    union.addUnion(query.copyOf()
        .addConstant(0, VAR_UNLOADING)
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromLeft(TBL_CARGO_PLACES,
            sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_LOADING_PLACE))

        .addUnion(query.copyOf()
            .addConstant(1, VAR_UNLOADING)
            .addFromInner(TBL_CARGO_HANDLING,
                sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
            .addFromLeft(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE)))

        .addUnion(query.copyOf()
            .addConstant(0, VAR_UNLOADING)
            .addFromInner(TBL_CARGO_HANDLING,
                sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))
            .addFromLeft(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_LOADING_PLACE)))

        .addUnion(query.copyOf()
            .addConstant(1, VAR_UNLOADING)
            .addFromInner(TBL_CARGO_HANDLING,
                sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))
            .addFromLeft(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE)))

        .addOrder(null, COL_PLACE_DATE, VAR_UNLOADING));

    Long currentCargo = null;
    double currentWeight = 0;
    DateTime currentDate = null;
    Long currentCountry = null;
    Long currentCity = null;

    Multimap<Long, Map<String, Object>> data = LinkedListMultimap.create();
    Map<Long, Pair<Integer, Integer>> stack = new HashMap<>();
    Set<Long> nonPartials = new HashSet<>();

    for (SimpleRow row : qs.getData(union)) {
      Long orderCargo = row.getLong(COL_ROUTE_CARGO);
      boolean loaded = stack.containsKey(orderCargo);
      boolean unloading = BeeUtils.unbox(row.getBoolean(VAR_UNLOADING));

      if (unloading && !loaded) {
        continue;
      }
      double weight = BeeUtils.unbox(row.getDouble(COL_CARGO_WEIGHT));
      DateTime date = row.getDateTime(COL_PLACE_DATE);
      Long country = row.getLong(COL_PLACE_COUNTRY);
      Long city = row.getLong(COL_PLACE_CITY);

      if (!BeeUtils.isEmpty(stack) && TimeUtils.isMore(date, currentDate)) {
        Map<String, Object> rec = new HashMap<>();
        data.put(unloading || !BeeUtils.isPositive(currentWeight) ? orderCargo : currentCargo, rec);

        rec.put(COL_ROUTE_DEPARTURE_DATE, currentDate);
        rec.put(COL_ROUTE_DEPARTURE_COUNTRY, currentCountry);
        rec.put(COL_ROUTE_DEPARTURE_CITY, currentCity);
        rec.put(COL_ROUTE_WEIGHT, currentWeight);
        rec.put(COL_ROUTE_ARRIVAL_DATE, date);
        rec.put(COL_ROUTE_ARRIVAL_COUNTRY, country);
        rec.put(COL_ROUTE_ARRIVAL_CITY, city);
      }
      currentDate = date;
      currentCountry = country;
      currentCity = city;

      if (unloading) {
        Pair<Integer, Integer> pair = stack.get(orderCargo);

        int emptyKm = BeeUtils.unbox(row.getInt(COL_EMPTY_KILOMETERS));
        pair.setA(pair.getA() + BeeUtils.unbox(row.getInt(COL_LOADED_KILOMETERS)) + emptyKm
            + BeeUtils.unbox(row.getInt(COL_UNPLANNED_KILOMETERS)));
        pair.setB(pair.getB() + emptyKm);

        currentWeight -= weight;
      } else {
        if (!loaded) {
          if (!BeeUtils.unbox(row.getBoolean(COL_CARGO_PARTIAL))) {
            nonPartials.add(orderCargo);
          }
          stack.put(orderCargo, Pair.of(0, 0));
        }
        currentCargo = orderCargo;
        currentWeight += weight;
      }
    }
    if (currentWeight > 0) {
      Map<String, Object> rec = new HashMap<>();
      data.put(currentCargo, rec);

      rec.put(COL_ROUTE_DEPARTURE_DATE, currentDate);
      rec.put(COL_ROUTE_DEPARTURE_COUNTRY, currentCountry);
      rec.put(COL_ROUTE_DEPARTURE_CITY, currentCity);
      rec.put(COL_ROUTE_WEIGHT, currentWeight);
    }
    for (Long orderCargo : stack.keySet()) {
      if (data.containsKey(orderCargo)) {
        Collection<Map<String, Object>> recs = data.get(orderCargo);
        Map<String, Object> info = new HashMap<>();

        info.put(COL_ROUTE_KILOMETERS, stack.get(orderCargo).getA().doubleValue() / recs.size());
        info.put(COL_EMPTY_KILOMETERS, stack.get(orderCargo).getB().doubleValue() / recs.size());
        info.put(COL_ROUTE_CARGO, nonPartials.isEmpty() || nonPartials.contains(orderCargo)
            ? orderCargo : nonPartials.iterator().next());

        for (Map<String, Object> rec : recs) {
          rec.putAll(info);
        }
      }
    }
    if (data.isEmpty()) {
      return ResponseObject.warning(Localized.dictionary().noData());
    }
    qs.updateData(new SqlDelete(TBL_TRIP_ROUTES)
        .setWhere(SqlUtils.equals(TBL_TRIP_ROUTES, COL_TRIP, tripId)));

    for (Map<String, Object> rec : data.values()) {
      SqlInsert insert = new SqlInsert(TBL_TRIP_ROUTES)
          .addConstant(COL_TRIP, tripId);

      for (Entry<String, Object> entry : rec.entrySet()) {
        insert.addConstant(entry.getKey(), entry.getValue());

        if (Objects.equals(entry.getKey(), COL_ROUTE_DEPARTURE_DATE)) {
          insert.addConstant(COL_ROUTE_SEASON,
              BeeUtils.betweenInclusive(((DateTime) entry.getValue()).getMonth(), 4, 10)
                  ? FuelSeason.SUMMER.ordinal() : FuelSeason.WINTER.ordinal());
        }
      }
      qs.insertData(insert);
    }
    return ResponseObject.emptyResponse();
  }

  private ResponseObject getAssessmentQuantityReport(RequestInfo reqInfo) {
    Long startDate = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_FROM));
    Long endDate = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_TO));

    Set<Long> departments = DataUtils.parseIdSet(reqInfo.getParameter(AR_DEPARTMENT));
    Set<Long> managers = DataUtils.parseIdSet(reqInfo.getParameter(AR_MANAGER));

    List<String> groupBy = NameUtils.toList(reqInfo.getParameter(Service.VAR_GROUP_BY));

    HasConditions where = SqlUtils.and(SqlUtils.notNull(TBL_ASSESSMENTS, COL_ASSESSMENT_STATUS));

    if (startDate != null) {
      where.add(SqlUtils.moreEqual(TBL_ORDERS, COL_ORDER_DATE, startDate));
    }
    if (endDate != null) {
      where.add(SqlUtils.less(TBL_ORDERS, COL_ORDER_DATE, endDate));
    }

    if (!departments.isEmpty()) {
      where.add(SqlUtils.inList(TBL_ASSESSMENTS, COL_DEPARTMENT, departments));
    }
    if (!managers.isEmpty()) {
      where.add(SqlUtils.inList(TBL_USERS, COL_COMPANY_PERSON, managers));
    }

    SqlSelect query = new SqlSelect();
    query.addFields(TBL_ASSESSMENTS, COL_ASSESSMENT_STATUS, COL_ASSESSMENT);

    if (groupBy.contains(BeeConst.MONTH)) {
      query.addFields(TBL_ORDERS, COL_ORDER_DATE);
      query.addEmptyNumeric(BeeConst.YEAR, 4, 0);
      query.addEmptyNumeric(BeeConst.MONTH, 2, 0);
    }

    if (groupBy.contains(AR_DEPARTMENT)) {
      query.addFields(TBL_ASSESSMENTS, COL_DEPARTMENT);
    }
    if (groupBy.contains(AR_MANAGER)) {
      query.addFields(TBL_USERS, COL_COMPANY_PERSON);
    }

    query.addFrom(TBL_ASSESSMENTS);
    if (!managers.isEmpty() || groupBy.contains(AR_MANAGER) || groupBy.contains(BeeConst.MONTH)
        || startDate != null || endDate != null) {

      query.addFromInner(TBL_ORDER_CARGO,
          sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS, COL_CARGO));
      query.addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER));

      if (!managers.isEmpty() || groupBy.contains(AR_MANAGER)) {
        query.addFromLeft(TBL_USERS, sys.joinTables(TBL_USERS, TBL_ORDERS, COL_ORDER_MANAGER));
      }
    }

    query.setWhere(where);

    String tmp = qs.sqlCreateTemp(query);

    long count;
    if (groupBy.contains(BeeConst.MONTH)) {
      count = qs.setYearMonth(tmp, COL_ORDER_DATE, BeeConst.YEAR, BeeConst.MONTH);
    } else {
      count = qs.sqlCount(tmp, null);
    }

    if (count <= 0) {
      qs.sqlDropTemp(tmp);
      return ResponseObject.emptyResponse();
    }

    query = new SqlSelect();
    query.addFrom(tmp);

    for (String by : groupBy) {
      switch (by) {
        case BeeConst.MONTH:
          query.addFields(tmp, BeeConst.YEAR, BeeConst.MONTH);
          query.addGroup(tmp, BeeConst.YEAR, BeeConst.MONTH);
          query.addOrder(tmp, BeeConst.YEAR, BeeConst.MONTH);
          break;

        case AR_DEPARTMENT:
          query.addFields(tmp, COL_DEPARTMENT);
          query.addFields(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);

          query.addFromLeft(TBL_DEPARTMENTS,
              SqlUtils.join(TBL_DEPARTMENTS, sys.getIdName(TBL_DEPARTMENTS), tmp, COL_DEPARTMENT));

          query.addGroup(tmp, COL_DEPARTMENT);
          query.addGroup(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);
          query.addOrder(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);
          break;

        case AR_MANAGER:
          query.addFields(tmp, COL_COMPANY_PERSON);
          query.addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);

          query.addFromLeft(TBL_COMPANY_PERSONS,
              SqlUtils.join(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS),
                  tmp, COL_COMPANY_PERSON));
          query.addFromLeft(TBL_PERSONS,
              sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON));

          query.addGroup(tmp, COL_COMPANY_PERSON);
          query.addGroup(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);

          query.addOrder(TBL_PERSONS, COL_LAST_NAME, COL_FIRST_NAME);
          query.addOrder(tmp, COL_COMPANY_PERSON);
          break;
      }
    }

    query.addCount(AR_RECEIVED);

    IsExpression xpr = SqlUtils.field(tmp, COL_ASSESSMENT_STATUS);

    query.addSum(SqlUtils.sqlCase(xpr, AssessmentStatus.ANSWERED.ordinal(), 1, 0), AR_ANSWERED);
    query.addSum(SqlUtils.sqlCase(xpr, AssessmentStatus.LOST.ordinal(), 1, 0), AR_LOST);
    query.addSum(SqlUtils.sqlCase(xpr, AssessmentStatus.APPROVED.ordinal(), 1, 0), AR_APPROVED);

    query.addSum(SqlUtils.sqlIf(SqlUtils.isNull(tmp, COL_ASSESSMENT), 0, 1), AR_SECONDARY);

    SimpleRowSet result = qs.getData(query);
    qs.sqlDropTemp(tmp);

    if (DataUtils.isEmpty(result)) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result);
    }
  }

  private ResponseObject getAssessmentTotals(Long assessmentId, Long currency, boolean isPrimary) {
    Assert.state(DataUtils.isId(assessmentId));

    SqlSelect query = null;

    for (String tbl : new String[] {TBL_CARGO_INCOMES, TBL_CARGO_EXPENSES}) {
      for (int i = 0; i < 2; i++) {
        if (i > 0 && !isPrimary) {
          break;
        }
        SqlSelect ss = new SqlSelect()
            .addConstant(tbl + (i > 0 ? VAR_TOTAL : ""), COL_SERVICE)
            .addFrom(tbl)
            .addFromInner(TBL_ASSESSMENTS, SqlUtils.joinUsing(tbl, TBL_ASSESSMENTS, COL_CARGO))
            .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, tbl, COL_CARGO))
            .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER));

        if (i > 0) {
          ss.setWhere(SqlUtils.equals(TBL_ASSESSMENTS, COL_ASSESSMENT, assessmentId));
        } else {
          ss.setWhere(sys.idEquals(TBL_ASSESSMENTS, assessmentId));
        }
        IsExpression amountExpr = SqlUtils.field(tbl, COL_AMOUNT);

        if (BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT))) {
          amountExpr = TradeModuleBean.getWithoutVatExpression(tbl, amountExpr);
        } else {
          amountExpr = TradeModuleBean.getTotalExpression(tbl, amountExpr);
        }
        IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss, amountExpr,
            SqlUtils.field(tbl, COL_CURRENCY),
            SqlUtils.nvl(SqlUtils.field(tbl, COL_DATE), SqlUtils.field(TBL_ORDERS, COL_DATE)),
            SqlUtils.constant(currency));

        ss.addSum(xpr, COL_AMOUNT);

        if (query == null) {
          query = ss;
        } else {
          query.setUnionAllMode(true).addUnion(ss);
        }
      }
    }
    return ResponseObject.response(qs.getData(query));
  }

  private ResponseObject getAssessmentTurnoverReport(RequestInfo reqInfo) {
    Long startDate = reqInfo.getParameterLong(Service.VAR_FROM);
    Long endDate = reqInfo.getParameterLong(Service.VAR_TO);

    Long currency = reqInfo.getParameterLong(COL_CURRENCY);

    Set<Long> departments = DataUtils.parseIdSet(reqInfo.getParameter(AR_DEPARTMENT));
    Set<Long> managers = DataUtils.parseIdSet(reqInfo.getParameter(AR_MANAGER));
    Set<Long> customers = DataUtils.parseIdSet(reqInfo.getParameter(AR_CUSTOMER));

    List<String> groupBy = NameUtils.toList(reqInfo.getParameter(Service.VAR_GROUP_BY));

    HasConditions where = SqlUtils.and(
        SqlUtils.equals(TBL_ORDERS, COL_STATUS, OrderStatus.COMPLETED.ordinal()),
        SqlUtils.in(TBL_ASSESSMENTS, COL_CARGO, TBL_CARGO_INCOMES, COL_CARGO,
            SqlUtils.notNull(TBL_CARGO_INCOMES, COL_SALE)),
        SqlUtils.not(SqlUtils.in(TBL_ASSESSMENTS, COL_CARGO, TBL_CARGO_INCOMES, COL_CARGO,
            SqlUtils.isNull(TBL_CARGO_INCOMES, COL_SALE))));

    if (startDate != null || endDate != null) {
      if (startDate != null) {
        where.add(SqlUtils.moreEqual(TBL_CARGO_PLACES, COL_PLACE_DATE, startDate));
      }
      if (endDate != null) {
        where.add(SqlUtils.less(TBL_CARGO_PLACES, COL_PLACE_DATE, endDate));
      }
    } else {
      where.add(SqlUtils.notNull(TBL_CARGO_PLACES, COL_PLACE_DATE));
    }

    if (!departments.isEmpty()) {
      where.add(SqlUtils.inList(TBL_ASSESSMENTS, COL_DEPARTMENT, departments));
    }
    if (!managers.isEmpty()) {
      where.add(SqlUtils.inList(TBL_USERS, COL_COMPANY_PERSON, managers));
    }
    if (!customers.isEmpty()) {
      where.add(SqlUtils.inList(TBL_ORDERS, COL_CUSTOMER, customers));
    }

    SqlSelect query = new SqlSelect();
    query.addFields(TBL_ASSESSMENTS, COL_ASSESSMENT, COL_CARGO);

    String orderDateAlias = "OrdDt" + SqlUtils.uniqueName();
    query.addField(TBL_ORDERS, COL_ORDER_DATE, orderDateAlias);

    if (groupBy.contains(BeeConst.MONTH)) {
      query.addFields(TBL_CARGO_PLACES, COL_PLACE_DATE);
      query.addEmptyNumeric(BeeConst.YEAR, 4, 0);
      query.addEmptyNumeric(BeeConst.MONTH, 2, 0);
    }

    if (groupBy.contains(AR_DEPARTMENT)) {
      query.addFields(TBL_ASSESSMENTS, COL_DEPARTMENT);
    }
    if (groupBy.contains(AR_MANAGER)) {
      query.addFields(TBL_USERS, COL_COMPANY_PERSON);
    }
    if (groupBy.contains(AR_CUSTOMER)) {
      query.addFields(TBL_ORDERS, COL_CUSTOMER);
    }

    query.addFrom(TBL_ASSESSMENTS)
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS, COL_CARGO))
        .addFromLeft(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromInner(TBL_CARGO_PLACES,
            sys.joinTables(TBL_CARGO_PLACES, COL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER));

    if (!managers.isEmpty() || groupBy.contains(AR_MANAGER)) {
      query.addFromLeft(TBL_USERS, sys.joinTables(TBL_USERS, TBL_ORDERS, COL_ORDER_MANAGER));
    }

    query.setWhere(where);

    String tmp = qs.sqlCreateTemp(query);

    long count;
    if (groupBy.contains(BeeConst.MONTH)) {
      count = qs.setYearMonth(tmp, COL_PLACE_DATE, BeeConst.YEAR, BeeConst.MONTH);
    } else {
      count = qs.sqlCount(tmp, null);
    }

    if (count <= 0) {
      qs.sqlDropTemp(tmp);
      return ResponseObject.emptyResponse();
    }

    query = new SqlSelect();
    query.addFrom(tmp);

    for (String by : groupBy) {
      switch (by) {
        case BeeConst.MONTH:
          query.addFields(tmp, BeeConst.YEAR, BeeConst.MONTH);
          query.addGroup(tmp, BeeConst.YEAR, BeeConst.MONTH);
          query.addOrder(tmp, BeeConst.YEAR, BeeConst.MONTH);
          break;

        case AR_DEPARTMENT:
          query.addFields(tmp, COL_DEPARTMENT);
          query.addFields(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);

          query.addFromLeft(TBL_DEPARTMENTS,
              SqlUtils.join(TBL_DEPARTMENTS, sys.getIdName(TBL_DEPARTMENTS), tmp, COL_DEPARTMENT));

          query.addGroup(tmp, COL_DEPARTMENT);
          query.addGroup(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);
          query.addOrder(TBL_DEPARTMENTS, COL_DEPARTMENT_NAME);
          break;

        case AR_MANAGER:
          query.addFields(tmp, COL_COMPANY_PERSON);
          query.addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);

          query.addFromLeft(TBL_COMPANY_PERSONS,
              SqlUtils.join(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS),
                  tmp, COL_COMPANY_PERSON));
          query.addFromLeft(TBL_PERSONS,
              sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON));

          query.addGroup(tmp, COL_COMPANY_PERSON);
          query.addGroup(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);

          query.addOrder(TBL_PERSONS, COL_LAST_NAME, COL_FIRST_NAME);
          query.addOrder(tmp, COL_COMPANY_PERSON);
          break;

        case AR_CUSTOMER:
          query.addFields(tmp, COL_CUSTOMER);
          query.addField(TBL_COMPANIES, COL_COMPANY_NAME, ALS_COMPANY_NAME);

          query.addFromLeft(TBL_COMPANIES,
              SqlUtils.join(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), tmp, COL_CUSTOMER));

          query.addGroup(tmp, COL_CUSTOMER);
          query.addGroup(TBL_COMPANIES, COL_COMPANY_NAME);
          query.addOrder(TBL_COMPANIES, COL_COMPANY_NAME);
          break;
      }
    }

    query.addCount(AR_RECEIVED);

    if (!DataUtils.isId(currency)) {
      currency = prm.getRelation(PRM_CURRENCY);
    }

    SqlSelect subIncome = new SqlSelect()
        .addFields(tmp, COL_CARGO)
        .addFrom(tmp)
        .addFromInner(TBL_CARGO_INCOMES,
            SqlUtils.join(TBL_CARGO_INCOMES, COL_CARGO, tmp, COL_CARGO))
        .addGroup(tmp, COL_CARGO);

    IsExpression incomeXpr = getAssessmentTurnoverExpression(subIncome, TBL_CARGO_INCOMES,
        tmp, orderDateAlias, currency, BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT)));
    subIncome.addSum(incomeXpr, AR_INCOME);

    SqlSelect subExpense = new SqlSelect()
        .addFields(tmp, COL_CARGO)
        .addFrom(tmp)
        .addFromInner(TBL_CARGO_EXPENSES,
            SqlUtils.join(TBL_CARGO_EXPENSES, COL_CARGO, tmp, COL_CARGO))
        .addGroup(tmp, COL_CARGO);

    IsExpression expenseXpr = getAssessmentTurnoverExpression(subExpense, TBL_CARGO_EXPENSES,
        tmp, orderDateAlias, currency, BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT)));
    subExpense.addSum(expenseXpr, AR_EXPENSE);

    String incomeAlias = "Inc" + SqlUtils.uniqueName();
    String expenseAlias = "Exp" + SqlUtils.uniqueName();

    query.addFromLeft(subIncome, incomeAlias,
        SqlUtils.join(incomeAlias, COL_CARGO, tmp, COL_CARGO));
    query.addFromLeft(subExpense, expenseAlias,
        SqlUtils.join(expenseAlias, COL_CARGO, tmp, COL_CARGO));

    query.addSum(incomeAlias, AR_INCOME);
    query.addSum(expenseAlias, AR_EXPENSE);

    IsCondition condition = SqlUtils.isNull(tmp, COL_ASSESSMENT);
    query.addSum(SqlUtils.sqlIf(condition, 0, 1), AR_SECONDARY);

    query.addSum(SqlUtils.sqlIf(condition, 0, SqlUtils.field(incomeAlias, AR_INCOME)),
        AR_SECONDARY_INCOME);
    query.addSum(SqlUtils.sqlIf(condition, 0, SqlUtils.field(expenseAlias, AR_EXPENSE)),
        AR_SECONDARY_EXPENSE);

    SimpleRowSet result = qs.getData(query);
    qs.sqlDropTemp(tmp);

    if (DataUtils.isEmpty(result)) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result);
    }
  }

  private ResponseObject getCargoTotal(long cargoId, Long currency) {
    String val = null;
    SimpleRow row = qs.getRow(rep.getCargoIncomeQuery(new SqlSelect()
            .addConstant(cargoId, COL_CARGO), currency,
        BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT))));

    if (row != null) {
      val = BeeUtils.round(row.getValue("CargoIncome"), 2);
    }
    return ResponseObject.response(BeeUtils.notEmpty(val, "0.00"));
  }

  private ResponseObject getCargoUsage(String viewName, List<Long> ids, String saleColumn) {
    String source = sys.getViewSource(viewName);
    SqlSelect ss;
    IsExpression ref = null;

    if (!BeeUtils.isEmpty(saleColumn)) {
      ss = new SqlSelect()
          .addFrom(TBL_CARGO_INCOMES)
          .setWhere(SqlUtils.notNull(TBL_CARGO_INCOMES, saleColumn));

      switch (source) {
        case TBL_ORDER_CARGO:
          ref = SqlUtils.field(TBL_CARGO_INCOMES, COL_CARGO);
          break;
        case TBL_ORDERS:
          ss.addFromInner(TBL_ORDER_CARGO,
              sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO));
          ref = SqlUtils.field(TBL_ORDER_CARGO, COL_ORDER);
          break;
      }
    } else {
      ss = new SqlSelect().addFrom(TBL_CARGO_TRIPS);

      switch (source) {
        case TBL_TRIPS:
          ref = SqlUtils.field(TBL_CARGO_TRIPS, COL_TRIP);
          break;
        case TBL_ORDER_CARGO:
          ref = SqlUtils.field(TBL_CARGO_TRIPS, COL_CARGO);
          break;
        case TBL_ORDERS:
          ss.addFromInner(TBL_ORDER_CARGO,
              sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO));
          ref = SqlUtils.field(TBL_ORDER_CARGO, COL_ORDER);
          break;
      }
    }
    if (Objects.isNull(ref)) {
      return ResponseObject.error("Table not supported:", source);
    }
    return ResponseObject.response(qs.sqlCount(ss.setWhere(SqlUtils.and(SqlUtils.inList(ref, ids),
        ss.getWhere()))));
  }

  private Map<String, String> getCityNames() {
    Map<String, String> result = new HashMap<>();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_CITIES, sys.getIdName(TBL_CITIES), COL_CITY_NAME)
        .addFrom(TBL_CITIES);

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        result.put(row.getValue(0), row.getValue(1));
      }
    }

    return result;
  }

  private ResponseObject getColors(RequestInfo reqInfo) {
    Long theme;
    if (reqInfo.hasParameter(Service.VAR_ID)) {
      theme = BeeUtils.toLong(reqInfo.getParameter(Service.VAR_ID));
    } else {
      theme = null;
    }

    return ResponseObject.response(getThemeColors(theme));
  }

  @SuppressWarnings("unchecked")
  private ResponseObject getCreditInfo(RequestInfo reqInfo) {
    Long company = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY));

    if (!DataUtils.isId(company)) {
      Long incomeId = BeeUtils.toLongOrNull(reqInfo.getParameter(TBL_CARGO_INCOMES));

      if (!DataUtils.isId(incomeId)) {
        return ResponseObject.emptyResponse();
      }
      SimpleRow row = qs.getRow(new SqlSelect()
          .addFields(TBL_ORDERS, COL_CUSTOMER, COL_PAYER)
          .addFrom(TBL_CARGO_INCOMES)
          .addFromInner(TBL_ORDER_CARGO,
              sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
          .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
          .setWhere(sys.idEquals(TBL_CARGO_INCOMES, incomeId)));

      if (row != null) {
        company = BeeUtils.nvl(row.getLong(COL_PAYER), row.getLong(COL_CUSTOMER));
      }
    }
    Map<String, Object> resp = new HashMap<>();

    if (DataUtils.isId(company)) {
      ResponseObject response = trd.getCreditInfo(company);

      if (response.hasErrors()) {
        return response;
      }
      resp.putAll((Map<? extends String, ?>) response.getResponse());
      Long curr = (Long) resp.get(COL_COMPANY_LIMIT_CURRENCY);

      SqlSelect query = new SqlSelect()
          .addFrom(TBL_ORDERS)
          .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
          .addFromInner(TBL_CARGO_INCOMES,
              sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
          .setWhere(SqlUtils.and(SqlUtils.or(SqlUtils.equals(TBL_ORDERS, COL_PAYER, company),
              SqlUtils.and(SqlUtils.isNull(TBL_ORDERS, COL_PAYER),
                  SqlUtils.equals(TBL_ORDERS, COL_CUSTOMER, company))),
              SqlUtils.isNull(TBL_CARGO_INCOMES, COL_SALE)));

      IsExpression cargoIncome;
      IsExpression dateExpr = SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
          SqlUtils.field(TBL_ORDERS, COL_DATE));

      if (DataUtils.isId(curr)) {
        cargoIncome = ExchangeUtils.exchangeFieldTo(query,
            TradeModuleBean.getTotalExpression(TBL_CARGO_INCOMES,
                SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT)),
            SqlUtils.field(TBL_CARGO_INCOMES, COL_CURRENCY), dateExpr, SqlUtils.constant(curr));
      } else {
        cargoIncome = ExchangeUtils.exchangeField(query,
            TradeModuleBean.getTotalExpression(TBL_CARGO_INCOMES,
                SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT)),
            SqlUtils.field(TBL_CARGO_INCOMES, COL_CURRENCY), dateExpr);
      }
      resp.put(VAR_INCOME, BeeUtils.round(qs.getValue(query.addSum(cargoIncome, VAR_INCOME)), 2));
    }
    return ResponseObject.response(resp);
  }

  private ResponseObject getDriverBusyDates(Long driver, Long from, Long to) {
    SqlSelect query = new SqlSelect()
        .addField(TBL_TRIPS, COL_TRIP_NO, COL_NOTES)
        .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_FROM),
            SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE)), COL_ABSENCE_FROM)
        .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_TO),
            SqlUtils.field(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE)), COL_ABSENCE_TO)
        .addFrom(TBL_TRIPS)
        .addFromInner(TBL_TRIP_DRIVERS,
            SqlUtils.and(sys.joinTables(TBL_TRIPS, TBL_TRIP_DRIVERS, COL_TRIP),
                SqlUtils.equals(TBL_TRIP_DRIVERS, COL_DRIVER, driver)))
        .setWhere(SqlUtils.or(
            SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                SqlUtils.isNull(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE)),
            SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                SqlUtils.more(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE, from)),
            SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                SqlUtils.more(TBL_TRIPS, COL_TRIP_DATE_TO, from))))
        .addOrder(null, COL_ABSENCE_FROM, COL_ABSENCE_TO);

    if (Objects.nonNull(to)) {
      query.setWhere(SqlUtils.and(query.getWhere(),
          SqlUtils.or(SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_FROM),
              SqlUtils.less(TBL_TRIPS, COL_TRIP_DATE, to)),
              SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_FROM),
                  SqlUtils.less(TBL_TRIPS, COL_TRIP_DATE_FROM, to)))));
    }
    query.addUnion(new SqlSelect()
        .addExpr(SqlUtils.concat(SqlUtils.field(TBL_ABSENCE_TYPES, COL_ABSENCE_NAME), "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_DRIVER_ABSENCE, COL_ABSENCE_NOTES), "''")), COL_NOTES)
        .addFields(TBL_DRIVER_ABSENCE, COL_ABSENCE_FROM, COL_ABSENCE_TO)
        .addFrom(TBL_DRIVER_ABSENCE)
        .addFromInner(TBL_ABSENCE_TYPES,
            sys.joinTables(TBL_ABSENCE_TYPES, TBL_DRIVER_ABSENCE, COL_ABSENCE))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_DRIVER_ABSENCE, COL_DRIVER, driver),
            SqlUtils.more(TBL_DRIVER_ABSENCE, COL_ABSENCE_TO, from),
            Objects.nonNull(to) ? SqlUtils.less(TBL_DRIVER_ABSENCE, COL_ABSENCE_FROM, to) : null)));

    List<String> messages = new ArrayList<>();
    Dictionary loc = usr.getDictionary();

    for (SimpleRow row : qs.getData(query)) {
      messages.add(BeeUtils.joinWords(loc.dateFromShort(), row.getDate(COL_ABSENCE_FROM),
          Objects.isNull(row.getDate(COL_ABSENCE_TO))
              ? null : loc.dateToShort() + " " + row.getDate(COL_ABSENCE_TO),
          row.getValue(COL_NOTES)));
    }

    return ResponseObject.response(messages);
  }

  private Multimap<Long, Long> getDriverGroups(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_DRIVER_GROUPS, COL_DRIVER, COL_GROUP)
        .addFrom(TBL_DRIVER_GROUPS)
        .setWhere(condition);

    SimpleRowSet data = qs.getData(query);

    Multimap<Long, Long> result = HashMultimap.create();
    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        result.put(row.getLong(COL_DRIVER), row.getLong(COL_GROUP));
      }
    }

    return result;
  }

  private ResponseObject getDtbData() {
    BeeRowSet settings = getSettings();
    if (DataUtils.isEmpty(settings)) {
      return ResponseObject.error("user settings not available");
    }

    JustDate minDate = settings.getDate(0, COL_DTB_MIN_DATE);
    JustDate maxDate = settings.getDate(0, COL_DTB_MAX_DATE);

    String filterGroups = settings.getString(0, COL_DTB_TRANSPORT_GROUPS);
    Boolean completedTrips = settings.getBoolean(0, COL_DTB_COMPLETED_TRIPS);

    Range<Value> period = TransportUtils.getChartPeriod(minDate, maxDate);

    List<Color> colors = getThemeColors(null);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet transportGroups = qs.getViewData(VIEW_TRANSPORT_GROUPS);
    if (!DataUtils.isEmpty(transportGroups)) {
      settings.setTableProperty(PROP_TRANSPORT_GROUPS, transportGroups.serialize());
    }

    BeeRowSet cargoTypes = qs.getViewData(VIEW_CARGO_TYPES);
    if (!DataUtils.isEmpty(cargoTypes)) {
      settings.setTableProperty(PROP_CARGO_TYPES, cargoTypes.serialize());
    }

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    Map<String, String> cityNames = getCityNames();
    if (!BeeUtils.isEmpty(cityNames)) {
      settings.setTableProperty(PROP_CITIES, Codec.beeSerialize(cityNames));
    }

    Filter driverFilter = null;
    if (!BeeUtils.isEmpty(filterGroups) && !DataUtils.isEmpty(transportGroups)) {
      Set<Long> groups = DataUtils.parseIdSet(filterGroups);
      groups.retainAll(transportGroups.getRowIds());

      if (!groups.isEmpty()) {
        Set<Long> ids = qs.getDistinctLongs(TBL_DRIVER_GROUPS, COL_DRIVER,
            SqlUtils.inList(TBL_DRIVER_GROUPS, COL_GROUP, groups));

        if (BeeUtils.isEmpty(ids)) {
          driverFilter = Filter.isFalse();
        } else {
          driverFilter = Filter.idIn(ids);
        }
      }
    }

    BeeRowSet drivers = qs.getViewData(VIEW_DRIVERS, driverFilter);
    if (DataUtils.isEmpty(drivers)) {
      logger.warning(SVC_GET_DTB_DATA, filterGroups, "drivers not available");
      return ResponseObject.response(settings);
    }

    List<Long> driverIds = DataUtils.getRowIds(drivers);

    IsCondition groupWhere = SqlUtils.inList(TBL_DRIVER_GROUPS, COL_DRIVER, driverIds);
    Multimap<Long, Long> driverGroups = getDriverGroups(groupWhere);
    if (!driverGroups.isEmpty()) {
      for (BeeRow row : drivers) {
        if (driverGroups.containsKey(row.getId())) {
          row.setProperty(PROP_DRIVER_GROUPS,
              DataUtils.buildIdList(driverGroups.get(row.getId())));
        }
      }
    }

    settings.setTableProperty(PROP_DRIVERS, drivers.serialize());

    BeeRowSet absence = qs.getViewData(VIEW_DRIVER_ABSENCE,
        Filter.and(Filter.any(COL_DRIVER, driverIds),
            Filter.anyIntersects(Lists.newArrayList(COL_ABSENCE_FROM, COL_ABSENCE_TO), period)));
    if (!DataUtils.isEmpty(absence)) {
      settings.setTableProperty(PROP_ABSENCE, absence.serialize());
    }

    IsCondition tripDriverWhere = SqlUtils.inList(TBL_TRIP_DRIVERS, COL_DRIVER, driverIds);
    IsCondition chartTripCondition = getChartTripCondition(period, completedTrips);

    SimpleRowSet tripDrivers = getTripDrivers(SqlUtils.and(tripDriverWhere, chartTripCondition));
    if (DataUtils.isEmpty(tripDrivers)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_TRIP_DRIVERS, tripDrivers.serialize());

    IsCondition tripWhere = SqlUtils.and(chartTripCondition,
        SqlUtils.in(TBL_TRIPS, sys.getIdName(TBL_TRIPS),
            TBL_TRIP_DRIVERS, COL_TRIP, tripDriverWhere));

    SqlSelect tripQuery = getTripQuery(tripWhere);
    tripQuery.addOrder(TBL_TRIPS, COL_TRIP_DATE);

    SimpleRowSet trips = qs.getData(tripQuery);
    if (DataUtils.isEmpty(trips)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_TRIPS, trips.serialize());

    SqlSelect freightQuery = getFreightQuery(tripWhere);

    SimpleRowSet freights = qs.getData(freightQuery);
    if (DataUtils.isEmpty(freights)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_FREIGHTS, freights.serialize());

    SqlSelect cargoHandlingQuery = getFreightHandlingQuery(tripWhere);

    SimpleRowSet cargoHandling = qs.getData(cargoHandlingQuery);
    if (!DataUtils.isEmpty(cargoHandling)) {
      settings.setTableProperty(PROP_CARGO_HANDLING, cargoHandling.serialize());
    }

    return ResponseObject.response(settings);
  }

  private SqlSelect getFreightHandlingQuery(IsCondition tripWhere) {
    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    IsCondition handlingWhere = SqlUtils.or(SqlUtils.notNull(loadAlias, COL_PLACE_DATE),
        SqlUtils.notNull(unlAlias, COL_PLACE_DATE));

    return new SqlSelect()
        .addFrom(TBL_TRIPS)
        .addFromInner(TBL_CARGO_TRIPS, sys.joinTables(TBL_TRIPS, TBL_CARGO_TRIPS, COL_TRIP))
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromInner(TBL_CARGO_HANDLING,
            SqlUtils.or(SqlUtils.and(SqlUtils.notNull(TBL_CARGO_TRIPS, COL_CARGO_HANDLING),
                sys.joinTables(TBL_CARGO_TRIPS, TBL_CARGO_HANDLING, COL_CARGO_TRIP),
                SqlUtils.joinNotEqual(TBL_CARGO_TRIPS, COL_CARGO_HANDLING, TBL_CARGO_HANDLING,
                    sys.getIdName(TBL_CARGO_HANDLING))),
                SqlUtils.and(SqlUtils.isNull(TBL_CARGO_TRIPS, COL_CARGO_HANDLING),
                    sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            sys.joinTables(TBL_CARGO_PLACES, loadAlias, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            sys.joinTables(TBL_CARGO_PLACES, unlAlias, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFields(TBL_CARGO_TRIPS, COL_CARGO)
        .addFields(TBL_CARGO_HANDLING, COL_CARGO_HANDLING_NOTES)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_POST_INDEX, loadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(loadAlias, COL_PLACE_CITY, loadingColumnAlias(COL_PLACE_CITY))
        .addField(loadAlias, COL_PLACE_NUMBER, loadingColumnAlias(COL_PLACE_NUMBER))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_POST_INDEX, unloadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(unlAlias, COL_PLACE_CITY, unloadingColumnAlias(COL_PLACE_CITY))
        .addField(unlAlias, COL_PLACE_NUMBER, unloadingColumnAlias(COL_PLACE_NUMBER))
        .setWhere(SqlUtils.and(tripWhere, handlingWhere))
        .addOrder(TBL_CARGO_TRIPS, COL_CARGO);
  }

  private SqlSelect getFreightQuery(IsCondition where) {
    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String defLoadAlias = "defl_" + SqlUtils.uniqueName();
    String defUnlAlias = "defu_" + SqlUtils.uniqueName();
    String defHandling = SqlUtils.uniqueName();

    return new SqlSelect()
        .addFrom(TBL_TRIPS)
        .addFromInner(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, COL_TRIP_ID))
        .addFromLeft(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_CARGO_TRIPS, COL_CARGO_HANDLING))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            sys.joinTables(TBL_CARGO_PLACES, loadAlias, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            sys.joinTables(TBL_CARGO_PLACES, unlAlias, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromLeft(TBL_CARGO_HANDLING, defHandling,
            sys.joinTables(TBL_CARGO_HANDLING, defHandling, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromLeft(TBL_CARGO_PLACES, defLoadAlias,
            sys.joinTables(TBL_CARGO_PLACES, defLoadAlias, defHandling, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, defUnlAlias,
            sys.joinTables(TBL_CARGO_PLACES, defUnlAlias, defHandling, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFields(TBL_TRIPS, COL_TRIP_ID, COL_VEHICLE)
        .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_CARGO_TRIPS, COL_TRAILER),
            SqlUtils.field(TBL_TRIPS, COL_TRAILER)), COL_TRAILER)
        .addFields(TBL_CARGO_TRIPS, COL_CARGO, COL_CARGO_TRIP_ID)
        .addField(TBL_CARGO_TRIPS, sys.getVersionName(TBL_CARGO_TRIPS), ALS_CARGO_TRIP_VERSION)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_POST_INDEX, loadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(loadAlias, COL_PLACE_CITY, loadingColumnAlias(COL_PLACE_CITY))
        .addField(loadAlias, COL_PLACE_NUMBER, loadingColumnAlias(COL_PLACE_NUMBER))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_POST_INDEX, unloadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(unlAlias, COL_PLACE_CITY, unloadingColumnAlias(COL_PLACE_CITY))
        .addField(unlAlias, COL_PLACE_NUMBER, unloadingColumnAlias(COL_PLACE_NUMBER))
        .addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_TYPE, COL_CARGO_DESCRIPTION,
            COL_CARGO_NOTES)
        .addField(defLoadAlias, COL_PLACE_DATE, defaultLoadingColumnAlias(COL_PLACE_DATE))
        .addField(defLoadAlias, COL_PLACE_COUNTRY, defaultLoadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(defLoadAlias, COL_PLACE_ADDRESS, defaultLoadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(defLoadAlias, COL_PLACE_POST_INDEX,
            defaultLoadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(defLoadAlias, COL_PLACE_CITY, defaultLoadingColumnAlias(COL_PLACE_CITY))
        .addField(defLoadAlias, COL_PLACE_NUMBER, defaultLoadingColumnAlias(COL_PLACE_NUMBER))
        .addField(defUnlAlias, COL_PLACE_DATE, defaultUnloadingColumnAlias(COL_PLACE_DATE))
        .addField(defUnlAlias, COL_PLACE_COUNTRY, defaultUnloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(defUnlAlias, COL_PLACE_ADDRESS, defaultUnloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(defUnlAlias, COL_PLACE_POST_INDEX,
            defaultUnloadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(defUnlAlias, COL_PLACE_CITY, defaultUnloadingColumnAlias(COL_PLACE_CITY))
        .addField(defUnlAlias, COL_PLACE_NUMBER, defaultUnloadingColumnAlias(COL_PLACE_NUMBER))
        .addFields(TBL_ORDERS, COL_ORDER_NO, COL_CUSTOMER, COL_ORDER_MANAGER, COL_STATUS)
        .addField(TBL_ORDERS, COL_ORDER_DATE, ALS_ORDER_DATE)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_CUSTOMER_NAME)
        .setWhere(where)
        .addOrder(TBL_TRIPS, COL_TRIP_ID);
  }

  private ResponseObject getFxData() {
    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Long theme = settings.getLong(0, settings.getColumnIndex(COL_FX_THEME));
    List<Color> colors = getThemeColors(theme);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet cargoTypes = qs.getViewData(VIEW_CARGO_TYPES);
    if (!DataUtils.isEmpty(cargoTypes)) {
      settings.setTableProperty(PROP_CARGO_TYPES, cargoTypes.serialize());
    }

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    Map<String, String> cityNames = getCityNames();
    if (!BeeUtils.isEmpty(cityNames)) {
      settings.setTableProperty(PROP_CITIES, Codec.beeSerialize(cityNames));
    }

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_ORDER_CARGO)
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFromLeft(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_CARGO_HANDLING, TBL_ORDER_CARGO, COL_CARGO_HANDLING))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            sys.joinTables(TBL_CARGO_PLACES, loadAlias, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            sys.joinTables(TBL_CARGO_PLACES, unlAlias, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_CARGO, TBL_ORDER_CARGO, COL_CARGO_ID));

    query.addFields(TBL_ORDERS, COL_STATUS, COL_ORDER_DATE, COL_ORDER_NO, COL_CUSTOMER,
        COL_ORDER_MANAGER);
    query.addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_CUSTOMER_NAME);

    query.addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_ID, COL_CARGO_TYPE,
        COL_CARGO_DESCRIPTION, COL_CARGO_NOTES);

    query.addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE));
    query.addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY));
    query.addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS));
    query.addField(loadAlias, COL_PLACE_POST_INDEX, loadingColumnAlias(COL_PLACE_POST_INDEX));
    query.addField(loadAlias, COL_PLACE_CITY, loadingColumnAlias(COL_PLACE_CITY));
    query.addField(loadAlias, COL_PLACE_NUMBER, loadingColumnAlias(COL_PLACE_NUMBER));

    query.addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE));
    query.addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY));
    query.addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS));
    query.addField(unlAlias, COL_PLACE_POST_INDEX, unloadingColumnAlias(COL_PLACE_POST_INDEX));
    query.addField(unlAlias, COL_PLACE_CITY, unloadingColumnAlias(COL_PLACE_CITY));
    query.addField(unlAlias, COL_PLACE_NUMBER, unloadingColumnAlias(COL_PLACE_NUMBER));

    Set<Integer> statuses = Sets.newHashSet(OrderStatus.REQUEST.ordinal(),
        OrderStatus.ACTIVE.ordinal());
    IsCondition cargoWhere = SqlUtils.and(SqlUtils.inList(TBL_ORDERS, COL_STATUS, statuses),
        SqlUtils.or(SqlUtils.notNull(TBL_ORDER_CARGO, COL_CARGO_MULTIPLE_SEGMENTS),
            SqlUtils.isNull(TBL_CARGO_TRIPS, COL_CARGO)));

    query.setWhere(cargoWhere);

    query.addOrder(TBL_COMPANIES, COL_COMPANY_NAME);
    query.addOrder(TBL_ORDERS, COL_ORDER_DATE, COL_ORDER_NO);
    query.addOrder(loadAlias, COL_PLACE_DATE);
    query.addOrder(unlAlias, COL_PLACE_DATE);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_ORDER_CARGO, data.serialize());

    IsCondition cargoHandlingWhere = SqlUtils.or(SqlUtils.notNull(loadAlias, COL_PLACE_DATE),
        SqlUtils.notNull(unlAlias, COL_PLACE_DATE));

    SqlSelect cargoHandlingQuery = new SqlSelect()
        .addFrom(TBL_ORDER_CARGO)
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_CARGO, TBL_ORDER_CARGO, COL_CARGO_ID))
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            sys.joinTables(TBL_CARGO_PLACES, loadAlias, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            sys.joinTables(TBL_CARGO_PLACES, unlAlias, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFields(TBL_CARGO_HANDLING, COL_CARGO, COL_CARGO_HANDLING_NOTES)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_POST_INDEX, loadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(loadAlias, COL_PLACE_CITY, loadingColumnAlias(COL_PLACE_CITY))
        .addField(loadAlias, COL_PLACE_NUMBER, loadingColumnAlias(COL_PLACE_NUMBER))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_POST_INDEX, unloadingColumnAlias(COL_PLACE_POST_INDEX))
        .addField(unlAlias, COL_PLACE_CITY, unloadingColumnAlias(COL_PLACE_CITY))
        .addField(unlAlias, COL_PLACE_NUMBER, unloadingColumnAlias(COL_PLACE_NUMBER))
        .setWhere(SqlUtils.and(cargoWhere, cargoHandlingWhere))
        .addOrder(TBL_CARGO_HANDLING, COL_CARGO);

    SimpleRowSet cargoHandling = qs.getData(cargoHandlingQuery);
    if (!DataUtils.isEmpty(cargoHandling)) {
      settings.setTableProperty(PROP_CARGO_HANDLING, cargoHandling.serialize());
    }

    return ResponseObject.response(settings);
  }

  private Map<String, Long> getReferences(String tableName, String keyName) {
    return getReferences(tableName, keyName, null);
  }

  private Map<String, Long> getReferences(String tableName, String keyName, IsCondition clause) {
    Map<String, Long> ref = new HashMap<>();

    for (SimpleRow row : qs.getData(new SqlSelect()
        .addFields(tableName, keyName)
        .addField(tableName, sys.getIdName(tableName), tableName)
        .addFrom(tableName)
        .setWhere(SqlUtils.and(SqlUtils.notNull(tableName, keyName), clause)))) {

      ref.put(row.getValue(keyName), row.getLong(tableName));
    }
    return ref;
  }

  private BeeRowSet getSettings() {
    long userId = usr.getCurrentUserId();
    Filter filter = Filter.equals(COL_USER, userId);

    BeeRowSet rowSet = qs.getViewData(VIEW_TRANSPORT_SETTINGS, filter);
    if (!DataUtils.isEmpty(rowSet)) {
      return rowSet;
    }

    SqlInsert sqlInsert = new SqlInsert(TBL_TRANSPORT_SETTINGS)
        .addConstant(COL_USER, userId);

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return null;
    }

    return qs.getViewData(VIEW_TRANSPORT_SETTINGS, filter);
  }

  private ResponseObject getTextConstant(RequestInfo reqInfo) {

    Integer textConstant = Assert.notNull(reqInfo.getParameterInt(COL_TEXT_CONSTANT));
    Integer userLocale = reqInfo.getParameterInt(COL_USER_LOCALE);
    TextConstant constant = EnumUtils.getEnumByIndex(TextConstant.class, textConstant);

    return ResponseObject.response(getTextConstant(constant, userLocale));
  }

  public String getTextConstant(TextConstant constant, Integer userLocale) {
    BeeRowSet rowSet =
        qs.getViewData(VIEW_TEXT_CONSTANTS, Filter.equals(COL_TEXT_CONSTANT, constant));

    String localizedContent = Localized.column(COL_TEXT_CONTENT,
        EnumUtils.getEnumByIndex(SupportedLocale.class, userLocale).getLanguage());
    String text;

    if (DataUtils.isEmpty(rowSet)) {
      text = constant.getDefaultContent();
    } else if (BeeConst.isUndef(DataUtils.getColumnIndex(localizedContent, rowSet.getColumns()))) {
      text = rowSet.getString(0, COL_TEXT_CONTENT);
    } else {
      text = BeeUtils.notEmpty(rowSet.getString(0, localizedContent),
          rowSet.getString(0, COL_TEXT_CONTENT));
    }
    return text;
  }

  private List<Color> getThemeColors(Long theme) {
    List<Color> result = new ArrayList<>();

    BeeRowSet rowSet;
    if (theme != null) {
      rowSet = qs.getViewData(VIEW_THEME_COLORS, Filter.equals(COL_THEME, theme));
    } else {
      rowSet = null;
    }

    if (DataUtils.isEmpty(rowSet)) {
      rowSet = qs.getViewData(VIEW_COLORS);
      if (DataUtils.isEmpty(rowSet)) {
        return result;
      }
    }

    int bgIndex = rowSet.getColumnIndex(COL_BACKGROUND);
    int fgIndex = rowSet.getColumnIndex(COL_FOREGROUND);

    for (BeeRow row : rowSet.getRows()) {
      String bg = row.getString(bgIndex);
      String fg = row.getString(fgIndex);

      if (!BeeUtils.isEmpty(bg)) {
        result.add(new Color(row.getId(), bg.trim(), BeeUtils.trim(fg)));
      }
    }
    return result;
  }

  private ResponseObject getTripBeforeData(long vehicle, Long date) {
    Pair<String, String> pair = Pair.empty();

    if (Objects.nonNull(date)) {
      SimpleRow row = qs.getRow(new SqlSelect()
          .addField(TBL_TRIPS, sys.getIdName(TBL_TRIPS), COL_TRIP)
          .addFields(TBL_TRIPS, COL_SPEEDOMETER_BEFORE, COL_SPEEDOMETER_AFTER, COL_FUEL_BEFORE,
              COL_FUEL_AFTER)
          .addFields(TBL_VEHICLES, COL_SPEEDOMETER)
          .addFrom(TBL_TRIPS)
          .addFromInner(TBL_VEHICLES,
              SqlUtils.and(sys.joinTables(TBL_VEHICLES, TBL_TRIPS, COL_VEHICLE),
                  sys.idEquals(TBL_VEHICLES, vehicle)))
          .setWhere(SqlUtils.less(TBL_TRIPS, COL_DATE, date))
          .addOrderDesc(TBL_TRIPS, COL_DATE)
          .setLimit(1));

      if (Objects.nonNull(row)) {
        Long tripId = row.getLong(COL_TRIP);
        Double speedometer = row.getDouble(COL_SPEEDOMETER_AFTER);
        Double fuel = row.getDouble(COL_FUEL_AFTER);

        if (Objects.isNull(speedometer)) {
          Double km = qs.getDouble(new SqlSelect()
              .addSum(TBL_TRIP_ROUTES, COL_ROUTE_KILOMETERS)
              .addFrom(TBL_TRIP_ROUTES)
              .setWhere(SqlUtils.equals(TBL_TRIP_ROUTES, COL_TRIP, tripId)));

          speedometer = BeeUtils.unbox(row.getDouble(COL_SPEEDOMETER_BEFORE)) + BeeUtils.unbox(km);
          Integer scale = row.getInt(COL_SPEEDOMETER);

          if (BeeUtils.isPositive(scale) && scale < speedometer) {
            speedometer -= scale;
          }
        }
        if (Objects.isNull(fuel)) {
          Double fill = qs.getDouble(new SqlSelect()
              .addSum(TBL_TRIP_FUEL_COSTS, COL_COSTS_QUANTITY)
              .addFrom(TBL_TRIP_FUEL_COSTS)
              .setWhere(SqlUtils.equals(TBL_TRIP_FUEL_COSTS, COL_TRIP, tripId)));

          SimpleRow cons = qs.getRow(rep.getFuelConsumptionsQuery(new SqlSelect()
              .addFields(TBL_TRIP_ROUTES, sys.getIdName(TBL_TRIP_ROUTES))
              .addFrom(TBL_TRIP_ROUTES)
              .setWhere(SqlUtils.equals(TBL_TRIP_ROUTES, COL_TRIP, tripId)), false));

          Double consume = Objects.isNull(cons) ? null : cons.getDouble(COL_ROUTE_CONSUMPTION);

          Double addit = qs.getDouble(new SqlSelect()
              .addSum(TBL_TRIP_FUEL_CONSUMPTIONS, COL_COSTS_QUANTITY)
              .addFrom(TBL_TRIP_FUEL_CONSUMPTIONS)
              .setWhere(SqlUtils.equals(TBL_TRIP_FUEL_CONSUMPTIONS, COL_TRIP, tripId)));

          fuel = BeeUtils.unbox(row.getDouble(COL_FUEL_BEFORE)) + BeeUtils.unbox(fill)
              - BeeUtils.unbox(consume) - BeeUtils.unbox(addit);
        }
        pair.setA(BeeUtils.toString(speedometer));
        pair.setB(BeeUtils.toString(fuel));
      }
    }
    return ResponseObject.response(pair);
  }

  private SimpleRowSet getTripDrivers(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TRIP_DRIVERS)
        .addFromLeft(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_TRIP_DRIVERS, COL_TRIP))
        .addFromLeft(TBL_DRIVERS, sys.joinTables(TBL_DRIVERS, TBL_TRIP_DRIVERS, COL_DRIVER))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_DRIVERS, COL_DRIVER_PERSON))
        .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON));

    query.addFields(TBL_TRIP_DRIVERS, COL_TRIP, COL_DRIVER,
        COL_TRIP_DRIVER_FROM, COL_TRIP_DRIVER_TO, COL_TRIP_DRIVER_NOTE);
    query.addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);

    if (condition != null) {
      query.setWhere(condition);
    }

    return qs.getData(query);
  }

  private SqlSelect getTripQuery(IsCondition where) {
    String truckJoinAlias = "truck_" + SqlUtils.uniqueName();
    String trailerJoinAlias = "trail_" + SqlUtils.uniqueName();

    return new SqlSelect().addFrom(TBL_TRIPS)
        .addFromLeft(TBL_VEHICLES, truckJoinAlias,
            SqlUtils.join(truckJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLES, trailerJoinAlias,
            SqlUtils.join(trailerJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_TRAILER))
        .addFields(TBL_TRIPS, COL_TRIP_ID, COL_TRIP_NO, COL_VEHICLE, COL_TRAILER,
            COL_TRIP_DATE, COL_TRIP_PLANNED_END_DATE, COL_TRIP_DATE_FROM, COL_TRIP_DATE_TO,
            COL_TRIP_STATUS, COL_TRIP_NOTES)
        .addField(TBL_TRIPS, sys.getVersionName(TBL_TRIPS), ALS_TRIP_VERSION)
        .addFields(TBL_TRIPS, COL_TRIP_MANAGER)
        .addField(truckJoinAlias, COL_NUMBER, ALS_VEHICLE_NUMBER)
        .addField(trailerJoinAlias, COL_NUMBER, ALS_TRAILER_NUMBER)
        .setWhere(where);
  }

  private ResponseObject getUnassignedCargos(RequestInfo reqInfo) {
    long orderId = BeeUtils.toLong(reqInfo.getParameter(COL_ORDER));

    SqlSelect query = new SqlSelect()
        .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
        .addFrom(TBL_ORDER_CARGO)
        .addFromLeft(TBL_CARGO_TRIPS, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_ORDER_CARGO, COL_ORDER, orderId),
            SqlUtils.isNull(TBL_CARGO_TRIPS, COL_CARGO)));

    return ResponseObject.response(qs.getColumn(query));
  }

  private ResponseObject getVehicleBusyDates(Long vehicle, Long trailer, Long from, Long to) {
    SqlSelect select = null;

    for (Pair<String, Long> pair : Arrays.asList(Pair.of(COL_VEHICLE, vehicle),
        Pair.of(COL_TRAILER, trailer))) {

      if (DataUtils.isId(pair.getB())) {
        SqlSelect query = new SqlSelect()
            .addFields(TBL_VEHICLES, COL_VEHICLE_NUMBER)
            .addField(TBL_TRIPS, COL_TRIP_NO, COL_NOTES)
            .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_FROM),
                SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE)), COL_ABSENCE_FROM)
            .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_TO),
                SqlUtils.field(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE)), COL_ABSENCE_TO)
            .addFrom(TBL_TRIPS)
            .addFromInner(TBL_VEHICLES,
                SqlUtils.and(sys.joinTables(TBL_VEHICLES, TBL_TRIPS, pair.getA()),
                    sys.idEquals(TBL_VEHICLES, pair.getB())))
            .setWhere(SqlUtils.or(
                SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                    SqlUtils.isNull(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE)),
                SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                    SqlUtils.more(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE, from)),
                SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                    SqlUtils.more(TBL_TRIPS, COL_TRIP_DATE_TO, from))));

        if (Objects.nonNull(to)) {
          query.setWhere(SqlUtils.and(query.getWhere(),
              SqlUtils.or(SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_TRIP_DATE_FROM),
                  SqlUtils.less(TBL_TRIPS, COL_TRIP_DATE, to)),
                  SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_FROM),
                      SqlUtils.less(TBL_TRIPS, COL_TRIP_DATE_FROM, to)))));
        }
        if (Objects.isNull(select)) {
          select = query.setUnionAllMode(false);
        } else {
          select.addUnion(query);
        }
        select.addUnion(new SqlSelect()
            .addFields(TBL_VEHICLES, COL_VEHICLE_NUMBER)
            .addExpr(SqlUtils
                .concat(SqlUtils.field(TBL_VEHICLE_SERVICE_TYPES, COL_VEHICLE_SERVICE_NAME), "' '",
                    SqlUtils.nvl(SqlUtils.field(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_NOTES),
                        "''")), COL_NOTES)
            .addField(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_DATE, COL_ABSENCE_FROM)
            .addField(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_DATE_TO, COL_ABSENCE_TO)
            .addFrom(TBL_VEHICLE_SERVICES)
            .addFromInner(TBL_VEHICLES,
                SqlUtils.and(sys.joinTables(TBL_VEHICLES, TBL_VEHICLE_SERVICES, COL_VEHICLE),
                    sys.idEquals(TBL_VEHICLES, pair.getB())))
            .addFromInner(TBL_VEHICLE_SERVICE_TYPES,
                sys.joinTables(TBL_VEHICLE_SERVICE_TYPES, TBL_VEHICLE_SERVICES,
                    COL_VEHICLE_SERVICE_TYPE))
            .setWhere(SqlUtils.and(
                SqlUtils.or(SqlUtils.isNull(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_DATE_TO),
                    SqlUtils.more(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_DATE_TO, from)),
                Objects.nonNull(to)
                    ? SqlUtils.less(TBL_VEHICLE_SERVICES, COL_VEHICLE_SERVICE_DATE, to) : null)));
      }
    }
    List<String> messages = new ArrayList<>();
    Dictionary loc = usr.getDictionary();

    for (SimpleRow row : qs.getData(select
        .addOrder(null, COL_VEHICLE_NUMBER, COL_ABSENCE_FROM, COL_ABSENCE_TO))) {

      messages.add(BeeUtils.joinWords(row.getValue(COL_VEHICLE_NUMBER),
          loc.dateFromShort(), row.getDate(COL_ABSENCE_FROM),
          Objects.isNull(row.getDate(COL_ABSENCE_TO))
              ? null : loc.dateToShort() + " " + row.getDate(COL_ABSENCE_TO),
          row.getValue(COL_NOTES)));
    }
    return ResponseObject.response(messages);
  }

  private Multimap<Long, Long> getVehicleGroups(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_VEHICLE_GROUPS, COL_VEHICLE, COL_GROUP)
        .addFrom(TBL_VEHICLE_GROUPS)
        .setWhere(condition);

    SimpleRowSet data = qs.getData(query);

    Multimap<Long, Long> result = HashMultimap.create();
    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        result.put(row.getLong(COL_VEHICLE), row.getLong(COL_GROUP));
      }
    }

    return result;
  }

  private Map<Long, Long> getVehicleManagers(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_VEHICLE_GROUPS)
        .addFromInner(TBL_TRANSPORT_GROUPS,
            sys.joinTables(TBL_TRANSPORT_GROUPS, TBL_VEHICLE_GROUPS, COL_GROUP))
        .addFields(TBL_VEHICLE_GROUPS, COL_VEHICLE)
        .addMax(TBL_TRANSPORT_GROUPS, COL_GROUP_MANAGER)
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_TRANSPORT_GROUPS, COL_GROUP_MANAGER),
            condition))
        .addGroup(TBL_VEHICLE_GROUPS, COL_VEHICLE)
        .setHaving(SqlUtils.equals(SqlUtils.aggregate(SqlFunction.COUNT_DISTINCT,
            SqlUtils.field(TBL_TRANSPORT_GROUPS, COL_GROUP_MANAGER)), 1));

    SimpleRowSet data = qs.getData(query);

    Map<Long, Long> result = new HashMap<>();
    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        result.put(row.getLong(COL_VEHICLE), row.getLong(COL_GROUP_MANAGER));
      }
    }

    return result;
  }

  private ResponseObject getVehicleRepairs(String externalId) {
    Map<String, String> cols = new LinkedHashMap<>();
    cols.put("tipas", "tipai.tipas");
    cols.put("data", "data");
    cols.put("preke", "pavad");
    cols.put("artikulas", "tr_remon.artikulas");
    cols.put("kiekis", "kiekis");
    cols.put("mato_vnt", "tr_remon.mato_vien");
    cols.put("kaina", "kaina");
    cols.put("valiuta", "valiuta");
    cols.put("pvm_plus", "pvm_stat");
    cols.put("pvm", "pvm");
    cols.put("pvm_proc", "pvm_p_md");
    cols.put("pastaba", "pastaba");

    StringBuilder sql = new StringBuilder("SELECT ");
    int c = 0;

    for (Entry<String, String> entry : cols.entrySet()) {
      if (c++ > 0) {
        sql.append(", ");
      }
      sql.append(entry.getValue()).append(" AS ").append(entry.getKey());
    }
    sql.append(" FROM tr_remon")
        .append(" INNER JOIN prekes ON tr_remon.preke=prekes.preke")
        .append(" INNER JOIN tipai ON prekes.tipas=tipai.tipas AND tipai.tip_kod IS NOT NULL")
        .append(" WHERE car_id=")
        .append(externalId);

    cols.put("artikulas", "apyv_gr.artikulas");
    cols.put("mato_vnt", "apyv_gr.mato_vien");

    for (int i = 0; i < 2; i++) {
      String wh;

      if (i > 0) {
        wh = " WHERE apyv_gr.car_id=" + externalId;
      } else {
        wh = " WHERE apyv_gr.car_id IS NULL AND apyvarta.car_id=" + externalId;
      }
      sql.append(" UNION ALL SELECT ");
      c = 0;

      for (Entry<String, String> entry : cols.entrySet()) {
        if (c++ > 0) {
          sql.append(", ");
        }
        sql.append(entry.getValue()).append(" AS ").append(entry.getKey());
      }
      sql.append(" FROM apyvarta")
          .append(" INNER JOIN apyv_gr ON apyvarta.apyv_id=apyv_gr.apyv_id")
          .append(" INNER JOIN prekes ON apyv_gr.preke=prekes.preke")
          .append(" INNER JOIN tipai ON prekes.tipas=tipai.tipas AND tipai.tip_kod IS NOT NULL")
          .append(wh);
    }
    sql.append(" ORDER BY tipas, data DESC");

    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);
    SimpleRowSet rs;

    try {
      rs = ButentWS.connect(remoteAddress, remoteLogin, remotePassword)
          .getSQLData(sql.toString(), cols.keySet().toArray(new String[0]));

    } catch (BeeException e) {
      logger.error(e);
      return ResponseObject.error(e);
    }
    return ResponseObject.response(rs);
  }

  private SimpleRowSet getVehicleServices(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_VEHICLE_SERVICES)
        .addFromLeft(TBL_VEHICLES,
            sys.joinTables(TBL_VEHICLES, TBL_VEHICLE_SERVICES, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLE_SERVICE_TYPES,
            sys.joinTables(TBL_VEHICLE_SERVICE_TYPES, TBL_VEHICLE_SERVICES,
                COL_VEHICLE_SERVICE_TYPE));

    query.addFields(TBL_VEHICLE_SERVICES, COL_VEHICLE, COL_VEHICLE_SERVICE_DATE,
        COL_VEHICLE_SERVICE_DATE_TO, COL_VEHICLE_SERVICE_NOTES);
    query.addFields(TBL_VEHICLES, COL_NUMBER);
    query.addFields(TBL_VEHICLE_SERVICE_TYPES, COL_VEHICLE_SERVICE_NAME);

    query.addOrder(TBL_VEHICLE_SERVICES, COL_VEHICLE, COL_VEHICLE_SERVICE_DATE);

    if (condition != null) {
      query.setWhere(condition);
    }

    return qs.getData(query);
  }

  private ResponseObject getVehicleTbData(String svc, Filter vehicleFilter,
      boolean filterVehiclesByTrip, VehicleType vehicleType, String themeColumnName,
      String minDateColumnName, String maxDateColumnName,
      String groupsColumnName, String completedTripsColumnName) {

    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Long theme = settings.getLong(0, settings.getColumnIndex(themeColumnName));

    JustDate minDate = settings.getDate(0, minDateColumnName);
    JustDate maxDate = settings.getDate(0, maxDateColumnName);

    String filterGroups = settings.getString(0, groupsColumnName);
    Boolean completedTrips = settings.getBoolean(0, completedTripsColumnName);

    Range<Value> period = TransportUtils.getChartPeriod(minDate, maxDate);

    String tripVehicleIdColumnName = (vehicleType == null)
        ? COL_VEHICLE : vehicleType.getTripVehicleIdColumnName();

    List<Color> colors = getThemeColors(theme);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet transportGroups = qs.getViewData(VIEW_TRANSPORT_GROUPS);
    if (!DataUtils.isEmpty(transportGroups)) {
      settings.setTableProperty(PROP_TRANSPORT_GROUPS, transportGroups.serialize());
    }

    BeeRowSet cargoTypes = qs.getViewData(VIEW_CARGO_TYPES);
    if (!DataUtils.isEmpty(cargoTypes)) {
      settings.setTableProperty(PROP_CARGO_TYPES, cargoTypes.serialize());
    }

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    Map<String, String> cityNames = getCityNames();
    if (!BeeUtils.isEmpty(cityNames)) {
      settings.setTableProperty(PROP_CITIES, Codec.beeSerialize(cityNames));
    }

    Filter vehicleTripFilter;
    if (filterVehiclesByTrip) {
      vehicleTripFilter = Filter.in(COL_VEHICLE_ID, VIEW_TRIPS, tripVehicleIdColumnName,
          getChartTripFilter(period, completedTrips));
    } else {
      vehicleTripFilter = null;
    }

    Filter vehicleGroupFilter = null;
    if (!BeeUtils.isEmpty(filterGroups) && !DataUtils.isEmpty(transportGroups)) {
      Set<Long> groups = DataUtils.parseIdSet(filterGroups);
      groups.retainAll(transportGroups.getRowIds());

      if (!groups.isEmpty()) {
        Set<Long> ids = qs.getDistinctLongs(TBL_VEHICLE_GROUPS, COL_VEHICLE,
            SqlUtils.inList(TBL_VEHICLE_GROUPS, COL_GROUP, groups));

        if (BeeUtils.isEmpty(ids)) {
          vehicleGroupFilter = Filter.isFalse();
        } else {
          vehicleGroupFilter = Filter.idIn(ids);
        }
      }
    }

    Order vehicleOrder = new Order(COL_NUMBER, true);

    BeeRowSet vehicles = qs.getViewData(VIEW_VEHICLES,
        Filter.and(vehicleFilter, vehicleTripFilter, vehicleGroupFilter), vehicleOrder);
    if (DataUtils.isEmpty(vehicles)) {
      logger.warning(svc, vehicleTripFilter, filterGroups, "vehicles not available");
      return ResponseObject.response(settings);
    }

    List<Long> vehicleIds = DataUtils.getRowIds(vehicles);
    IsCondition groupWhere = SqlUtils.inList(TBL_VEHICLE_GROUPS, COL_VEHICLE, vehicleIds);

    Multimap<Long, Long> vehicleGroups = getVehicleGroups(groupWhere);
    if (!vehicleGroups.isEmpty()) {
      for (BeeRow row : vehicles) {
        if (vehicleGroups.containsKey(row.getId())) {
          row.setProperty(PROP_VEHICLE_GROUPS,
              DataUtils.buildIdList(vehicleGroups.get(row.getId())));
        }
      }
    }

    Map<Long, Long> vehicleManagers = getVehicleManagers(groupWhere);
    if (!BeeUtils.isEmpty(vehicleManagers)) {
      for (BeeRow row : vehicles) {
        Long manager = vehicleManagers.get(row.getId());
        if (manager != null) {
          row.setProperty(PROP_VEHICLE_MANAGER, BeeUtils.toString(manager));
        }
      }
    }

    settings.setTableProperty(PROP_VEHICLES, vehicles.serialize());

    SimpleRowSet vehicleServices = getVehicleServices(SqlUtils.inList(TBL_VEHICLE_SERVICES,
        COL_VEHICLE, vehicleIds));
    if (!DataUtils.isEmpty(vehicleServices)) {
      settings.setTableProperty(PROP_VEHICLE_SERVICES, vehicleServices.serialize());
    }

    IsCondition tripWhere = SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION),
        SqlUtils.inList(TBL_TRIPS, tripVehicleIdColumnName, vehicleIds),
        getChartTripCondition(period, completedTrips));

    SqlSelect tripQuery = getTripQuery(tripWhere);
    tripQuery.addOrder(TBL_TRIPS, tripVehicleIdColumnName, COL_TRIP_DATE);

    SimpleRowSet trips = qs.getData(tripQuery);
    if (DataUtils.isEmpty(trips)) {
      logger.warning(svc, "trips not available");
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_TRIPS, trips.serialize());

    SimpleRowSet drivers = getTripDrivers(tripWhere);
    if (!DataUtils.isEmpty(drivers)) {
      settings.setTableProperty(PROP_TRIP_DRIVERS, drivers.serialize());
    }

    SqlSelect freightQuery = getFreightQuery(tripWhere);

    SimpleRowSet freights = qs.getData(freightQuery);
    if (DataUtils.isEmpty(freights)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_FREIGHTS, freights.serialize());

    SqlSelect cargoHandlingQuery = getFreightHandlingQuery(tripWhere);

    SimpleRowSet cargoHandling = qs.getData(cargoHandlingQuery);
    if (!DataUtils.isEmpty(cargoHandling)) {
      settings.setTableProperty(PROP_CARGO_HANDLING, cargoHandling.serialize());
    }

    return ResponseObject.response(settings);
  }

  private void importEmployees() {
    long historyId = sys.eventStart(PRM_SYNC_ERP_EMPLOYEES);
    SimpleRowSet rs;
    Long company = prm.getRelation(PRM_COMPANY);

    if (!DataUtils.isId(company)) {
      sys.eventError(historyId, null, "Nenurodyta pagrindinė įmonė. Parametras:", PRM_COMPANY);
      return;
    }
    try {
      rs = ButentWS.connect(prm.getText(PRM_ERP_ADDRESS), prm.getText(PRM_ERP_LOGIN),
          prm.getText(PRM_ERP_PASSWORD))
          .getEmployees(qs.getDateTime(new SqlSelect()
              .addMax(TBL_EVENT_HISTORY, COL_EVENT_STARTED)
              .addFrom(TBL_EVENT_HISTORY)
              .setWhere(SqlUtils.and(SqlUtils.equals(TBL_EVENT_HISTORY, COL_EVENT,
                  PRM_SYNC_ERP_EMPLOYEES),
                  SqlUtils.startsWith(TBL_EVENT_HISTORY, COL_EVENT_RESULT, "OK")))));
    } catch (BeeException e) {
      sys.eventError(historyId, e);
      return;
    }
    String companyDepartments = "CompanyDepartments";
    Map<String, Long> departments = getReferences(companyDepartments, "Name",
        SqlUtils.equals(companyDepartments, COL_COMPANY, company));
    Map<String, Long> positions = getReferences(TBL_POSITIONS, COL_POSITION_NAME);
    Map<String, Long> driverPosition = getReferences(TBL_POSITIONS, COL_POSITION_NAME, SqlUtils
        .notNull(TBL_POSITIONS, COL_ERP_DRIVER_SYNC));
    Map<String, Long> drivers = getReferences(TBL_DRIVERS, COL_COMPANY_PERSON);

    SimpleRowSet employees = qs.getData(new SqlSelect()
        .addField(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS), COL_COMPANY_PERSON)
        .addFields(TBL_COMPANY_PERSONS, COL_PERSON, COL_CONTACT)
        .addField(TBL_PERSONS, COL_CONTACT, COL_PERSON + COL_CONTACT)
        .addFields(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)
        .addField(PayrollConstants.TBL_EMPLOYEES, sys.getIdName(PayrollConstants.TBL_EMPLOYEES),
            PayrollConstants.COL_EMPLOYEE)
        .addFrom(TBL_COMPANY_PERSONS)
        .addFromInner(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(PayrollConstants.TBL_EMPLOYEES,
            sys.joinTables(TBL_COMPANY_PERSONS, PayrollConstants.TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .setWhere(SqlUtils.equals(TBL_COMPANY_PERSONS, COL_COMPANY, company)));

    int emplNew = 0;
    int emplUpd = 0;
    int drvNew = 0;
    int posNew = 0;
    int deptNew = 0;
    String tabNr = null;
    String cardsInfo;

    try {
      for (SimpleRow row : rs) {
        tabNr = row.getValue("CODE");
        SimpleRow info = employees.getRowByKey(PayrollConstants.COL_TAB_NUMBER, tabNr);

        Long person;
        Long personContact = null;
        Long companyPerson;
        Long contact = null;

        if (info == null) {
          person = qs.insertData(new SqlInsert(TBL_PERSONS)
              .addConstant(COL_FIRST_NAME, row.getValue("NAME"))
              .addConstant(COL_LAST_NAME, row.getValue("SURNAME")));

          companyPerson = qs.insertData(new SqlInsert(TBL_COMPANY_PERSONS)
              .addConstant(COL_COMPANY, company)
              .addConstant(COL_PERSON, person));

          qs.insertData(new SqlInsert(PayrollConstants.TBL_EMPLOYEES)
              .addConstant(COL_COMPANY_PERSON, companyPerson)
              .addConstant(PayrollConstants.COL_TAB_NUMBER, tabNr));
          emplNew++;
        } else {
          person = info.getLong(COL_PERSON);
          personContact = info.getLong(COL_PERSON + COL_CONTACT);
          companyPerson = info.getLong(COL_COMPANY_PERSON);
          contact = info.getLong(COL_CONTACT);
          emplUpd++;
        }
        String address = row.getValue("ADDRESS1");

        if (!BeeUtils.isEmpty(address)) {
          if (!DataUtils.isId(personContact)) {
            personContact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                .addConstant(COL_ADDRESS, address));

            qs.updateData(new SqlUpdate(TBL_PERSONS)
                .addConstant(COL_CONTACT, personContact)
                .setWhere(sys.idEquals(TBL_PERSONS, person)));
          } else {
            qs.updateData(new SqlUpdate(TBL_CONTACTS)
                .addConstant(COL_ADDRESS, address)
                .setWhere(sys.idEquals(TBL_CONTACTS, personContact)));
          }
        }
        String[] phones = BeeUtils.split(row.getValue("MOBILEPHONE"), BeeConst.CHAR_SEMICOLON);

        if (!ArrayUtils.isEmpty(phones)) {
          if (!DataUtils.isId(contact)) {
            contact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                .addConstant(COL_PHONE, ArrayUtils.getQuietly(phones, 0))
                .addConstant(COL_MOBILE, ArrayUtils.getQuietly(phones, 1)));

            qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
                .addConstant(COL_CONTACT, contact)
                .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));
          } else {
            qs.updateData(new SqlUpdate(TBL_CONTACTS)
                .addConstant(COL_PHONE, ArrayUtils.getQuietly(phones, 0))
                .addConstant(COL_MOBILE, ArrayUtils.getQuietly(phones, 1))
                .setWhere(sys.idEquals(TBL_CONTACTS, contact)));
          }
        }
        String email = BeeUtils.normalize(row.getValue("EMAIL"));

        if (!BeeUtils.isEmpty(email)) {
          Long emailId = qs.getLong(new SqlSelect()
              .addFields(TBL_EMAILS, sys.getIdName(TBL_EMAILS))
              .addFrom(TBL_EMAILS)
              .setWhere(SqlUtils.equals(TBL_EMAILS, COL_EMAIL_ADDRESS, email)));

          if (!DataUtils.isId(emailId)) {
            ResponseObject response = qs.insertDataWithResponse(new SqlInsert(TBL_EMAILS)
                .addConstant(COL_EMAIL_ADDRESS, email));

            if (response.hasErrors()) {
              ctx.setRollbackOnly();
              sys.eventError(historyId, null,
                  ArrayUtils.join(BeeConst.STRING_EOL, response.getErrors()),
                  BeeUtils.join(": ", PayrollConstants.COL_TAB_NUMBER, tabNr));
              return;
            }
            emailId = response.getResponseAsLong();
          }
          if (DataUtils.isId(emailId)) {
            if (!DataUtils.isId(contact)) {
              contact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                  .addConstant(COL_EMAIL, emailId));

              qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
                  .addConstant(COL_CONTACT, contact)
                  .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));
            } else {
              qs.updateData(new SqlUpdate(TBL_CONTACTS)
                  .addConstant(COL_EMAIL, emailId)
                  .setWhere(sys.idEquals(TBL_CONTACTS, contact)));
            }
          }
        }
        String department = row.getValue("DEPARTCODE");

        if (!BeeUtils.isEmpty(department) && !departments.containsKey(department)) {
          departments.put(department, qs.insertData(new SqlInsert(companyDepartments)
              .addConstant(COL_COMPANY, company)
              .addConstant("Name", department)));
          deptNew++;
        }
        String position = row.getValue("POSITIONCODE");

        if (!BeeUtils.isEmpty(position) && !positions.containsKey(position)) {
          positions.put(position, qs.insertData(new SqlInsert(TBL_POSITIONS)
              .addConstant(COL_POSITION_NAME, position)));
          posNew++;
        }
        qs.updateData(new SqlUpdate(TBL_PERSONS)
            .addConstant(COL_DATE_OF_BIRTH, TimeUtils.parseDate(row.getValue("BIRTHDAY")))
            .setWhere(sys.idEquals(TBL_PERSONS, person)));

        qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
            .addConstant(COL_DEPARTMENT, departments.get(department))
            .addConstant(COL_POSITION, positions.get(position))
            .addConstant(PayrollConstants.COL_DATE_OF_EMPLOYMENT,
                TimeUtils.parseDate(row.getValue("DIRBA_NUO")))
            .addConstant(PayrollConstants.COL_DATE_OF_DISMISSAL,
                TimeUtils.parseDate(row.getValue("DISMISSED")))
            .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));

        if (!BeeUtils.isEmpty(driverPosition) && Objects
            .equals(positions.get(position), driverPosition.get(position))
            && !drivers.containsKey(BeeUtils.toString(companyPerson))) {

          drivers.put(BeeUtils.toString(companyPerson),
              qs.insertData(new SqlInsert(TBL_DRIVERS)
                  .addConstant(COL_COMPANY_PERSON, companyPerson)));
          drvNew++;
        }
      }
      cardsInfo = importTimeCards();

    } catch (Throwable e) {
      ctx.setRollbackOnly();
      sys.eventError(historyId, e, BeeUtils.join(": ", PayrollConstants.COL_TAB_NUMBER, tabNr));
      return;
    }
    sys.eventEnd(historyId, "OK", deptNew > 0 ? companyDepartments + ": +" + deptNew : null,
        posNew > 0 ? TBL_POSITIONS + ": +" + posNew : null,
        (emplNew + emplUpd) > 0
            ? PayrollConstants.TBL_EMPLOYEES + ":" + (emplNew > 0 ? " +" + emplNew : "")
            + (emplUpd > 0 ? " " + emplUpd : "") : null,
        drvNew > 0 ? TBL_DRIVERS + ": +" + drvNew : null, cardsInfo);
  }

  private String importTimeCards() throws BeeException {
    SimpleRowSet rs = ButentWS.connect(prm.getText(PRM_ERP_ADDRESS), prm.getText(PRM_ERP_LOGIN),
        prm.getText(PRM_ERP_PASSWORD))
        .getTimeCards(qs.getDateTime(new SqlSelect()
            .addMax(TBL_EVENT_HISTORY, COL_EVENT_STARTED)
            .addFrom(TBL_EVENT_HISTORY)
            .setWhere(SqlUtils.and(SqlUtils.equals(TBL_EVENT_HISTORY, COL_EVENT,
                PRM_SYNC_ERP_EMPLOYEES),
                SqlUtils.startsWith(TBL_EVENT_HISTORY, COL_EVENT_RESULT, "OK")))));

    SimpleRowSet drivers = qs.getData(new SqlSelect()
        .addField(TBL_DRIVERS, sys.getIdName(TBL_DRIVERS), COL_DRIVER)
        .addFields(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)
        .addFrom(TBL_DRIVERS)
        .addFromInner(PayrollConstants.TBL_EMPLOYEES,
            SqlUtils.joinUsing(TBL_DRIVERS, PayrollConstants.TBL_EMPLOYEES, COL_COMPANY_PERSON)));

    Map<String, Long> absenceTypes = getReferences(TBL_ABSENCE_TYPES, COL_ABSENCE_NAME);

    int tp = 0;
    int ins = 0;
    int upd = 0;
    int del = 0;

    for (SimpleRow row : rs) {
      Long id = row.getLong("D_TAB_ID");
      String tabNumber = row.getValue("TAB_NR");

      if (BeeUtils.isEmpty(tabNumber)) {
        del += qs.updateData(new SqlDelete(TBL_DRIVER_ABSENCE)
            .setWhere(SqlUtils.equals(TBL_DRIVER_ABSENCE, COL_COSTS_EXTERNAL_ID, id)));
        continue;
      }
      Long driver = BeeUtils.toLongOrNull(drivers.getValueByKey(PayrollConstants.COL_TAB_NUMBER,
          tabNumber, COL_DRIVER));

      if (!DataUtils.isId(driver)) {
        continue;
      }
      String type = sys.clampValue(TBL_ABSENCE_TYPES, COL_ABSENCE_NAME, row.getValue("PAVAD"));

      if (!absenceTypes.containsKey(type)) {
        absenceTypes.put(type, qs.insertData(new SqlInsert(TBL_ABSENCE_TYPES)
            .addConstant(COL_ABSENCE_NAME, type)
            .addConstant(COL_ABSENCE_LABEL,
                sys.clampValue(TBL_ABSENCE_TYPES, COL_ABSENCE_LABEL, row.getValue("TAB_KODAS")))));
        tp++;
      }
      int c = qs.updateData(new SqlUpdate(TBL_DRIVER_ABSENCE)
          .addConstant(COL_DRIVER, driver)
          .addConstant(COL_ABSENCE, absenceTypes.get(type))
          .addConstant(COL_ABSENCE_FROM, TimeUtils.parseDate(row.getValue("DATA_NUO")))
          .addConstant(COL_ABSENCE_TO, TimeUtils.parseDate(row.getValue("DATA_IKI")))
          .addConstant(COL_ABSENCE_NOTES, row.getValue("ISAK_PAVAD"))
          .setWhere(SqlUtils.equals(TBL_DRIVER_ABSENCE, COL_COSTS_EXTERNAL_ID, id)));

      if (BeeUtils.isPositive(c)) {
        upd++;
      } else {
        qs.insertData(new SqlInsert(TBL_DRIVER_ABSENCE)
            .addConstant(COL_DRIVER, driver)
            .addConstant(COL_ABSENCE, absenceTypes.get(type))
            .addConstant(COL_ABSENCE_FROM, TimeUtils.parseDate(row.getValue("DATA_NUO")))
            .addConstant(COL_ABSENCE_TO, TimeUtils.parseDate(row.getValue("DATA_IKI")))
            .addConstant(COL_ABSENCE_NOTES, row.getValue("ISAK_PAVAD"))
            .addConstant(COL_COSTS_EXTERNAL_ID, id));
        ins++;
      }
    }
    return BeeUtils.join(BeeConst.STRING_EOL, tp > 0 ? TBL_ABSENCE_TYPES + ": +" + tp : null,
        (ins + upd + del) > 0 ? TBL_DRIVER_ABSENCE + ":" + (ins > 0 ? " +" + ins : "")
            + (upd > 0 ? " " + upd : "") + (del > 0 ? " -" + del : "") : null);
  }

  private void importVehicles() {
    long historyId = sys.eventStart(PRM_SYNC_ERP_VEHICLES);
    SimpleRowSet rs;

    try {
      rs = ButentWS.connect(prm.getText(PRM_ERP_ADDRESS), prm.getText(PRM_ERP_LOGIN),
          prm.getText(PRM_ERP_PASSWORD))
          .getCars(qs.getDateTime(new SqlSelect()
              .addMax(TBL_EVENT_HISTORY, COL_EVENT_STARTED)
              .addFrom(TBL_EVENT_HISTORY)
              .setWhere(SqlUtils.and(SqlUtils.equals(TBL_EVENT_HISTORY, COL_EVENT,
                  PRM_SYNC_ERP_VEHICLES),
                  SqlUtils.startsWith(TBL_EVENT_HISTORY, COL_EVENT_RESULT, "OK")))));
    } catch (BeeException e) {
      logger.error(e);
      sys.eventError(historyId, e);
      return;
    }
    Map<String, String> mappings = new HashMap<>();
    mappings.put(COL_VEHICLE_NUMBER, "VALST_NR");
    mappings.put(COL_VEHICLE_TYPE, "TIPAS");
    mappings.put(COL_MODEL, "MODELIS");
    mappings.put("ProductionDate", "PAG_METAI");
    mappings.put("EngineVolume", "KUBATURA");
    mappings.put("BodyNumber", "KEBUL_NR");
    mappings.put("EngineNumber", "VARIKL_NR");
    mappings.put(COL_NOTES, "NOTES");
    mappings.put("TankCapacity", "BAKAS");
    mappings.put("Speedometer", "SKALE");
    mappings.put("Power", "GALIA");
    mappings.put("Netto", "NETO");
    mappings.put("Brutto", "BRUTO");
    mappings.put(COL_OWNER, "SAVININKAS");
    int tp = 0;
    int md = 0;
    int vhNew = 0;
    int vhUpd = 0;

    String name = "Name";

    Map<String, Long> vehicleNumbers = getReferences(TBL_VEHICLES, COL_VEHICLE_NUMBER);
    Map<String, Long> vehicles = getReferences(TBL_VEHICLES, COL_ITEM_EXTERNAL_CODE);
    Map<String, Long> types = getReferences(TBL_VEHICLE_TYPES, name);
    Map<String, Long> models = getReferences(TBL_VEHICLE_MODELS, name);
    Map<String, Long> companyCodes = getReferences(TBL_COMPANIES, COL_COMPANY_CODE);
    Map<String, Long> companies = getReferences(TBL_COMPANIES, COL_COMPANY_NAME);

    for (SimpleRow row : rs) {
      String type = row.getValue("TIPAS");

      if (!types.containsKey(type)) {
        types.put(type, qs.insertData(new SqlInsert(TBL_VEHICLE_TYPES)
            .addConstant(name, type)));
        tp++;
      }
      String model = row.getValue("MODELIS");

      if (!models.containsKey(model)) {
        models.put(model, qs.insertData(new SqlInsert(TBL_VEHICLE_MODELS)
            .addConstant(name, model)));
        md++;
      }
      String externalId = row.getValue("CAR_ID");

      if (!vehicles.containsKey(externalId)) {
        Long id = vehicleNumbers.get(row.getValue("VALST_NR"));

        if (DataUtils.isId(id)) {
          qs.updateData(new SqlUpdate(TBL_VEHICLES)
              .addConstant(COL_ITEM_EXTERNAL_CODE, externalId)
              .setWhere(sys.idEquals(TBL_VEHICLES, id)));

          vehicles.put(externalId, id);
        }
      }
      String owner = row.getValue("SAVININKAS");

      if (!BeeUtils.isEmpty(owner)) {
        String companyCode = row.getValue("KODAS");
        Long id = companyCodes.get(companyCode);

        if (DataUtils.isId(id)) {
          companies.put(owner, id);
        } else {
          id = companies.get(owner);
        }
        if (!DataUtils.isId(id)) {
          id = qs.insertData(new SqlInsert(TBL_COMPANIES)
              .addConstant(COL_COMPANY_NAME, owner)
              .addNotEmpty(COL_COMPANY_CODE, companyCode));

          if (!BeeUtils.isEmpty(companyCode)) {
            companyCodes.put(companyCode, id);
          }
          companies.put(owner, id);
        }
      }
      SqlInsert insert = null;
      SqlUpdate update = null;

      if (vehicles.containsKey(externalId)) {
        update = new SqlUpdate(TBL_VEHICLES)
            .setWhere(sys.idEquals(TBL_VEHICLES, vehicles.get(externalId)));
      } else {
        insert = new SqlInsert(TBL_VEHICLES)
            .addConstant(COL_ITEM_EXTERNAL_CODE, externalId);
      }
      for (String key : mappings.keySet()) {
        Object value;

        switch (key) {
          case COL_VEHICLE_TYPE:
            value = types.get(type);
            break;
          case COL_MODEL:
            value = models.get(model);
            break;
          case "ProductionDate":
            Integer year = row.getInt(mappings.get(key));

            if (BeeUtils.isPositive(year)) {
              value = TimeUtils.startOfYear(year);
            } else {
              value = null;
            }
            break;
          case COL_OWNER:
            value = companies.get(owner);
            break;
          default:
            value = row.getValue(mappings.get(key));

            if (BeeUtils.isEmpty((String) value)) {
              value = null;
            }
            break;
        }
        if (insert != null) {
          insert.addNotNull(key, value);
        } else {
          update.addConstant(key, value);
        }
      }
      try {
        if (insert != null) {
          qs.insertData(insert);
          vhNew++;
        } else {
          qs.updateData(update);
          vhUpd++;
        }
      } catch (Throwable e) {
        sys.eventError(historyId, e);
        return;
      }
    }
    sys.eventEnd(historyId, "OK", tp > 0 ? TBL_VEHICLE_TYPES + ": +" + tp : null,
        md > 0 ? TBL_VEHICLE_MODELS + ": +" + md : null,
        (vhNew + vhUpd) > 0 ? TBL_VEHICLES + ":" + (vhNew > 0 ? " +" + vhNew : "")
            + (vhUpd > 0 ? " " + vhUpd : "") : null);
  }

  private ResponseObject sendMessage(String message, String[] recipients) {
    String address = prm.getText("SmsServiceAddress");

    if (BeeUtils.isEmpty(address)) {
      return ResponseObject.error("SmsServiceAddress is empty");
    }
    StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
        .append("<sms-send>")
        .append("<authentication>")
        .append(XmlUtils.tag("username", prm.getText("SmsUserName")))
        .append(XmlUtils.tag("password", prm.getText("SmsPassword")))
        .append(XmlUtils.tag("serviceId", prm.getText("SmsServiceId")))
        .append("</authentication>")
        .append("<originator>")
        .append(XmlUtils.tag("source", prm.getText("SmsDisplayText")))
        .append("</originator>")
        .append("<sms-messages>");

    for (String phone : recipients) {
      xml.append("<sms>")
          .append(XmlUtils.tag("destination", phone))
          .append(XmlUtils.tag("msg", message))
          .append(XmlUtils.tag("dr", true))
          .append(XmlUtils.tag("id", 0))
          .append(XmlUtils.tag("sendTime", new DateTime().toString()))
          .append("</sms>");
    }
    xml.append("</sms-messages>")
        .append("</sms-send>");

    ResponseObject response = ResponseObject.info(Localized.dictionary().messageSent());
    BufferedWriter wr = null;
    BufferedReader in = null;
    HttpURLConnection conn = null;

    try {
      conn = (HttpURLConnection) new URL(address).openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);

      wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),
          BeeConst.CHARSET_UTF8));
      wr.write(xml.toString());
      wr.close();

      if (conn.getResponseCode() != HttpServletResponse.SC_OK) {
        response = ResponseObject.error(Localized.dictionary().error(), conn.getResponseCode(),
            conn.getResponseMessage());
      } else {
        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String input = in.readLine();

        while (input != null) {
          sb.append(input);
          input = in.readLine();
        }
        in.close();
        input = sb.toString();
        String status = XmlUtils.getText(input, "status");

        if (!BeeUtils.same(status, "OK")) {
          response = ResponseObject.error(input);
        }
      }
    } catch (IOException e) {
      logger.error(e);
      response = ResponseObject.error(e);

      try {
        if (wr != null) {
          wr.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        logger.error(ex);
      }
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return response;
  }

  private ResponseObject updatePercent(Long id, String column, Double amount, boolean percentMode) {
    String keyName;
    Function<Long, Double> totalSupplier;

    switch (column) {
      case COL_TRIP_PERCENT:
        keyName = COL_CARGO;
        totalSupplier = value -> BeeUtils.unbox(qs.getDouble(new SqlSelect()
            .addFields("als", "CargoIncome")
            .addFrom(rep.getCargoIncomeQuery(new SqlSelect().addConstant(value, keyName),
                prm.getRelation(PRM_CURRENCY), BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT))),
                "als")));
        break;
      case COL_CARGO_PERCENT:
        keyName = COL_TRIP;
        totalSupplier = value -> {
          String tmp = rep.getTripCosts(new SqlSelect().addConstant(value, keyName),
              prm.getRelation(PRM_CURRENCY), BeeUtils.unbox(prm.getBoolean(PRM_EXCLUDE_VAT)));
          SimpleRow row = qs.getRow(new SqlSelect().addAllFields(tmp).addFrom(tmp));
          qs.sqlDropTemp(tmp);

          return BeeUtils.unbox(row.getDouble("DailyCosts"))
              + BeeUtils.unbox(row.getDouble("RoadCosts"))
              + BeeUtils.unbox(row.getDouble("OtherCosts"))
              + BeeUtils.unbox(row.getDouble("FuelCosts"));
        };
        break;
      default:
        Assert.unsupported(column);
        return null;
    }
    long keyValue = qs.getLong(new SqlSelect()
        .addFields(TBL_CARGO_TRIPS, keyName)
        .addFrom(TBL_CARGO_TRIPS)
        .setWhere(sys.idEquals(TBL_CARGO_TRIPS, id)));

    if (!DataUtils.isId(keyValue)) {
      return ResponseObject.error(Localized.dictionary().noData());
    }
    double availablePercent = BeeUtils.max(100 - BeeUtils.unbox(qs.getDouble(new SqlSelect()
        .addSum(TBL_CARGO_TRIPS, column)
        .addFrom(TBL_CARGO_TRIPS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CARGO_TRIPS, keyName, keyValue),
            SqlUtils.notEqual(TBL_CARGO_TRIPS, sys.getIdName(TBL_CARGO_TRIPS), id))))), 0.0);

    Double percent = null;

    if (Objects.nonNull(amount)) {
      percent = Math.max(amount, 0);
    }
    if (BeeUtils.isPositive(percent)) {
      if (percentMode) {
        if (percent > availablePercent) {
          return ResponseObject.error(percent + "% > " + availablePercent + "%");
        }
      } else {
        double total = totalSupplier.apply(keyValue);
        double availableAmount = BeeUtils.round(total / 100 * availablePercent, 2);

        if (percent > availableAmount) {
          return ResponseObject.error(percent + " > " + availableAmount);
        }
        percent = percent / total * 100;
      }
    }
    qs.updateData(new SqlUpdate(TBL_CARGO_TRIPS)
        .addConstant(column, percent)
        .setWhere(sys.idEquals(TBL_CARGO_TRIPS, id)));

    return ResponseObject.emptyResponse();
  }
}
