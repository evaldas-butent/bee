package com.butent.bee.server.modules.payroll;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.AdministrationModuleBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.RangeMap;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.SqlConstants.SqlFunction;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.payroll.Earning;
import com.butent.bee.shared.modules.payroll.Earnings;
import com.butent.bee.shared.modules.payroll.PayrollConstants.WorkScheduleKind;
import com.butent.bee.shared.modules.payroll.PayrollUtils;
import com.butent.bee.shared.modules.payroll.WorkScheduleSummary;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.TimeRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class PayrollModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(PayrollModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  AdministrationModuleBean adm;
  @EJB
  ParamHolderBean prm;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();

    if (BeeUtils.isPositiveInt(query)) {
      result.addAll(qs.getSearchResults(VIEW_EMPLOYEES,
          Filter.equals(COL_TAB_NUMBER, BeeUtils.toInt(query))));

    } else {
      result.addAll(qs.getSearchResults(VIEW_EMPLOYEES,
          Filter.anyContains(Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME,
              ALS_DEPARTMENT_NAME, ALS_POSITION_NAME), query)));

      result.addAll(qs.getSearchResults(VIEW_LOCATIONS,
          Filter.anyContains(Sets.newHashSet(COL_LOCATION_NAME, ALS_COMPANY_NAME,
              ALS_LOCATION_MANAGER_FIRST_NAME, ALS_LOCATION_MANAGER_LAST_NAME,
              COL_ADDRESS), query)));
    }

    return result;
  }

  @Override
  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);
    switch (svc) {
      case SVC_GET_SCHEDULE_OVERLAP:
        response = getScheduleOverlap(reqInfo);
        break;

      case SVC_GET_SCHEDULED_MONTHS:
        response = getScheduledMonths(reqInfo);
        break;

      case SVC_INIT_EARNINGS:
        response = initializeEarnings(reqInfo);
        break;

      case SVC_GET_EARNINGS:
        response = getEarnings(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  public ResponseObject getEmployeeEarnings(String companyCode, Integer tabNumber,
      Integer year, Integer month) {

    if (BeeUtils.isEmpty(companyCode)) {
      return ResponseObject.error("company code not specified");
    }
    if (!TimeUtils.isYear(year)) {
      return ResponseObject.error("year not specified");
    }
    if (!TimeUtils.isMonth(month)) {
      return ResponseObject.error("month not specified");
    }

    ResponseObject ecr = getEmployeeCondition(companyCode, tabNumber);
    if (ecr.hasErrors()) {
      return ecr;
    }

    if (!(ecr.getResponse() instanceof IsCondition)) {
      return ResponseObject.error("cannot filter employees", companyCode, tabNumber);
    }

    IsCondition employeeCondition = (IsCondition) ecr.getResponse();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_EMPLOYEES, COL_TAB_NUMBER)
        .addFields(TBL_LOCATIONS, COL_LOCATION_NAME)
        .addFields(TBL_EMPLOYEE_EARNINGS, COL_EARNINGS_APPROVED_AMOUNT)
        .addFrom(TBL_EMPLOYEE_EARNINGS)
        .addFromLeft(TBL_EMPLOYEES,
            sys.joinTables(TBL_EMPLOYEES, TBL_EMPLOYEE_EARNINGS, COL_EMPLOYEE))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .addFromLeft(TBL_LOCATIONS,
            sys.joinTables(TBL_LOCATIONS, TBL_EMPLOYEE_EARNINGS, COL_PAYROLL_OBJECT))
        .setWhere(
            SqlUtils.and(employeeCondition,
                SqlUtils.equals(TBL_EMPLOYEE_EARNINGS, COL_EARNINGS_YEAR, year),
                SqlUtils.equals(TBL_EMPLOYEE_EARNINGS, COL_EARNINGS_MONTH, month),
                SqlUtils.positive(TBL_EMPLOYEE_EARNINGS, COL_EARNINGS_APPROVED_AMOUNT)));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.info("employee earnings not found", companyCode, tabNumber,
          year, month);
    }

    Table<Integer, String, Double> table = HashBasedTable.create();

    for (SimpleRow row : data) {
      Integer tnr = row.getInt(COL_TAB_NUMBER);
      String obj = row.getValue(COL_LOCATION_NAME);
      Double amount = row.getDouble(COL_EARNINGS_APPROVED_AMOUNT);

      if (BeeUtils.isPositive(tnr) && !BeeUtils.isEmpty(obj) && BeeUtils.isPositive(amount)) {
        if (table.contains(tnr, obj)) {
          amount += table.get(tnr, obj);
        }

        table.put(tnr, obj, amount);
      }
    }

    return ResponseObject.response(table);
  }

  @Override
  public Module getModule() {
    return Module.PAYROLL;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  public ResponseObject getWorkSchedule(String companyCode, Integer tabNumber, DateRange range,
      WorkScheduleKind kind) {

    if (BeeUtils.isEmpty(companyCode)) {
      return ResponseObject.error("company code not specified");
    }
    if (range == null) {
      return ResponseObject.error("date range not specified");
    }
    if (kind == null) {
      return ResponseObject.error("schedule kind not specified");
    }

    ResponseObject ecr = getEmployeeCondition(companyCode, tabNumber);
    if (ecr.hasErrors()) {
      return ecr;
    }

    if (!(ecr.getResponse() instanceof IsCondition)) {
      return ResponseObject.error("cannot filter employees", companyCode, tabNumber);
    }

    IsCondition employeeCondition = (IsCondition) ecr.getResponse();

    JustDate from = range.getMinDate();
    JustDate until = range.getMaxDate();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_EMPLOYEES, COL_TAB_NUMBER)
        .addFields(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE,
            COL_TIME_RANGE_CODE, COL_TIME_CARD_CODE,
            COL_WORK_SCHEDULE_FROM, COL_WORK_SCHEDULE_UNTIL, COL_WORK_SCHEDULE_DURATION)
        .addField(TBL_TIME_RANGES, COL_TR_FROM, ALS_TR_FROM)
        .addField(TBL_TIME_RANGES, COL_TR_UNTIL, ALS_TR_UNTIL)
        .addField(TBL_TIME_RANGES, COL_TR_DURATION, ALS_TR_DURATION)
        .addFields(TBL_TIME_CARD_CODES, COL_TC_CODE)
        .addFrom(TBL_WORK_SCHEDULE)
        .addFromLeft(TBL_TIME_RANGES,
            sys.joinTables(TBL_TIME_RANGES, TBL_WORK_SCHEDULE, COL_TIME_RANGE_CODE))
        .addFromLeft(TBL_TIME_CARD_CODES,
            sys.joinTables(TBL_TIME_CARD_CODES, TBL_WORK_SCHEDULE, COL_TIME_CARD_CODE))
        .addFromLeft(TBL_EMPLOYEES,
            sys.joinTables(TBL_EMPLOYEES, TBL_WORK_SCHEDULE, COL_EMPLOYEE))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .setWhere(
            SqlUtils.and(employeeCondition,
                SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, kind),
                SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, from),
                SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, until)));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.info("work schedule not found", companyCode, tabNumber, range);
    }

    Table<Integer, JustDate, WorkScheduleSummary> table = HashBasedTable.create();

    long duration;
    String tcCode;
    WorkScheduleSummary wss;

    for (SimpleRow row : data) {
      if (DataUtils.isId(row.getLong(COL_TIME_CARD_CODE))) {
        duration = BeeConst.LONG_UNDEF;
        tcCode = row.getValue(COL_TC_CODE);

      } else if (DataUtils.isId(row.getLong(COL_TIME_RANGE_CODE))) {
        duration = PayrollUtils.getMillis(row.getValue(ALS_TR_FROM), row.getValue(ALS_TR_UNTIL),
            row.getValue(ALS_TR_DURATION));
        tcCode = null;

      } else {
        duration = PayrollUtils.getMillis(row.getValue(COL_WORK_SCHEDULE_FROM),
            row.getValue(COL_WORK_SCHEDULE_UNTIL), row.getValue(COL_WORK_SCHEDULE_DURATION));
        tcCode = null;
      }

      if (duration > 0 || !BeeUtils.isEmpty(tcCode)) {
        Integer tnr = row.getInt(COL_TAB_NUMBER);
        JustDate date = row.getDate(COL_WORK_SCHEDULE_DATE);

        if (BeeUtils.isPositive(tnr) && date != null) {
          if (table.contains(tnr, date)) {
            wss = table.get(tnr, date);
          } else {
            wss = new WorkScheduleSummary();
            table.put(tnr, date, wss);
          }

          if (duration > 0) {
            wss.addMillis(duration);
          } else {
            wss.addTimeCardCode(tcCode);
          }
        }
      }
    }

    return ResponseObject.response(table);
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      @AllowConcurrentEvents
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isAfter(VIEW_OBJECT_EARNINGS, VIEW_EMPLOYEE_EARNINGS) && event.hasData()
            && event.getRowset().containsColumns(COL_PAYROLL_OBJECT, COL_EARNINGS_YEAR,
                COL_EARNINGS_MONTH)) {

          Long currency = prm.getRelation(PRM_CURRENCY);

          int objectIndex = event.getRowset().getColumnIndex(COL_PAYROLL_OBJECT);
          int yearIndex = event.getRowset().getColumnIndex(COL_EARNINGS_YEAR);
          int monthIndex = event.getRowset().getColumnIndex(COL_EARNINGS_MONTH);

          switch (event.getTargetName()) {
            case VIEW_EMPLOYEE_EARNINGS:
              if (event.getRowset().containsColumn(COL_EMPLOYEE)) {
                int employeeIndex = event.getRowset().getColumnIndex(COL_EMPLOYEE);

                for (BeeRow row : event.getRowset()) {
                  Long employee = row.getLong(employeeIndex);
                  Long object = row.getLong(objectIndex);

                  Integer year = row.getInteger(yearIndex);
                  Integer month = row.getInteger(monthIndex);

                  if (DataUtils.isId(employee) && DataUtils.isId(object)
                      && TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {

                    Earning earning = getEarning(employee, object, year, month, currency);
                    if (earning != null) {
                      earning.appplyTo(row);
                    }
                  }
                }
              }
              break;

            case VIEW_OBJECT_EARNINGS:
              for (BeeRow row : event.getRowset()) {
                Long object = row.getLong(objectIndex);

                Integer year = row.getInteger(yearIndex);
                Integer month = row.getInteger(monthIndex);

                if (DataUtils.isId(object) && TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {
                  Earning earning = getObjectEarning(object, year, month, currency);
                  if (earning != null) {
                    earning.appplyTo(row);
                  }

                  Double salaryFund = getSalaryFund(object, year, month, currency);
                  if (BeeUtils.isPositive(salaryFund)) {
                    row.setProperty(PRP_SALARY_FUND, BeeUtils.round(salaryFund, 2));
                  }
                }
              }
              break;
          }
        }
      }
    });
  }

  private Earning getEarning(long employee, long object, int year, int month, Long currency) {
    JustDate from = new JustDate(year, month, 1);
    JustDate until = TimeUtils.endOfMonth(from);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, COL_TIME_RANGE_CODE,
            COL_WORK_SCHEDULE_FROM, COL_WORK_SCHEDULE_UNTIL, COL_WORK_SCHEDULE_DURATION)
        .addField(TBL_TIME_RANGES, COL_TR_FROM, ALS_TR_FROM)
        .addField(TBL_TIME_RANGES, COL_TR_UNTIL, ALS_TR_UNTIL)
        .addField(TBL_TIME_RANGES, COL_TR_DURATION, ALS_TR_DURATION)
        .addFrom(TBL_WORK_SCHEDULE)
        .addFromLeft(TBL_TIME_RANGES,
            sys.joinTables(TBL_TIME_RANGES, TBL_WORK_SCHEDULE, COL_TIME_RANGE_CODE))
        .setWhere(
            SqlUtils.and(
                SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, WorkScheduleKind.ACTUAL,
                    COL_EMPLOYEE, employee, COL_PAYROLL_OBJECT, object),
                SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, from),
                SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, until)));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return null;
    }

    RangeMap<JustDate, Double> wages = getWages(employee, object, from, until, currency);

    Set<Integer> days = new HashSet<>();
    long millis = 0;
    double amount = BeeConst.DOUBLE_ZERO;

    long duration;

    for (SimpleRow row : data) {
      if (DataUtils.isId(row.getLong(COL_TIME_RANGE_CODE))) {
        duration = PayrollUtils.getMillis(row.getValue(ALS_TR_FROM), row.getValue(ALS_TR_UNTIL),
            row.getValue(ALS_TR_DURATION));
      } else {
        duration = PayrollUtils.getMillis(row.getValue(COL_WORK_SCHEDULE_FROM),
            row.getValue(COL_WORK_SCHEDULE_UNTIL), row.getValue(COL_WORK_SCHEDULE_DURATION));
      }

      if (duration > 0) {
        millis += duration;

        JustDate date = row.getDate(COL_WORK_SCHEDULE_DATE);
        if (date != null) {
          days.add(date.getDays());

          Double wage = wages.get(date);
          if (BeeUtils.isPositive(wage)) {
            amount += wage * duration / TimeUtils.MILLIS_PER_HOUR;
          }
        }
      }
    }

    if (millis > 0) {
      return new Earning(days, millis, amount);
    } else {
      return null;
    }
  }

  private ResponseObject getEarnings(RequestInfo reqInfo) {
    Integer year = reqInfo.getParameterInt(Service.VAR_YEAR);
    if (!TimeUtils.isYear(year)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), Service.VAR_YEAR);
    }

    Integer month = reqInfo.getParameterInt(Service.VAR_MONTH);
    if (!TimeUtils.isMonth(month)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), Service.VAR_MONTH);
    }

    YearMonth ym = new YearMonth(year, month);
    JustDate from = ym.getDate();
    JustDate until = ym.getLast();

    Long employeeId = reqInfo.getParameterLong(COL_EMPLOYEE);
    Long objectId = reqInfo.getParameterLong(COL_PAYROLL_OBJECT);

    IsCondition wsCondition = getScheduleEarningsCondition(from, until, employeeId, null, objectId);

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, COL_EMPLOYEE, COL_SUBSTITUTE_FOR, COL_PAYROLL_OBJECT)
        .addFrom(TBL_WORK_SCHEDULE)
        .setWhere(wsCondition);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();
    }

    Long currency = prm.getRelation(PRM_CURRENCY);
    Set<JustDate> holidays = getHolidays(from, until);

    List<Earnings> result = new ArrayList<>();

    for (SimpleRow row : data) {
      Long empl = row.getLong(COL_EMPLOYEE);
      Long subst = row.getLong(COL_SUBSTITUTE_FOR);
      Long obj = row.getLong(COL_PAYROLL_OBJECT);

      Map<DateRange, Pair<Double, Double>> fundsAndWages = getFundsAndWages(from, until,
          empl, subst, obj, currency);

      fundsAndWages.forEach((range, pair) -> {
        Earnings earnings = new Earnings(empl, subst, obj);

        earnings.setDateFrom(range.getMinDate());
        earnings.setDateUntil(range.getMaxDate());

        if (pair != null) {
          earnings.setSalaryFund(pair.getA());
          earnings.setHourlyWage(pair.getB());
        }

        setScheduledTime(earnings, holidays);

        result.add(earnings);
      });
    }

    if (result.isEmpty()) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.responseWithSize(result);
    }
  }

  private ResponseObject getEmployeeCondition(String companyCode, Integer tabNumber) {
    SqlSelect companyQuery = new SqlSelect()
        .addFields(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES))
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.equals(TBL_COMPANIES, COL_COMPANY_CODE, companyCode));

    List<Long> companies = qs.getLongList(companyQuery);

    if (BeeUtils.isEmpty(companies)) {
      return ResponseObject.error("Company code", companyCode, "not found");

    } else if (companies.size() > 1) {
      return ResponseObject.error("Company code", companyCode, "found", companies.size(),
          "companies", companies);

    } else {
      Long company = companies.get(0);

      IsCondition companyWhere = SqlUtils.equals(TBL_COMPANY_PERSONS, COL_COMPANY, company);

      if (BeeUtils.isPositive(tabNumber)) {
        return ResponseObject.response(SqlUtils.and(companyWhere,
            SqlUtils.equals(TBL_EMPLOYEES, COL_TAB_NUMBER, tabNumber)));

      } else {
        return ResponseObject.response(companyWhere);
      }
    }
  }

  @SuppressWarnings({"static-method", "unused"})
  private Map<DateRange, Pair<Double, Double>> getFundsAndWages(JustDate from, JustDate until,
      Long employeeId, Long substituteFor, Long objectId, Long currency) {

    Map<DateRange, Pair<Double, Double>> fundsAndWages = new HashMap<>();

    return fundsAndWages;
  }

  private Set<JustDate> getHolidays(JustDate from, JustDate until) {
    Set<JustDate> holidays = new HashSet<>();

    Long country = prm.getRelation(PRM_COUNTRY);

    if (DataUtils.isId(country)) {
      HasConditions where = SqlUtils.and();
      where.add(SqlUtils.equals(TBL_HOLIDAYS, COL_HOLY_COUNTRY, country));

      if (from != null) {
        where.add(SqlUtils.moreEqual(TBL_HOLIDAYS, COL_HOLY_DAY, from.getDays()));
      }
      if (until != null) {
        where.add(SqlUtils.lessEqual(TBL_HOLIDAYS, COL_HOLY_DAY, until.getDays()));
      }

      SqlSelect holidayQuery = new SqlSelect()
          .addFields(TBL_HOLIDAYS, COL_HOLY_DAY)
          .addFrom(TBL_HOLIDAYS)
          .setWhere(where);

      Integer[] days = qs.getIntColumn(holidayQuery);
      if (days != null) {
        for (Integer day : days) {
          if (BeeUtils.isPositive(day)) {
            holidays.add(new JustDate(day));
          }
        }
      }
    }

    return holidays;
  }

  private Earning getObjectEarning(long object, int year, int month, Long currency) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_EMPLOYEE_EARNINGS, COL_EMPLOYEE,
            COL_EARNINGS_BONUS_PERCENT, COL_EARNINGS_BONUS_1, COL_EARNINGS_BONUS_2)
        .addFrom(TBL_EMPLOYEE_EARNINGS)
        .setWhere(SqlUtils.equals(TBL_EMPLOYEE_EARNINGS, COL_PAYROLL_OBJECT, object,
            COL_EARNINGS_YEAR, year, COL_EARNINGS_MONTH, month));

    SimpleRowSet employees = qs.getData(query);
    if (DataUtils.isEmpty(employees)) {
      return null;
    }

    Set<Integer> days = new HashSet<>();
    long millis = 0;
    double amount = BeeConst.DOUBLE_ZERO;

    double emplAmount;

    for (SimpleRow row : employees) {
      Long employee = row.getLong(COL_EMPLOYEE);

      if (DataUtils.isId(employee)) {
        Earning earning = getEarning(employee, object, year, month, currency);

        if (earning == null) {
          emplAmount = BeeConst.DOUBLE_ZERO;

        } else {
          days.addAll(earning.getDays());
          millis += earning.getMillis();

          emplAmount = earning.getAmount();
        }

        amount += PayrollUtils.calculateEarnings(emplAmount,
            row.getDouble(COL_EARNINGS_BONUS_PERCENT),
            row.getDouble(COL_EARNINGS_BONUS_1), row.getDouble(COL_EARNINGS_BONUS_2));
      }
    }

    if (BeeUtils.nonZero(amount)) {
      return new Earning(days, millis, amount);
    } else {
      return null;
    }
  }

  private Double getSalaryFund(long object, int year, int month, Long currency) {
    RangeMap<YearMonth, Double> ranges = RangeMap.create();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_OBJECT_SALARY_FUND, COL_OSF_YEAR_FROM, COL_OSF_MONTH_FROM,
            COL_OSF_YEAR_UNTIL, COL_OSF_MONTH_UNTIL, COL_OSF_AMOUNT, COL_CURRENCY)
        .addFrom(TBL_OBJECT_SALARY_FUND)
        .setWhere(
            SqlUtils.and(
                SqlUtils.equals(TBL_OBJECT_SALARY_FUND, COL_PAYROLL_OBJECT, object),
                SqlUtils.positive(TBL_OBJECT_SALARY_FUND, COL_OSF_AMOUNT)));

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        Double v = adm.maybeExchange(row.getLong(COL_CURRENCY), currency,
            row.getDouble(COL_OSF_AMOUNT), null);

        if (BeeUtils.isPositive(v)) {
          MonthRange range = MonthRange.closed(
              row.getInt(COL_OSF_YEAR_FROM), row.getInt(COL_OSF_MONTH_FROM),
              row.getInt(COL_OSF_YEAR_UNTIL), row.getInt(COL_OSF_MONTH_UNTIL));

          if (range != null) {
            ranges.put(range, v);
          }
        }
      }
    }

    return ranges.get(new YearMonth(year, month));
  }

  private static IsCondition getScheduleEarningsCondition(JustDate from, JustDate until,
      Long employeeId, Long substituteFor, Long objectId) {

    HasConditions conditions = SqlUtils.and();

    conditions.add(SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, from));
    conditions.add(SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, until));

    if (DataUtils.isId(employeeId)) {
      IsCondition emplCondition = SqlUtils.equals(TBL_WORK_SCHEDULE, COL_EMPLOYEE, employeeId);

      if (DataUtils.isId(substituteFor) && !Objects.equals(employeeId, substituteFor)) {
        IsCondition substCondition = SqlUtils.equals(TBL_WORK_SCHEDULE, COL_SUBSTITUTE_FOR,
            substituteFor);

        if (WorkScheduleKind.PLANNED.isSubstitutionEnabled()) {
          conditions.add(emplCondition);
          conditions.add(substCondition);

        } else {
          conditions.add(
              SqlUtils.or(
                  SqlUtils.and(getScheduleKindCondition(WorkScheduleKind.PLANNED),
                      SqlUtils.equals(TBL_WORK_SCHEDULE, COL_EMPLOYEE, substituteFor)),
                  SqlUtils.and(getScheduleKindCondition(WorkScheduleKind.ACTUAL),
                      emplCondition, substCondition)));
        }

      } else {
        conditions.add(emplCondition);
      }
    }

    if (DataUtils.isId(objectId)) {
      conditions.add(SqlUtils.equals(TBL_WORK_SCHEDULE, COL_PAYROLL_OBJECT, objectId));
    }

    conditions.add(SqlUtils.or(
        SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_TIME_RANGE_CODE),
        SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_FROM, COL_WORK_SCHEDULE_UNTIL),
        SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DURATION)));

    return conditions;
  }

  private static IsCondition getScheduleKindCondition(WorkScheduleKind kind) {
    return SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, kind);
  }

  private ResponseObject getScheduledMonths(RequestInfo reqInfo) {
    Long manager = reqInfo.getParameterLong(COL_LOCATION_MANAGER);

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, COL_PAYROLL_OBJECT)
        .addFrom(TBL_WORK_SCHEDULE);

    HasConditions where = SqlUtils.and();
    where.add(SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, WorkScheduleKind.ACTUAL));

    if (DataUtils.isId(manager)) {
      query.addFromInner(TBL_LOCATIONS,
          sys.joinTables(TBL_LOCATIONS, TBL_WORK_SCHEDULE, COL_PAYROLL_OBJECT));

      where.add(SqlUtils.equals(TBL_LOCATIONS, COL_LOCATION_MANAGER, manager));
    }

    query.setWhere(where);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();
    }

    HashMultimap<YearMonth, Long> map = HashMultimap.create();

    for (SimpleRow row : data) {
      JustDate date = row.getDate(COL_WORK_SCHEDULE_DATE);
      Long objId = row.getLong(COL_PAYROLL_OBJECT);

      if (date != null && DataUtils.isId(objId)) {
        map.put(new YearMonth(date), objId);
      }
    }

    if (map.isEmpty()) {
      return ResponseObject.emptyResponse();
    }

    List<YearMonth> months = new ArrayList<>(map.keySet());
    if (months.size() > 1) {
      Collections.sort(months);
    }

    StringBuilder sb = new StringBuilder();

    for (YearMonth ym : months) {
      if (sb.length() > 0) {
        sb.append(BeeConst.CHAR_COMMA);
      }
      sb.append(ym.serialize()).append(BeeConst.CHAR_EQ).append(map.get(ym).size());
    }

    return ResponseObject.response(sb.toString());
  }

  private ResponseObject getScheduleOverlap(RequestInfo reqInfo) {
    String relationColumn = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(relationColumn)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), Service.VAR_COLUMN);
    }

    Long relId = reqInfo.getParameterLong(Service.VAR_VALUE);
    if (!DataUtils.isId(relId)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), Service.VAR_VALUE);
    }

    WorkScheduleKind kind = EnumUtils.getEnumByIndex(WorkScheduleKind.class,
        reqInfo.getParameter(COL_WORK_SCHEDULE_KIND));
    if (kind == null) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_WORK_SCHEDULE_KIND);
    }

    String partitionColumn;
    switch (relationColumn) {
      case COL_PAYROLL_OBJECT:
        partitionColumn = COL_EMPLOYEE;
        break;

      case COL_EMPLOYEE:
        partitionColumn = COL_PAYROLL_OBJECT;
        break;

      default:
        return ResponseObject.error(reqInfo.getSubService(), "unrecognized relation column",
            relationColumn);
    }

    Integer from = reqInfo.getParameterInt(Service.VAR_FROM);
    JustDate dateFrom = (from == null) ? null : new JustDate(from);

    Integer to = reqInfo.getParameterInt(Service.VAR_TO);
    JustDate dateUntil = (to == null) ? null : new JustDate(to);

    HasConditions wsWhere = SqlUtils.and();
    wsWhere.add(SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, kind));

    if (dateFrom != null) {
      wsWhere.add(SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, dateFrom));
    }
    if (dateUntil != null) {
      wsWhere.add(SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, dateUntil));
    }

    IsCondition relWhere = SqlUtils.equals(TBL_WORK_SCHEDULE, relationColumn, relId);

    SqlSelect subQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, partitionColumn, COL_WORK_SCHEDULE_DATE)
        .addFrom(TBL_WORK_SCHEDULE);

    String subAlias = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, partitionColumn, COL_WORK_SCHEDULE_DATE)
        .addFrom(TBL_WORK_SCHEDULE);

    switch (relationColumn) {
      case COL_PAYROLL_OBJECT:
        subQuery.setWhere(SqlUtils.and(SqlUtils.notEqual(TBL_WORK_SCHEDULE, relationColumn, relId),
            wsWhere));

        query.addFromInner(subQuery, subAlias, SqlUtils.joinUsing(TBL_WORK_SCHEDULE, subAlias,
            partitionColumn, COL_WORK_SCHEDULE_DATE));
        query.setWhere(SqlUtils.and(relWhere, wsWhere));
        break;

      case COL_EMPLOYEE:
        subQuery.setWhere(SqlUtils.and(relWhere, wsWhere));

        SqlSelect datesQuery = new SqlSelect()
            .addFields(subAlias, COL_WORK_SCHEDULE_DATE)
            .addFrom(subQuery, subAlias)
            .addGroup(subAlias, COL_WORK_SCHEDULE_DATE)
            .setHaving(SqlUtils.more(SqlUtils.aggregate(SqlFunction.COUNT, null), 1));

        String datesAlias = SqlUtils.uniqueName();

        query.addFromInner(datesQuery, datesAlias,
            SqlUtils.joinUsing(TBL_WORK_SCHEDULE, datesAlias, COL_WORK_SCHEDULE_DATE));
        query.setWhere(relWhere);
        break;
    }

    SimpleRowSet candidates = qs.getData(query);
    if (DataUtils.isEmpty(candidates)) {
      return ResponseObject.emptyResponse();
    }

    Multimap<Long, Integer> overlap = HashMultimap.create();

    long objId = BeeConst.LONG_UNDEF;
    long emplId = BeeConst.LONG_UNDEF;

    switch (relationColumn) {
      case COL_PAYROLL_OBJECT:
        objId = relId;
        break;

      case COL_EMPLOYEE:
        emplId = relId;
        break;
    }

    for (SimpleRow row : candidates) {
      Long partId = row.getLong(partitionColumn);
      JustDate date = row.getDate(COL_WORK_SCHEDULE_DATE);

      if (DataUtils.isId(partId) && date != null && date.getDays() > 0) {
        switch (relationColumn) {
          case COL_PAYROLL_OBJECT:
            emplId = partId;
            break;

          case COL_EMPLOYEE:
            objId = partId;
            break;
        }

        if (overlaps(objId, emplId, date, kind)) {
          overlap.put(partId, -date.getDays());
        } else {
          overlap.put(partId, date.getDays());
        }
      }
    }

    if (overlap.isEmpty()) {
      return ResponseObject.emptyResponse();

    } else {
      StringBuilder sb = new StringBuilder();

      for (long partId : overlap.keySet()) {
        if (sb.length() > 0) {
          sb.append(BeeConst.DEFAULT_ROW_SEPARATOR);
        }
        sb.append(BeeUtils.join(BeeConst.DEFAULT_VALUE_SEPARATOR,
            partId, BeeUtils.joinInts(overlap.get(partId))));
      }

      return ResponseObject.response(sb.toString());
    }
  }

  private RangeMap<JustDate, Double> getWages(long employee, long object,
      JustDate from, JustDate until, Long currency) {

    RangeMap<JustDate, Double> result = RangeMap.create();

    SimpleRow emplRow = qs.getRow(TBL_EMPLOYEES, employee);
    if (emplRow != null) {
      Double salary = emplRow.getDouble(COL_SALARY);
      if (BeeUtils.isPositive(salary)) {
        Double v = adm.maybeExchange(emplRow.getLong(COL_CURRENCY), currency, salary, null);

        if (BeeUtils.isPositive(v)) {
          result.put(DateRange.all(), v);
        }
      }
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_EMPLOYEE_OBJECTS, COL_EMPLOYEE_OBJECT_FROM, COL_EMPLOYEE_OBJECT_UNTIL,
            COL_WAGE, COL_CURRENCY)
        .addFrom(TBL_EMPLOYEE_OBJECTS)
        .setWhere(
            SqlUtils.and(
                SqlUtils.equals(TBL_EMPLOYEE_OBJECTS,
                    COL_EMPLOYEE, employee, COL_PAYROLL_OBJECT, object),
                SqlUtils.positive(TBL_EMPLOYEE_OBJECTS, COL_WAGE)));

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        JustDate min = BeeUtils.max(row.getDate(COL_EMPLOYEE_OBJECT_FROM), from);
        JustDate max = BeeUtils.min(row.getDate(COL_EMPLOYEE_OBJECT_UNTIL), until);

        if (DateRange.isValidClosedRange(min, max)) {
          Double v = adm.maybeExchange(row.getLong(COL_CURRENCY), currency,
              row.getDouble(COL_WAGE), null);

          if (BeeUtils.isPositive(v)) {
            result.put(DateRange.closed(min, max), v);
          }
        }
      }
    }

    return result;
  }

  private ResponseObject initializeEarnings(RequestInfo reqInfo) {
    Integer year = reqInfo.getParameterInt(COL_EARNINGS_YEAR);
    if (!TimeUtils.isYear(year)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_EARNINGS_YEAR);
    }

    Integer month = reqInfo.getParameterInt(COL_EARNINGS_MONTH);
    if (!TimeUtils.isMonth(month)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_EARNINGS_MONTH);
    }

    int result = 0;

    JustDate from = new JustDate(year, month, 1);
    JustDate until = TimeUtils.endOfMonth(from);

    HasConditions scheduleWhere =
        SqlUtils.and(
            SqlUtils.equals(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, WorkScheduleKind.ACTUAL),
            SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, from),
            SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, until),
            SqlUtils.or(
                SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_TIME_RANGE_CODE),
                SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_FROM),
                SqlUtils.notNull(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DURATION)));

    SqlSelect wsEmplQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, COL_EMPLOYEE, COL_PAYROLL_OBJECT)
        .addFrom(TBL_WORK_SCHEDULE)
        .setWhere(scheduleWhere);

    SimpleRowSet scheduledEmployees = qs.getData(wsEmplQuery);
    if (DataUtils.isEmpty(scheduledEmployees)) {
      return ResponseObject.response(result);
    }

    Set<Pair<Long, Long>> existingEmployees = new HashSet<>();

    SqlSelect earnEmplQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_EMPLOYEE_EARNINGS, COL_EMPLOYEE, COL_PAYROLL_OBJECT)
        .addFrom(TBL_EMPLOYEE_EARNINGS)
        .setWhere(SqlUtils.equals(TBL_EMPLOYEE_EARNINGS,
            COL_EARNINGS_YEAR, year, COL_EARNINGS_MONTH, month));

    SimpleRowSet earnEmplData = qs.getData(earnEmplQuery);
    if (!DataUtils.isEmpty(earnEmplData)) {
      for (SimpleRow row : earnEmplData) {
        Long employee = row.getLong(COL_EMPLOYEE);
        Long object = row.getLong(COL_PAYROLL_OBJECT);

        if (DataUtils.isId(employee) && DataUtils.isId(object)) {
          existingEmployees.add(Pair.of(employee, object));
        }
      }
    }

    for (SimpleRow row : scheduledEmployees) {
      Long employee = row.getLong(COL_EMPLOYEE);
      Long object = row.getLong(COL_PAYROLL_OBJECT);

      if (DataUtils.isId(employee) && DataUtils.isId(object)
          && !existingEmployees.contains(Pair.of(employee, object))) {

        SqlInsert insert = new SqlInsert(TBL_EMPLOYEE_EARNINGS)
            .addConstant(COL_EMPLOYEE, employee)
            .addConstant(COL_PAYROLL_OBJECT, object)
            .addConstant(COL_EARNINGS_YEAR, year)
            .addConstant(COL_EARNINGS_MONTH, month);

        ResponseObject response = qs.insertDataWithResponse(insert);
        if (response.hasErrors()) {
          return response;
        }

        result++;
      }
    }

    SqlSelect wsObjQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, COL_PAYROLL_OBJECT)
        .addFrom(TBL_WORK_SCHEDULE)
        .setWhere(scheduleWhere);

    Set<Long> scheduledObjects = qs.getLongSet(wsObjQuery);

    SqlSelect earnObjQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_OBJECT_EARNINGS, COL_PAYROLL_OBJECT)
        .addFrom(TBL_OBJECT_EARNINGS)
        .setWhere(SqlUtils.equals(TBL_OBJECT_EARNINGS,
            COL_EARNINGS_YEAR, year, COL_EARNINGS_MONTH, month));

    Set<Long> existingObjects = qs.getLongSet(earnObjQuery);
    scheduledObjects.removeAll(existingObjects);

    for (Long object : scheduledObjects) {
      if (DataUtils.isId(object)) {
        SqlInsert insert = new SqlInsert(TBL_OBJECT_EARNINGS)
            .addConstant(COL_PAYROLL_OBJECT, object)
            .addConstant(COL_EARNINGS_YEAR, year)
            .addConstant(COL_EARNINGS_MONTH, month);

        ResponseObject response = qs.insertDataWithResponse(insert);
        if (response.hasErrors()) {
          return response;
        }

        result++;
      }
    }

    return ResponseObject.response(result);
  }

  private boolean overlaps(long objId, long emplId, JustDate date, WorkScheduleKind kind) {
    Set<TimeRange> objRanges = new HashSet<>();
    Set<TimeRange> otherRanges = new HashSet<>();

    Set<Long> objTrIds = new HashSet<>();
    Set<Long> otherTrIds = new HashSet<>();

    Filter filter = Filter.and(Filter.equals(COL_EMPLOYEE, emplId),
        Filter.equals(COL_WORK_SCHEDULE_DATE, date),
        Filter.equals(COL_WORK_SCHEDULE_KIND, kind));

    BeeRowSet rowSet = qs.getViewData(VIEW_WORK_SCHEDULE, filter);

    if (!DataUtils.isEmpty(rowSet)) {
      int objIndex = rowSet.getColumnIndex(COL_PAYROLL_OBJECT);

      int trIndex = rowSet.getColumnIndex(COL_TIME_RANGE_CODE);
      int tcIndex = rowSet.getColumnIndex(COL_TIME_CARD_CODE);

      int fromIndex = rowSet.getColumnIndex(COL_WORK_SCHEDULE_FROM);
      int untilIndex = rowSet.getColumnIndex(COL_WORK_SCHEDULE_UNTIL);
      int durIndex = rowSet.getColumnIndex(COL_WORK_SCHEDULE_DURATION);

      int trFromIndex = rowSet.getColumnIndex(ALS_TR_FROM);
      int trUntilIndex = rowSet.getColumnIndex(ALS_TR_UNTIL);
      int trDurIndex = rowSet.getColumnIndex(ALS_TR_DURATION);

      String from;
      String until;
      String duration;

      for (BeeRow row : rowSet) {
        if (!DataUtils.isId(row.getLong(tcIndex))) {
          boolean isObj = Objects.equals(objId, row.getLong(objIndex));

          Long trId = row.getLong(trIndex);

          if (DataUtils.isId(trId)) {
            if (isObj) {
              if (otherTrIds.contains(trId)) {
                return true;
              }
              objTrIds.add(trId);

            } else {
              if (objTrIds.contains(trId)) {
                return true;
              }
              otherTrIds.add(trId);
            }

            from = row.getString(trFromIndex);
            until = row.getString(trUntilIndex);
            duration = row.getString(trDurIndex);

          } else {
            from = row.getString(fromIndex);
            until = row.getString(untilIndex);
            duration = row.getString(durIndex);
          }

          if (!BeeUtils.isEmpty(from)) {
            TimeRange range = TimeRange.of(from, until, duration);

            if (range != null) {
              if (isObj) {
                objRanges.add(range);
              } else {
                otherRanges.add(range);
              }
            }
          }
        }
      }
    }

    if (BeeUtils.intersects(objTrIds, otherTrIds)) {
      return true;

    } else if (!objRanges.isEmpty() && !otherRanges.isEmpty()) {
      for (TimeRange tr : objRanges) {
        if (BeeUtils.intersects(otherRanges, tr.getRange())) {
          return true;
        }
      }
    }

    return false;
  }

  private void setScheduledTime(Earnings earnings, Set<JustDate> holidays) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_KIND, COL_EMPLOYEE, COL_SUBSTITUTE_FOR,
            COL_WORK_SCHEDULE_DATE, COL_TIME_RANGE_CODE,
            COL_WORK_SCHEDULE_FROM, COL_WORK_SCHEDULE_UNTIL, COL_WORK_SCHEDULE_DURATION)
        .addField(TBL_TIME_RANGES, COL_TR_FROM, ALS_TR_FROM)
        .addField(TBL_TIME_RANGES, COL_TR_UNTIL, ALS_TR_UNTIL)
        .addField(TBL_TIME_RANGES, COL_TR_DURATION, ALS_TR_DURATION)
        .addFrom(TBL_WORK_SCHEDULE)
        .addFromLeft(TBL_TIME_RANGES,
            sys.joinTables(TBL_TIME_RANGES, TBL_WORK_SCHEDULE, COL_TIME_RANGE_CODE))
        .setWhere(getScheduleEarningsCondition(earnings.getDateFrom(), earnings.getDateUntil(),
            earnings.getEmployeeId(), earnings.getSubstituteFor(), earnings.getObjectId()));

    SimpleRowSet data = qs.getData(query);
    if (!DataUtils.isEmpty(data)) {

      Map<JustDate, Long> planned = new HashMap<>();
      Map<JustDate, Long> actual = new HashMap<>();

      boolean substitutePlanned = earnings.isSubstitution()
          && !WorkScheduleKind.PLANNED.isSubstitutionEnabled();

      Long millis;
      Long value;
      boolean ok;

      for (SimpleRow row : data) {
        WorkScheduleKind kind = EnumUtils.getEnumByIndex(WorkScheduleKind.class,
            row.getInt(COL_WORK_SCHEDULE_KIND));

        Long empl = row.getLong(COL_EMPLOYEE);
        Long subst = row.getLong(COL_SUBSTITUTE_FOR);

        if (substitutePlanned && kind == WorkScheduleKind.PLANNED) {
          ok = subst == null && Objects.equals(empl, earnings.getSubstituteFor());
        } else {
          ok = Objects.equals(empl, earnings.getEmployeeId())
              && Objects.equals(subst, earnings.getSubstituteFor());
        }

        if (ok) {
          JustDate date = row.getDate(COL_WORK_SCHEDULE_DATE);

          if (DataUtils.isId(row.getLong(COL_TIME_RANGE_CODE))) {
            millis = PayrollUtils.getMillis(row.getValue(ALS_TR_FROM), row.getValue(ALS_TR_UNTIL),
                row.getValue(ALS_TR_DURATION));
          } else {
            millis = PayrollUtils.getMillis(row.getValue(COL_WORK_SCHEDULE_FROM),
                row.getValue(COL_WORK_SCHEDULE_UNTIL), row.getValue(COL_WORK_SCHEDULE_DURATION));
          }

          if (BeeUtils.isPositive(millis) && kind != null && date != null) {
            switch (kind) {
              case PLANNED:
                value = planned.get(date);
                if (value != null) {
                  millis += value;
                }

                planned.put(date, millis);
                break;

              case ACTUAL:
                value = actual.get(date);
                if (value != null) {
                  millis += value;
                }

                actual.put(date, millis);
                break;
            }
          }
        }
      }

      if (!planned.isEmpty()) {
        earnings.setPlannedDays(planned.size());
        earnings.setPlannedMillis(planned.values().stream().mapToLong(n -> n.longValue()).sum());
      }

      if (!actual.isEmpty()) {
        earnings.setActualDays(actual.size());
        earnings.setActualMillis(actual.values().stream().mapToLong(n -> n.longValue()).sum());

        if (BeeUtils.intersects(actual.keySet(), holidays)) {
          Map<JustDate, Long> holy = actual.entrySet().stream()
              .filter(entry -> holidays.contains(entry.getKey()))
              .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

          earnings.setHolyDays(holy.size());
          earnings.setHolyMillis(holy.values().stream().mapToLong(n -> n.longValue()).sum());
        }
      }
    }
  }
}
