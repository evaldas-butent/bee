package com.butent.bee.server.modules.calendar;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_NOTE;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.report.ReportInfo;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CustomCalendarModuleBean {

  @EJB QueryServiceBean qs;
  @EJB SystemBean sys;

  public ResponseObject getAppointmentReport(RequestInfo reqInfo) {
    ReportInfo report = ReportInfo.restore(reqInfo.getParameter(Service.VAR_DATA));

    HasConditions clause = SqlUtils.and();

    clause.add(report.getCondition(SqlUtils.cast(SqlUtils.field(TBL_APPOINTMENTS,
        sys.getIdName(TBL_APPOINTMENTS)), SqlConstants.SqlDataType.STRING, 20, 0),
        COL_APPOINTMENT));
    clause.add(report.getCondition(SqlUtils.field(TBL_APPOINTMENT_TYPES, COL_APPOINTMENT_TYPE_NAME),
        COL_APPOINTMENT_TYPE));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_SUMMARY));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_DESCRIPTION));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_STATUS));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_APPOINTMENT_LOCATION));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_START_DATE_TIME));
    clause.add(report.getCondition(TBL_APPOINTMENTS, COL_END_DATE_TIME));
    clause.add(report.getCondition(SqlUtils.field(TBL_COMPANIES, COL_COMPANY_NAME), COL_COMPANY));
    clause.add(report.getCondition(SqlUtils.field(TBL_ATTENDEES, COL_ATTENDEE_NAME), COL_ATTENDEE));
    clause.add(report.getCondition(SqlUtils.field(VIEW_ATTENDEE_TYPES, COL_ATTENDEE_TYPE_NAME),
        COL_ATTENDEE_TYPE));
    clause.add(report.getCondition(SqlUtils.field(TBL_SERVICE_OBJECTS, COL_SERVICE_ADDRESS),
        COL_SERVICE_OBJECT));

    String tmp = qs.sqlCreateTemp(new SqlSelect()
        .addField(TBL_APPOINTMENTS, sys.getIdName(TBL_APPOINTMENTS), COL_APPOINTMENT)
        .addField(TBL_APPOINTMENT_TYPES, COL_APPOINTMENT_TYPE_NAME, COL_APPOINTMENT_TYPE)
        .addFields(TBL_APPOINTMENTS, COL_SUMMARY, COL_DESCRIPTION, COL_STATUS,
            COL_APPOINTMENT_LOCATION, COL_START_DATE_TIME, COL_END_DATE_TIME)
        .addExpr(SqlUtils.concat(SqlUtils.field(TBL_PERSONS, COL_FIRST_NAME), "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_PERSONS, COL_LAST_NAME), "''")),
            CalendarConstants.COL_CREATOR)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY)
        .addField(TBL_ATTENDEES, COL_ATTENDEE_NAME, COL_ATTENDEE)
        .addField(VIEW_ATTENDEE_TYPES, COL_ATTENDEE_TYPE_NAME, COL_ATTENDEE_TYPE)
        .addExpr(SqlUtils.concat(SqlUtils.field(TBL_TRADE_SERIES, COL_SERIES_NAME), "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_TRADE_ACTS, COL_TA_NUMBER), "''")), COL_TRADE_ACT)
        .addField(TBL_SERVICE_OBJECTS, COL_SERVICE_ADDRESS, COL_SERVICE_OBJECT)
        .addExpr(SqlUtils.concat(SqlUtils.nvl(SqlUtils.field("SO", COL_SERVICE_MODEL), "''"),
            "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_COMPANY_OBJECTS, COL_COMPANY_OBJECT_NAME), "''"),
            "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_COMPANY_OBJECTS, COL_COMPANY_OBJECT_ADDRESS),
                "''")), COL_SERVICE_MAINTENANCE)
        .addExpr(SqlUtils.concat(SqlUtils.field(TBL_TRADE_ACT_SERVICES, COL_ITEM), "' '",
            SqlUtils.field(TBL_ITEMS, COL_ITEM_NAME)), COL_ITEM)
        .addFields(TBL_TRADE_ACT_SERVICES, COL_DATE_FROM, COL_COST_AMOUNT, COL_NOTE)
        .addField(TBL_COMPANIES + COL_DEFECT_SUPPLIER, COL_COMPANY_NAME, COL_DEFECT_SUPPLIER)
        .addField(TBL_TRADE_ACT_SERVICES + TBL_TRADE_SERIES, COL_SERIES_NAME, COL_SERIES_NAME)
        .addExpr(TradeModuleBean.getWithoutVatExpression(TBL_TRADE_ACT_SERVICES), COL_TRADE_AMOUNT)
        .addFrom(TBL_APPOINTMENTS)
        .addFromInner(TBL_APPOINTMENT_TYPES,
            sys.joinTables(TBL_APPOINTMENT_TYPES, TBL_APPOINTMENTS, COL_APPOINTMENT_TYPE))
        .addFromLeft(TBL_USERS,
            sys.joinTables(TBL_USERS, TBL_APPOINTMENTS, CalendarConstants.COL_CREATOR))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS,
            sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .addFromLeft(TBL_COMPANIES,
            sys.joinTables(TBL_COMPANIES, TBL_APPOINTMENTS, COL_COMPANY))
        .addFromLeft(TBL_APPOINTMENT_ATTENDEES,
            sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT))
        .addFromLeft(TBL_ATTENDEES,
            sys.joinTables(TBL_ATTENDEES, TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE))
        .addFromLeft(VIEW_ATTENDEE_TYPES,
            sys.joinTables(VIEW_ATTENDEE_TYPES, TBL_ATTENDEES, COL_ATTENDEE_TYPE))
        .addFromLeft(TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_APPOINTMENTS, COL_TRADE_ACT))
        .addFromLeft(TBL_TRADE_SERIES,
            sys.joinTables(TBL_TRADE_SERIES, TBL_TRADE_ACTS, COL_TA_SERIES))
        .addFromLeft(TBL_SERVICE_OBJECTS,
            sys.joinTables(TBL_SERVICE_OBJECTS, TBL_APPOINTMENTS, COL_SERVICE_OBJECT))
        .addFromLeft(TBL_SERVICE_MAINTENANCE,
            sys.joinTables(TBL_SERVICE_MAINTENANCE, TBL_APPOINTMENTS, COL_SERVICE_MAINTENANCE))
        .addFromLeft(TBL_SERVICE_OBJECTS, "SO",
            sys.joinTables(TBL_SERVICE_OBJECTS, "SO", TBL_SERVICE_MAINTENANCE, COL_SERVICE_OBJECT))
        .addFromLeft(TBL_COMPANY_OBJECTS, sys.joinTables(TBL_COMPANY_OBJECTS, "SO", COL_OBJECT))
        .addFromLeft(TBL_TRADE_ACT_SERVICES,
            sys.joinTables(TBL_APPOINTMENTS, TBL_TRADE_ACT_SERVICES, COL_APPOINTMENT))
        .addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRADE_ACT_SERVICES, COL_ITEM))
        .addFromLeft(TBL_COMPANIES, TBL_COMPANIES + COL_DEFECT_SUPPLIER,
            sys.joinTables(TBL_COMPANIES, TBL_COMPANIES + COL_DEFECT_SUPPLIER,
                TBL_TRADE_ACT_SERVICES, COL_DEFECT_SUPPLIER))
        .addFromLeft(TBL_TRADE_ACTS, TBL_TRADE_ACT_SERVICES + TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_SERVICES + TBL_TRADE_ACTS,
                TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT))
        .addFromLeft(TBL_TRADE_SERIES, TBL_TRADE_ACT_SERVICES + TBL_TRADE_SERIES,
            sys.joinTables(TBL_TRADE_SERIES, TBL_TRADE_ACT_SERVICES + TBL_TRADE_SERIES,
                TBL_TRADE_ACTS, COL_TA_SERIES))
        .setWhere(clause)
    );

    return report.getResultResponse(qs, tmp,
        Localizations.getDictionary(reqInfo.getParameter(VAR_LOCALE)),
        report.getCondition(tmp, CalendarConstants.COL_CREATOR),
        report.getCondition(tmp, COL_TRADE_ACT),
        report.getCondition(tmp, COL_SERVICE_MAINTENANCE),
        report.getCondition(tmp, COL_ITEM)
    );
  }
}