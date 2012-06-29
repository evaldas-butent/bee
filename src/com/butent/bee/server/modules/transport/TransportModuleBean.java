package com.butent.bee.server.modules.transport;

import com.google.common.eventbus.Subscribe;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TransportModuleBean implements BeeModule {

  private static Logger logger = Logger.getLogger(TransportModuleBean.class.getName());

  @EJB
  DataEditorBean deb;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  @Override
  public String dependsOn() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(TransportConstants.TRANSPORT_METHOD);

    if (BeeUtils.same(svc, TransportConstants.SVC_GET_BEFORE)) {
      long vehicle = BeeUtils.toLong(reqInfo.getParameter("Vehicle"));
      long date = BeeUtils.toLong(reqInfo.getParameter("Date"));

      response = getTripBeforeData(vehicle, date);

    } else if (BeeUtils.same(svc, TransportConstants.SVC_GET_PROFIT)) {
      if (reqInfo.hasParameter(TransportConstants.VAR_TRIP_ID)) {
        response =
            getTripProfit(BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_TRIP_ID)));

      } else if (reqInfo.hasParameter(TransportConstants.VAR_CARGO_ID)) {
        Long cargoId = BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_CARGO_ID));

        response = getCargoProfit(new SqlSelect().addConstant(cargoId, "Cargo"));

      } else if (reqInfo.hasParameter(TransportConstants.VAR_ORDER_ID)) {
        Long orderId = BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_ORDER_ID));
        String cargo = TransportConstants.VIEW_CARGO;

        response = getCargoProfit(new SqlSelect()
            .addField(cargo, sys.getIdName(cargo), "Cargo")
            .addFrom(cargo)
            .setWhere(SqlUtils.equal(cargo, "Order", orderId)));

      } else {
        response = ResponseObject.error("Profit of WHAT?");
      }

    } else {
      String msg = BeeUtils.concat(1, "Transport service not recognized:", svc);
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
    return TransportConstants.TRANSPORT_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void fillFuelConsumptions(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TransportConstants.VIEW_TRIP_ROUTES)) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Consumption");
            SimpleRowSet rs = qs.getData(getFuelConsumptionsQuery(event.getQuery()
                .resetFields().resetOrder()
                .addFields(TransportConstants.VIEW_TRIP_ROUTES,
                    sys.getIdName(TransportConstants.VIEW_TRIP_ROUTES)), true));
            int idIndex = rs.getColumnIndex(sys.getIdName(TransportConstants.VIEW_TRIP_ROUTES));
            int qtyIndex = rs.getColumnIndex("Quantity");

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              rowset.updateCell(rs.getLong(i, idIndex), colIndex, rs.getValue(i, qtyIndex));
            }
          }
        }
      }
    });

    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void fillCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TransportConstants.VIEW_CARGO)) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Income");
            SimpleRowSet rs = qs.getData(getCargoIncomeQuery(event.getQuery()
                .resetFields().resetOrder()
                .addField(TransportConstants.VIEW_CARGO,
                    sys.getIdName(TransportConstants.VIEW_CARGO), "Cargo")));
            int idIndex = rs.getColumnIndex("Cargo");
            int incomeIndex = rs.getColumnIndex("CargoIncome");

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              rowset.updateCell(rs.getLong(i, idIndex), colIndex, rs.getValue(i, incomeIndex));
            }
          }
        }
      }
    });

    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void fillTripCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TransportConstants.VIEW_TRIP_CARGO)) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Income");
            int cargoIndex = rowset.getColumnIndex("Cargo");
            String crs = getTripIncome(event.getQuery().resetFields().resetOrder()
                .addFields(TransportConstants.VIEW_CARGO_TRIPS, "Trip"));

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
    });
  }

  /**
   * Return SqlSelect query, calculating cargo incomes from CargoServices table.
   * 
   * @param flt - query filter with <b>unique</b> "Cargo" values.
   * @return query with two columns: "Cargo" and "CargoIncome"
   */
  private SqlSelect getCargoIncomeQuery(SqlSelect flt) {
    String orders = TransportConstants.VIEW_ORDERS;
    String cargo = TransportConstants.VIEW_CARGO;
    String services = TransportConstants.VIEW_CARGO_SERVICES;
    String cargoId = "Cargo";

    SqlSelect ss = new SqlSelect()
        .addFields(services, cargoId)
        .addFrom(services)
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", cargoId)
            .addFrom(flt, "subId"), "sub", SqlUtils.joinUsing(services, "sub", cargoId))
        .addFromInner(cargo, SqlUtils.join(services, cargoId, cargo, sys.getIdName(cargo)))
        .addFromInner(orders, SqlUtils.join(cargo, "Order", orders, sys.getIdName(orders)))
        .addGroup(services, cargoId);

    ss.addSum(SqlUtils.multiply(SqlUtils.field(services, "Quantity"),
        ExchangeUtils.exchangeField(ss, SqlUtils.field(services, "Price"),
            SqlUtils.field(services, "Currency"), SqlUtils.field(orders, "Date"))),
        "CargoIncome");

    return ss;
  }

  private ResponseObject getCargoProfit(SqlSelect flt) {
    String services = TransportConstants.VIEW_CARGO_SERVICES;
    String cargoTrips = TransportConstants.VIEW_CARGO_TRIPS;
    String cargoId = "Cargo";
    String tripId = "Trip";

    SqlSelect ss = getCargoIncomeQuery(flt)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "CostPrice")),
            "CargoCost")
        .addEmptyDouble("TripCost");

    String crsTotals = qs.sqlCreateTemp(ss);
    qs.sqlIndex(crsTotals, cargoId);

    ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields(cargoTrips, tripId)
        .addFrom(cargoTrips)
        .addFromInner(crsTotals, SqlUtils.joinUsing(cargoTrips, crsTotals, cargoId));

    String crsIncomes = getTripIncome(ss);
    String crsCosts = getTripCost(ss);

    ss = new SqlSelect()
        .addFields(crsIncomes, tripId)
        .addSum(cargoTrips, "CargoPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "CargoPercent"),
            SqlUtils.field(crsIncomes, "TripIncome"), 0), "TotalIncome")
        .addFrom(crsIncomes)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(crsIncomes, cargoTrips, tripId, cargoId))
        .addGroup(crsIncomes, tripId);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, tripId);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(
            SqlUtils.plus(SqlUtils.nvl(SqlUtils.field(crsCosts, "TripCost"), 0),
                SqlUtils.nvl(SqlUtils.field(crsCosts, "FuelCost"), 0)),
            100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "CargoPercent"),
            SqlUtils.multiply(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp, "TotalPercent"), 0)),
                SqlUtils.divide(SqlUtils.field(crsIncomes, "TripIncome"),
                    SqlUtils.field(tmp, "TotalIncome"))),
            SqlUtils.field(cargoTrips, "CargoPercent")));

    ss = new SqlSelect()
        .addFields(crsIncomes, cargoId)
        .addSum(xpr, "Cost")
        .addFrom(crsIncomes)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(crsIncomes, cargoTrips, tripId, cargoId))
        .addFromInner(crsCosts, SqlUtils.joinUsing(crsIncomes, crsCosts, tripId))
        .addFromInner(tmp, SqlUtils.joinUsing(crsIncomes, tmp, tripId))
        .addGroup(crsIncomes, cargoId);

    String crsTripCosts = qs.sqlCreateTemp(ss);
    qs.sqlIndex(crsTripCosts, cargoId);

    qs.sqlDropTemp(tmp);
    qs.sqlDropTemp(crsCosts);
    qs.sqlDropTemp(crsIncomes);

    SqlUpdate su = new SqlUpdate(crsTotals)
        .setFrom(crsTripCosts, SqlUtils.joinUsing(crsTotals, crsTripCosts, cargoId))
        .addExpression("TripCost", SqlUtils.field(crsTripCosts, "Cost"));

    qs.updateData(su);

    qs.sqlDropTemp(crsTripCosts);

    ss = new SqlSelect()
        .addSum(crsTotals, "CargoIncome")
        .addSum(crsTotals, "CargoCost")
        .addSum(crsTotals, "TripCost")
        .addFrom(crsTotals);

    Map<String, String> res = qs.getRow(ss);

    qs.sqlDropTemp(crsTotals);

    return ResponseObject.response(new String[] {BeeUtils.transformMap(res)});
  }

  /**
   * Return SqlSelect query, calculating trip fuel consumptions from TripRoutes table.
   * 
   * @param flt - query filter with <b>unique</b> TripRoutes ID values.
   * @param routeMode - if true, returns results, grouped by TripRoutes ID, else grouped by Trip ID
   * @return query with two columns: (TripRoutes ID or "Trip") and "Quantity"
   */
  private SqlSelect getFuelConsumptionsQuery(SqlSelect flt, boolean routeMode) {
    String trips = TransportConstants.VIEW_TRIPS;
    String routes = TransportConstants.VIEW_TRIP_ROUTES;
    String fuel = TransportConstants.VIEW_FUEL_CONSUMPTIONS;
    String temps = TransportConstants.VIEW_FUEL_TEMPERATURES;
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

    return new SqlSelect()
        .addFields(routes, routeMode ? routeId : "Trip")
        .addSum(xpr, "Quantity")
        .addFrom(routes)
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", routeId)
            .addFrom(flt, "subId"), "sub", SqlUtils.joinUsing(routes, "sub", routeId))
        .addFromInner(trips, SqlUtils.join(routes, "Trip", trips, sys.getIdName(trips)))
        .addFromInner(fuel, SqlUtils.joinUsing(trips, fuel, "Vehicle"))
        .addFromLeft(temps,
            SqlUtils.and(SqlUtils.join(fuel, sys.getIdName(fuel), temps, "Consumption"),
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

  private ResponseObject getTripBeforeData(long vehicle, long date) {
    String[] resp = new String[2];

    if (!BeeUtils.isEmpty(date)) {
      String trips = TransportConstants.VIEW_TRIPS;
      String routes = TransportConstants.VIEW_TRIP_ROUTES;
      String fuels = TransportConstants.VIEW_TRIP_FUEL_COSTS;
      String consumptions = TransportConstants.VIEW_TRIP_FUEL_CONSUMPTIONS;
      String tripId = sys.getIdName(trips);

      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(trips,
              tripId, "SpeedometerBefore", "SpeedometerAfter", "FuelBefore", "FuelAfter")
          .addFrom(trips)
          .setWhere(SqlUtils.and(SqlUtils.equal(trips, "Vehicle", vehicle),
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
              .setWhere(SqlUtils.equal(routes, "Trip", rs.getLong(cnt, tripId))));

          speedometer = BeeUtils.unbox(rs.getDouble(cnt, "SpeedometerBefore"))
              + BeeUtils.unbox(km);

          Integer scale = BeeUtils.toIntOrNull(qs.sqlValue(TransportConstants.VIEW_VEHICLES,
              "Speedometer", vehicle));

          if (BeeUtils.isPositive(scale) && scale < speedometer) {
            speedometer -= scale;
          }
        }
        if (fuel == null) {
          Double fill = qs.getDouble(new SqlSelect()
              .addSum(fuels, "Quantity")
              .addFrom(fuels)
              .setWhere(SqlUtils.equal(fuels, "Trip", rs.getLong(cnt, tripId))));

          Map<String, String> row = qs.getRow(getFuelConsumptionsQuery(new SqlSelect()
              .addFields(routes, sys.getIdName(routes))
              .addFrom(routes)
              .setWhere(SqlUtils.equal(routes, "Trip", rs.getLong(cnt, tripId))), false));

          Double consume = BeeUtils.isEmpty(row) ? null : BeeUtils.toDouble(row.get("Quantity"));

          Double addit = qs.getDouble(new SqlSelect()
              .addSum(consumptions, "Quantity")
              .addFrom(consumptions)
              .setWhere(SqlUtils.equal(consumptions, "Trip", rs.getLong(cnt, tripId))));

          fuel = BeeUtils.unbox(rs.getDouble(cnt, "FuelBefore")) + BeeUtils.unbox(fill)
              - BeeUtils.unbox(consume) - BeeUtils.unbox(addit);
        }
        resp[0] = BeeUtils.transform(speedometer);
        resp[1] = BeeUtils.transform(fuel);
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
    String trips = TransportConstants.VIEW_TRIPS;
    String costs = TransportConstants.VIEW_TRIP_COSTS;
    String fuels = TransportConstants.VIEW_TRIP_FUEL_COSTS;
    String routes = TransportConstants.VIEW_TRIP_ROUTES;
    String consumptions = TransportConstants.VIEW_TRIP_FUEL_CONSUMPTIONS;
    String tripId = "Trip";
    String tripNativeId = sys.getIdName(trips);

    // Trip costs
    SqlSelect ss = new SqlSelect()
        .addField(trips, tripNativeId, tripId)
        .addField(trips, "Date", "TripDate")
        .addFields(trips, "Vehicle", "FuelBefore", "FuelAfter")
        .addEmptyDouble("FuelCost")
        .addFrom(trips)
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", tripId)
            .addFrom(flt, "subId"), "sub", SqlUtils.join(trips, tripNativeId, "sub", tripId))
        .addFromLeft(costs, SqlUtils.join(trips, tripNativeId, costs, tripId))
        .addGroup(trips, tripNativeId, "Date", "Vehicle", "FuelBefore", "FuelAfter");

    ss.addSum(SqlUtils.multiply(SqlUtils.field(costs, "Quantity"),
        ExchangeUtils.exchangeField(ss, costs, "Price", "Currency", "Date")),
        "TripCost");

    String tmpCosts = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpCosts, tripId);

    // Fuel costs
    ss = new SqlSelect()
        .addFields(tmpCosts, tripId)
        .addSum(fuels, "Quantity")
        .addFrom(tmpCosts)
        .addFromLeft(fuels, SqlUtils.joinUsing(tmpCosts, fuels, tripId))
        .addGroup(tmpCosts, tripId);

    ss.addSum(SqlUtils.multiply(SqlUtils.field(fuels, "Quantity"),
        ExchangeUtils.exchangeField(ss, fuels, "Price", "Currency", "Date")),
        "FuelCost");

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, tripId);

    qs.updateData(new SqlUpdate(tmpCosts)
        .setFrom(tmp, SqlUtils.joinUsing(tmpCosts, tmp, tripId))
        .addExpression("FuelCost", SqlUtils.field(tmp, "FuelCost")));

    // Fuel consumptions
    if (qs.sqlExists(tmpCosts, SqlUtils.isNull(tmpCosts, "FuelAfter"))) {
      ss = new SqlSelect()
          .addFields(routes, sys.getIdName(routes))
          .addFrom(routes)
          .addFromInner(tmpCosts, SqlUtils.joinUsing(routes, tmpCosts, tripId));

      String tmpRoutes = qs.sqlCreateTemp(getFuelConsumptionsQuery(ss, false));
      qs.sqlIndex(tmpRoutes, tripId);

      String tmpConsumptions = qs.sqlCreateTemp(new SqlSelect()
          .addFields(consumptions, tripId)
          .addSum(consumptions, "Quantity")
          .addFrom(consumptions)
          .addFromInner(tmpCosts, SqlUtils.joinUsing(consumptions, tmpCosts, tripId))
          .addGroup(consumptions, tripId));

      qs.sqlIndex(tmpConsumptions, tripId);

      qs.updateData(new SqlUpdate(tmpCosts)
          .setFrom(new SqlSelect()
              .addFields(tmp, tripId, "Quantity")
              .addField(tmpRoutes, "Quantity", "routeQuantity")
              .addField(tmpConsumptions, "Quantity", "consumeQuantity")
              .addFrom(tmp)
              .addFromLeft(tmpRoutes, SqlUtils.joinUsing(tmp, tmpRoutes, tripId))
              .addFromLeft(tmpConsumptions, SqlUtils.joinUsing(tmp, tmpConsumptions, tripId)),
              "sub", SqlUtils.joinUsing(tmpCosts, "sub", tripId))
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
        .addFromInner(fuels, SqlUtils.join(trips, tripNativeId, fuels, tripId))
        .addFromInner(new SqlSelect()
            .addFields(trips, "Vehicle")
            .addMax(trips, "Date", "MaxDate")
            .addFrom(trips)
            .addFromInner(tmpCosts, SqlUtils.join(trips, tripNativeId, tmpCosts, tripId))
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
          .addFields(tmpCosts, tripId, "TripDate", "Vehicle")
          .addField(tmpCosts, fld, "Remainder")
          .addEmptyDate("Date")
          .addEmptyDouble("Cost")
          .addFrom(tmpCosts)
          .setWhere(SqlUtils.positive(tmpCosts, fld)));

      qs.sqlIndex(tmp, tripId, "Vehicle");
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
          .setFrom(tmp, SqlUtils.joinUsing(tmpCosts, tmp, tripId))
          .addExpression("FuelCost", expr));

      qs.sqlDropTemp(tmp);
    }
    qs.sqlDropTemp(tmpFuels);

    return tmpCosts;
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
    String services = TransportConstants.VIEW_CARGO_SERVICES;
    String cargoTrips = TransportConstants.VIEW_CARGO_TRIPS;
    String tripId = "Trip";
    String cargoId = "Cargo";

    String tmp = qs.sqlCreateTemp(getCargoIncomeQuery(
        new SqlSelect()
            .setDistinctMode(true)
            .addFields(cargoTrips, cargoId)
            .addFrom(cargoTrips)
            .addFromInner(new SqlSelect()
                .setDistinctMode(true)
                .addFields("subId", tripId)
                .addFrom(flt, "subId"), "sub", SqlUtils.joinUsing(cargoTrips, "sub", tripId)))
        .setWhere(SqlUtils.isNull(services, "CostPrice")));

    qs.sqlIndex(tmp, cargoId);

    String tmp2 = qs.sqlCreateTemp(new SqlSelect()
        .addFields(tmp, cargoId, "CargoIncome")
        .addSum(cargoTrips, "TripPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"), 1, 0), "CntEmpty")
        .addFrom(tmp)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp, cargoTrips, cargoId))
        .addGroup(tmp, cargoId, "CargoIncome"));

    qs.sqlIndex(tmp2, cargoId);
    qs.sqlDropTemp(tmp);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(SqlUtils.field(tmp2, "CargoIncome"), 100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"),
            SqlUtils.divide(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp2, "TotalPercent"), 0)),
                SqlUtils.field(tmp2, "CntEmpty")),
            SqlUtils.field(cargoTrips, "TripPercent")));

    tmp = qs.sqlCreateTemp(new SqlSelect()
        .addFields(cargoTrips, cargoId, tripId)
        .addExpr(xpr, "TripIncome")
        .addFrom(tmp2)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp2, cargoTrips, cargoId))
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", tripId)
            .addFrom(flt, "subId"), "sub", SqlUtils.joinUsing(cargoTrips, "sub", tripId)));

    qs.sqlIndex(tmp, cargoId, tripId);
    qs.sqlDropTemp(tmp2);

    return tmp;
  }

  private ResponseObject getTripProfit(long tripId) {
    String crs = getTripCost(new SqlSelect().addConstant(tripId, "Trip"));

    SqlSelect ss = new SqlSelect()
        .addSum(crs, "TripCost")
        .addSum(crs, "FuelCost")
        .addFrom(crs);

    Map<String, String> res = qs.getRow(ss);

    qs.sqlDropTemp(crs);

    crs = getTripIncome(new SqlSelect().addConstant(tripId, "Trip"));

    ss = new SqlSelect()
        .addSum(crs, "TripIncome")
        .addFrom(crs);

    res.put("TripIncome", qs.getValue(ss));

    qs.sqlDropTemp(crs);

    return ResponseObject.response(new String[] {BeeUtils.transformMap(res)});
  }
}
