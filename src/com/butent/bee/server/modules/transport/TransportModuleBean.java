package com.butent.bee.server.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
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
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TransportModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(TransportModuleBean.class);

  @EJB
  DataEditorBean deb;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
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
      response = getSsData();

    } else if (BeeUtils.same(svc, SVC_GET_DTB_DATA)) {
      response = getDtbData();

    } else if (BeeUtils.same(svc, SVC_GET_TRUCK_TB_DATA)) {
      response = getVehicleTbData(svc, true);

    } else if (BeeUtils.same(svc, SVC_GET_TRAILER_TB_DATA)) {
      response = getVehicleTbData(svc, false);

    } else if (BeeUtils.same(svc, SVC_GET_COLORS)) {
      response = getColors(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ORDER_TRIPS)) {
      response = getOrderTripData(BeeUtils.toLong(reqInfo.getParameter(COL_ORDER)));

    } else if (BeeUtils.same(svc, SVC_GET_CARGO_USAGE)) {
      response = getCargoUsage(reqInfo.getParameter("ViewName"),
          Codec.beeDeserializeCollection(reqInfo.getParameter("IdList")));

    } else if (BeeUtils.same(svc, SVC_GET_ASSESSMENT_INFO)) {
      response = getAssessmentData(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ASSESSOR)),
          BeeUtils.toLong(reqInfo.getParameter(COL_CARGO)));

    } else {
      String msg = BeeUtils.joinWords("Transport service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
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
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void fillCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), VIEW_ORDER_CARGO) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Income");
            SimpleRowSet rs = qs.getData(getCargoIncomeQuery(event.getQuery()
                .resetFields().resetOrder().resetGroup()
                .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
                .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO))));
            int idIndex = rs.getColumnIndex("Cargo");
            int cargoIndex = rs.getColumnIndex("CargoIncome");
            int servicesIndex = rs.getColumnIndex("ServicesIncome");

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              rowset.updateCell(rs.getLong(i, idIndex), colIndex,
                  BeeUtils.toString(BeeUtils.unbox(rs.getDouble(i, cargoIndex))
                      + BeeUtils.unbox(rs.getDouble(i, servicesIndex))));
            }
          }
        }
      }

      @Subscribe
      public void fillFuelConsumptions(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), VIEW_TRIP_ROUTES) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Consumption");
            SimpleRowSet rs = qs.getData(getFuelConsumptionsQuery(event.getQuery()
                .resetFields().resetOrder().resetGroup()
                .addFields(VIEW_TRIP_ROUTES, sys.getIdName(VIEW_TRIP_ROUTES))
                .addGroup(VIEW_TRIP_ROUTES, sys.getIdName(VIEW_TRIP_ROUTES)), true));

            int idIndex = rs.getColumnIndex(sys.getIdName(VIEW_TRIP_ROUTES));
            int qtyIndex = rs.getColumnIndex("Quantity");

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              rowset.updateCell(rs.getLong(i, idIndex), colIndex, rs.getValue(i, qtyIndex));
            }
          }
        }
      }

      @Subscribe
      public void fillTripCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), VIEW_TRIP_CARGO) && event.isAfter()) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Income");

            if (colIndex != BeeConst.UNDEF) {
              int cargoIndex = rowset.getColumnIndex("Cargo");
              String crs = getTripIncome(event.getQuery().resetFields().resetOrder().resetGroup()
                  .addFields(VIEW_CARGO_TRIPS, "Trip").addGroup(VIEW_CARGO_TRIPS, "Trip"));

              SimpleRowSet rs = qs.getData(new SqlSelect().addAllFields(crs).addFrom(crs));
              qs.sqlDropTemp(crs);

              for (int i = 0; i < rowset.getNumberOfRows(); i++) {
                BeeRow row = rowset.getRow(i);

                row.setValue(colIndex,
                    rs.getValueByKey("Cargo", row.getString(cargoIndex), "TripIncome"));
              }
            }
          }
        }
      }

      @Subscribe
      public void getVisibleDrivers(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TBL_DRIVERS) && event.isBefore()) {
          BeeView view = sys.getView(event.getViewName());

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
        if (BeeUtils.same(event.getViewName(), TBL_VEHICLES) && event.isBefore()) {
          BeeView view = sys.getView(event.getViewName());

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

  private ResponseObject getAssessmentData(Long assessorId, long cargoId) {
    if (!DataUtils.isId(assessorId)) {
      return ResponseObject.error("Assessor ID is not valid:", assessorId);
    }
    if (!DataUtils.isId(cargoId)) {
      return ResponseObject.error("Cargo ID is not valid:", cargoId);
    }
    Map<String, SimpleRowSet> packet = Maps.newHashMap();

    SqlSelect ss = sys.getView(TBL_CARGO_SERVICES).getQuery()
        .addFromInner(TBL_ORDER_CARGO,
            sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_SERVICES, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CARGO_SERVICES, COL_CARGO, cargoId),
            SqlUtils.equals(TBL_CARGO_SERVICES, COL_ASSESSOR, assessorId)));

    ss.addExpr(ExchangeUtils.exchangeField(ss,
        SqlUtils.field(TBL_CARGO_SERVICES, COL_SERVICE_AMOUNT),
        SqlUtils.field(TBL_CARGO_SERVICES, ExchangeUtils.FLD_CURRENCY),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_SERVICES, COL_SERVICE_DATE),
            SqlUtils.field(TBL_ORDERS, COL_ORDER_DATE))), "CargoCost");

    packet.put(TBL_CARGO_SERVICES, qs.getData(ss));

    packet.put(TBL_CARGO_ASSESSORS, qs.getData(new SqlSelect()
        .addField(TBL_CARGO_ASSESSORS, sys.getIdName(TBL_CARGO_ASSESSORS), COL_ASSESSOR)
        .addFields(TBL_CARGO_ASSESSORS, COL_ASSESSOR_DATE)
        .addFields(CommonsConstants.TBL_PERSONS,
            CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)
        .addFrom(TBL_CARGO_ASSESSORS)
        .addFromInner(CommonsConstants.TBL_USERS,
            sys.joinTables(CommonsConstants.TBL_USERS, TBL_CARGO_ASSESSORS, COL_ASSESSOR_MANAGER))
        .addFromInner(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.TBL_USERS,
                CommonsConstants.COL_COMPANY_PERSON))
        .addFromInner(CommonsConstants.TBL_PERSONS,
            sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_PERSON))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_CARGO_ASSESSORS, COL_CARGO, cargoId),
            SqlUtils.equals(TBL_CARGO_ASSESSORS, COL_ASSESSOR, assessorId)))));

    return ResponseObject.response(packet);
  }

  /**
   * Return SqlSelect query, calculating cargo costs from CargoServices table.
   * 
   * @param flt - query filter with <b>unique</b> "Cargo" values.
   * @return query with columns: "Cargo", "CargoCost"
   */
  private SqlSelect getCargoCostQuery(SqlSelect flt) {
    String alias = SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect()
        .addField(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO), COL_CARGO)
        .addFrom(TBL_ORDER_CARGO)
        .addFromInner(flt, alias, sys.joinTables(TBL_ORDER_CARGO, alias, COL_CARGO))
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_SERVICES,
            SqlUtils.and(sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_SERVICES, COL_CARGO),
                SqlUtils.notNull(TBL_CARGO_SERVICES, COL_SERVICE_EXPENSE)))
        .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO));

    ss.addSum(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_CARGO_SERVICES, "Amount"),
        SqlUtils.field(TBL_CARGO_SERVICES, "Currency"),
        SqlUtils.nvl(SqlUtils.field(TBL_CARGO_SERVICES, "Date"),
            SqlUtils.field(TBL_ORDERS, "Date"))), "CargoCost");

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
        .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(TBL_CARGO_SERVICES,
            SqlUtils.and(sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_SERVICES, COL_CARGO),
                SqlUtils.isNull(TBL_CARGO_SERVICES, COL_SERVICE_EXPENSE)))
        .addGroup(TBL_ORDER_CARGO, sys.getIdName(TBL_ORDER_CARGO));

    IsExpression dateExpr = SqlUtils.nvl(SqlUtils.field(TBL_CARGO_SERVICES, "Date"),
        SqlUtils.field(TBL_ORDERS, "Date"));

    ss.addMax(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_ORDER_CARGO, "Price"),
        SqlUtils.field(TBL_ORDER_CARGO, "Currency"), dateExpr), "CargoIncome")
        .addSum(ExchangeUtils.exchangeField(ss, SqlUtils.field(TBL_CARGO_SERVICES, "Amount"),
            SqlUtils.field(TBL_CARGO_SERVICES, "Currency"), dateExpr), "ServicesIncome");

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
        .addExpression("ServicesCost", SqlUtils.field(alias, "CargoCost")));

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

    BeeRowSet drivers = qs.getViewData(VIEW_DRIVERS);
    if (DataUtils.isEmpty(drivers)) {
      String msg = BeeUtils.joinWords(SVC_GET_DTB_DATA, "drivers not available");
      logger.warning(msg);
      return ResponseObject.error(msg);
    }
    settings.setTableProperty(PROP_DRIVERS, drivers.serialize());

    BeeRowSet absence = qs.getViewData(VIEW_DRIVER_ABSENCE);
    if (!DataUtils.isEmpty(absence)) {
      settings.setTableProperty(PROP_ABSENCE, absence.serialize());
    }

    String vehicleJoinAlias = "veh_" + SqlUtils.uniqueName();
    String trailerJoinAlias = "trail_" + SqlUtils.uniqueName();

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String defLoadAlias = "defl_" + SqlUtils.uniqueName();
    String defUnlAlias = "defu_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    String colLoadDate = loadingColumnAlias(COL_PLACE_DATE);
    String colUnloadDate = unloadingColumnAlias(COL_PLACE_DATE);

    String colDefLoadDate = defaultLoadingColumnAlias(COL_PLACE_DATE);
    String colDefUnloadDate = defaultUnloadingColumnAlias(COL_PLACE_DATE);

    SqlSelect inner = new SqlSelect()
        .addFrom(TBL_TRIP_DRIVERS)
        .addFromLeft(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_TRIP_DRIVERS, COL_TRIP))
        .addFromLeft(TBL_VEHICLES, vehicleJoinAlias,
            SqlUtils.join(vehicleJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLES, trailerJoinAlias,
            SqlUtils.join(trailerJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_TRAILER))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, COL_TRIP_ID))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_CARGO_TRIPS, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_CARGO_TRIPS, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromLeft(TBL_CARGO_PLACES, defLoadAlias,
            SqlUtils.join(defLoadAlias, colPlaceId, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, defUnlAlias,
            SqlUtils.join(defUnlAlias, colPlaceId, TBL_ORDER_CARGO, COL_UNLOADING_PLACE));

    inner.addFields(TBL_TRIP_DRIVERS, COL_DRIVER, COL_TRIP);
    inner.addField(TBL_TRIP_DRIVERS, COL_TRIP_DRIVER_FROM, ALS_TRIP_DRIVER_FROM);
    inner.addField(TBL_TRIP_DRIVERS, COL_TRIP_DRIVER_TO, ALS_TRIP_DRIVER_TO);

    inner.addFields(TBL_TRIPS, COL_TRIP_NO, COL_TRIP_PLANNED_END_DATE);
    inner.addField(TBL_TRIPS, COL_TRIP_DATE, ALS_TRIP_DATE);
    inner.addField(TBL_TRIPS, COL_TRIP_DATE_FROM, ALS_TRIP_DATE_FROM);
    inner.addField(TBL_TRIPS, COL_TRIP_DATE_TO, ALS_TRIP_DATE_TO);

    inner.addField(vehicleJoinAlias, COL_NUMBER, ALS_VEHICLE_NUMBER);
    inner.addField(trailerJoinAlias, COL_NUMBER, ALS_TRAILER_NUMBER);

    inner.addField(loadAlias, COL_PLACE_DATE, colLoadDate);
    inner.addField(unlAlias, COL_PLACE_DATE, colUnloadDate);

    inner.addField(defLoadAlias, COL_PLACE_DATE, colDefLoadDate);
    inner.addField(defUnlAlias, COL_PLACE_DATE, colDefUnloadDate);

    String alias = "inner_" + SqlUtils.uniqueName();

    String[] group = new String[] {COL_DRIVER, COL_TRIP, ALS_TRIP_DRIVER_FROM, ALS_TRIP_DRIVER_TO,
        COL_TRIP_NO, COL_TRIP_PLANNED_END_DATE, ALS_TRIP_DATE, ALS_TRIP_DATE_FROM,
        ALS_TRIP_DATE_TO, ALS_VEHICLE_NUMBER, ALS_TRAILER_NUMBER};

    SqlSelect query = new SqlSelect()
        .addFrom(inner, alias)
        .addFields(alias, group)
        .addMin(alias, colLoadDate)
        .addMax(alias, colUnloadDate)
        .addMin(alias, colDefLoadDate)
        .addMax(alias, colDefUnloadDate)
        .addGroup(alias, group)
        .addOrder(alias, COL_DRIVER, ALS_TRIP_DATE, COL_TRIP_NO, COL_TRIP);

    SimpleRowSet data = qs.getData(query);
    if (data != null) {
      settings.setTableProperty(PROP_DATA, data.serialize());
    }

    return ResponseObject.response(settings);
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

    BeeRowSet countries = qs.getViewData(CommonsConstants.VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_ORDER_CARGO)
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_ORDER_CARGO, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_CARGO, TBL_ORDER_CARGO, COL_CARGO_ID));

    query.addFields(TBL_ORDERS, COL_STATUS, COL_ORDER_DATE, COL_ORDER_NO, COL_CUSTOMER);
    query.addField(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_NAME, COL_CUSTOMER_NAME);

    query.addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_ID, COL_CARGO_DESCRIPTION);

    query.addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE));
    query.addField(loadAlias, COL_COUNTRY, loadingColumnAlias(COL_COUNTRY));
    query.addField(loadAlias, COL_PLACE, loadingColumnAlias(COL_PLACE_NAME));
    query.addField(loadAlias, COL_TERMINAL, loadingColumnAlias(COL_TERMINAL));

    query.addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE));
    query.addField(unlAlias, COL_COUNTRY, unloadingColumnAlias(COL_COUNTRY));
    query.addField(unlAlias, COL_PLACE, unloadingColumnAlias(COL_PLACE_NAME));
    query.addField(unlAlias, COL_TERMINAL, unloadingColumnAlias(COL_TERMINAL));

    Set<Integer> statuses = Sets.newHashSet(OrderStatus.CREATED.ordinal(),
        OrderStatus.ACTIVATED.ordinal(), OrderStatus.CONFIRMED.ordinal());
    IsCondition where = SqlUtils.and(SqlUtils.inList(TBL_ORDERS, COL_STATUS, statuses),
        SqlUtils.isNull(TBL_CARGO_TRIPS, COL_CARGO));

    query.setWhere(where);

    query.addOrder(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_NAME);
    query.addOrder(TBL_ORDERS, COL_ORDER_DATE, COL_ORDER_NO);
    query.addOrder(loadAlias, COL_PLACE_DATE);
    query.addOrder(unlAlias, COL_PLACE_DATE);

    SimpleRowSet data = qs.getData(query);
    if (data == null) {
      return ResponseObject.error(SVC_GET_FX_DATA, "data not available");
    }

    settings.setTableProperty(PROP_DATA, data.serialize());
    return ResponseObject.response(settings);
  }

  private ResponseObject getOrderTripData(long orderId) {
    if (!DataUtils.isId(orderId)) {
      return ResponseObject.error("Order ID is not valid:", orderId);
    }
    SimpleRowSet data = qs.getData(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TRIPS, COL_TRIP_NO, COL_FORWARDER_VEHICLE)
        .addField(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_NAME, COL_FORWARDER)
        .addField(TBL_EXPEDITION_TYPES, COL_EXPEDITION_TYPE, COL_EXPEDITION)
        .addField(TBL_VEHICLES, COL_NUMBER, COL_VEHICLE_NUMBER)
        .addField("Trailers", COL_NUMBER, COL_TRAILER_NUMBER)
        .addFrom(TBL_ORDER_CARGO)
        .addFromInner(TBL_CARGO_TRIPS, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_CARGO_TRIPS, COL_TRIP))
        .addFromLeft(TBL_VEHICLES,
            sys.joinTables(TBL_VEHICLES, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLES, "Trailers",
            SqlUtils.join("Trailers", sys.getIdName(TBL_VEHICLES), TBL_TRIPS, COL_TRAILER))
        .addFromLeft(TBL_EXPEDITION_TYPES,
            sys.joinTables(TBL_EXPEDITION_TYPES, TBL_TRIPS, COL_EXPEDITION))
        .addFromLeft(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_TRIPS, COL_FORWARDER))
        .setWhere(SqlUtils.equals(TBL_ORDER_CARGO, COL_ORDER, orderId)));

    return ResponseObject.response(data);
  }

  private BeeRowSet getSettings() {
    long userId = usr.getCurrentUserId();
    Filter filter = ComparisonFilter.isEqual(COL_USER, new LongValue(userId));

    BeeRowSet rowSet = qs.getViewData(VIEW_TRANSPORT_SETTINGS, filter);
    if (!DataUtils.isEmpty(rowSet)) {
      return rowSet;
    }

    SqlInsert sqlInsert = new SqlInsert(TBL_TRANSPORT_SETTINGS).addConstant(COL_USER, userId);

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return null;
    }

    return qs.getViewData(VIEW_TRANSPORT_SETTINGS, filter);
  }

  private ResponseObject getSsData() {
    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Long theme = settings.getLong(0, settings.getColumnIndex(COL_SS_THEME));
    List<Color> colors = getThemeColors(theme);

    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet countries = qs.getViewData(CommonsConstants.VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    IsCondition where = SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION);

    SimpleRowSet drivers = getTripDrivers(where);
    if (!DataUtils.isEmpty(drivers)) {
      settings.setTableProperty(PROP_DRIVERS, drivers.serialize());
    }

    SimpleRowSet vehicleServices = getVehicleServices(null);
    if (!DataUtils.isEmpty(vehicleServices)) {
      settings.setTableProperty(PROP_VEHICLE_SERVICES, vehicleServices.serialize());
    }

    String vehicleJoinAlias = "veh_" + SqlUtils.uniqueName();
    String trailerJoinAlias = "trail_" + SqlUtils.uniqueName();

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String defLoadAlias = "defl_" + SqlUtils.uniqueName();
    String defUnlAlias = "defu_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TRIPS)
        .addFromLeft(TBL_VEHICLES, vehicleJoinAlias,
            SqlUtils.join(vehicleJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_VEHICLE))
        .addFromLeft(TBL_VEHICLES, trailerJoinAlias,
            SqlUtils.join(trailerJoinAlias, COL_VEHICLE_ID, TBL_TRIPS, COL_TRAILER))
        .addFromLeft(TBL_CARGO_TRIPS,
            SqlUtils.join(TBL_CARGO_TRIPS, COL_TRIP, TBL_TRIPS, COL_TRIP_ID))
        .addFromLeft(TBL_CARGO_PLACES, loadAlias,
            SqlUtils.join(loadAlias, colPlaceId, TBL_CARGO_TRIPS, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, unlAlias,
            SqlUtils.join(unlAlias, colPlaceId, TBL_CARGO_TRIPS, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_CARGO_TRIPS, COL_CARGO))
        .addFromLeft(TBL_CARGO_PLACES, defLoadAlias,
            SqlUtils.join(defLoadAlias, colPlaceId, TBL_ORDER_CARGO, COL_LOADING_PLACE))
        .addFromLeft(TBL_CARGO_PLACES, defUnlAlias,
            SqlUtils.join(defUnlAlias, colPlaceId, TBL_ORDER_CARGO, COL_UNLOADING_PLACE))
        .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
        .addFromLeft(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER));

    query.addField(TBL_TRIPS, COL_TRIP_DATE, ALS_TRIP_DATE);
    query.addFields(TBL_TRIPS, COL_TRIP_ID, COL_TRIP_NO, COL_VEHICLE, COL_TRAILER,
        COL_TRIP_DATE_FROM, COL_TRIP_DATE_TO);

    query.addField(vehicleJoinAlias, COL_NUMBER, ALS_VEHICLE_NUMBER);
    query.addField(trailerJoinAlias, COL_NUMBER, ALS_TRAILER_NUMBER);

    query.addFields(TBL_CARGO_TRIPS, COL_CARGO, COL_CARGO_TRIP_ID);
    query.addField(TBL_CARGO_TRIPS, sys.getVersionName(TBL_CARGO_TRIPS), ALS_CARGO_TRIP_VERSION);

    query.addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE));
    query.addField(loadAlias, COL_COUNTRY, loadingColumnAlias(COL_COUNTRY));
    query.addField(loadAlias, COL_PLACE, loadingColumnAlias(COL_PLACE_NAME));
    query.addField(loadAlias, COL_TERMINAL, loadingColumnAlias(COL_TERMINAL));

    query.addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE));
    query.addField(unlAlias, COL_COUNTRY, unloadingColumnAlias(COL_COUNTRY));
    query.addField(unlAlias, COL_PLACE, unloadingColumnAlias(COL_PLACE_NAME));
    query.addField(unlAlias, COL_TERMINAL, unloadingColumnAlias(COL_TERMINAL));

    query.addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_DESCRIPTION);

    query.addField(defLoadAlias, COL_PLACE_DATE, defaultLoadingColumnAlias(COL_PLACE_DATE));
    query.addField(defLoadAlias, COL_COUNTRY, defaultLoadingColumnAlias(COL_COUNTRY));
    query.addField(defLoadAlias, COL_PLACE, defaultLoadingColumnAlias(COL_PLACE_NAME));
    query.addField(defLoadAlias, COL_TERMINAL, defaultLoadingColumnAlias(COL_TERMINAL));

    query.addField(defUnlAlias, COL_PLACE_DATE, defaultUnloadingColumnAlias(COL_PLACE_DATE));
    query.addField(defUnlAlias, COL_COUNTRY, defaultUnloadingColumnAlias(COL_COUNTRY));
    query.addField(defUnlAlias, COL_PLACE, defaultUnloadingColumnAlias(COL_PLACE_NAME));
    query.addField(defUnlAlias, COL_TERMINAL, defaultUnloadingColumnAlias(COL_TERMINAL));

    query.addField(TBL_ORDERS, COL_ORDER_DATE, ALS_ORDER_DATE);
    query.addFields(TBL_ORDERS, COL_STATUS, COL_ORDER_NO, COL_CUSTOMER);
    query.addField(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_NAME, COL_CUSTOMER_NAME);

    query.setWhere(where);

    query.addOrder(vehicleJoinAlias, COL_NUMBER);
    query.addOrder(TBL_TRIPS, COL_TRIP_DATE, COL_TRIP_NO, COL_TRIP_ID);

    SimpleRowSet data = qs.getData(query);
    if (data == null) {
      return ResponseObject.error(SVC_GET_SS_DATA, "data not available");
    }

    settings.setTableProperty(PROP_DATA, data.serialize());
    return ResponseObject.response(settings);
  }

  private List<Color> getThemeColors(Long theme) {
    List<Color> result = Lists.newArrayList();

    BeeRowSet rowSet;
    if (theme != null) {
      rowSet = qs.getViewData(CommonsConstants.VIEW_THEME_COLORS,
          ComparisonFilter.isEqual(CommonsConstants.COL_THEME, new LongValue(theme)));
    } else {
      rowSet = null;
    }

    if (DataUtils.isEmpty(rowSet)) {
      rowSet = qs.getViewData(CommonsConstants.VIEW_COLORS);
      if (DataUtils.isEmpty(rowSet)) {
        return result;
      }
    }

    int bgIndex = rowSet.getColumnIndex(CommonsConstants.COL_BACKGROUND);
    int fgIndex = rowSet.getColumnIndex(CommonsConstants.COL_FOREGROUND);

    for (BeeRow row : rowSet.getRows()) {
      String bg = row.getString(bgIndex);
      String fg = row.getString(fgIndex);

      if (!BeeUtils.isEmpty(bg)) {
        result.add(new Color(bg.trim(), BeeUtils.trim(fg)));
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
      boolean plusMode = (i == 0);
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
        .addFromLeft(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, TBL_DRIVERS, COL_DRIVER_PERSON))
        .addFromLeft(CommonsConstants.TBL_PERSONS,
            sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_PERSON));

    query.addFields(TBL_TRIP_DRIVERS, COL_TRIP);
    query.addFields(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME,
        CommonsConstants.COL_LAST_NAME);

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

  private SimpleRowSet getVehicleServices(IsCondition condition) {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_VEHICLE_SERVICES)
        .addFromLeft(TBL_VEHICLES, sys.joinTables(TBL_VEHICLES, TBL_VEHICLE_SERVICES, COL_VEHICLE))
        .addFromLeft(TBL_SERVICE_TYPES, sys.joinTables(TBL_SERVICE_TYPES, TBL_VEHICLE_SERVICES,
            COL_SERVICE_TYPE));

    query.addFields(TBL_VEHICLE_SERVICES, COL_VEHICLE, COL_SERVICE_DATE, COL_SERVICE_DATE_TO,
        COL_SERVICE_NOTES);
    query.addFields(TBL_VEHICLES, COL_NUMBER);
    query.addFields(TBL_SERVICE_TYPES, COL_SERVICE_NAME);

    query.addOrder(TBL_VEHICLE_SERVICES, COL_VEHICLE, COL_SERVICE_DATE);

    if (condition != null) {
      query.setWhere(condition);
    }

    return qs.getData(query);
  }

  private ResponseObject getVehicleTbData(String svc, boolean truck) {
    BeeRowSet settings = getSettings();
    if (settings == null) {
      return ResponseObject.error("user settings not available");
    }

    Filter vehicleFilter = Filter.notEmpty(truck ? COL_IS_TRUCK : COL_IS_TRAILER);
    Order vehicleOrder = new Order(COL_NUMBER, true);

    BeeRowSet vehicles = qs.getViewData(VIEW_VEHICLES, vehicleFilter, vehicleOrder);
    if (DataUtils.isEmpty(vehicles)) {
      String msg = BeeUtils.joinWords(svc, "vehicles not available");
      logger.warning(msg);
      return ResponseObject.error(msg);
    }
    settings.setTableProperty(PROP_VEHICLES, vehicles.serialize());

    String themeColumnName = truck ? COL_TRUCK_THEME : COL_TRAILER_THEME;
    Long theme = settings.getLong(0, settings.getColumnIndex(themeColumnName));
    List<Color> colors = getThemeColors(theme);
    settings.setTableProperty(PROP_COLORS, Codec.beeSerialize(colors));

    BeeRowSet countries = qs.getViewData(CommonsConstants.VIEW_COUNTRIES);
    settings.setTableProperty(PROP_COUNTRIES, countries.serialize());

    List<Long> vehicleIds = DataUtils.getRowIds(vehicles);

    SimpleRowSet vehicleServices = getVehicleServices(SqlUtils.inList(TBL_VEHICLE_SERVICES,
        COL_VEHICLE, vehicleIds));
    if (!DataUtils.isEmpty(vehicleServices)) {
      settings.setTableProperty(PROP_VEHICLE_SERVICES, vehicleServices.serialize());
    }

    IsCondition tripWhere = SqlUtils.and(SqlUtils.isNull(TBL_TRIPS, COL_EXPEDITION),
        SqlUtils.inList(TBL_TRIPS, truck ? COL_VEHICLE : COL_TRAILER, vehicleIds));

    String truckJoinAlias = "truck_" + SqlUtils.uniqueName();
    String trailerJoinAlias = "trail_" + SqlUtils.uniqueName();

    SqlSelect tripQuery = new SqlSelect().addFrom(TBL_TRIPS)
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
        .setWhere(tripWhere)
        .addOrder(truck ? truckJoinAlias : trailerJoinAlias, COL_NUMBER);

    SimpleRowSet trips = qs.getData(tripQuery);
    if (DataUtils.isEmpty(trips)) {
      logger.warning(svc, "trips not available");
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_TRIPS, trips.serialize());

    SimpleRowSet drivers = getTripDrivers(tripWhere);
    if (!DataUtils.isEmpty(drivers)) {
      settings.setTableProperty(PROP_DRIVERS, drivers.serialize());
    }

    String loadAlias = "load_" + SqlUtils.uniqueName();
    String unlAlias = "unl_" + SqlUtils.uniqueName();

    String defLoadAlias = "defl_" + SqlUtils.uniqueName();
    String defUnlAlias = "defu_" + SqlUtils.uniqueName();

    String colPlaceId = sys.getIdName(TBL_CARGO_PLACES);

    SqlSelect freightQuery = new SqlSelect()
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
        .addFromLeft(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_ORDERS, COL_CUSTOMER))
        .addFields(TBL_TRIPS, COL_TRIP_ID, COL_VEHICLE, COL_TRAILER)
        .addFields(TBL_CARGO_TRIPS, COL_CARGO, COL_CARGO_TRIP_ID)
        .addField(TBL_CARGO_TRIPS, sys.getVersionName(TBL_CARGO_TRIPS), ALS_CARGO_TRIP_VERSION)
        .addField(loadAlias, COL_PLACE_DATE, loadingColumnAlias(COL_PLACE_DATE))
        .addField(loadAlias, COL_COUNTRY, loadingColumnAlias(COL_COUNTRY))
        .addField(loadAlias, COL_PLACE, loadingColumnAlias(COL_PLACE_NAME))
        .addField(loadAlias, COL_TERMINAL, loadingColumnAlias(COL_TERMINAL))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_COUNTRY, unloadingColumnAlias(COL_COUNTRY))
        .addField(unlAlias, COL_PLACE, unloadingColumnAlias(COL_PLACE_NAME))
        .addField(unlAlias, COL_TERMINAL, unloadingColumnAlias(COL_TERMINAL))
        .addFields(TBL_ORDER_CARGO, COL_ORDER, COL_CARGO_DESCRIPTION, COL_CARGO_NOTES)
        .addField(defLoadAlias, COL_PLACE_DATE, defaultLoadingColumnAlias(COL_PLACE_DATE))
        .addField(defLoadAlias, COL_COUNTRY, defaultLoadingColumnAlias(COL_COUNTRY))
        .addField(defLoadAlias, COL_PLACE, defaultLoadingColumnAlias(COL_PLACE_NAME))
        .addField(defLoadAlias, COL_TERMINAL, defaultLoadingColumnAlias(COL_TERMINAL))
        .addField(defUnlAlias, COL_PLACE_DATE, defaultUnloadingColumnAlias(COL_PLACE_DATE))
        .addField(defUnlAlias, COL_COUNTRY, defaultUnloadingColumnAlias(COL_COUNTRY))
        .addField(defUnlAlias, COL_PLACE, defaultUnloadingColumnAlias(COL_PLACE_NAME))
        .addField(defUnlAlias, COL_TERMINAL, defaultUnloadingColumnAlias(COL_TERMINAL))
        .addFields(TBL_ORDERS, COL_ORDER_NO)
        .addField(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_NAME, COL_CUSTOMER_NAME)
        .setWhere(tripWhere)
        .addOrder(TBL_TRIPS, COL_TRIP_ID);

    SimpleRowSet freights = qs.getData(freightQuery);
    if (DataUtils.isEmpty(freights)) {
      return ResponseObject.response(settings);
    }
    settings.setTableProperty(PROP_FREIGHTS, freights.serialize());

    IsCondition cargoHandlingWhere = SqlUtils.or(SqlUtils.notNull(loadAlias, COL_PLACE_DATE),
        SqlUtils.notNull(unlAlias, COL_PLACE_DATE));

    SqlSelect cargoHandlingQuery = new SqlSelect()
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
        .addField(loadAlias, COL_COUNTRY, loadingColumnAlias(COL_COUNTRY))
        .addField(loadAlias, COL_PLACE, loadingColumnAlias(COL_PLACE_NAME))
        .addField(loadAlias, COL_TERMINAL, loadingColumnAlias(COL_TERMINAL))
        .addField(unlAlias, COL_PLACE_DATE, unloadingColumnAlias(COL_PLACE_DATE))
        .addField(unlAlias, COL_COUNTRY, unloadingColumnAlias(COL_COUNTRY))
        .addField(unlAlias, COL_PLACE, unloadingColumnAlias(COL_PLACE_NAME))
        .addField(unlAlias, COL_TERMINAL, unloadingColumnAlias(COL_TERMINAL))
        .setWhere(SqlUtils.and(tripWhere, cargoHandlingWhere))
        .addOrder(TBL_CARGO_HANDLING, COL_CARGO);

    SimpleRowSet cargoHandling = qs.getData(cargoHandlingQuery);
    if (!DataUtils.isEmpty(cargoHandling)) {
      settings.setTableProperty(PROP_CARGO_HANDLING, cargoHandling.serialize());
    }

    return ResponseObject.response(settings);
  }
}
