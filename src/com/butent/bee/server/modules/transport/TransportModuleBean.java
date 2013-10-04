package com.butent.bee.server.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.ParameterEvent;
import com.butent.bee.server.modules.ParameterEventHandler;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.VehicleType;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.webservice.ButentWS;
import com.butent.webservice.WSDocument;
import com.butent.webservice.WSDocument.WSDocumentItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

@Stateless
@LocalBean
public class TransportModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(TransportModuleBean.class);

  private static IsCondition tripCondition(IsCondition where) {
    return SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION), where);
  }

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

  @Resource
  TimerService timerService;

  private Timer erpTimer;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(COMMONS_MODULE, TRADE_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return qs.getSearchResults(VIEW_VEHICLES, Filter.anyContains(Sets.newHashSet(COL_NUMBER,
        COL_PARENT_MODEL_NAME, COL_MODEL_NAME, COL_OWNER_NAME), query));
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(TRANSPORT_METHOD);

    if (BeeUtils.same(svc, SVC_GET_BEFORE)) {
      long vehicle = BeeUtils.toLong(reqInfo.getParameter("Vehicle"));
      long date = BeeUtils.toLong(reqInfo.getParameter("Date"));

      response = getTripBeforeData(vehicle, date);
    } else if (BeeUtils.same(svc, SVC_GET_UNASSIGNED_CARGOS)) {
      response = getUnassignedCargos(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_PROFIT)) {
      if (reqInfo.hasParameter(VAR_TRIP_ID)) {
        response = getTripProfit(BeeUtils.toLong(reqInfo.getParameter(VAR_TRIP_ID)));

      } else if (reqInfo.hasParameter(VAR_CARGO_ID)) {
        Long cargoId = BeeUtils.toLong(reqInfo.getParameter(VAR_CARGO_ID));

        response = getCargoProfit(new SqlSelect().addConstant(cargoId, COL_CARGO));

      } else if (reqInfo.hasParameter(COL_ORDER)) {
        Long orderId = BeeUtils.toLong(reqInfo.getParameter(COL_ORDER));
        String cargo = VIEW_ORDER_CARGO;

        response = getCargoProfit(new SqlSelect()
            .addField(cargo, sys.getIdName(cargo), COL_CARGO)
            .addFrom(cargo)
            .setWhere(SqlUtils.equals(cargo, COL_ORDER, orderId)));

      } else {
        response = ResponseObject.error("Profit of WHAT?");
      }

    } else if (BeeUtils.same(svc, SVC_GET_FX_DATA)) {
      response = getFxData();

    } else if (BeeUtils.same(svc, SVC_GET_SS_DATA)) {
      response = getVehicleTbData(svc, Filter.in(COL_VEHICLE_ID, VIEW_TRIPS, COL_VEHICLE),
          VehicleType.TRUCK, COL_SS_THEME);

    } else if (BeeUtils.same(svc, SVC_GET_DTB_DATA)) {
      response = getDtbData();

    } else if (BeeUtils.same(svc, SVC_GET_TRUCK_TB_DATA)) {
      response = getVehicleTbData(svc, Filter.notEmpty(COL_IS_TRUCK), VehicleType.TRUCK,
          COL_TRUCK_THEME);

    } else if (BeeUtils.same(svc, SVC_GET_TRAILER_TB_DATA)) {
      response = getVehicleTbData(svc, Filter.notEmpty(COL_IS_TRAILER), VehicleType.TRAILER,
          COL_TRAILER_THEME);

    } else if (BeeUtils.same(svc, SVC_GET_COLORS)) {
      response = getColors(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CARGO_USAGE)) {
      response = getCargoUsage(reqInfo.getParameter("ViewName"),
          Codec.beeDeserializeCollection(reqInfo.getParameter("IdList")));

    } else if (BeeUtils.same(svc, SVC_GET_ASSESSMENT_TOTALS)) {
      response = getAssessmentTotals(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ASSESSOR)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CARGO)));

    } else if (BeeUtils.same(svc, SVC_CREATE_INVOICE_ITEMS)) {
      Long saleId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SALE));
      Long currency = BeeUtils.toLongOrNull(reqInfo.getParameter(ExchangeUtils.COL_CURRENCY));
      Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter("IdList"));
      Long item = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ITEM));

      if (DataUtils.isId(saleId)) {
        response = createInvoiceItems(saleId, currency, ids, item);
      } else {
        response = createCreditInvoiceItems(
            BeeUtils.toLongOrNull(reqInfo.getParameter(COL_PURCHASE)),
            BeeUtils.toDoubleOrNull(reqInfo.getParameter(COL_TRADE_AMOUNT)),
            currency, ids, item);
      }
    } else if (BeeUtils.same(svc, SVC_SEND_TO_ERP)) {
      response = sendToERP(reqInfo.getParameter("view_name"),
          DataUtils.parseIdSet(reqInfo.getParameter("IdList")));

    } else {
      String msg = BeeUtils.joinWords("Transport service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return Lists.newArrayList(
        new BeeParameter(TRANSPORT_MODULE, "ERPCreditOperation", ParameterType.TEXT,
            "Credit document operation name in ERP system", false, null),
        new BeeParameter(TRANSPORT_MODULE, PRM_ERP_REFRESH_INTERVAL, ParameterType.NUMBER,
            "Interval of ERP payments renewal in minutes", false, null));
  }

  @Override
  public String getName() {
    return TRANSPORT_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    initTimer();

    prm.registerParameterEventHandler(new ParameterEventHandler() {
      @Subscribe
      public void initTimers(ParameterEvent event) {
        if (BeeUtils.same(event.getModule(), TRANSPORT_MODULE)
            && BeeUtils.same(event.getParameter(), PRM_ERP_REFRESH_INTERVAL)) {

          initTimer();
        }
      }
    });

    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void calcAssessmentAmounts(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_CARGO_ASSESSORS) && event.isAfter()) {
          for (int i = 0; i < 2; i++) {
            String tbl = (i == 0) ? TBL_CARGO_INCOMES : TBL_CARGO_EXPENSES;

            SqlSelect query = new SqlSelect()
                .addField(TBL_CARGO_ASSESSORS, sys.getIdName(TBL_CARGO_ASSESSORS), COL_ASSESSOR)
                .addFrom(TBL_CARGO_ASSESSORS)
                .addFromInner(TBL_ORDER_CARGO,
                    sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_ASSESSORS, COL_CARGO))
                .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
                .addFromLeft(tbl, sys.joinTables(TBL_CARGO_ASSESSORS, tbl, COL_ASSESSOR))
                .setWhere(event.getQuery().getWhere())
                .addGroup(TBL_CARGO_ASSESSORS, sys.getIdName(TBL_CARGO_ASSESSORS));

            IsExpression xpr = ExchangeUtils.exchangeFieldTo(query,
                SqlUtils.field(tbl, COL_AMOUNT),
                SqlUtils.field(tbl, ExchangeUtils.COL_CURRENCY),
                SqlUtils.nvl(SqlUtils.field(tbl, COL_DATE), SqlUtils.field(TBL_ORDERS, COL_DATE)),
                SqlUtils.field(TBL_ORDER_CARGO, ExchangeUtils.COL_CURRENCY));

            SimpleRowSet rs = qs.getData(query.addSum(xpr, VAR_TOTAL));

            for (BeeRow row : event.getRowset().getRows()) {
              row.setProperty((i == 0) ? VAR_INCOME : VAR_EXPENSE,
                  rs.getValueByKey(COL_ASSESSOR, BeeUtils.toString(row.getId()), VAR_TOTAL));
            }
          }
        }
      }

      @Subscribe
      public void fillCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), VIEW_ORDER_CARGO) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            SimpleRowSet rs = qs.getData(getCargoIncomeQuery(event.getQuery()
                .resetFields().resetOrder().resetGroup()
                .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
                .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO))));

            for (BeeRow row : rowset.getRows()) {
              String cargoId = BeeUtils.toString(row.getId());
              String cargoIncome = rs.getValueByKey(COL_CARGO, cargoId, "CargoIncome");
              String servicesIncome = rs.getValueByKey(COL_CARGO, cargoId, "ServicesIncome");

              row.setProperty(VAR_INCOME, BeeUtils.toString(BeeUtils.toDouble(cargoIncome)
                  + BeeUtils.toDouble(servicesIncome)));
            }
          }
        }
      }

      @Subscribe
      public void fillFuelConsumptions(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), VIEW_TRIP_ROUTES) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            SimpleRowSet rs = qs.getData(getFuelConsumptionsQuery(event.getQuery()
                .resetFields().resetOrder().resetGroup()
                .addFields(VIEW_TRIP_ROUTES, sys.getIdName(VIEW_TRIP_ROUTES))
                .addGroup(VIEW_TRIP_ROUTES, sys.getIdName(VIEW_TRIP_ROUTES)), true));

            int colIndex = rowset.getColumnIndex("Consumption");

            for (BeeRow row : rowset.getRows()) {
              row.setValue(colIndex, rs.getValueByKey(sys.getIdName(VIEW_TRIP_ROUTES),
                  BeeUtils.toString(row.getId()), "Quantity"));
            }
          }
        }
      }

      @Subscribe
      public void fillTripCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), VIEW_TRIP_CARGO) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            String crs = getTripIncome(event.getQuery().resetFields().resetOrder().resetGroup()
                .addFields(VIEW_CARGO_TRIPS, COL_TRIP).addGroup(VIEW_CARGO_TRIPS, COL_TRIP));

            SimpleRowSet rs = qs.getData(new SqlSelect().addAllFields(crs).addFrom(crs));
            qs.sqlDropTemp(crs);

            int cargoIndex = rowset.getColumnIndex(COL_CARGO);

            for (BeeRow row : rowset.getRows()) {
              row.setProperty(VAR_INCOME, rs.getValueByKey(COL_CARGO, row.getString(cargoIndex),
                  "TripIncome"));
            }
          }
        }
      }

      @Subscribe
      public void getVisibleDrivers(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_DRIVERS) && event.isBefore()) {
          BeeView view = sys.getView(event.getTargetName());

          SqlSelect query = new SqlSelect().setDistinctMode(true)
              .addFields(TBL_DRIVER_GROUPS, COL_DRIVER)
              .addFrom(TBL_DRIVER_GROUPS)
              .addFromInner(TBL_TRANSPORT_GROUPS,
                  sys.joinTables(TBL_TRANSPORT_GROUPS, TBL_DRIVER_GROUPS, COL_GROUP));

          sys.filterVisibleState(query, TBL_TRANSPORT_GROUPS);

          event.getQuery().addFromInner(query, "subq",
              SqlUtils.join(view.getSourceAlias(), view.getSourceIdName(), "subq", COL_DRIVER));
        }
      }

      @Subscribe
      public void getVisibleVehicles(ViewQueryEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_VEHICLES) && event.isBefore()) {
          BeeView view = sys.getView(event.getTargetName());

          SqlSelect query = new SqlSelect().setDistinctMode(true)
              .addFields(TBL_VEHICLE_GROUPS, COL_VEHICLE)
              .addFrom(TBL_VEHICLE_GROUPS)
              .addFromInner(TBL_TRANSPORT_GROUPS,
                  sys.joinTables(TBL_TRANSPORT_GROUPS, TBL_VEHICLE_GROUPS, COL_GROUP));

          sys.filterVisibleState(query, TBL_TRANSPORT_GROUPS);

          event.getQuery().addFromInner(query, "subq",
              SqlUtils.join(view.getSourceAlias(), view.getSourceIdName(), "subq", COL_VEHICLE));
        }
      }
    });
  }

  private ResponseObject createCreditInvoiceItems(Long purchaseId, Double amount, Long currency,
      Set<Long> idList, Long mainItem) {

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
        .addFields(TBL_SALES, COL_TRADE_INVOICE_PREFIX, COL_TRADE_INVOICE_NO)
        .addFields(TBL_CARGO_INCOMES, COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC)
        .addFrom(TBL_CARGO_INCOMES)
        .addFromInner(TBL_SERVICES,
            sys.joinTables(TBL_SERVICES, TBL_CARGO_INCOMES, COL_SERVICE))
        .addFromInner(TBL_SALES,
            sys.joinTables(TBL_SALES, TBL_CARGO_INCOMES, COL_SALE))
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .setWhere(SqlUtils.and(wh, SqlUtils.positive(TBL_CARGO_INCOMES, COL_AMOUNT)))
        .addGroup(TBL_ORDERS, COL_ORDER_NO)
        .addGroup(TBL_SALES, COL_TRADE_INVOICE_PREFIX, COL_TRADE_INVOICE_NO)
        .addGroup(TBL_CARGO_INCOMES, COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC);

    if (DataUtils.isId(mainItem)) {
      ss.addConstant(mainItem, COL_ITEM);
    } else {
      ss.addFields(TBL_SERVICES, COL_ITEM)
          .addGroup(TBL_SERVICES, COL_ITEM);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_INCOMES, ExchangeUtils.COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE)), SqlUtils.constant(currency));

    SimpleRowSet rs = qs.getData(ss.addSum(xpr, COL_AMOUNT));
    ResponseObject response = new ResponseObject();

    double totalAmount = 0;

    for (Double n : rs.getDoubleColumn(COL_AMOUNT)) {
      totalAmount += n;
    }
    for (SimpleRow row : rs) {
      String xml = XmlUtils.createString("CreditInfo",
          COL_ORDER_NO, row.getValue(COL_ORDER_NO),
          COL_TRADE_INVOICE_NO, BeeUtils.joinWords(row.getValue(COL_TRADE_INVOICE_PREFIX),
              row.getValue(COL_TRADE_INVOICE_NO)));

      SqlInsert insert = new SqlInsert(TBL_PURCHASE_ITEMS)
          .addConstant(COL_PURCHASE, purchaseId)
          .addConstant(COL_ITEM, row.getLong(COL_ITEM))
          .addConstant(COL_TRADE_ITEM_QUANTITY, 1)
          .addConstant(COL_TRADE_ITEM_PRICE,
              BeeUtils.round(amount * row.getDouble(COL_AMOUNT) / totalAmount, 2))
          .addConstant(COL_TRADE_ITEM_VAT, row.getDouble(COL_TRADE_ITEM_VAT))
          .addConstant(COL_TRADE_ITEM_VAT_PERC, row.getBoolean(COL_TRADE_ITEM_VAT_PERC))
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
    String loadCity = SqlUtils.uniqueName();
    String unloadCity = SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_ORDERS, COL_ORDER_NO)
        .addFields(TBL_ORDER_CARGO, COL_CARGO_CMR)
        .addFields(TBL_CARGO_INCOMES, COL_CARGO, COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC)
        .addField(loadPlace, COL_DATE, COL_LOADING_PLACE)
        .addField(unloadPlace, COL_DATE, COL_UNLOADING_PLACE)
        .addField(loadPlace, COL_POST_INDEX, loadPlace)
        .addField(unloadPlace, COL_POST_INDEX, unloadPlace)
        .addField(loadCity, COL_CITY_NAME, loadCity)
        .addField(unloadCity, COL_CITY_NAME, unloadCity)
        .addField(loadCountry, COL_COUNTRY_CODE, loadCountry)
        .addField(unloadCountry, COL_COUNTRY_CODE, unloadCountry)
        .addFrom(TBL_CARGO_INCOMES)
        .addFromInner(TBL_SERVICES,
            sys.joinTables(TBL_SERVICES, TBL_CARGO_INCOMES, COL_SERVICE))
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_PLACES, loadPlace,
            sys.joinTables(TBL_CARGO_PLACES, loadPlace, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CITIES, loadCity,
            sys.joinTables(TBL_CITIES, loadCity, loadPlace, COL_CITY))
        .addFromLeft(TBL_COUNTRIES, loadCountry,
            sys.joinTables(TBL_COUNTRIES, loadCountry, loadPlace, COL_COUNTRY))
        .addFromLeft(TBL_CARGO_PLACES, unloadPlace,
            sys.joinTables(TBL_CARGO_PLACES, unloadPlace, TBL_ORDER_CARGO,
                COL_UNLOADING_PLACE))
        .addFromLeft(TBL_CITIES, unloadCity,
            sys.joinTables(TBL_CITIES, unloadCity, unloadPlace, COL_CITY))
        .addFromLeft(TBL_COUNTRIES, unloadCountry,
            sys.joinTables(TBL_COUNTRIES, unloadCountry, unloadPlace, COL_COUNTRY))
        .setWhere(wh)
        .addGroup(TBL_ORDERS, COL_ORDER_NO)
        .addGroup(TBL_ORDER_CARGO, COL_CARGO_CMR)
        .addGroup(TBL_CARGO_INCOMES, COL_CARGO, COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC)
        .addGroup(loadPlace, COL_DATE, COL_POST_INDEX)
        .addGroup(unloadPlace, COL_DATE, COL_POST_INDEX)
        .addGroup(loadCity, COL_CITY_NAME)
        .addGroup(unloadCity, COL_CITY_NAME)
        .addGroup(loadCountry, COL_COUNTRY_CODE)
        .addGroup(unloadCountry, COL_COUNTRY_CODE);

    if (DataUtils.isId(mainItem)) {
      ss.addConstant(mainItem, COL_ITEM);
    } else {
      ss.addFields(TBL_SERVICES, COL_ITEM)
          .addGroup(TBL_SERVICES, COL_ITEM);
    }
    IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss,
        SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_INCOMES, ExchangeUtils.COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE)), SqlUtils.constant(currency));

    SimpleRowSet rs = qs.getData(ss.addSum(xpr, COL_AMOUNT));
    ResponseObject response = new ResponseObject();

    String vehicle = SqlUtils.uniqueName();
    String trailer = SqlUtils.uniqueName();

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
        .setWhere(SqlUtils.inList(TBL_CARGO_TRIPS, COL_CARGO,
            Sets.newHashSet(rs.getLongColumn(COL_CARGO)))));

    Multimap<Long, String> drivers = HashMultimap.create();
    Multimap<Long, String> vehicles = HashMultimap.create();

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
    for (SimpleRow row : rs) {
      String xml = XmlUtils.createString("CargoInfo",
          COL_ORDER_NO, row.getValue(COL_ORDER_NO),
          COL_LOADING_PLACE,
          BeeUtils.joinWords(JustDate.get(row.getDateTime(COL_LOADING_PLACE)),
              row.getValue(loadCountry),
              BeeUtils.parenthesize(BeeUtils.joinItems(row.getValue(loadPlace),
                  row.getValue(loadCity)))),
          COL_UNLOADING_PLACE,
          BeeUtils.joinWords(JustDate.get(row.getDateTime(COL_UNLOADING_PLACE)),
              row.getValue(unloadCountry),
              BeeUtils.parenthesize(BeeUtils.joinItems(row.getValue(unloadPlace),
                  row.getValue(unloadCity)))),
          COL_CARGO_CMR, row.getValue(COL_CARGO_CMR),
          COL_DRIVER, BeeUtils.joinItems(drivers.get(row.getLong(COL_CARGO))),
          COL_VEHICLE, BeeUtils.joinItems(vehicles.get(row.getLong(COL_CARGO))));

      SqlInsert insert = new SqlInsert(TBL_SALE_ITEMS)
          .addConstant(COL_SALE, saleId)
          .addConstant(COL_ITEM, row.getLong(COL_ITEM))
          .addConstant(COL_TRADE_ITEM_QUANTITY, 1)
          .addConstant(COL_TRADE_ITEM_PRICE, row.getDouble(COL_AMOUNT))
          .addConstant(COL_TRADE_ITEM_VAT, row.getDouble(COL_TRADE_ITEM_VAT))
          .addConstant(COL_TRADE_ITEM_VAT_PERC, row.getBoolean(COL_TRADE_ITEM_VAT_PERC))
          .addConstant(COL_TRADE_ITEM_NOTE, xml);

      qs.insertData(insert);
    }
    return response.addErrorsFrom(qs.updateDataWithResponse(new SqlUpdate(TBL_CARGO_INCOMES)
        .addConstant(COL_SALE, saleId)
        .setWhere(wh)));
  }

  private ResponseObject getAssessmentTotals(Long assessorId, Long cargoId) {
    if (!DataUtils.isId(cargoId)) {
      return ResponseObject.error("Cargo ID is not valid:", cargoId);
    }
    SqlSelect query = null;

    for (int i = 0; i < 2; i++) {
      String tbl = (i == 0) ? TBL_CARGO_INCOMES : TBL_CARGO_EXPENSES;

      SqlSelect ss = new SqlSelect()
          .addConstant(tbl, COL_SERVICE)
          .addFrom(tbl)
          .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, tbl, COL_CARGO))
          .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
          .setWhere(SqlUtils.equals(tbl, COL_CARGO, cargoId));

      IsExpression xpr = ExchangeUtils.exchangeFieldTo(ss, SqlUtils.field(tbl, COL_AMOUNT),
          SqlUtils.field(tbl, ExchangeUtils.COL_CURRENCY),
          SqlUtils.nvl(SqlUtils.field(tbl, COL_DATE), SqlUtils.field(TBL_ORDERS, COL_DATE)),
          SqlUtils.field(TBL_ORDER_CARGO, ExchangeUtils.COL_CURRENCY));

      ss.addSum(xpr, VAR_TOTAL)
          .addSum(SqlUtils.sqlIf(SqlUtils.equals(tbl, COL_ASSESSOR, assessorId), xpr, null),
              COL_AMOUNT);

      if (query == null) {
        query = ss;
      } else {
        query.setUnionAllMode(true).addUnion(ss);
      }
    }
    return ResponseObject.response(qs.getData(query));
  }

  /**
   * Return SqlSelect query, calculating cargo costs from CargoServices table.
   * 
   * @param flt - query filter with <b>unique</b> "Cargo" values.
   * @return query with columns: "Cargo", "Expense"
   */
  private SqlSelect getCargoCostQuery(SqlSelect flt) {
    String alias = SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect()
        .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
        .addFrom(TBL_ORDER_CARGO)
        .addFromInner(flt, alias, sys.joinTables(TBL_ORDER_CARGO, alias, COL_CARGO))
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_EXPENSES,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_EXPENSES, COL_CARGO))
        .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO));

    ss.addSum(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_CARGO_EXPENSES, COL_AMOUNT),
        SqlUtils.field(TBL_CARGO_EXPENSES, ExchangeUtils.COL_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_EXPENSES, COL_DATE),
            SqlUtils.field(TBL_ORDERS, COL_DATE))), VAR_EXPENSE);

    return ss;
  }

  /**
   * Return SqlSelect query, calculating cargo incomes from CargoServices table.
   * 
   * @param flt - query filter with <b>unique</b> "Cargo" values.
   * @return query with columns: "Cargo", "CargoIncome", "ServicesIncome"
   */
  private SqlSelect getCargoIncomeQuery(SqlSelect flt) {
    String alias = SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect()
        .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
        .addFrom(TBL_ORDER_CARGO)
        .addFromInner(flt, alias, sys.joinTables(TBL_ORDER_CARGO, alias, COL_CARGO))
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_INCOMES,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_INCOMES, COL_CARGO))
        .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO));

    IsExpression dateExpr = SqlUtils.nvl(SqlUtils.field(TBL_CARGO_INCOMES, COL_DATE),
        SqlUtils.field(TBL_ORDERS, COL_DATE));

    ss.addMax(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_ORDER_CARGO, "Price"),
        SqlUtils.field(TBL_ORDER_CARGO, ExchangeUtils.COL_CURRENCY), dateExpr), "CargoIncome")
        .addSum(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_CARGO_INCOMES, COL_AMOUNT),
            SqlUtils.field(TBL_CARGO_INCOMES, ExchangeUtils.COL_CURRENCY), dateExpr),
            "ServicesIncome");

    return ss;
  }

  private ResponseObject getCargoProfit(SqlSelect flt) {
    SqlSelect ss = getCargoIncomeQuery(flt)
        .addEmptyDouble("ServicesCost")
        .addEmptyDouble("TripCost");

    String crsTotals = qs.sqlCreateTemp(ss);
    qs.sqlIndex(crsTotals, COL_CARGO);

    String alias = SqlUtils.uniqueName();

    qs.updateData(new SqlUpdate(crsTotals)
        .setFrom(getCargoCostQuery(flt), alias, SqlUtils.joinUsing(crsTotals, alias, COL_CARGO))
        .addExpression("ServicesCost", SqlUtils.field(alias, VAR_EXPENSE)));

    ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields(TBL_CARGO_TRIPS, COL_TRIP)
        .addFrom(TBL_CARGO_TRIPS)
        .addFromInner(crsTotals, SqlUtils.joinUsing(TBL_CARGO_TRIPS, crsTotals, COL_CARGO));

    String crsIncomes = getTripIncome(ss);
    String crsCosts = getTripCost(ss);

    ss = new SqlSelect()
        .addFields(crsIncomes, COL_TRIP)
        .addSum(TBL_CARGO_TRIPS, "CargoPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(TBL_CARGO_TRIPS, "CargoPercent"),
            SqlUtils.field(crsIncomes, "TripIncome"), 0), "TotalIncome")
        .addFrom(crsIncomes)
        .addFromInner(TBL_CARGO_TRIPS,
            SqlUtils.joinUsing(crsIncomes, TBL_CARGO_TRIPS, COL_TRIP, COL_CARGO))
        .addGroup(crsIncomes, COL_TRIP);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, COL_TRIP);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(
            SqlUtils.plus(SqlUtils.nvl(SqlUtils.field(crsCosts, "TripCost"), 0),
                SqlUtils.nvl(SqlUtils.field(crsCosts, "FuelCost"), 0)),
            100),
        SqlUtils.sqlIf(SqlUtils.isNull(TBL_CARGO_TRIPS, "CargoPercent"),
            SqlUtils.multiply(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp, "TotalPercent"), 0)),
                SqlUtils.divide(SqlUtils.field(crsIncomes, "TripIncome"),
                    SqlUtils.field(tmp, "TotalIncome"))),
            SqlUtils.field(TBL_CARGO_TRIPS, "CargoPercent")));

    ss = new SqlSelect()
        .addFields(crsIncomes, COL_CARGO)
        .addSum(xpr, "Cost")
        .addFrom(crsIncomes)
        .addFromInner(TBL_CARGO_TRIPS,
            SqlUtils.joinUsing(crsIncomes, TBL_CARGO_TRIPS, COL_TRIP, COL_CARGO))
        .addFromInner(crsCosts, SqlUtils.joinUsing(crsIncomes, crsCosts, COL_TRIP))
        .addFromInner(tmp, SqlUtils.joinUsing(crsIncomes, tmp, COL_TRIP))
        .addGroup(crsIncomes, COL_CARGO);

    String crsTripCosts = qs.sqlCreateTemp(ss);
    qs.sqlIndex(crsTripCosts, COL_CARGO);

    qs.sqlDropTemp(tmp);
    qs.sqlDropTemp(crsCosts);
    qs.sqlDropTemp(crsIncomes);

    SqlUpdate su = new SqlUpdate(crsTotals)
        .setFrom(crsTripCosts, SqlUtils.joinUsing(crsTotals, crsTripCosts, COL_CARGO))
        .addExpression("TripCost", SqlUtils.field(crsTripCosts, "Cost"));

    qs.updateData(su);

    qs.sqlDropTemp(crsTripCosts);

    ss = new SqlSelect()
        .addSum(crsTotals, "CargoIncome")
        .addSum(crsTotals, "TripCost")
        .addSum(crsTotals, "ServicesIncome")
        .addSum(crsTotals, "ServicesCost")
        .addFrom(crsTotals);

    SimpleRow res = qs.getRow(ss);

    qs.sqlDropTemp(crsTotals);

    return ResponseObject.response(new String[] {"CargoIncome:", res.getValue("CargoIncome"),
        "TripCost:", res.getValue("TripCost"), "ServicesIncome:", res.getValue("ServicesIncome"),
        "ServicesCost:", res.getValue("ServicesCost")});
  }

  private ResponseObject getCargoUsage(String viewName, String[] ids) {
    String source = sys.getViewSource(viewName);
    IsExpression ref;
    SqlSelect ss = new SqlSelect().addFrom(TBL_CARGO_TRIPS);

    if (BeeUtils.same(source, TBL_TRIPS)) {
      ref = SqlUtils.field(TBL_CARGO_TRIPS, COL_TRIP);

    } else if (BeeUtils.same(source, TBL_ORDER_CARGO)) {
      ref = SqlUtils.field(TBL_CARGO_TRIPS, COL_CARGO);

    } else if (BeeUtils.same(source, TBL_ORDERS)) {
      ss.addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO));
      ref = SqlUtils.field(TBL_ORDER_CARGO, COL_ORDER);

    } else {
      return ResponseObject.error("Table not supported:", source);
    }
    int cnt = qs.sqlCount(ss.setWhere(SqlUtils.inList(ref, (Object[]) ids)));

    return ResponseObject.response(cnt);
  }

  private ResponseObject getColors(RequestInfo reqInfo) {
    Long theme;
    if (reqInfo.hasParameter(VAR_THEME_ID)) {
      theme = BeeUtils.toLong(reqInfo.getParameter(VAR_THEME_ID));
    } else {
      theme = null;
    }

    return ResponseObject.response(getThemeColors(theme));
  }

  private ResponseObject getDtbData() {
    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    List<Color> colors = getThemeColors(null);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    BeeRowSet drivers = qs.getViewData(VIEW_DRIVERS);
    if (DataUtils.isEmpty(drivers)) {
      logger.warning(SVC_GET_DTB_DATA, "drivers not available");
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_DRIVERS, drivers.serialize());

    List<Long> driverIds = DataUtils.getRowIds(drivers);

    BeeRowSet absence = qs.getViewData(VIEW_DRIVER_ABSENCE, Filter.any(COL_DRIVER, driverIds));
    if (!DataUtils.isEmpty(absence)) {
      settings.setTableProperty(PROP_ABSENCE, absence.serialize());
    }

    IsCondition tripDriverWhere = SqlUtils.inList(TBL_TRIP_DRIVERS, COL_DRIVER, driverIds);

    SimpleRowSet tripDrivers = getTripDrivers(tripDriverWhere);
    if (DataUtils.isEmpty(tripDrivers)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_TRIP_DRIVERS, tripDrivers.serialize());

    IsCondition tripWhere = SqlUtils.in(TBL_TRIPS, sys.getIdName(TBL_TRIPS),
        TBL_TRIP_DRIVERS, COL_TRIP, tripDriverWhere);

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

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    IsCondition handlingWhere = SqlUtils.or(SqlUtils.notNull(loadAlias, COL_PLACE_DATE),
        SqlUtils.notNull(unlAlias, COL_PLACE_DATE));

    return new SqlSelect()
        .addFrom(TBL_TRIPS)
        .addFromInner(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, COL_TRIP_ID))
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromInner(TBL_CARGO_HANDLING,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_HANDLING, COL_CARGO))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFields(TBL_CARGO_HANDLING, COL_CARGO, COL_CARGO_HANDLING_NOTES)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_TERMINAL, loadingColumnAlias(COL_PLACE_TERMINAL))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_TERMINAL, unloadingColumnAlias(COL_PLACE_TERMINAL))
        .setWhere(SqlUtils.and(tripWhere, handlingWhere))
        .addOrder(TBL_CARGO_HANDLING, COL_CARGO);
  }

  private SqlSelect getFreightQuery(IsCondition where) {
    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String defLoadAlias = "defl_" + SqlUtils.uniqueName();
    String defUnlAlias = "defu_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    return new SqlSelect()
        .addFrom(TBL_TRIPS)
        .addFromInner(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, COL_TRIP_ID))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_CARGO_TRIPS, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_CARGO_TRIPS, COL_UNLOADING_PLACE))
        .addFromInner(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromLeft(TBL_CARGO_PLACES, defLoadAlias,
            SqlUtils.join(defLoadAlias, colPlaceId, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, defUnlAlias,
            SqlUtils.join(defUnlAlias, colPlaceId, TBL_ORDER_CARGO, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFields(TBL_TRIPS, COL_TRIP_ID, COL_VEHICLE, COL_TRAILER)
        .addFields(TBL_CARGO_TRIPS, COL_CARGO, COL_CARGO_TRIP_ID)
        .addField(TBL_CARGO_TRIPS, sys.getVersionName(TBL_CARGO_TRIPS), ALS_CARGO_TRIP_VERSION)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_TERMINAL, loadingColumnAlias(COL_PLACE_TERMINAL))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_TERMINAL, unloadingColumnAlias(COL_PLACE_TERMINAL))
        .addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_DESCRIPTION, COL_CARGO_NOTES)
        .addField(defLoadAlias, COL_PLACE_DATE, defaultLoadingColumnAlias(COL_PLACE_DATE))
        .addField(defLoadAlias, COL_PLACE_COUNTRY, defaultLoadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(defLoadAlias, COL_PLACE_ADDRESS, defaultLoadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(defLoadAlias, COL_PLACE_TERMINAL, defaultLoadingColumnAlias(COL_PLACE_TERMINAL))
        .addField(defUnlAlias, COL_PLACE_DATE, defaultUnloadingColumnAlias(COL_PLACE_DATE))
        .addField(defUnlAlias, COL_PLACE_COUNTRY, defaultUnloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(defUnlAlias, COL_PLACE_ADDRESS, defaultUnloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(defUnlAlias, COL_PLACE_TERMINAL, defaultUnloadingColumnAlias(COL_PLACE_TERMINAL))
        .addFields(TBL_ORDERS, COL_ORDER_NO, COL_CUSTOMER, COL_STATUS)
        .addField(TBL_ORDERS, COL_ORDER_DATE, ALS_ORDER_DATE)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_CUSTOMER_NAME)
        .setWhere(where)
        .addOrder(TBL_TRIPS, COL_TRIP_ID);
  }

  /**
   * Return SqlSelect query, calculating trip fuel consumptions from TripRoutes table.
   * 
   * @param flt - query filter with <b>unique</b> TripRoutes ID values.
   * @param routeMode - if true, returns results, grouped by TripRoutes ID, else grouped by Trip ID
   * @return query with two columns: (TripRoutes ID or "Trip") and "Quantity"
   */
  private SqlSelect getFuelConsumptionsQuery(SqlSelect flt, boolean routeMode) {
    String trips = VIEW_TRIPS;
    String routes = VIEW_TRIP_ROUTES;
    String fuel = VIEW_FUEL_CONSUMPTIONS;
    String temps = VIEW_FUEL_TEMPERATURES;
    String routeId = sys.getIdName(routes);

    IsExpression xpr = SqlUtils.round(
        SqlUtils.sqlIf(SqlUtils.isNull(routes, "Consumption"),
            SqlUtils.plus(
                SqlUtils.divide(
                    SqlUtils.plus(
                        SqlUtils.nvl(
                            SqlUtils.multiply(
                                SqlUtils.field(routes, "Kilometers"),
                                SqlUtils.sqlCase(SqlUtils.field(routes, "Season"), 0,
                                    SqlUtils.field(fuel, "Summer"),
                                    SqlUtils.field(fuel, "Winter")),
                                SqlUtils.plus(1,
                                    SqlUtils.divide(SqlUtils.nvl(SqlUtils.field(temps, "Rate"), 0),
                                        100))),
                            0),
                        SqlUtils.nvl(
                            SqlUtils.multiply(
                                SqlUtils.field(routes, "Kilometers"),
                                SqlUtils.field(routes, "CargoWeight"),
                                SqlUtils.field(fuel, "TonneKilometer")),
                            0)),
                    100),
                SqlUtils.nvl(SqlUtils.multiply(SqlUtils.field(routes, "MotoHours"),
                    SqlUtils.field(fuel, "MotoHour")), 0)),
            SqlUtils.field(routes, "Consumption")), 2);

    String alias = SqlUtils.uniqueName();

    return new SqlSelect()
        .addFields(routes, routeMode ? routeId : "Trip")
        .addSum(xpr, "Quantity")
        .addFrom(routes)
        .addFromInner(flt, alias, SqlUtils.joinUsing(routes, alias, routeId))
        .addFromInner(trips, sys.joinTables(trips, routes, "Trip"))
        .addFromInner(fuel, SqlUtils.joinUsing(trips, fuel, "Vehicle"))
        .addFromLeft(temps,
            SqlUtils.and(sys.joinTables(fuel, temps, "Consumption"),
                SqlUtils.joinUsing(temps, routes, "Season"),
                SqlUtils.or(SqlUtils.isNull(temps, "TempFrom"),
                    SqlUtils.lessEqual(SqlUtils.field(temps, "TempFrom"),
                        SqlUtils.nvl(SqlUtils.field(routes, "Temperature"), 0))),
                SqlUtils.or(SqlUtils.isNull(temps, "TempTo"),
                    SqlUtils.more(SqlUtils.field(temps, "TempTo"),
                        SqlUtils.nvl(SqlUtils.field(routes, "Temperature"), 0)))))
        .setWhere(SqlUtils.and(
            SqlUtils.or(SqlUtils.isNull(fuel, "DateFrom"),
                SqlUtils.joinLessEqual(fuel, "DateFrom", routes, "Date")),
            SqlUtils.or(SqlUtils.isNull(fuel, "DateTo"),
                SqlUtils.joinMore(fuel, "DateTo", routes, "Date"))))
        .addGroup(routes, routeMode ? routeId : "Trip");
  }

  private ResponseObject getFxData() {
    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Long theme = settings.getLong(0, settings.getColumnIndex(COL_FX_THEME));
    List<Color> colors = getThemeColors(theme);

    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_ORDER_CARGO)
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_COMPANIES, sys.joinTables(TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_ORDER_CARGO, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_CARGO, TBL_ORDER_CARGO, COL_CARGO_ID));

    query.addFields(TBL_ORDERS, COL_STATUS, COL_ORDER_DATE, COL_ORDER_NO, COL_CUSTOMER);
    query.addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_CUSTOMER_NAME);

    query.addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_ID, COL_CARGO_DESCRIPTION,
        COL_CARGO_NOTES);

    query.addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE));
    query.addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY));
    query.addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS));
    query.addField(loadAlias, COL_PLACE_TERMINAL, loadingColumnAlias(COL_PLACE_TERMINAL));

    query.addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE));
    query.addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY));
    query.addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS));
    query.addField(unlAlias, COL_PLACE_TERMINAL, unloadingColumnAlias(COL_PLACE_TERMINAL));

    Set<Integer> statuses = Sets.newHashSet(OrderStatus.NEW.ordinal(),
        OrderStatus.ACTIVE.ordinal());
    IsCondition cargoWhere = SqlUtils.and(SqlUtils.inList(TBL_ORDERS, COL_STATUS, statuses),
        SqlUtils.isNull(TBL_CARGO_TRIPS, COL_CARGO));

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
            SqlUtils.join(loadAlias, colPlaceId, TBL_CARGO_HANDLING, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_CARGO_HANDLING, COL_UNLOADING_PLACE))
        .addFields(TBL_CARGO_HANDLING, COL_CARGO, COL_CARGO_HANDLING_NOTES)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_PLACE_COUNTRY, loadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(loadAlias, COL_PLACE_ADDRESS, loadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(loadAlias, COL_PLACE_TERMINAL, loadingColumnAlias(COL_PLACE_TERMINAL))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_PLACE_COUNTRY, unloadingColumnAlias(COL_PLACE_COUNTRY))
        .addField(unlAlias, COL_PLACE_ADDRESS, unloadingColumnAlias(COL_PLACE_ADDRESS))
        .addField(unlAlias, COL_PLACE_TERMINAL, unloadingColumnAlias(COL_PLACE_TERMINAL))
        .setWhere(SqlUtils.and(cargoWhere, cargoHandlingWhere))
        .addOrder(TBL_CARGO_HANDLING, COL_CARGO);

    SimpleRowSet cargoHandling = qs.getData(cargoHandlingQuery);
    if (!DataUtils.isEmpty(cargoHandling)) {
      settings.setTableProperty(PROP_CARGO_HANDLING, cargoHandling.serialize());
    }

    return ResponseObject.response(settings);
  }

  private BeeRowSet getSettings() {
    long userId = usr.getCurrentUserId();
    Filter filter = ComparisonFilter.isEqual(COL_USER, new LongValue(userId));

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

  private List<Color> getThemeColors(Long theme) {
    List<Color> result = Lists.newArrayList();

    BeeRowSet rowSet;
    if (theme != null) {
      rowSet = qs.getViewData(VIEW_THEME_COLORS,
          ComparisonFilter.isEqual(COL_THEME, new LongValue(theme)));
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

  private ResponseObject getTripBeforeData(long vehicle, long date) {
    String[] resp = new String[2];

    if (date != 0) {
      String trips = VIEW_TRIPS;
      String routes = VIEW_TRIP_ROUTES;
      String fuels = VIEW_TRIP_FUEL_COSTS;
      String consumptions = VIEW_TRIP_FUEL_CONSUMPTIONS;
      String tripId = sys.getIdName(trips);

      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(trips,
              tripId, "SpeedometerBefore", "SpeedometerAfter", "FuelBefore", "FuelAfter")
          .addFrom(trips)
          .setWhere(SqlUtils.and(SqlUtils.equals(trips, "Vehicle", vehicle),
              SqlUtils.less(trips, "Date", date))));

      int cnt = rs.getNumberOfRows();

      if (cnt > 0) {
        cnt--;
        Double speedometer = rs.getDouble(cnt, "SpeedometerAfter");
        Double fuel = rs.getDouble(cnt, "FuelAfter");

        if (speedometer == null) {
          Double km = qs.getDouble(new SqlSelect()
              .addSum(routes, "Kilometers")
              .addFrom(routes)
              .setWhere(SqlUtils.equals(routes, "Trip", rs.getLong(cnt, tripId))));

          speedometer = BeeUtils.unbox(rs.getDouble(cnt, "SpeedometerBefore"))
              + BeeUtils.unbox(km);

          Integer scale = BeeUtils.toIntOrNull(qs.sqlValue(VIEW_VEHICLES, "Speedometer", vehicle));

          if (BeeUtils.isPositive(scale) && scale < speedometer) {
            speedometer -= scale;
          }
        }
        if (fuel == null) {
          Double fill = qs.getDouble(new SqlSelect()
              .addSum(fuels, "Quantity")
              .addFrom(fuels)
              .setWhere(SqlUtils.equals(fuels, "Trip", rs.getLong(cnt, tripId))));

          SimpleRow row = qs.getRow(getFuelConsumptionsQuery(new SqlSelect()
              .addFields(routes, sys.getIdName(routes))
              .addFrom(routes)
              .setWhere(SqlUtils.equals(routes, "Trip", rs.getLong(cnt, tripId))), false));

          Double consume = row == null ? null : row.getDouble("Quantity");

          Double addit = qs.getDouble(new SqlSelect()
              .addSum(consumptions, "Quantity")
              .addFrom(consumptions)
              .setWhere(SqlUtils.equals(consumptions, "Trip", rs.getLong(cnt, tripId))));

          fuel = BeeUtils.unbox(rs.getDouble(cnt, "FuelBefore")) + BeeUtils.unbox(fill)
              - BeeUtils.unbox(consume) - BeeUtils.unbox(addit);
        }
        resp[0] = BeeUtils.toString(speedometer);
        resp[1] = BeeUtils.toString(fuel);
      }
    }
    return ResponseObject.response(resp);
  }

  /**
   * Return Temporary table name with calculated trip costs.
   * 
   * @param flt - query filter with <b>unique</b> "Trip" values.
   * @return Temporary table name with following structure: <br>
   *         "Trip" - trip ID <br>
   *         "TripCost" - total trip cost <br>
   *         "FuelCost" - total trip fuel cost considering remainder corrections
   */
  private String getTripCost(SqlSelect flt) {
    String trips = VIEW_TRIPS;
    String costs = VIEW_TRIP_COSTS;
    String fuels = VIEW_TRIP_FUEL_COSTS;
    String routes = VIEW_TRIP_ROUTES;
    String consumptions = VIEW_TRIP_FUEL_CONSUMPTIONS;
    String tripNativeId = sys.getIdName(trips);
    String alias = SqlUtils.uniqueName();

    // Trip costs
    SqlSelect ss = new SqlSelect()
        .addField(trips, tripNativeId, COL_TRIP)
        .addField(trips, "Date", "TripDate")
        .addFields(trips, "Vehicle", "FuelBefore", "FuelAfter")
        .addEmptyDouble("FuelCost")
        .addFrom(trips)
        .addFromInner(flt, alias, sys.joinTables(trips, alias, COL_TRIP))
        .addFromLeft(costs, sys.joinTables(trips, costs, COL_TRIP))
        .addGroup(trips, tripNativeId, "Date", "Vehicle", "FuelBefore", "FuelAfter");

    ss.addSum(SqlUtils.multiply(SqlUtils.field(costs, "Quantity"),
        ExchangeUtils.exchangeField(ss, costs, "Price", "Currency", "Date")),
        "TripCost");

    String tmpCosts = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpCosts, COL_TRIP);

    // Fuel costs
    ss = new SqlSelect()
        .addFields(tmpCosts, COL_TRIP)
        .addSum(fuels, "Quantity")
        .addFrom(tmpCosts)
        .addFromLeft(fuels, SqlUtils.joinUsing(tmpCosts, fuels, COL_TRIP))
        .addGroup(tmpCosts, COL_TRIP);

    ss.addSum(SqlUtils.multiply(SqlUtils.field(fuels, "Quantity"),
        ExchangeUtils.exchangeField(ss, fuels, "Price", "Currency", "Date")),
        "FuelCost");

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, COL_TRIP);

    qs.updateData(new SqlUpdate(tmpCosts)
        .setFrom(tmp, SqlUtils.joinUsing(tmpCosts, tmp, COL_TRIP))
        .addExpression("FuelCost", SqlUtils.field(tmp, "FuelCost")));

    // Fuel consumptions
    if (qs.sqlExists(tmpCosts, SqlUtils.isNull(tmpCosts, "FuelAfter"))) {
      ss = new SqlSelect()
          .addFields(routes, sys.getIdName(routes))
          .addFrom(routes)
          .addFromInner(tmpCosts, SqlUtils.joinUsing(routes, tmpCosts, COL_TRIP));

      String tmpRoutes = qs.sqlCreateTemp(getFuelConsumptionsQuery(ss, false));
      qs.sqlIndex(tmpRoutes, COL_TRIP);

      String tmpConsumptions = qs.sqlCreateTemp(new SqlSelect()
          .addFields(consumptions, COL_TRIP)
          .addSum(consumptions, "Quantity")
          .addFrom(consumptions)
          .addFromInner(tmpCosts, SqlUtils.joinUsing(consumptions, tmpCosts, COL_TRIP))
          .addGroup(consumptions, COL_TRIP));

      qs.sqlIndex(tmpConsumptions, COL_TRIP);

      qs.updateData(new SqlUpdate(tmpCosts)
          .setFrom(new SqlSelect()
              .addFields(tmp, COL_TRIP, "Quantity")
              .addField(tmpRoutes, "Quantity", "routeQuantity")
              .addField(tmpConsumptions, "Quantity", "consumeQuantity")
              .addFrom(tmp)
              .addFromLeft(tmpRoutes, SqlUtils.joinUsing(tmp, tmpRoutes, COL_TRIP))
              .addFromLeft(tmpConsumptions, SqlUtils.joinUsing(tmp, tmpConsumptions, COL_TRIP)),
              "sub", SqlUtils.joinUsing(tmpCosts, "sub", COL_TRIP))
          .addExpression("FuelAfter", SqlUtils.minus(
              SqlUtils.plus(
                  SqlUtils.nvl(SqlUtils.field(tmpCosts, "FuelBefore"), 0),
                  SqlUtils.nvl(SqlUtils.field("sub", "Quantity"), 0)),
              SqlUtils.nvl(SqlUtils.field("sub", "routeQuantity"), 0),
              SqlUtils.nvl(SqlUtils.field("sub", "consumeQuantity"), 0)))
          .setWhere(SqlUtils.isNull(tmpCosts, "FuelAfter")));

      qs.sqlDropTemp(tmpRoutes);
      qs.sqlDropTemp(tmpConsumptions);
    }
    qs.sqlDropTemp(tmp);

    // Fuel cost correction
    ss = new SqlSelect()
        .addFields(trips, "Vehicle")
        .addField(trips, "Date", "TripDate")
        .addFields(fuels, "Date")
        .addSum(fuels, "Quantity")
        .addFrom(trips)
        .addFromInner(fuels, SqlUtils.join(trips, tripNativeId, fuels, COL_TRIP))
        .addFromInner(new SqlSelect()
            .addFields(trips, "Vehicle")
            .addMax(trips, "Date", "MaxDate")
            .addFrom(trips)
            .addFromInner(tmpCosts, SqlUtils.join(trips, tripNativeId, tmpCosts, COL_TRIP))
            .addGroup(trips, "Vehicle"), "sub",
            SqlUtils.and(SqlUtils.joinUsing(trips, "sub", "Vehicle"),
                SqlUtils.joinLessEqual(trips, "Date", "sub", "MaxDate"),
                SqlUtils.and(SqlUtils.positive(fuels, "Quantity"),
                    SqlUtils.positive(fuels, "Price"))))
        .addGroup(trips, "Vehicle", "Date")
        .addGroup(fuels, "Date");

    ss.addSum(SqlUtils.multiply(SqlUtils.field(fuels, "Quantity"),
        ExchangeUtils.exchangeField(ss, fuels, "Price", "Currency", "Date")),
        "Sum");

    String tmpFuels = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpFuels, "Vehicle");

    for (int i = 0; i < 2; i++) {
      boolean plusMode = i == 0;
      String fld = plusMode ? "FuelBefore" : "FuelAfter";

      tmp = qs.sqlCreateTemp(new SqlSelect()
          .addFields(tmpCosts, COL_TRIP, "TripDate", "Vehicle")
          .addField(tmpCosts, fld, "Remainder")
          .addEmptyDate("Date")
          .addEmptyDouble("Cost")
          .addFrom(tmpCosts)
          .setWhere(SqlUtils.positive(tmpCosts, fld)));

      qs.sqlIndex(tmp, COL_TRIP, "Vehicle");
      int c = 0;

      IsCondition cond = plusMode
          ? SqlUtils.joinLess(tmpFuels, "TripDate", tmp, "TripDate")
          : SqlUtils.joinLessEqual(tmpFuels, "TripDate", tmp, "TripDate");

      do {
        String tmp2 = qs.sqlCreateTemp(new SqlSelect()
            .addFields(tmp, "Vehicle", "TripDate")
            .addMax(tmpFuels, "Date")
            .addFrom(tmp)
            .addFromInner(tmpFuels, SqlUtils.joinUsing(tmp, tmpFuels, "Vehicle"))
            .setWhere(SqlUtils.and(cond,
                SqlUtils.or(SqlUtils.isNull(tmp, "Date"),
                    SqlUtils.joinLess(tmpFuels, "Date", tmp, "Date")),
                SqlUtils.positive(tmp, "Remainder")))
            .addGroup(tmp, "Vehicle", "TripDate"));

        qs.sqlIndex(tmp2, "Vehicle");

        c = qs.updateData(new SqlUpdate(tmp)
            .setFrom(new SqlSelect()
                .addFields(tmp2, "Vehicle", "TripDate", "Date")
                .addFields(tmpFuels, "Quantity", "Sum")
                .addFrom(tmp2)
                .addFromInner(tmpFuels, SqlUtils.joinUsing(tmp2, tmpFuels, "Vehicle", "Date")),
                "sub", SqlUtils.joinUsing(tmp, "sub", "Vehicle", "TripDate"))
            .addExpression("Date", SqlUtils.field("sub", "Date"))
            .addExpression("Cost", SqlUtils.plus(SqlUtils.nvl(SqlUtils.field(tmp, "Cost"), 0),
                SqlUtils.sqlIf(SqlUtils.joinLess(tmp, "Remainder", "sub", "Quantity"),
                    SqlUtils.multiply(SqlUtils.field(tmp, "Remainder"),
                        SqlUtils.divide((Object[]) SqlUtils.fields("sub", "Sum", "Quantity"))),
                    SqlUtils.field("sub", "Sum"))))
            .addExpression("Remainder", SqlUtils.minus(SqlUtils.field(tmp, "Remainder"),
                SqlUtils.field("sub", "Quantity")))
            .setWhere(SqlUtils.positive(tmp, "Remainder")));

        qs.sqlDropTemp(tmp2);

      } while (BeeUtils.isPositive(c));

      IsExpression expr = plusMode
          ? SqlUtils.plus(SqlUtils.nvl(SqlUtils.field(tmpCosts, "FuelCost"), 0),
              SqlUtils.nvl(SqlUtils.field(tmp, "Cost"), 0))
          : SqlUtils.minus(SqlUtils.nvl(SqlUtils.field(tmpCosts, "FuelCost"), 0),
              SqlUtils.nvl(SqlUtils.field(tmp, "Cost"), 0));

      qs.updateData(new SqlUpdate(tmpCosts)
          .setFrom(tmp, SqlUtils.joinUsing(tmpCosts, tmp, COL_TRIP))
          .addExpression("FuelCost", expr));

      qs.sqlDropTemp(tmp);
    }
    qs.sqlDropTemp(tmpFuels);

    return tmpCosts;
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

  /**
   * Return Temporary table name with calculated trip incomes by each cargo.
   * 
   * @param flt - query filter with <b>unique</b> "Trip" values.
   * @return Temporary table name with following structure: <br>
   *         "Trip" - trip ID <br>
   *         "Cargo" - cargo ID <br>
   *         "TripIncome" - total trip income <br>
   */
  private String getTripIncome(SqlSelect flt) {
    String cargoTrips = sys.getViewSource(VIEW_CARGO_TRIPS);
    String alias = SqlUtils.uniqueName();

    String tmp = qs.sqlCreateTemp(getCargoIncomeQuery(new SqlSelect()
        .setDistinctMode(true)
        .addFields(cargoTrips, COL_CARGO)
        .addFrom(cargoTrips)
        .addFromInner(flt, alias, SqlUtils.joinUsing(cargoTrips, alias, COL_TRIP))));

    qs.sqlIndex(tmp, COL_CARGO);

    String tmp2 = qs.sqlCreateTemp(new SqlSelect()
        .addFields(tmp, COL_CARGO, "CargoIncome")
        .addSum(cargoTrips, "TripPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"), 1, 0), "CntEmpty")
        .addFrom(tmp)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp, cargoTrips, COL_CARGO))
        .addGroup(tmp, COL_CARGO, "CargoIncome"));

    qs.sqlIndex(tmp2, COL_CARGO);
    qs.sqlDropTemp(tmp);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(SqlUtils.field(tmp2, "CargoIncome"), 100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"),
            SqlUtils.divide(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp2, "TotalPercent"), 0)),
                SqlUtils.field(tmp2, "CntEmpty")),
            SqlUtils.field(cargoTrips, "TripPercent")));

    tmp = qs.sqlCreateTemp(new SqlSelect()
        .addFields(cargoTrips, COL_CARGO, COL_TRIP)
        .addExpr(xpr, "TripIncome")
        .addFrom(tmp2)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp2, cargoTrips, COL_CARGO))
        .addFromInner(flt, alias, SqlUtils.joinUsing(cargoTrips, alias, COL_TRIP)));

    qs.sqlIndex(tmp, COL_CARGO, COL_TRIP);
    qs.sqlDropTemp(tmp2);

    return tmp;
  }

  private ResponseObject getTripProfit(long tripId) {
    String crs = getTripCost(new SqlSelect().addConstant(tripId, "Trip"));

    SqlSelect ss = new SqlSelect()
        .addSum(crs, "TripCost")
        .addSum(crs, "FuelCost")
        .addFrom(crs);

    SimpleRow res = qs.getRow(ss);

    qs.sqlDropTemp(crs);

    crs = getTripIncome(new SqlSelect().addConstant(tripId, "Trip"));

    ss = new SqlSelect()
        .addSum(crs, "TripIncome")
        .addFrom(crs);

    String tripIncome = qs.getValue(ss);

    qs.sqlDropTemp(crs);

    return ResponseObject.response(new String[] {"TripCost:", res.getValue("TripCost"),
        "FuelCost:", res.getValue("FuelCost"), "TripIncome:", tripIncome});
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
            COL_TRIP_NOTES)
        .addField(TBL_TRIPS, sys.getVersionName(TBL_TRIPS), ALS_TRIP_VERSION)
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

  private SimpleRowSet getVehicleServices(IsCondition condition) {
    SqlSelect query =
        new SqlSelect()
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
      VehicleType vehicleType, String themeColumnName) {

    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Long theme = settings.getLong(0, settings.getColumnIndex(themeColumnName));
    List<Color> colors = getThemeColors(theme);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet countries = qs.getViewData(VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    Order vehicleOrder = new Order(COL_NUMBER, true);
    BeeRowSet vehicles = qs.getViewData(VIEW_VEHICLES, vehicleFilter, vehicleOrder);
    if (DataUtils.isEmpty(vehicles)) {
      logger.warning(svc, "vehicles not available");
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_VEHICLES, vehicles.serialize());

    List<Long> vehicleIds = DataUtils.getRowIds(vehicles);

    SimpleRowSet vehicleServices = getVehicleServices(SqlUtils.inList(TBL_VEHICLE_SERVICES,
        COL_VEHICLE, vehicleIds));
    if (!DataUtils.isEmpty(vehicleServices)) {
      settings.setTableProperty(PROP_VEHICLE_SERVICES, vehicleServices.serialize());
    }

    IsCondition tripWhere = tripCondition(SqlUtils.inList(TBL_TRIPS,
        vehicleType.getTripVehicleIdColumnName(), vehicleIds));

    SqlSelect tripQuery = getTripQuery(tripWhere);
    tripQuery.addOrder(TBL_TRIPS, vehicleType.getTripVehicleIdColumnName(), COL_TRIP_DATE);

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

  @Timeout
  private void importERPPayments() {
    SimpleRowSet debts = qs.getData(new SqlSelect()
        .addField(TBL_SALES, sys.getIdName(TBL_SALES), COL_SALE)
        .addFields(TBL_SALES, COL_TRADE_PAID)
        .addFrom(TBL_SALES)
        .setWhere(SqlUtils.and(SqlUtils.isNull(TBL_SALES, COL_SALE_PROFORMA),
            SqlUtils.or(SqlUtils.isNull(TBL_SALES, COL_TRADE_PAID),
                SqlUtils.less(TBL_SALES, COL_TRADE_PAID,
                    SqlUtils.field(TBL_SALES, COL_TRADE_AMOUNT))))));

    if (debts.isEmpty()) {
      return;
    }
    StringBuilder ids = new StringBuilder();

    for (SimpleRow row : debts) {
      if (ids.length() > 0) {
        ids.append(",");
      }
      ids.append("'").append(row.getValue(COL_SALE)).append("'");
    }
    String remoteAddress = prm.getText(COMMONS_MODULE, PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(COMMONS_MODULE, PRM_ERP_LOGIN);
    String remotePassword = prm.getText(COMMONS_MODULE, PRM_ERP_PASSWORD);

    ResponseObject response = ButentWS.getSQLData(remoteAddress, remoteLogin, remotePassword,
        "SELECT extern_id AS id, apm_data AS data, apm_suma AS suma"
            + " FROM apyvarta WHERE extern_id IN(" + ids.toString() + ")",
        new String[] {"id", "data", "suma"});

    if (response.hasErrors()) {
      logger.severe((Object[]) response.getErrors());
    } else {
      SimpleRowSet payments = (SimpleRowSet) response.getResponse();

      for (SimpleRow payment : payments) {
        if (!Objects.equal(payment.getDouble("suma"),
            BeeUtils.toDoubleOrNull(debts.getValueByKey(COL_SALE, payment.getValue("id"),
                COL_TRADE_PAID)))) {

          qs.updateData(new SqlUpdate(TBL_SALES)
              .addConstant(COL_TRADE_PAID, payment.getDouble("suma"))
              .addConstant(COL_TRADE_PAYMENT_TIME,
                  TimeUtils.parseDateTime(payment.getValue("data")))
              .setWhere(sys.idEquals(TBL_SALES, payment.getLong("id"))));
        }
      }
    }
  }

  private void initTimer() {
    Integer minutes = prm.getInteger(TRANSPORT_MODULE, PRM_ERP_REFRESH_INTERVAL);
    boolean timerExists = erpTimer != null;

    if (timerExists) {
      try {
        erpTimer.cancel();
      } catch (NoSuchObjectLocalException e) {
        logger.error(e, "ERP");
      }
      erpTimer = null;
    }
    if (BeeUtils.isPositive(minutes)) {
      erpTimer = timerService.createIntervalTimer(minutes * TimeUtils.MILLIS_PER_MINUTE,
          minutes * TimeUtils.MILLIS_PER_MINUTE, new TimerConfig(null, false));

      logger.info("Created ERP refresh timer every", minutes, "minutes starting at",
          erpTimer.getNextTimeout());
    } else {
      if (timerExists) {
        logger.info("Removed ERP timer");
      }
    }
  }

  private ResponseObject sendToERP(String viewName, Set<Long> ids) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    String trade = sys.getView(viewName).getSourceName();
    String tradeItems;
    String itemsRelation;

    SqlSelect query = new SqlSelect()
        .addFields(trade, COL_TRADE_DATE, COL_TRADE_INVOICE_PREFIX, COL_TRADE_INVOICE_NO,
            COL_TRADE_NUMBER, COL_TRADE_TERM, ExchangeUtils.COL_CURRENCY, COL_TRADE_VAT_INCL,
            COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER)
        .addField(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_FROM)
        .addFrom(trade)
        .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM,
            sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM, trade,
                COL_TRADE_WAREHOUSE_FROM))
        .setWhere(sys.idInList(trade, ids));

    if (BeeUtils.same(trade, TBL_SALES)) {
      tradeItems = TBL_SALE_ITEMS;
      itemsRelation = COL_SALE;
      query.addFields(trade, COL_SALE_PAYER);

    } else if (BeeUtils.same(trade, TBL_PURCHASES)) {
      tradeItems = TBL_PURCHASE_ITEMS;
      itemsRelation = COL_PURCHASE;
      query.addField(COL_PURCHASE_WAREHOUSE_TO, COL_WAREHOUSE_CODE, COL_PURCHASE_WAREHOUSE_TO)
          .addFromLeft(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO,
              sys.joinTables(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO, trade,
                  COL_PURCHASE_WAREHOUSE_TO));
    } else {
      return ResponseObject.error("View source not supported:", trade);
    }
    String remoteAddress = prm.getText(COMMONS_MODULE, PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(COMMONS_MODULE, PRM_ERP_LOGIN);
    String remotePassword = prm.getText(COMMONS_MODULE, PRM_ERP_PASSWORD);

    SimpleRowSet invoices = qs.getData(query.addField(trade, sys.getIdName(trade), itemsRelation));

    Map<Long, String> companies = Maps.newHashMap();
    ResponseObject response = ResponseObject.emptyResponse();

    for (SimpleRow invoice : invoices) {
      for (String col : new String[] {COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER}) {
        Long id = invoices.hasColumn(col) ? invoice.getLong(col) : null;

        if (DataUtils.isId(id) && !companies.containsKey(id)) {
          SimpleRow data = qs.getRow(new SqlSelect()
              .addFields(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE)
              .addFields(TBL_CONTACTS, COL_ADDRESS, COL_POST_INDEX)
              .addField(TBL_CITIES, COL_CITY_NAME, COL_CITY)
              .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, COL_COUNTRY)
              .addFrom(TBL_COMPANIES)
              .addFromLeft(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANIES, COL_CONTACT))
              .addFromLeft(TBL_CITIES, sys.joinTables(TBL_CITIES, TBL_CONTACTS, COL_CITY))
              .addFromLeft(TBL_COUNTRIES, sys.joinTables(TBL_COUNTRIES, TBL_CONTACTS, COL_COUNTRY))
              .setWhere(sys.idEquals(TBL_COMPANIES, id)));

          ResponseObject resp = ButentWS.importClient(remoteAddress, remoteLogin, remotePassword,
              data.getValue(COL_COMPANY_NAME), data.getValue(COL_COMPANY_CODE),
              data.getValue(COL_COMPANY_VAT_CODE), data.getValue(COL_ADDRESS),
              data.getValue(COL_POST_INDEX), data.getValue(COL_CITY), data.getValue(COL_COUNTRY));

          if (resp.hasErrors()) {
            response.addErrorsFrom(resp);
            break;
          } else {
            companies.put(id, resp.getResponseAsString());
          }
        }
      }
      if (response.hasErrors()) {
        break;
      }
      String operation;
      String warehouse;
      String client;

      if (invoices.hasColumn(COL_PURCHASE_WAREHOUSE_TO)) {
        operation = prm.getText(TRANSPORT_MODULE, "ERPCreditOperation");
        warehouse = invoice.getValue(COL_PURCHASE_WAREHOUSE_TO);
        client = companies.get(invoice.getLong(COL_TRADE_SUPPLIER));
      } else {
        operation = prm.getText(COMMONS_MODULE, "ERPOperation");
        warehouse = prm.getText(COMMONS_MODULE, "ERPWarehouse");
        client = companies.get(invoice.getLong(COL_TRADE_CUSTOMER));
      }
      WSDocument doc = new WSDocument(invoice.getValue(itemsRelation),
          invoice.getDateTime(COL_TRADE_DATE), operation, client, warehouse);

      if (invoices.hasColumn(COL_SALE_PAYER)) {
        doc.setPayer(companies.get(invoice.getLong(COL_SALE_PAYER)));
      }
      doc.setSupplier(companies.get(invoice.getLong(COL_TRADE_SUPPLIER)));
      doc.setCustomer(companies.get(invoice.getLong(COL_TRADE_CUSTOMER)));
      doc.setTerm(invoice.getDate(COL_TRADE_TERM));

      SimpleRowSet items = qs.getData(new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_EXTERNAL_CODE)
          .addFields(tradeItems, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
              COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC, COL_TRADE_ITEM_NOTE)
          .addFrom(tradeItems)
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, tradeItems, COL_ITEM))
          .setWhere(SqlUtils.equals(tradeItems, itemsRelation, invoice.getLong(itemsRelation))));

      for (SimpleRow item : items) {
        if (BeeUtils.isEmpty(item.getValue(COL_ITEM_EXTERNAL_CODE))) {
          response.addError("Item", BeeUtils.bracket(item.getValue(COL_ITEM_NAME)),
              "does not have ERP code");
          break;
        }
        WSDocumentItem wsItem = doc.addItem(item.getValue(COL_ITEM_EXTERNAL_CODE),
            item.getValue(COL_TRADE_ITEM_QUANTITY));

        wsItem.setPrice(item.getValue(COL_TRADE_ITEM_PRICE));
        wsItem.setVat(item.getValue(COL_TRADE_ITEM_VAT), invoice.getBoolean(COL_TRADE_VAT_INCL),
            item.getBoolean(COL_TRADE_ITEM_VAT_PERC));
        wsItem.setNote(item.getValue(COL_TRADE_ITEM_NOTE));
      }
      if (response.hasErrors()) {
        break;
      }
      ResponseObject resp = ButentWS.importDoc(remoteAddress, remoteLogin, remotePassword, doc);

      if (resp.hasErrors()) {
        response.addErrorsFrom(resp);
        break;
      }
      qs.updateData(new SqlUpdate(trade)
          .addConstant("Exported", System.currentTimeMillis())
          .setWhere(sys.idEquals(trade, invoice.getLong(itemsRelation))));
    }
    if (response.hasErrors()) {
      response.log(logger);
    }
    return response;
  }
}
