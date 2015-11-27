package com.butent.bee.server.rest;

import com.google.common.collect.Table;

import com.butent.bee.server.modules.payroll.PayrollModuleBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.modules.payroll.WorkScheduleSummary;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("payroll")
public class PayrollWorker {

  private static final String PARAM_COMPANY = "company";
  private static final String PARAM_YEAR = "year";
  private static final String PARAM_MONTH = "month";
  private static final String PARAM_EMPLOYEE = "employee";

  private static final String TAG_EARNINGS = "earnings";
  private static final String TAG_SCHEDULE = "schedule";

  private static final String TAG_ITEM = "item";
  private static final String TAG_TAB_NUMBER = "tab";
  private static final String TAG_DATE = "date";
  private static final String TAG_HOURS = "hours";
  private static final String TAG_CODE = "code";

  @EJB
  PayrollModuleBean pmb;

  @GET
  @Path("earnings")
  @Trusted
  public String getEmployeeEarnings(@QueryParam(PARAM_COMPANY) String company,
      @QueryParam(PARAM_YEAR) String year, @QueryParam(PARAM_MONTH) String month,
      @QueryParam(PARAM_EMPLOYEE) String employee) {

    Document document = XmlUtils.createDocument();
    Element root = document.createElement(TAG_EARNINGS);

    XmlUtils.setNotEmptyAttribute(root, PARAM_COMPANY, company);
    XmlUtils.setNotEmptyAttribute(root, PARAM_YEAR, year);
    XmlUtils.setNotEmptyAttribute(root, PARAM_MONTH, month);
    XmlUtils.setNotEmptyAttribute(root, PARAM_EMPLOYEE, employee);

    document.appendChild(root);

    if (!validateParameters(document, root, company, year, month, employee)) {
      return toString(document);
    }

    return toString(document);
  }

  @GET
  @Path("schedule")
  @Trusted
  public String getWorkSchedule(@QueryParam(PARAM_COMPANY) String company,
      @QueryParam(PARAM_YEAR) String year, @QueryParam(PARAM_MONTH) String month,
      @QueryParam(PARAM_EMPLOYEE) String employee) {

    Document document = XmlUtils.createDocument();
    Element root = document.createElement(TAG_SCHEDULE);

    XmlUtils.setNotEmptyAttribute(root, PARAM_COMPANY, company);
    XmlUtils.setNotEmptyAttribute(root, PARAM_YEAR, year);
    XmlUtils.setNotEmptyAttribute(root, PARAM_MONTH, month);
    XmlUtils.setNotEmptyAttribute(root, PARAM_EMPLOYEE, employee);

    document.appendChild(root);

    if (!validateParameters(document, root, company, year, month, employee)) {
      return toString(document);
    }

    Integer tabNumber = BeeUtils.toIntOrNull(employee);

    YearMonth ym = YearMonth.parse(year, month);
    DateRange range = ym.getRange();

    ResponseObject response = pmb.getWorkSchedule(company, tabNumber, range);
    addMessages(document, root, response);

    Table<Integer, JustDate, WorkScheduleSummary> table = getScheduleTable(response);

    if (table == null || table.isEmpty()) {
      if (!response.hasMessages()) {
        addMessage(document, root, LogLevel.INFO, "work schedule not found");
      }
      return toString(document);
    }

    List<Integer> tabNumbers = new ArrayList<>(table.rowKeySet());
    BeeUtils.sort(tabNumbers);

    for (Integer tn : tabNumbers) {
      List<JustDate> dates = new ArrayList<>(table.row(tn).keySet());
      BeeUtils.sort(dates);

      for (JustDate date : dates) {
        WorkScheduleSummary wss = table.get(tn, date);

        if (wss != null) {
          String duration = wss.getDuration();
          if (!BeeUtils.isEmpty(duration)) {
            addScheduleItem(document, root, tn, date, duration, null);
          }

          if (wss.hasTimeCardCodes()) {
            List<String> codes = new ArrayList<>(wss.getTimeCardCodes());
            BeeUtils.sort(codes);

            for (String code : codes) {
              addScheduleItem(document, root, tn, date, null, code);
            }
          }
        }
      }
    }

    return toString(document);
  }

  private static void addMessage(Document document, Element parent, LogLevel level,
      String message) {

    XmlUtils.appendElementWithText(document, parent, level.name().toLowerCase(), message);
  }

  private static void addMessages(Document document, Element parent, ResponseObject response) {
    if (response.hasMessages()) {
      for (ResponseMessage rm : response.getMessages()) {
        addMessage(document, parent, rm.getLevel(), rm.getMessage());
      }
    }
  }

  private static void addScheduleItem(Document document, Element parent,
      Integer tabNumber, JustDate date, String duration, String tcCode) {

    Element item = document.createElement(TAG_ITEM);
    parent.appendChild(item);

    XmlUtils.appendElementWithText(document, item, TAG_TAB_NUMBER, BeeUtils.toString(tabNumber));
    XmlUtils.appendElementWithText(document, item, TAG_DATE, date.toTimeStamp());

    if (!BeeUtils.isEmpty(duration)) {
      XmlUtils.appendElementWithText(document, item, TAG_HOURS, duration);
    }
    if (!BeeUtils.isEmpty(tcCode)) {
      XmlUtils.appendElementWithText(document, item, TAG_CODE, tcCode);
    }
  }

  @SuppressWarnings("unchecked")
  private static Table<Integer, JustDate, WorkScheduleSummary> getScheduleTable(
      ResponseObject response) {

    if (response.getResponse() instanceof Table) {
      return (Table<Integer, JustDate, WorkScheduleSummary>) response.getResponse();
    } else {
      return null;
    }
  }

  private static void parameterNotFound(Document document, Element parent, String paramName) {
    addMessage(document, parent, LogLevel.ERROR,
        BeeUtils.joinWords(paramName, "parameter not found"));
  }

  private static void parameterNotValid(Document document, Element parent, String name,
      String value) {

    addMessage(document, parent, LogLevel.ERROR,
        BeeUtils.joinWords("parameter", name, "value", value, "is not valid"));
  }

  private static String toString(Document document) {
    return XmlUtils.toString(document, true);
  }

  private static boolean validateParameters(Document document, Element parent,
      String company, String year, String month, String employee) {

    boolean ok = true;

    if (BeeUtils.isEmpty(company)) {
      parameterNotFound(document, parent, PARAM_COMPANY);
      ok = false;
    }

    if (BeeUtils.isEmpty(year)) {
      parameterNotFound(document, parent, PARAM_YEAR);
      ok = false;
    } else {
      Integer y = TimeUtils.parseYear(year);
      if (y == null) {
        parameterNotValid(document, parent, PARAM_YEAR, year);
        ok = false;
      }
    }

    if (BeeUtils.isEmpty(month)) {
      parameterNotFound(document, parent, PARAM_MONTH);
      ok = false;
    } else {
      Integer m = TimeUtils.parseMonth(month);
      if (m == null) {
        parameterNotValid(document, parent, PARAM_MONTH, month);
        ok = false;
      }
    }

    if (!BeeUtils.isEmpty(employee) && !BeeUtils.isPositiveInt(employee)) {
      parameterNotValid(document, parent, PARAM_EMPLOYEE, employee);
      ok = false;
    }

    return ok;
  }
}
