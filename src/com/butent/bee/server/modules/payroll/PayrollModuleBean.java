package com.butent.bee.server.modules.payroll;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_COSTS_EXTERNAL_ID;

import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.Invocation;
import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.AdministrationModuleBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
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
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.payroll.Earning;
import com.butent.bee.shared.modules.payroll.PayrollUtils;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.TimeRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.webservice.ButentWS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Stateless
@LocalBean
public class PayrollModuleBean implements BeeModule, ConcurrencyBean.HasTimerService {

  private static BeeLogger logger = LogUtils.getLogger(PayrollModuleBean.class);

  private static final String PRM_ERP_SYNC_HOURS = "VitarestaSyncHours";

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  ConcurrencyBean cb;
  @EJB
  SystemBean sys;
  @EJB
  AdministrationModuleBean adm;
  @EJB
  ParamHolderBean prm;

  @Resource
  TimerService timerService;
  @Resource
  EJBContext ctx;

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

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public void ejbTimeout(Timer timer) {
    if (cb.isParameterTimer(timer, PRM_ERP_SYNC_HOURS)) {
      cb.asynchronousCall(new ConcurrencyBean.AsynchronousRunnable() {
        @Override
        public String getId() {
          return BeeUtils.join("-", PayrollModuleBean.class.getSimpleName(), PRM_ERP_SYNC_HOURS);
        }

        @Override
        public void run() {
          PayrollModuleBean bean =
              Assert.notNull(Invocation.locateRemoteBean(PayrollModuleBean.class));
          bean.importERPData();
        }
      });
    }
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    return Collections.singleton(BeeParameter.createNumber(module, PRM_ERP_SYNC_HOURS));
  }

  @Override
  public Module getModule() {
    return Module.PAYROLL;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public TimerService getTimerService() {
    return timerService;
  }

  public void importERPData() {
    long historyId = sys.eventStart(PRM_ERP_SYNC_HOURS);

    SimpleRowSet companies = qs.getData(new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), COL_COMPANY)
        .addFields(TBL_COMPANIES, COL_COMPANY_NAME, PRM_ERP_ADDRESS, PRM_ERP_LOGIN,
            PRM_ERP_PASSWORD)
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.notNull(TBL_COMPANIES, PRM_ERP_ADDRESS)));

    Map<String, Long> positions = getReferences(TBL_POSITIONS, COL_POSITION_NAME);
    String companyDepartments = "CompanyDepartments";
    String log = null;

    DateTime lastSyncTime = qs.getDateTime(new SqlSelect()
        .addMax(TBL_EVENT_HISTORY, COL_EVENT_STARTED)
        .addFrom(TBL_EVENT_HISTORY)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_EVENT_HISTORY, COL_EVENT,
                PRM_ERP_SYNC_HOURS),
            SqlUtils.startsWith(TBL_EVENT_HISTORY, COL_EVENT_RESULT, "OK"))));

    for (SimpleRow companyInfo : companies) {
      Long company = companyInfo.getLong(COL_COMPANY);
      SimpleRowSet rs = null;
      String erpAddress = companyInfo.getValue(PRM_ERP_ADDRESS);
      String erpLogin = companyInfo.getValue(PRM_ERP_LOGIN);
      String erpPassword = companyInfo.getValue(PRM_ERP_PASSWORD);

      try {
        rs = ButentWS.connect(erpAddress, erpLogin, erpPassword)
            .getEmployees(lastSyncTime);
      } catch (BeeException e) {
        ctx.setRollbackOnly();
        sys.eventError(historyId, e);
        return;
      }
      int emplNew = 0;
      int emplUpd = 0;
      int posNew = 0;
      int deptNew = 0;
      int locNew = 0;
      String cardsInfo = null;
      Map<String, Long> departments = getReferences(companyDepartments, "Name",
          SqlUtils.equals(companyDepartments, COL_COMPANY, company));

      SimpleRowSet employees = qs.getData(new SqlSelect()
          .addField(TBL_COMPANY_PERSONS, sys.getIdName(TBL_COMPANY_PERSONS), COL_COMPANY_PERSON)
          .addFields(TBL_COMPANY_PERSONS, COL_PERSON, COL_CONTACT)
          .addExpr(SqlUtils.concat(SqlUtils.field(TBL_PERSONS, COL_FIRST_NAME), "' '",
              SqlUtils.field(TBL_PERSONS, COL_LAST_NAME)), COL_FIRST_NAME)
          .addField(TBL_PERSONS, COL_CONTACT, COL_PERSON + COL_CONTACT)
          .addFields(TBL_EMPLOYEES, COL_TAB_NUMBER)
          .addField(TBL_EMPLOYEES, sys.getIdName(TBL_EMPLOYEES), COL_EMPLOYEE)
          .addFrom(TBL_COMPANY_PERSONS)
          .addFromInner(TBL_PERSONS, sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
          .addFromLeft(TBL_EMPLOYEES,
              sys.joinTables(TBL_COMPANY_PERSONS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
          .setWhere(SqlUtils.equals(TBL_COMPANY_PERSONS, COL_COMPANY, company)));

      String tabNr = null;

      try {
        for (SimpleRow row : rs) {
          tabNr = row.getValue("CODE");
          SimpleRow info = employees.getRowByKey(COL_TAB_NUMBER, tabNr);

          Long person;
          Long personContact = null;
          Long companyPerson;
          Long contact = null;
          Long employee;

          if (info == null) {
            info = employees.getRowByKey(COL_FIRST_NAME,
                BeeUtils.joinWords(row.getValue("NAME"), row.getValue("SURNAME")));

            if (info != null) {
              employee = info.getLong(COL_EMPLOYEE);

              if (DataUtils.isId(employee)) {
                qs.updateData(new SqlUpdate(TBL_EMPLOYEES)
                    .addConstant(COL_TAB_NUMBER, tabNr)
                    .setWhere(sys.idEquals(TBL_EMPLOYEES, employee)));
              } else {
                employee = qs.insertData(new SqlInsert(TBL_EMPLOYEES)
                    .addConstant(COL_COMPANY_PERSON, info.getLong(COL_COMPANY_PERSON))
                    .addConstant(COL_TAB_NUMBER, tabNr));
              }
            }
          }
          if (info == null) {
            person = qs.insertData(new SqlInsert(TBL_PERSONS)
                .addConstant(COL_FIRST_NAME, row.getValue("NAME"))
                .addConstant(COL_LAST_NAME, row.getValue("SURNAME")));

            companyPerson = qs.insertData(new SqlInsert(TBL_COMPANY_PERSONS)
                .addConstant(COL_COMPANY, company)
                .addConstant(COL_PERSON, person));

            employee = qs.insertData(new SqlInsert(TBL_EMPLOYEES)
                .addConstant(COL_COMPANY_PERSON, companyPerson)
                .addConstant(COL_TAB_NUMBER, tabNr));
            emplNew++;
          } else {
            person = info.getLong(COL_PERSON);
            personContact = info.getLong(COL_PERSON + COL_CONTACT);
            companyPerson = info.getLong(COL_COMPANY_PERSON);
            contact = info.getLong(COL_CONTACT);
            employee = info.getLong(COL_EMPLOYEE);
            emplUpd++;
          }
          String address = row.getValue("ADDRESS1");

          if (!BeeUtils.isEmpty(address)) {
            if (!DataUtils.isId(personContact)) {
              personContact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                  .addConstant(COL_ADDRESS, address));

              qs.updateData(new SqlUpdate(TBL_PERSONS)
                  .addConstant(COL_CONTACT, personContact)
                  .setWhere(sys.idEquals(TBL_PERSONS, person)));
            } else {
              qs.updateData(new SqlUpdate(TBL_CONTACTS)
                  .addConstant(COL_ADDRESS, address)
                  .setWhere(sys.idEquals(TBL_CONTACTS, personContact)));
            }
          }
          String phone = row.getValue("MOBILEPHONE");

          if (!BeeUtils.isEmpty(phone)) {
            if (!DataUtils.isId(contact)) {
              contact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                  .addConstant(COL_MOBILE, phone));

              qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
                  .addConstant(COL_CONTACT, contact)
                  .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));
            } else {
              qs.updateData(new SqlUpdate(TBL_CONTACTS)
                  .addConstant(COL_MOBILE, phone)
                  .setWhere(sys.idEquals(TBL_CONTACTS, contact)));
            }
          }
          String email = BeeUtils.normalize(row.getValue("EMAIL"));

          if (!BeeUtils.isEmpty(email)) {
            Long emailId = qs.getLong(new SqlSelect()
                .addFields(TBL_EMAILS, sys.getIdName(TBL_EMAILS))
                .addFrom(TBL_EMAILS)
                .setWhere(SqlUtils.equals(TBL_EMAILS, COL_EMAIL_ADDRESS, email)));

            if (!DataUtils.isId(emailId)) {
              emailId = qs.insertData(new SqlInsert(TBL_EMAILS)
                  .addConstant(COL_EMAIL_ADDRESS, email));
            }
            if (DataUtils.isId(emailId)) {
              if (!DataUtils.isId(contact)) {
                contact = qs.insertData(new SqlInsert(TBL_CONTACTS)
                    .addConstant(COL_EMAIL, emailId));

                qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
                    .addConstant(COL_CONTACT, contact)
                    .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));
              } else {
                qs.updateData(new SqlUpdate(TBL_CONTACTS)
                    .addConstant(COL_EMAIL, emailId)
                    .setWhere(sys.idEquals(TBL_CONTACTS, contact)));
              }
            }
          }
          String department = row.getValue("DEPARTCODE");

          if (!BeeUtils.isEmpty(department) && !departments.containsKey(department)) {
            departments.put(department, qs.insertData(new SqlInsert(companyDepartments)
                .addConstant(COL_COMPANY, company)
                .addConstant("Name", department)));
            deptNew++;
          }
          String position = row.getValue("POSITIONCODE");

          if (!BeeUtils.isEmpty(position) && !positions.containsKey(position)) {
            positions.put(position, qs.insertData(new SqlInsert(TBL_POSITIONS)
                .addConstant(COL_POSITION_NAME, position)));
            posNew++;
          }
          qs.updateData(new SqlUpdate(TBL_PERSONS)
              .addConstant(COL_DATE_OF_BIRTH, TimeUtils.parseDate(row.getValue("BIRTHDAY")))
              .setWhere(sys.idEquals(TBL_PERSONS, person)));

          qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
              .addConstant(COL_DEPARTMENT, departments.get(department))
              .addConstant(COL_POSITION, positions.get(position))
              .addConstant(COL_DATE_OF_EMPLOYMENT, TimeUtils.parseDate(row.getValue("DIRBA_NUO")))
              .addConstant(COL_DATE_OF_DISMISSAL, TimeUtils.parseDate(row.getValue("DISMISSED")))
              .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));

          qs.updateData(new SqlUpdate(TBL_EMPLOYEES)
              .addConstant(COL_PART_TIME, row.getDecimal("ETATAS"))
              .setWhere(sys.idEquals(TBL_EMPLOYEES, employee)));
        }
        locNew = importLocations(erpAddress, erpLogin, erpPassword);
        cardsInfo = importTimeCards(erpAddress, erpLogin, erpPassword, lastSyncTime, company);

      } catch (Throwable e) {
        ctx.setRollbackOnly();
        sys.eventError(historyId, e, BeeUtils.join(": ", COL_TAB_NUMBER, tabNr));
        return;
      }
      log = BeeUtils.join(BeeConst.STRING_EOL, log, companyInfo.getValue(COL_COMPANY_NAME),
          deptNew > 0 ? companyDepartments + ": +" + deptNew : null,
          posNew > 0 ? TBL_POSITIONS + ": +" + posNew : null,
          locNew > 0 ? TBL_LOCATIONS + ": +" + locNew : null,
          (emplNew + emplUpd) > 0 ? TBL_EMPLOYEES + ":" + (emplNew > 0 ? " +" + emplNew : "")
              + (emplUpd > 0 ? " " + emplUpd : "") : null, cardsInfo);
    }
    sys.eventEnd(historyId, "OK", log);
  }

  private int importLocations(String erpAddress, String erpLogin, String erpPassword)
      throws BeeException {

    SimpleRowSet rs = ButentWS.connect(erpAddress, erpLogin, erpPassword)
        .getObjects();

    int locNew = 0;
    Map<String, Long> locations = getReferences(TBL_LOCATIONS, COL_LOCATION_NAME);

    for (SimpleRow row : rs) {
      String location = row.getValue("objektas");

      if (!locations.containsKey(location)) {
        locations.put(location, qs.insertData(new SqlInsert(TBL_LOCATIONS)
            .addConstant(COL_LOCATION_NAME, location)
            .addConstant(COL_LOCATION_STATUS, ObjectStatus.INACTIVE.ordinal())));
        locNew++;
      }
    }
    return locNew;
  }

  private String importTimeCards(String erpAddress, String erpLogin, String erpPassword,
      DateTime lastSyncTime, Long company) throws BeeException {
    SimpleRowSet rs = ButentWS.connect(erpAddress, erpLogin, erpPassword)
        .getTimeCards(lastSyncTime);

    SimpleRowSet employees = qs.getData(new SqlSelect()
        .addField(TBL_EMPLOYEES, sys.getIdName(TBL_EMPLOYEES), COL_EMPLOYEE)
        .addFields(TBL_EMPLOYEES, COL_TAB_NUMBER)
        .addFrom(TBL_EMPLOYEES)
        .addFromInner(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .setWhere(SqlUtils.equals(TBL_COMPANY_PERSONS, COL_COMPANY, company)));

    Map<String, Long> tcCodes = getReferences(VIEW_TIME_CARD_CODES, COL_TC_CODE);

    int cds = 0;
    int ins = 0;
    int upd = 0;
    int del = 0;

    for (SimpleRow row : rs) {
      Long id = row.getLong("D_TAB_ID");
      String tabNumber = row.getValue("TAB_NR");

      if (BeeUtils.isEmpty(tabNumber)) {
        del += qs.updateData(new SqlDelete(VIEW_TIME_CARD_CHANGES)
            .setWhere(SqlUtils.equals(VIEW_TIME_CARD_CHANGES, COL_COSTS_EXTERNAL_ID, id)));
        continue;
      }
      Long employee = BeeUtils.toLongOrNull(employees.getValueByKey(COL_TAB_NUMBER, tabNumber,
          COL_EMPLOYEE));

      if (!DataUtils.isId(employee)) {
        continue;
      }
      String code = row.getValue("TAB_KODAS");

      if (!tcCodes.containsKey(code)) {
        tcCodes.put(code, qs.insertData(new SqlInsert(VIEW_TIME_CARD_CODES)
            .addConstant(COL_TC_CODE, code)
            .addConstant(COL_TC_NAME,
                sys.clampValue(VIEW_TIME_CARD_CODES, COL_TC_NAME, row.getValue("PAVAD")))));
        cds++;
      }
      int c = qs.updateData(new SqlUpdate(VIEW_TIME_CARD_CHANGES)
          .addConstant(COL_EMPLOYEE, employee)
          .addConstant(COL_TIME_CARD_CODE, tcCodes.get(code))
          .addConstant(COL_TIME_CARD_CHANGES_FROM, TimeUtils.parseDate(row.getValue("DATA_NUO")))
          .addConstant(COL_TIME_CARD_CHANGES_UNTIL, TimeUtils.parseDate(row.getValue("DATA_IKI")))
          .addConstant(COL_NOTES, row.getValue("ISAK_PAVAD"))
          .setWhere(SqlUtils.equals(VIEW_TIME_CARD_CHANGES, COL_COSTS_EXTERNAL_ID, id)));

      if (BeeUtils.isPositive(c)) {
        upd++;
      } else {
        qs.insertData(new SqlInsert(VIEW_TIME_CARD_CHANGES)
            .addConstant(COL_EMPLOYEE, employee)
            .addConstant(COL_TIME_CARD_CODE, tcCodes.get(code))
            .addConstant(COL_TIME_CARD_CHANGES_FROM, TimeUtils.parseDate(row.getValue("DATA_NUO")))
            .addConstant(COL_TIME_CARD_CHANGES_UNTIL, TimeUtils.parseDate(row.getValue("DATA_IKI")))
            .addConstant(COL_NOTES, row.getValue("ISAK_PAVAD"))
            .addConstant(COL_COSTS_EXTERNAL_ID, id));
        ins++;
      }
    }
    return BeeUtils.join(BeeConst.STRING_EOL, cds > 0 ? VIEW_TIME_CARD_CODES + ": +" + cds : null,
        (ins + upd + del) > 0 ? VIEW_TIME_CARD_CHANGES + ":" + (ins > 0 ? " +" + ins : "")
            + (upd > 0 ? " " + upd : "") + (del > 0 ? " -" + del : "") : null);
  }

  @Override
  public void init() {
    cb.createIntervalTimer(this.getClass(), PRM_ERP_SYNC_HOURS);

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

  private Map<String, Long> getReferences(String tableName, String keyName) {
    return getReferences(tableName, keyName, null);
  }

  private Map<String, Long> getReferences(String tableName, String keyName, IsCondition clause) {
    Map<String, Long> ref = new HashMap<>();

    for (SimpleRow row : qs.getData(new SqlSelect()
        .addFields(tableName, keyName)
        .addField(tableName, sys.getIdName(tableName), tableName)
        .addFrom(tableName)
        .setWhere(SqlUtils.and(SqlUtils.notNull(tableName, keyName), clause)))) {

      ref.put(row.getValue(keyName), row.getLong(tableName));
    }
    return ref;
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
                SqlUtils.equals(TBL_WORK_SCHEDULE,
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

  private ResponseObject getScheduledMonths(RequestInfo reqInfo) {
    Long manager = reqInfo.getParameterLong(COL_LOCATION_MANAGER);

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, COL_PAYROLL_OBJECT)
        .addFrom(TBL_WORK_SCHEDULE);

    if (DataUtils.isId(manager)) {
      query
          .addFromInner(TBL_LOCATIONS,
              sys.joinTables(TBL_LOCATIONS, TBL_WORK_SCHEDULE, COL_PAYROLL_OBJECT))
          .setWhere(SqlUtils.equals(TBL_LOCATIONS, COL_LOCATION_MANAGER, manager));
    }

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

    HasConditions dateWhere = SqlUtils.and();
    if (dateFrom != null) {
      dateWhere.add(SqlUtils.moreEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, dateFrom));
    }
    if (dateUntil != null) {
      dateWhere.add(SqlUtils.lessEqual(TBL_WORK_SCHEDULE, COL_WORK_SCHEDULE_DATE, dateUntil));
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
            dateWhere));

        query.addFromInner(subQuery, subAlias, SqlUtils.joinUsing(TBL_WORK_SCHEDULE, subAlias,
            partitionColumn, COL_WORK_SCHEDULE_DATE));
        query.setWhere(SqlUtils.and(relWhere, dateWhere));
        break;

      case COL_EMPLOYEE:
        subQuery.setWhere(SqlUtils.and(relWhere, dateWhere));

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

        if (overlaps(objId, emplId, date)) {
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

  private boolean overlaps(long objId, long emplId, JustDate date) {
    Set<TimeRange> objRanges = new HashSet<>();
    Set<TimeRange> otherRanges = new HashSet<>();

    Set<Long> objTrIds = new HashSet<>();
    Set<Long> otherTrIds = new HashSet<>();

    Filter filter = Filter.and(Filter.equals(COL_EMPLOYEE, emplId),
        Filter.equals(COL_WORK_SCHEDULE_DATE, date));

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
}
