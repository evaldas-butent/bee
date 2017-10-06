package com.butent.bee.server.rest;

import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.transport.TransportModuleBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("transport")
@Produces(RestResponse.JSON_TYPE)
public class CustomTransportWorker {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  TransportModuleBean tmb;

  private static final String SECRET_PHRASE = "B-NOVO KVS Request";

  @GET
  @Path("vehicle/{vehicleNumber}")
  @Trusted(secret = SECRET_PHRASE)
  public RestResponse getDataByVehicle(@PathParam("vehicleNumber") String vehicleNumber) {

    String errorMessage = "Transporto priemonė valst. Nr. " + vehicleNumber
        + " B-NOVO sistemai nežinomas";

    SqlSelect select = getVehicleQuery(COL_IS_TRUCK, vehicleNumber);
    Map<String, String> result = getResult(select);
    result.putAll(getResult(getTripQuery(COL_VEHICLE, COL_IS_TRUCK, vehicleNumber)));

    result.remove(COL_TRIP);

    return result.isEmpty() ? RestResponse.error(errorMessage) : RestResponse.ok(result);
  }

  @GET
  @Path("trailer/{trailerNumber}")
  @Trusted(secret = SECRET_PHRASE)
  public RestResponse getDataByTrailer(@PathParam("trailerNumber") String trailerNumber) {

    String errorMessage = "Transporto priemonė valst. Nr. " + trailerNumber
        + " B-NOVO sistemai nežinoma";

    SqlSelect select = getVehicleQuery(COL_IS_TRAILER, trailerNumber);
    Map<String, String> result = getResult(select);
    Map<String, String> tripData = getResult(getTripQuery(COL_TRAILER, COL_IS_TRAILER,
        trailerNumber));

    if (!BeeUtils.isEmpty(tripData)) {

      Long tripId = BeeUtils.toLong(tripData.get(COL_TRIP));

      boolean hasCargo = getCargoPlaces(tripId);
      tripData.put(COL_CARGO, BeeUtils.toString(hasCargo));

      tripData.remove(COL_TRIP);
    }

    result.putAll(tripData);
    return result.isEmpty() ? RestResponse.error(errorMessage) : RestResponse.ok(result);
  }

  @GET
  @Path("driver/{tabNumber}")
  @Trusted(secret = SECRET_PHRASE)
  public RestResponse getDataByTabNumber(@PathParam("tabNumber") String tabNumber) {

    SqlSelect select = new SqlSelect()
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
        .addFrom(TBL_DRIVERS)
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_DRIVERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(TBL_EMPLOYEES,
            SqlUtils.joinUsing(TBL_DRIVERS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .setWhere(SqlUtils.equals(TBL_EMPLOYEES, COL_TAB_NUMBER, tabNumber));

    String errorMessage = "Vairuotojas tab. Nr. " + tabNumber + " B-NOVO sistemai nežinomas";
    Map<String, String> result = getResult(select);

    return result.isEmpty() ? RestResponse.error(errorMessage) : RestResponse.ok(result);
  }

  private boolean getCargoPlaces(Long tripId) {
    SqlSelect select = new SqlSelect()
        .addField(TBL_CARGO_TRIPS, sys.getIdName(TBL_CARGO_TRIPS), COL_CARGO_TRIP)
        .addFrom(TBL_CARGO_TRIPS)
        .setWhere(SqlUtils.equals(TBL_CARGO_TRIPS, COL_TRIP, tripId));

    List<Long> cargoTrips = qs.getLongList(select);
    boolean hasCargo = false;

    if (cargoTrips.size() > 0) {
      Table<Long, String, String> places = tmb.getExtremes(sys.idInList(TBL_CARGO_TRIPS,
          cargoTrips), COL_CARGO_TRIP);

      long currentTime = System.currentTimeMillis();

      for (Long key : places.rowKeySet()) {
        long loadingDate = BeeUtils.toLong(places.row(key).get(ALS_LOADING_DATE));
        long unloadingDate = BeeUtils.toLong(places.row(key).get(ALS_UNLOADING_DATE));

        if (loadingDate < currentTime && (unloadingDate == 0 || unloadingDate > currentTime)) {
          hasCargo = true;
          break;
        }
      }
    }

    return hasCargo;
  }

  private SqlSelect getTripQuery(String column, String vehicleType, String vehicleNumber) {

    long currentTime = System.currentTimeMillis();

    IsExpression dateFrom = SqlUtils.nvl(
        SqlUtils.field(TBL_TRIPS, COL_DATE_FROM),
        SqlUtils.field(TBL_TRIPS, COL_DATE));

    IsExpression dateTo = SqlUtils.nvl(
        SqlUtils.field(TBL_TRIPS, COL_DATE_TO),
        SqlUtils.field(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE));

    IsCondition filter = SqlUtils.and(
        SqlUtils.notNull(dateFrom), SqlUtils.notNull(dateTo),
        SqlUtils.lessEqual(dateFrom, SqlUtils.constant(currentTime)),
        SqlUtils.moreEqual(dateTo, SqlUtils.constant(currentTime))
    );

    return new SqlSelect()
        .addField(TBL_TRIPS, sys.getIdName(TBL_TRIPS), COL_TRIP)
        .addFields(TBL_TRIPS, COL_TRIP_NO)
        .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
        .addFields(TBL_EMPLOYEES, COL_TAB_NUMBER)
        .addFrom(TBL_TRIPS)
        .addFromLeft(TBL_VEHICLES, sys.joinTables(TBL_VEHICLES, TBL_TRIPS, column))
        .addFromLeft(TBL_VEHICLE_TYPES, sys.joinTables(TBL_VEHICLE_TYPES, TBL_VEHICLES,
            COL_VEHICLE_TYPE))
        .addFromLeft(TBL_TRIP_DRIVERS, sys.joinTables(TBL_TRIP_DRIVERS, TBL_TRIPS, COL_MAIN_DRIVER))
        .addFromLeft(TBL_DRIVERS, sys.joinTables(TBL_DRIVERS, TBL_TRIP_DRIVERS, COL_DRIVER))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_DRIVERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(TBL_EMPLOYEES,
            SqlUtils.joinUsing(TBL_DRIVERS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_VEHICLE_TYPES, vehicleType),
            SqlUtils.same(TBL_VEHICLES, COL_VEHICLE_NUMBER, vehicleNumber), filter))
        .addOrder(TBL_TRIPS, sys.getIdName(TBL_TRIPS));
  }

  private  Map<String, String> getResult(SqlSelect select) {
    Map<String, String> result = new HashMap<>();
    SimpleRowSet rowSet = qs.getData(select);

    if (!rowSet.isEmpty()) {
      SimpleRowSet.SimpleRow row = rowSet.getRow(0);

      for (String column : rowSet.getColumnNames()) {
        result.put(column, row.getValue(column));
      }
    }

    return result;
  }

  private SqlSelect getVehicleQuery(String vehicleType, String vehicleNumber) {

    return new SqlSelect()
        .addField(TBL_VEHICLE_MODELS, COL_VEHICLE_MODEL_NAME, COL_MODEL_NAME)
        .addFields(TBL_VEHICLE_BRANDS, COL_VEHICLE_BRAND_NAME)
        .addFields(TBL_VEHICLES, COL_BODY_NUMBER)
        .addField(TBL_VEHICLE_TYPES, COL_VEHICLE_TYPE_NAME, TransportConstants.COL_TYPE_NAME)
        .addFrom(TBL_VEHICLES)
        .addFromLeft(TBL_VEHICLE_MODELS,
            sys.joinTables(TBL_VEHICLE_MODELS, TBL_VEHICLES, COL_MODEL))
        .addFromLeft(TBL_VEHICLE_BRANDS,
            sys.joinTables(TBL_VEHICLE_BRANDS, TBL_VEHICLE_MODELS, COL_VEHICLE_BRAND))
        .addFromLeft(TBL_VEHICLE_TYPES,
            sys.joinTables(TBL_VEHICLE_TYPES, TBL_VEHICLES, COL_VEHICLE_TYPE))
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_VEHICLE_TYPES, vehicleType),
            SqlUtils.same(TBL_VEHICLES, COL_VEHICLE_NUMBER, vehicleNumber)));
  }
}