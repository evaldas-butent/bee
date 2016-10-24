package com.butent.bee.server.modules.payroll;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants.ObjectStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.webservice.ButentWS;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Timer;
import javax.ejb.TimerService;

abstract class CustomERPIntegration implements BeeModule, ConcurrencyBean.HasTimerService {

  @EJB
  private QueryServiceBean qs;

  @EJB
  private SystemBean sys;

  @EJB
  ConcurrencyBean cb;

  @Resource
  TimerService timerService;
  @Resource
  EJBContext ctx;

  @Override
  public void ejbTimeout(Timer timer) {
    if (cb.isParameterTimer(timer, PRM_ERP_SYNC_HOURS_VITARESTA)) {
      cb.asynchronousCall(new ConcurrencyBean.AsynchronousRunnable() {
        @Override
        public String getId() {
          return BeeUtils.join("-", PayrollModuleBean.class.getSimpleName(),
              PRM_ERP_SYNC_HOURS_VITARESTA);
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
  public TimerService getTimerService() {
    return timerService;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    return Collections.singleton(BeeParameter.createNumber(module, PRM_ERP_SYNC_HOURS_VITARESTA));
  }

  @Override
  public void init() {
    cb.createIntervalTimer(this.getClass(), PRM_ERP_SYNC_HOURS_VITARESTA);
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

  public void importERPData() {
    SimpleRowSet companies = qs.getData(new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), COL_COMPANY)
        .addFields(TBL_COMPANIES, COL_COMPANY_NAME, PRM_ERP_ADDRESS, PRM_ERP_LOGIN,
            PRM_ERP_PASSWORD, "ERPLastEvent")
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.notNull(TBL_COMPANIES, PRM_ERP_ADDRESS)));

    Map<String, Long> positions = getReferences(TBL_POSITIONS, COL_POSITION_NAME);
    String companyDepartments = "CompanyDepartments";


    for (SimpleRow companyInfo : companies) {
      String log = null;
      Long company = companyInfo.getLong(COL_COMPANY);
      long historyId = sys.eventStart(PRM_ERP_SYNC_HOURS_VITARESTA);
      DateTime lastSyncTime = companyInfo.getDateTime("ERPLastEvent");

      SimpleRowSet rs = null;
      String erpAddress = companyInfo.getValue(PRM_ERP_ADDRESS);
      String erpLogin = companyInfo.getValue(PRM_ERP_LOGIN);
      String erpPassword = companyInfo.getValue(PRM_ERP_PASSWORD);

      try {
        rs = ButentWS.connect(erpAddress, erpLogin, erpPassword)
            .getEmployees(lastSyncTime);
      } catch (BeeException e) {
        ctx.setRollbackOnly();
        sys.eventError(historyId, e, companyInfo.getValue(COL_COMPANY_NAME));
        continue;
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
            if (!DataUtils.isId(personContact) && DataUtils.isId(person)) {
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

          if (!BeeUtils.isEmpty(phone) && DataUtils.isId(companyPerson)) {
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

          if (DataUtils.isId(person)) {
            qs.updateData(new SqlUpdate(TBL_PERSONS)
                .addConstant(COL_DATE_OF_BIRTH, TimeUtils.parseDate(row.getValue("BIRTHDAY")))
                .setWhere(sys.idEquals(TBL_PERSONS, person)));
          }

          if (DataUtils.isId(companyPerson)) {
            qs.updateData(new SqlUpdate(TBL_COMPANY_PERSONS)
                .addConstant(AdministrationConstants.COL_DEPARTMENT, departments.get(department))
                .addConstant(COL_POSITION, positions.get(position))
                .addConstant(COL_DATE_OF_EMPLOYMENT, TimeUtils.parseDate(row.getValue("DIRBA_NUO")))
                .addConstant(COL_DATE_OF_DISMISSAL, TimeUtils.parseDate(row.getValue("DISMISSED")))
                .setWhere(sys.idEquals(TBL_COMPANY_PERSONS, companyPerson)));
          }

          if (DataUtils.isId(employee)) {
            qs.updateData(new SqlUpdate(TBL_EMPLOYEES)
                .addConstant(COL_PART_TIME, row.getDecimal("ETATAS"))
                .setWhere(sys.idEquals(TBL_EMPLOYEES, employee)));
          }
        }
        locNew = importLocations(erpAddress, erpLogin, erpPassword);
        cardsInfo = importTimeCards(erpAddress, erpLogin, erpPassword, lastSyncTime, company);

        qs.updateData(new SqlUpdate(TBL_COMPANIES)
            .addConstant("ERPLastEvent", System.currentTimeMillis())
            .setWhere(sys.idEquals(TBL_COMPANIES, company)));

      } catch (Throwable e) {
        ctx.setRollbackOnly();
        sys.eventError(historyId, e, BeeUtils.join(": ", companyInfo.getValue(COL_COMPANY_NAME),
            COL_TAB_NUMBER, tabNr));
        return;
      }
      log = BeeUtils.join(BeeConst.STRING_EOL, log, companyInfo.getValue(COL_COMPANY_NAME),
          deptNew > 0 ? companyDepartments + ": +" + deptNew : null,
          posNew > 0 ? TBL_POSITIONS + ": +" + posNew : null,
          locNew > 0 ? TBL_LOCATIONS + ": +" + locNew : null,
          (emplNew + emplUpd) > 0 ? TBL_EMPLOYEES + ":" + (emplNew > 0 ? " +" + emplNew : "")
              + (emplUpd > 0 ? " " + emplUpd : "") : null, cardsInfo);
      sys.eventEnd(historyId, "OK", log);
    }
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

}
