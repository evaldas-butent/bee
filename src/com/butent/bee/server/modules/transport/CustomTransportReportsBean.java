package com.butent.bee.server.modules.transport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.ALS_LOADING_DATE;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.report.ReportInfo;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class CustomTransportReportsBean {

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  TransportModuleBean trp;

  public void calculateLoadingDates(ReportInfo report, boolean cargoRequired, String tmp) {
    boolean loadingRangeRequired = report.requiresField(ALS_MIN_LOADING_DATE)
        || report.requiresField(ALS_MAX_UNLOADING_DATE);
    boolean loadingRequired = report.requiresField(ALS_LOADING_DATE)
        || report.requiresField(ALS_UNLOADING_DATE);

    if (loadingRequired || loadingRangeRequired) {
      String als = SqlUtils.uniqueName();
      IsCondition handlingClause = SqlUtils.inList(TBL_CARGO_TRIPS, COL_TRIP,
          qs.getNotNullLongSet(tmp, COL_TRIP));

      SqlSelect handlingQuery =  new SqlSelect()
          .addFields(TBL_CARGO_PLACES, COL_DATE)
          .addAllFields(als)
          .addFields(TBL_CARGO_TRIPS, COL_TRIP)
          .addFrom(TBL_CARGO_PLACES)
          .addFromInner(trp.getHandlingQuery(handlingClause, true), als,
              SqlUtils.joinUsing(TBL_CARGO_PLACES, als, sys.getIdName(TBL_CARGO_PLACES)))
          .addFromLeft(TBL_CARGO_TRIPS, sys.joinTables(TBL_CARGO_TRIPS, als, COL_CARGO_TRIP));
      SimpleRowSet handlingRowSet =  qs.getData(handlingQuery);

      if (!DataUtils.isEmpty(handlingRowSet)) {
        Table<Long, Boolean, SortedSet<Long>> dates = HashBasedTable.create();
        String keyColumn = cargoRequired ? COL_CARGO : COL_TRIP;

        for (SimpleRowSet.SimpleRow row : handlingRowSet) {
          SortedSet<Long> rowDates = dates
              .get(row.getLong(keyColumn), row.getBoolean(VAR_UNLOADING));

          if (rowDates == null) {
            rowDates = new TreeSet<>();
          }

          rowDates.add(row.getLong(COL_PLACE_DATE));
          dates.put(row.getLong(keyColumn), row.getBoolean(VAR_UNLOADING), rowDates);
        }

        for (Table.Cell<Long, Boolean, SortedSet<Long>> dateRow : dates.cellSet()) {
          Boolean unloading = dateRow.getColumnKey();
          SqlUpdate updateSql = new SqlUpdate(tmp)
              .setWhere(SqlUtils.equals(tmp, keyColumn, dateRow.getRowKey()));

          if (loadingRangeRequired) {
            if (BeeUtils.isTrue(unloading)) {
              updateSql.addConstant(ALS_MAX_UNLOADING_DATE, dateRow.getValue().last());
            } else {
              updateSql.addConstant(ALS_MIN_LOADING_DATE, dateRow.getValue().first());
            }
          }
          if (loadingRequired) {
            updateSql.addConstant(unloading ? ALS_UNLOADING_DATE : ALS_LOADING_DATE,
                BeeUtils.joinItems(dateRow.getValue().stream().map((dateLong) ->
                    TimeUtils.dateToString(new DateTime(dateLong))).collect(Collectors.toList())));
          }
          qs.updateData(updateSql);
        }
      }
    }
  }
}
