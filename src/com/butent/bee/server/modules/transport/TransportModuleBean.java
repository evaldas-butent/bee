package com.butent.bee.server.modules.transport;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.ViewCallback;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
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
      response = getProfit(reqInfo);

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
  }

  private SimpleRowSet getFuelConsumptions(SqlSelect query) {
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

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addField(routes, routeId, "Id")
        .addSum(xpr, "Consumption")
        .addFrom(routes)
        .addFromInner(query.resetFields().resetOrder().addFields(routes, routeId), "sub",
            SqlUtils.joinUsing(routes, "sub", routeId))
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

    return rs;
  }

  private ResponseObject getProfit(RequestInfo reqInfo) {
    long tripId = BeeUtils.toLong(reqInfo.getParameter(TransportConstants.VAR_TRIP_ID));
    String routes = "TripCosts";
    String fuel = "TripFuelCosts";

    SqlSelect ss = new SqlSelect()
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(routes, "Quantity", "Price")), "suma")
        .addFrom(routes)
        .setWhere(SqlUtils.equal(routes, "Trip", tripId));

    Double isl = qs.getDouble(ss);

    ss = new SqlSelect()
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(fuel, "Quantity", "Price")), "suma")
        .addFrom(fuel)
        .setWhere(SqlUtils.equal(fuel, "Trip", tripId));

    Double kur = qs.getDouble(ss);

    String services = "CargoServices";
    String cargo = "OrderCargo";
    String cargoIdName = sys.getIdName(cargo);

    String cargoTrips = "CargoTrips";

    ss = new SqlSelect()
        .addFields(cargo, cargoIdName)
        .addSum(SqlUtils.multiply((Object[]) SqlUtils.fields(services, "Quantity", "Price")),
            "Total")
        .addFrom(cargoTrips)
        .addFromInner(cargo, SqlUtils.join(cargoTrips, "Cargo", cargo, cargoIdName))
        .addFromInner(services, SqlUtils.join(cargo, cargoIdName, services, "Cargo"))
        .setWhere(SqlUtils.and(SqlUtils.equal(cargoTrips, "Trip", tripId),
            SqlUtils.isNull(services, "CostPrice")))
        .addGroup(cargo, cargoIdName);

    String tmp = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp, cargoIdName);

    ss = new SqlSelect()
        .addFields(tmp, cargoIdName, "Total")
        .addSum(cargoTrips, "TripPercent", "TotalPercent")
        .addSum(SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"), 1, 0), "CntEmpty")
        .addFrom(tmp)
        .addFromInner(cargoTrips, SqlUtils.join(tmp, cargoIdName, cargoTrips, "Cargo"))
        .addGroup(tmp, cargoIdName, "Total");

    String tmp2 = qs.sqlCreateTemp(ss);
    qs.sqlIndex(tmp2, cargoIdName);
    qs.sqlDropTemp(tmp);

    IsExpression xpr = SqlUtils.multiply(
        SqlUtils.divide(SqlUtils.field(tmp2, "Total"), 100),
        SqlUtils.sqlIf(SqlUtils.isNull(cargoTrips, "TripPercent"),
            SqlUtils.divide(
                SqlUtils.minus(100, SqlUtils.nvl(SqlUtils.field(tmp2, "TotalPercent"), 0)),
                SqlUtils.field(tmp2, "CntEmpty")),
            SqlUtils.field(cargoTrips, "TripPercent")));

    ss = new SqlSelect()
        .addSum(xpr, "Income")
        .addFrom(tmp2)
        .addFromInner(cargoTrips, SqlUtils.join(tmp2, cargoIdName, cargoTrips, "Cargo"))
        .setWhere(SqlUtils.equal(cargoTrips, "Trip", tripId));

    Double paj = qs.getDouble(ss);

    qs.sqlDropTemp(tmp2);

    return ResponseObject.response(new String[] {"Išlaidos:", BeeUtils.transform(isl),
        "Kuras:", BeeUtils.transform(kur), "Pajamos", BeeUtils.transform(paj)});
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
