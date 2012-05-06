package com.butent.bee.server.modules.transport;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.ViewCallback;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Operator;
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
    sys.registerViewCallback("TripRoutes", new ViewCallback(getName()) {
      @Override
      public void afterViewData(SqlSelect query, BeeRowSet rowset) {
        if (rowset.isEmpty()) {
          return;
        }
        int colIndex = rowset.getColumnIndex("Consumption");
        SimpleRowSet rs = getFuelConsumptions(query);

        for (int i = 0; i < rs.getNumberOfRows(); i++) {
          BeeRow row = rowset.getRowById(rs.getLong(i, 0));

          if (row != null) {
            row.setValue(colIndex, rs.getValue(i, 1));
          }
        }
      }
    });
    sys.registerViewCallback("TripCargo", new ViewCallback(getName()) {
      @Override
      public void afterViewData(SqlSelect query, BeeRowSet rowset) {
        if (rowset.isEmpty()) {
          return;
        }
        int colIndex = rowset.getColumnIndex("XXX");
        int cargoIndex = rowset.getColumnIndex("Cargo");
        String crs = getTripIncome(query.resetFields().resetOrder()
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
    });
  }

  private ResponseObject getCargoProfit(SqlSelect flt) {
    String services = "CargoServices";
    String cargoTrips = "CargoTrips";
    String cargoId = "Cargo";
    String tripId = "Trip";

    SqlSelect ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields("sub", cargoId)
        .addFrom(flt, "sub");

    String tmpId = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpId, cargoId);

    ss = new SqlSelect()
        .addFields(services, cargoId)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "Price")),
            "Income")
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "CostPrice")),
            "Cost")
        .addEmptyDouble("TripCost")
        .addFrom(services)
        .addFromInner(tmpId, SqlUtils.joinUsing(services, tmpId, cargoId))
        .addGroup(services, cargoId);

    String crsTotals = qs.sqlCreateTemp(ss);
    qs.sqlIndex(crsTotals, cargoId);

    ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields(cargoTrips, tripId)
        .addFrom(cargoTrips)
        .addFromInner(tmpId, SqlUtils.joinUsing(cargoTrips, tmpId, cargoId));

    String crsIncomes = getTripIncome(ss);
    String crsCosts = getTripCost(ss);

    qs.sqlDropTemp(tmpId);

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
        .addFrom(crsTripCosts)
        .addExpression("TripCost", SqlUtils.field(crsTripCosts, "Cost"))
        .setWhere(SqlUtils.joinUsing(crsTotals, crsTripCosts, cargoId));

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

  private SimpleRowSet getFuelConsumptions(SqlSelect flt) {
    String trips = "Trips";
    String routes = "TripRoutes";
    String fuel = "FuelConsumptions";
    String temps = "FuelTemperatures";
    String routeId = sys.getIdName(routes);

    SqlSelect ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields("sub", routeId)
        .addFrom(flt, "sub");

    String tmpId = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpId, routeId);

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

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addField(routes, routeId, "Id")
        .addSum(xpr, "Consumption")
        .addFrom(routes)
        .addFromInner(tmpId, SqlUtils.joinUsing(routes, tmpId, routeId))
        .addFromInner(trips, SqlUtils.join(routes, "Trip", trips, sys.getIdName(trips)))
        .addFromInner(fuel, SqlUtils.and(SqlUtils.joinUsing(trips, fuel, "Vehicle"),
            SqlUtils.joinUsing(fuel, routes, "Fuel")))
        .addFromLeft(temps,
            SqlUtils.and(SqlUtils.join(fuel, sys.getIdName(fuel), temps, "Consumption"),
                SqlUtils.joinUsing(temps, routes, "Season"),
                SqlUtils.or(SqlUtils.isNull(temps, "TempFrom"),
                    SqlUtils.compare(SqlUtils.field(temps, "TempFrom"), Operator.LE,
                        SqlUtils.nvl(SqlUtils.field(routes, "Temperature"), 0))),
                SqlUtils.or(SqlUtils.isNull(temps, "TempTo"),
                    SqlUtils.compare(SqlUtils.field(temps, "TempTo"), Operator.GT,
                        SqlUtils.nvl(SqlUtils.field(routes, "Temperature"), 0)))))
        .setWhere(SqlUtils.and(
            SqlUtils.or(SqlUtils.isNull(fuel, "DateFrom"),
                SqlUtils.joinLessEqual(fuel, "DateFrom", routes, "Date")),
            SqlUtils.or(SqlUtils.isNull(fuel, "DateTo"),
                SqlUtils.joinMore(fuel, "DateTo", routes, "Date"))))
        .addGroup(routes, routeId));

    qs.sqlDropTemp(tmpId);

    return rs;
  }

  private String getTripCost(SqlSelect flt) {
    String routes = "TripCosts";
    String fuel = "TripFuelCosts";
    String tripId = "Trip";

    SqlSelect ss = new SqlSelect()
        .setDistinctMode(true)
        .addFields("sub", tripId)
        .addFrom(flt, "sub");

    String tmpId = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmpId, tripId);

    ss = new SqlSelect()
        .addFields(tmpId, tripId)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(routes, "Quantity", "Price")), "Cost")
        .addEmptyDouble("FuelCost")
        .addFrom(tmpId)
        .addFromLeft(routes, SqlUtils.joinUsing(tmpId, routes, tripId))
        .addGroup(tmpId, tripId);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, tripId);

    ss = new SqlSelect()
        .addFields(fuel, tripId)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(fuel, "Quantity", "Price")), "Cost")
        .addFrom(fuel)
        .addFromInner(tmpId, SqlUtils.joinUsing(fuel, tmpId, tripId))
        .addGroup(fuel, tripId);

    SqlUpdate su = new SqlUpdate(tmp)
        .addFrom(ss, "sub")
        .addExpression("FuelCost", SqlUtils.field("sub", "Cost"))
        .setWhere(SqlUtils.joinUsing(tmp, "sub", tripId));

    qs.updateData(su);
    qs.sqlDropTemp(tmpId);

    return tmp;
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
        .addAllFields(crs)
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
