package com.butent.bee.server.modules.payroll;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
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
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class PayrollModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(PayrollModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

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
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
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
  public void init() {
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
