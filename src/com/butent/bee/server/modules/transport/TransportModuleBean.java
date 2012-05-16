package com.butent.bee.server.modules.transport;

import com.google.common.eventbus.Subscribe;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
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
import com.butent.bee.shared.utils.Codec;

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

    if (BeeUtils.same(svc, TransportConstants.SVC_UPDATE_KM)) {
      response = updateKilometers(reqInfo);

    } else if (BeeUtils.same(svc, TransportConstants.SVC_GET_PROFIT)) {
      if (reqInfo.hasParameter(TransportConstants.VAR_TRIP_ID)) {
        response =
            getTripProfit(BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_TRIP_ID)));

      } else if (reqInfo.hasParameter(TransportConstants.VAR_CARGO_ID)) {
        Long cargoId = BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_CARGO_ID));

        response = getCargoProfit(new SqlSelect().addConstant(cargoId, "Cargo"));

      } else if (reqInfo.hasParameter(TransportConstants.VAR_ORDER_ID)) {
        Long orderId = BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_ORDER_ID));
        String cargo = "OrderCargo";

        SqlSelect ss = new SqlSelect()
            .addField(cargo, sys.getIdName(cargo), "Cargo")
            .addFrom(cargo)
            .setWhere(SqlUtils.equal(cargo, "Order", orderId));

        response = getCargoProfit(ss);

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
      @SuppressWarnings("unused")
      @Subscribe
      public void fillFuelConsumptions(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TransportConstants.VIEW_TRIP_ROUTES)) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("Consumption");

            SimpleRowSet rs = qs.getData(getFuelConsumptionsQuery(event.getQuery(), true));

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              rowset.updateCell(rs.getLong(i, 0), colIndex, rs.getValue(i, 1));
            }
          }
        }
      }
    });

    sys.registerViewEventHandler(new ViewEventHandler() {
      @SuppressWarnings("unused")
      @Subscribe
      public void fillCargoIncomes(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), TransportConstants.VIEW_TRIP_CARGO)) {
          BeeRowSet rowset = event.getRowset();

          if (!rowset.isEmpty()) {
            int colIndex = rowset.getColumnIndex("XXX");
            int cargoIndex = rowset.getColumnIndex("Cargo");
            String crs = getTripIncome(event.getQuery().resetFields().resetOrder()
                .addFields("CargoTrips", "Trip"));

            SimpleRowSet rs = qs.getData(new SqlSelect().addAllFields(crs).addFrom(crs));
            qs.sqlDropTemp(crs);

            for (int i = 0; i < rs.getNumberOfRows(); i++) {
              Long cargoId = rs.getLong(i, "Cargo");

              for (int j = 0; j < rowset.getNumberOfRows(); j++) {
                BeeRow row = rowset.getRow(j);

                if (row.getLong(cargoIndex) == cargoId) {
                  row.setValue(colIndex, rs.getValue(i, "Income"));
                  break;
                }
              }
            }
          }
        }
      }
    });
  }

  private ResponseObject getCargoProfit(SqlSelect flt) {
    String services = "CargoServices";
    String cargoTrips = "CargoTrips";
    String cargoId = "Cargo";
    String tripId = "Trip";

    SqlSelect ss = new SqlSelect()
        .addFields(services, cargoId)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "Price")),
            "Income")
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "CostPrice")),
            "Cost")
        .addEmptyDouble("TripCost")
        .addFrom(services)
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", cargoId)
            .addFrom(flt, "subId"), "sub", SqlUtils.joinUsing(services, "sub", cargoId))
        .addGroup(services, cargoId);

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
            SqlUtils.field(crsIncomes, "Income"), 0), "TotalIncome")
        .addFrom(crsIncomes)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(crsIncomes, cargoTrips, tripId, cargoId))
        .addGroup(crsIncomes, tripId);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, tripId);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(
            SqlUtils.plus(SqlUtils.nvl(SqlUtils.field(crsCosts, "Cost"), 0),
                SqlUtils.nvl(SqlUtils.field(crsCosts, "FuelCost"), 0)),
            100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "CargoPercent"),
            SqlUtils.multiply(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp, "TotalPercent"), 0)),
                SqlUtils.divide(SqlUtils.field(crsIncomes, "Income"),
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
        .addSum(crsTotals, "Income")
        .addSum(crsTotals, "Cost")
        .addSum(crsTotals, "TripCost")
        .addFrom(crsTotals);

    Map<String, String> res = qs.getRow(ss);

    qs.sqlDropTemp(crsTotals);

    return ResponseObject.response(new String[] {BeeUtils.transformMap(res)});
  }

  private SqlSelect getFuelConsumptionsQuery(SqlSelect flt, boolean routeMode) {
    String trips = "Trips";
    String routes = "TripRoutes";
    String fuel = "FuelConsumptions";
    String temps = "FuelTemperatures";
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

    SqlSelect ss = new SqlSelect()
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

    return ss;
  }

  private String getTripCost(SqlSelect flt) {
    String trips = "Trips";
    String costs = "TripCosts";
    String fuel = "TripFuelCosts";
    String routes = "TripRoutes";
    String consumptions = "TripFuelConsumptions";
    String routeId = sys.getIdName(routes);
    String tripId = "Trip";
    String tripNativeId = sys.getIdName(trips);

    // Trip costs
    String tmpCosts = qs.sqlCreateTemp(new SqlSelect()
        .addField(trips, tripNativeId, tripId)
        .addField(trips, "Date", "TripDate")
        .addFields(trips, "Vehicle", "FuelBefore", "FuelAfter")
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(costs, "Quantity", "Price")), "Cost")
        .addEmptyDouble("FuelCost")
        .addFrom(trips)
        .addFromInner(new SqlSelect()
            .setDistinctMode(true)
            .addFields("subId", tripId)
            .addFrom(flt, "subId"), "sub",
            SqlUtils.join(trips, tripNativeId, "sub", tripId))
        .addFromLeft(costs, SqlUtils.join(trips, tripNativeId, costs, tripId))
        .addGroup(trips, tripNativeId, "Date", "Vehicle", "FuelBefore", "FuelAfter"));

    qs.sqlIndex(tmpCosts, tripId);

    // Fuel costs
    String tmp = qs.sqlCreateTemp(new SqlSelect()
        .addFields(tmpCosts, tripId)
        .addSum(fuel, "Quantity")
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(fuel, "Quantity", "Price")), "Cost")
        .addFrom(tmpCosts)
        .addFromLeft(fuel, SqlUtils.joinUsing(tmpCosts, fuel, tripId))
        .addGroup(tmpCosts, tripId));

    qs.sqlIndex(tmp, tripId);

    qs.updateData(new SqlUpdate(tmpCosts)
        .setFrom(tmp, SqlUtils.joinUsing(tmpCosts, tmp, tripId))
        .addExpression("FuelCost", SqlUtils.field(tmp, "Cost")));

    // Fuel consumptions
    if (qs.sqlExists(tmpCosts, SqlUtils.isNull(tmpCosts, "FuelAfter"))) {
      SqlSelect ss = new SqlSelect()
          .addFields(routes, routeId)
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
    String tmpFuels = qs.sqlCreateTemp(new SqlSelect()
        .addFields(trips, "Vehicle")
        .addField(trips, "Date", "TripDate")
        .addFields(fuel, "Date")
        .addSum(fuel, "Quantity")
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(fuel, "Quantity", "Price")), "Sum")
        .addFrom(trips)
        .addFromInner(fuel, SqlUtils.join(trips, tripNativeId, fuel, tripId))
        .addFromInner(new SqlSelect()
            .addFields(trips, "Vehicle")
            .addMax(trips, "Date", "MaxDate")
            .addFrom(trips)
            .addFromInner(tmpCosts, SqlUtils.join(trips, tripNativeId, tmpCosts, tripId))
            .addGroup(trips, "Vehicle"), "sub",
            SqlUtils.and(SqlUtils.joinUsing(trips, "sub", "Vehicle"),
                SqlUtils.joinLessEqual(trips, "Date", "sub", "MaxDate"),
                SqlUtils.and(SqlUtils.positive(fuel, "Quantity"),
                    SqlUtils.positive(fuel, "Price"))))
        .addGroup(trips, "Vehicle", "Date")
        .addGroup(fuel, "Date"));

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

  private String getTripIncome(SqlSelect flt) {
    String services = "CargoServices";
    String cargoTrips = "CargoTrips";
    String tripId = "Trip";
    String cargoId = "Cargo";

    SqlSelect ss = new SqlSelect()
        .addFields(cargoTrips, tripId, cargoId)
        .addFrom(cargoTrips)
        .addFromInner(flt, "sub", SqlUtils.joinUsing(cargoTrips, "sub", tripId));

    String tmpId = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpId, tripId, cargoId);

    ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields(tmpId, cargoId)
        .addFrom(tmpId);

    ss = new SqlSelect()
        .addFields(services, cargoId)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "Price")),
            "Total")
        .addFrom(services)
        .addFromInner(ss, "sub", SqlUtils.joinUsing(services, "sub", cargoId))
        .setWhere(SqlUtils.isNull(services, "CostPrice"))
        .addGroup(services, cargoId);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, cargoId);

    ss = new SqlSelect()
        .addFields(tmp, cargoId, "Total")
        .addSum(cargoTrips, "TripPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"), 1, 0), "CntEmpty")
        .addFrom(tmp)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp, cargoTrips, cargoId))
        .addGroup(tmp, cargoId, "Total");

    String tmp2 = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp2, cargoId);
    qs.sqlDropTemp(tmp);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(SqlUtils.field(tmp2, "Total"), 100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"),
            SqlUtils.divide(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp2, "TotalPercent"), 0)),
                SqlUtils.field(tmp2, "CntEmpty")),
            SqlUtils.field(cargoTrips, "TripPercent")));

    ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields(tmpId, tripId)
        .addFrom(tmpId);

    ss = new SqlSelect()
        .addFields(cargoTrips, cargoId, tripId)
        .addExpr(xpr, "Income")
        .addFrom(tmp2)
        .addFromInner(cargoTrips, SqlUtils.joinUsing(tmp2, cargoTrips, cargoId))
        .addFromInner(ss, "sub", SqlUtils.joinUsing(cargoTrips, "sub", tripId));

    tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, cargoId, tripId);
    qs.sqlDropTemp(tmp2);
    qs.sqlDropTemp(tmpId);

    return tmp;
  }

  private ResponseObject getTripProfit(long tripId) {
    String crs = getTripCost(new SqlSelect().addConstant(tripId, "Trip"));

    SqlSelect ss = new SqlSelect()
        .addSum(crs, "Cost")
        .addSum(crs, "FuelCost")
        .addFrom(crs);

    Map<String, String> res = qs.getRow(ss);

    qs.sqlDropTemp(crs);

    crs = getTripIncome(new SqlSelect().addConstant(tripId, "Trip"));

    ss = new SqlSelect()
        .addSum(crs, "Income")
        .addFrom(crs);

    res.put("Income", qs.getValue(ss));

    qs.sqlDropTemp(crs);

    return ResponseObject.response(new String[] {BeeUtils.transformMap(res)});
  }

  private ResponseObject updateKilometers(RequestInfo reqInfo) {
    BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter("Rowset"));
    String colName = rs.getColumnId(0);

    Double kmNew = null;
    Double km = BeeUtils.toDouble(rs.getRow(0).getString(0));
    boolean requiresScale = false;

    if (BeeUtils.equals(colName, "Kilometers")) {
      kmNew = km + BeeUtils
          .toDouble(Codec.beeDeserialize(reqInfo.getParameter("SpeedometerFrom")));
      requiresScale = true;

    } else {
      if (BeeUtils.equals(colName, "SpeedometerFrom")) {
        Double kmTo = BeeUtils
            .toDoubleOrNull(Codec.beeDeserialize(reqInfo.getParameter("SpeedometerTo")));

        if (kmTo != null) {
          kmNew = kmTo - km;
        }
      } else {
        kmNew = km - BeeUtils
            .toDouble(Codec.beeDeserialize(reqInfo.getParameter("SpeedometerFrom")));
      }
      requiresScale = (kmNew != null && kmNew < 0);
    }
    if (requiresScale) {
      Integer scale = qs.getInt(new SqlSelect().addFields("Vehicles", "Speedometer")
          .addFrom("TripRoutes").addFromInner("Trips",
              SqlUtils.join("TripRoutes", "Trip", "Trips", sys.getIdName("Trips")))
          .addFromInner("Vehicles",
              SqlUtils.join("Trips", "Vehicle", "Vehicles", sys.getIdName("Vehicles")))
          .setWhere(SqlUtils.equal("TripRoutes", sys.getIdName("TripRoutes"),
              rs.getRow(0).getId())));

      if (BeeUtils.isPositive(scale)) {
        if (kmNew < 0) {
          kmNew += scale;
        } else if (kmNew >= scale) {
          kmNew -= scale;
        }
      } else if (kmNew < 0) {
        kmNew = null;
      }
    }
    rs.getRow(0).preliminaryUpdate(1, BeeUtils.transform(kmNew));
    return deb.commitRow(rs, true);
  }
}
