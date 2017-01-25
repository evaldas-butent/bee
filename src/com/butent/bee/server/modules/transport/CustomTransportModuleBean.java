package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.VAR_PARAMETER_DEFAULT;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class CustomTransportModuleBean {

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  TransportModuleBean trp;

  public void validateHandlingKm(DataEvent.ViewModifyEvent event) {
    if (event.isBefore(TBL_CARGO_LOADING, TBL_CARGO_UNLOADING)) {
      List<BeeColumn> columns = new ArrayList<>();
      BeeRow modifiedRow = null;
      Long cargoId = null;
      SimpleRow handlingRow = null;
      List<String> validatingColumns = Arrays.asList(COL_EMPTY_KILOMETERS,
          COL_LOADED_KILOMETERS, COL_UNPLANNED_MANAGER_KM, COL_UNPLANNED_DRIVER_KM);

      if (event instanceof DataEvent.ViewUpdateEvent) {
        columns = ((DataEvent.ViewUpdateEvent) event).getColumns();
        modifiedRow = ((DataEvent.ViewUpdateEvent) event).getRow();
        String relColumn = BeeUtils.same(event.getTargetName(), TBL_CARGO_LOADING)
            ? COL_LOADING_PLACE : COL_UNLOADING_PLACE;
        SqlSelect handlingQuery = new SqlSelect()
            .addFields(TBL_CARGO_TRIPS, sys.getIdName(TBL_CARGO_TRIPS), COL_CARGO)
            .addFrom(event.getTargetName())
            .addFromLeft(TBL_CARGO_TRIPS,
                sys.joinTables(TBL_CARGO_TRIPS, event.getTargetName(), COL_CARGO_TRIP))
            .addFromLeft(TBL_CARGO_PLACES,
                sys.joinTables(TBL_CARGO_PLACES, event.getTargetName(), relColumn))
            .setWhere(sys.idEquals(event.getTargetName(), modifiedRow.getId()));

        for (String kmColumn : validatingColumns) {
          handlingQuery.addFields(TBL_CARGO_PLACES, kmColumn);
        }

        handlingRow = qs.getRow(handlingQuery);
        cargoId = handlingRow.getLong(COL_CARGO);

      } else if (event instanceof DataEvent.ViewInsertEvent) {
        columns = ((DataEvent.ViewInsertEvent) event).getColumns();
        modifiedRow = ((DataEvent.ViewInsertEvent) event).getRow();
        int cargoTripIdx = DataUtils.getColumnIndex(COL_CARGO_TRIP, columns);

        if (!BeeConst.isUndef(cargoTripIdx)) {
          Long cargoTripId = modifiedRow.getLong(cargoTripIdx);

          if (!DataUtils.isId(cargoTripId)) {
            return;
          }
          cargoId = qs.getLongById(TBL_CARGO_TRIPS, cargoTripId, COL_CARGO);
        }
      }
      if (!DataUtils.isId(cargoId)) {
        return;
      }

      String als = "tmpSubQuery";
      SqlSelect tripQuery = new SqlSelect()
          .addFrom(TBL_CARGO_PLACES)
          .addFromInner(trp.getHandlingQuery(sys.idEquals(TBL_ORDER_CARGO, cargoId), true), als,
              SqlUtils.joinUsing(TBL_CARGO_PLACES, als, sys.getIdName(TBL_CARGO_PLACES)))
          .setWhere(SqlUtils.not(SqlUtils.equals(als, VAR_PARAMETER_DEFAULT, Boolean.TRUE)))
          .setLimit(1);

      SqlSelect cargoQuery = new SqlSelect()
          .addFields(als, COL_CARGO_TRIP)
          .addFrom(TBL_CARGO_PLACES)
          .addFromInner(
              trp.getHandlingQuery(sys.idEquals(TBL_ORDER_CARGO, cargoId), false), als,
              SqlUtils.joinUsing(TBL_CARGO_PLACES, als, sys.getIdName(TBL_CARGO_PLACES)))
          .setWhere(SqlUtils.equals(als, VAR_PARAMETER_DEFAULT, Boolean.TRUE))
          .addGroup(als, COL_CARGO_TRIP)
          .setLimit(1);

      boolean valid = false;

      for (String column : validatingColumns) {
        if (!BeeConst.isUndef(DataUtils.getColumnIndex(column, columns, false))) {
          tripQuery.addSum(TBL_CARGO_PLACES, column);
          cargoQuery.addSum(TBL_CARGO_PLACES, column);
          valid = true;
        }
      }
      if (!valid) {
        return;
      }

      SimpleRow cargoRow = qs.getRow(cargoQuery);

      if (cargoRow != null) {
        SimpleRow tripRow = qs.getRow(tripQuery);

        for (String kmColumn : validatingColumns) {
          int columnIdx = DataUtils.getColumnIndex(kmColumn, columns, false);

          if (!BeeConst.isUndef(columnIdx)) {
            double cargoKmValue = BeeUtils.unbox(cargoRow.getDouble(kmColumn));

            if (BeeUtils.isPositive(cargoKmValue)) {
              double tripKmValue = tripRow != null
                  ? BeeUtils.unbox(tripRow.getDouble(kmColumn)) : 0;
              double editedKmValue = BeeUtils.unbox(modifiedRow.getDouble(columnIdx));
              double oldValue = handlingRow != null
                  ? BeeUtils.unbox(handlingRow.getDouble(kmColumn)) : 0;

              if (cargoKmValue - (tripKmValue - oldValue) - editedKmValue < 0) {
                event.addErrors(
                    ResponseObject.error(usr.getDictionary().trHandlingKilometerError()));
                break;
              }
            }
          }
        }
      }
    }
  }
}