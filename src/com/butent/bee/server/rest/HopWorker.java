package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

import java.util.Objects;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/")
public class HopWorker {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @GET
  @Path("timecards")
  public String getTimeCards(@QueryParam("from") String from, @QueryParam("to") String to) {
    JustDate dateFrom = TimeUtils.parseDate(from);
    JustDate dateTo = TimeUtils.parseDate(to);

    HasConditions clause =
        SqlUtils.and(SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_FROM),
            SqlUtils.or(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_TO),
                SqlUtils.notNull(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE))));

    if (dateFrom != null) {
      clause.add(SqlUtils.moreEqual(TBL_TRIPS, COL_TRIP_DATE_FROM, dateFrom));
    }
    if (dateTo != null) {
      clause.add(SqlUtils.or(SqlUtils.and(SqlUtils.notNull(TBL_TRIPS, COL_TRIP_DATE_TO),
              SqlUtils.less(TBL_TRIPS, COL_TRIP_DATE_TO, dateTo)),
          SqlUtils.less(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE, dateTo)));
    }
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)
        .addFields(TBL_TRIPS, COL_TRIP_DATE_FROM)
        .addExpr(SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_TO),
            SqlUtils.field(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE)), COL_TRIP_DATE_TO)
        .addFrom(PayrollConstants.TBL_EMPLOYEES)
        .addFromInner(TBL_DRIVERS, SqlUtils.joinUsing(PayrollConstants.TBL_EMPLOYEES, TBL_DRIVERS,
            ClassifierConstants.COL_COMPANY_PERSON))
        .addFromInner(TBL_TRIP_DRIVERS, sys.joinTables(TBL_DRIVERS, TBL_TRIP_DRIVERS, COL_DRIVER))
        .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, TBL_TRIP_DRIVERS, COL_TRIP))
        .setWhere(clause));

    StringBuilder xml = new StringBuilder("<data>");

    for (SimpleRowSet.SimpleRow row : rs) {
      for (String col : rs.getColumnNames()) {
        xml.append(XmlUtils.tag(col, Objects.equals(col, PayrollConstants.COL_TAB_NUMBER)
            ? row.getValue(col) : row.getDate(col)));
      }
    }
    xml.append("</data>");

    return xml.toString();
  }
}
