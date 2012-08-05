package com.butent.bee.server.modules.calendar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants.AppointmentStatus;
import com.butent.bee.shared.modules.calendar.CalendarConstants.View;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CalendarModuleBean implements BeeModule {

  private static Logger logger = Logger.getLogger(CalendarModuleBean.class.getName());

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;

  @Override
  public String dependsOn() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> results = Lists.newArrayList();

    Filter filter = Filter.or(
        DataUtils.anyColumnContains(Sets.newHashSet(COL_SUMMARY, COL_DESCRIPTION,
            COL_ORGANIZER_FIRST_NAME, COL_ORGANIZER_LAST_NAME,
            COL_COMPANY_NAME, COL_COMPANY_EMAIL,
            COL_VEHICLE_NUMBER, COL_VEHICLE_PARENT_MODEL, COL_VEHICLE_MODEL), query),
        DataUtils.anyItemContains(COL_STATUS, AppointmentStatus.class, query));

    BeeRowSet appointments = getAppointments(filter, new Order(COL_START_DATE_TIME, false), null);
    if (appointments != null) {
      for (BeeRow row : appointments.getRows()) {
        results.add(new SearchResult(VIEW_APPOINTMENTS, row));
      }
    }
    return results;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CALENDAR_METHOD);

    if (BeeUtils.same(svc, SVC_GET_USER_CALENDAR)) {
      response = getUserCalendar(reqInfo);
    } else if (BeeUtils.same(svc, SVC_CREATE_APPOINTMENT)) {
      response = createAppointment(reqInfo);
    } else if (BeeUtils.same(svc, SVC_UPDATE_APPOINTMENT)) {
      response = updateAppointment(reqInfo);
    } else if (BeeUtils.same(svc, SVC_GET_CALENDAR_APPOINTMENTS)) {
      response = getCalendarAppointments(reqInfo);
    } else if (BeeUtils.same(svc, SVC_SAVE_ACTIVE_VIEW)) {
      response = saveActiveView(reqInfo);
    } else if (BeeUtils.same(svc, SVC_GET_OVERLAPPING_APPOINTMENTS)) {
      response = getOverlappingAppointments(reqInfo);
    } else if (BeeUtils.same(svc, SVC_DO_REPORT)) {
      response = doReport(reqInfo);

    } else {
      String msg = BeeUtils.concat(1, "Calendar service not recognized:", svc);
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
  public String getName() {
    return CALENDAR_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
  }

  private boolean checkTable(String name) {
    return sys.isTable(name) && sys.getTable(name).isActive();
  }

  private ResponseObject createAppointment(RequestInfo reqInfo) {
    BeeRowSet rowSet = BeeRowSet.restore(reqInfo.getContent());
    if (rowSet.isEmpty()) {
      return ResponseObject.error(SVC_CREATE_APPOINTMENT, ": rowSet is empty");
    }

    String propIds = rowSet.getTableProperty(COL_PROPERTY);
    String attIds = rowSet.getTableProperty(COL_ATTENDEE);
    String rtIds = rowSet.getTableProperty(COL_REMINDER_TYPE);

    ResponseObject response = deb.commitRow(rowSet, true);
    if (response.hasErrors()) {
      return response;
    }

    long appId = ((BeeRow) response.getResponse()).getId();

    if (!BeeUtils.isEmpty(propIds)) {
      for (long propId : DataUtils.parseList(propIds)) {
        insertAppointmentProperty(appId, propId);
      }
    }

    if (!BeeUtils.isEmpty(attIds)) {
      for (long attId : DataUtils.parseList(attIds)) {
        insertAppointmentAttendee(appId, attId);
      }
    }

    if (!BeeUtils.isEmpty(rtIds)) {
      for (long rtId : DataUtils.parseList(rtIds)) {
        insertAppointmentReminder(appId, rtId);
      }
    }

    return response;
  }

  private ResponseObject doReport(RequestInfo reqInfo) {
    Integer paramRep = BeeUtils.toIntOrNull(reqInfo.getParameter(PARAM_REPORT));
    if (!BeeUtils.isOrdinal(Report.class, paramRep)) {
      return ResponseObject.error(SVC_DO_REPORT, PARAM_REPORT, "parameter not found");
    }

    Report report = Report.values()[paramRep];
    BeeRowSet result;

    switch (report) {
      case BUSY_HOURS:
        result = getAttendeesByHour(false);
        break;
      case BUSY_MONTHS:
        result = getAttendeesByMonth(false);
        break;
      case CANCEL_HOURS:
        result = getAttendeesByHour(true);
        break;
      case CANCEL_MONTHS:
        result = getAttendeesByMonth(true);
        break;
      default:
        Assert.untouchable();
        result = null;
    }

    if (result == null) {
      return ResponseObject.response(BeeConst.STRING_EMPTY);
    } else {
      return ResponseObject.response(result);
    }
  }
  
  private String formatMinutes(int minutes) {
    return BeeUtils.toString(minutes / TimeUtils.MINUTES_PER_HOUR) + DateTime.TIME_FIELD_SEPARATOR
        + TimeUtils.padTwo(minutes % TimeUtils.MINUTES_PER_HOUR);
  }

  private BeeRowSet getAppointments(Filter filter, Order order, Set<Long> attendees) {
    BeeRowSet appointments = qs.getViewData(VIEW_APPOINTMENTS, filter, order);
    if (appointments == null || appointments.isEmpty()) {
      return appointments;
    }

    BeeRowSet appAtts = qs.getViewData(VIEW_APPOINTMENT_ATTENDEES);
    BeeRowSet appProps = qs.getViewData(VIEW_APPOINTMENT_PROPS);
    BeeRowSet appRemind = qs.getViewData(VIEW_APPOINTMENT_REMINDERS);

    boolean filterByAttendee = !BeeUtils.isEmpty(attendees);

    int aaIndex = appAtts.getColumnIndex(COL_ATTENDEE);
    int apIndex = appProps.getColumnIndex(COL_PROPERTY);
    int arIndex = appRemind.getColumnIndex(COL_REMINDER_TYPE);

    List<BeeRow> children;
    Iterator<BeeRow> iterator = appointments.getRows().iterator();

    while (iterator.hasNext()) {
      BeeRow row = iterator.next();
      String appId = BeeUtils.toString(row.getId());

      children = DataUtils.filterRows(appAtts, COL_APPOINTMENT, appId);

      if (filterByAttendee) {
        boolean ok = false;
        for (BeeRow r : children) {
          if (attendees.contains(r.getLong(aaIndex))) {
            ok = true;
            break;
          }
        }
        if (!ok) {
          iterator.remove();
          continue;
        }
      }

      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_ATTENDEES,
            DataUtils.buildList(DataUtils.getDistinct(children, aaIndex)));
      }

      children = DataUtils.filterRows(appProps, COL_APPOINTMENT, appId);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_PROPS,
            DataUtils.buildList(DataUtils.getDistinct(children, apIndex)));
      }

      children = DataUtils.filterRows(appRemind, COL_APPOINTMENT, appId);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_REMINDERS,
            DataUtils.buildList(DataUtils.getDistinct(children, arIndex)));
      }
    }

    return appointments;
  }
  
  private SimpleRowSet getAttendeePeriods(boolean canceled) {
    int statusCanceled = AppointmentStatus.CANCELED.ordinal();
    IsCondition statusCondition = canceled
        ? SqlUtils.equal(TBL_APPOINTMENTS, COL_STATUS, statusCanceled)
        : SqlUtils.notEqual(TBL_APPOINTMENTS, COL_STATUS, statusCanceled);

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_APPOINTMENTS, COL_START_DATE_TIME, COL_END_DATE_TIME)
        .addFields(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE)
        .addFrom(TBL_APPOINTMENTS)
        .addFromInner(TBL_APPOINTMENT_ATTENDEES,
            SqlUtils.join(TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT,
                TBL_APPOINTMENTS, sys.getIdName(TBL_APPOINTMENTS)))
        .setWhere(statusCondition);

    return qs.getData(ss);
  }

  private BeeRowSet getAttendeesByHour(boolean canceled) {
    SimpleRowSet data = getAttendeePeriods(canceled);
    if (data == null || data.getNumberOfRows() <= 0) {
      return null;
    }

    int startIndex = data.getColumnIndex(COL_START_DATE_TIME);
    int endIndex = data.getColumnIndex(COL_END_DATE_TIME);
    int attIndex = data.getColumnIndex(COL_ATTENDEE);

    Table<Long, Integer, Integer> table = HashBasedTable.create();

    for (int r = 0; r < data.getNumberOfRows(); r++) {
      DateTime start = data.getDateTime(r, startIndex);
      DateTime end = data.getDateTime(r, endIndex);

      Long attendee = data.getLong(r, attIndex);

      for (Map.Entry<Integer, Integer> entry : splitByHour(start, end).entrySet()) {
        int hour = entry.getKey();
        int minutes = entry.getValue();

        Integer value = table.get(attendee, hour);
        if (value == null) {
          table.put(attendee, hour, minutes);
        } else {
          table.put(attendee, hour, value + minutes);
        }
      }
    }
    if (table.isEmpty()) {
      return null;
    }

    List<Integer> hours = Lists.newArrayList(table.columnKeySet());
    Collections.sort(hours);

    BeeRowSet attendees = qs.getViewData(VIEW_ATTENDEES);
    if (attendees.isEmpty()) {
      return null;
    }

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, COL_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_NAME));
    for (Integer hour : hours) {
      result.addColumn(ValueType.TEXT, hour.toString());
    }

    for (BeeRow attRow : attendees.getRows()) {
      Map<Integer, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[result.getNumberOfColumns()];
      values[0] = DataUtils.getString(attendees, attRow, COL_TYPE_NAME);
      values[1] = DataUtils.getString(attendees, attRow, COL_NAME);

      for (int c = 0; c < hours.size(); c++) {
        Integer minutes = tableRow.get(hours.get(c));
        values[c + 2] = (minutes == null) ? null : formatMinutes(minutes);
      }
      result.addRow(attRow.getId(), values);
    }
    return result;
  }

  private BeeRowSet getAttendeesByMonth(boolean canceled) {
    SimpleRowSet data = getAttendeePeriods(canceled);
    if (data == null || data.getNumberOfRows() <= 0) {
      return null;
    }

    int startIndex = data.getColumnIndex(COL_START_DATE_TIME);
    int endIndex = data.getColumnIndex(COL_END_DATE_TIME);
    int attIndex = data.getColumnIndex(COL_ATTENDEE);

    Table<Long, YearMonth, Integer> table = HashBasedTable.create();

    for (int r = 0; r < data.getNumberOfRows(); r++) {
      DateTime start = data.getDateTime(r, startIndex);
      DateTime end = data.getDateTime(r, endIndex);

      Long attendee = data.getLong(r, attIndex);

      for (Map.Entry<YearMonth, Integer> entry : splitByYearMonth(start, end).entrySet()) {
        YearMonth ym = entry.getKey();
        int minutes = entry.getValue();

        Integer value = table.get(attendee, ym);
        if (value == null) {
          table.put(attendee, ym, minutes);
        } else {
          table.put(attendee, ym, value + minutes);
        }
      }
    }
    if (table.isEmpty()) {
      return null;
    }

    List<YearMonth> months = Lists.newArrayList(table.columnKeySet());
    Collections.sort(months);

    BeeRowSet attendees = qs.getViewData(VIEW_ATTENDEES);
    if (attendees.isEmpty()) {
      return null;
    }

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, COL_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_NAME));
    for (YearMonth ym : months) {
      result.addColumn(ValueType.TEXT, ym.toString());
    }

    for (BeeRow attRow : attendees.getRows()) {
      Map<YearMonth, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[result.getNumberOfColumns()];
      values[0] = DataUtils.getString(attendees, attRow, COL_TYPE_NAME);
      values[1] = DataUtils.getString(attendees, attRow, COL_NAME);

      for (int c = 0; c < months.size(); c++) {
        Integer minutes = tableRow.get(months.get(c));
        values[c + 2] = (minutes == null) ? null : formatMinutes(minutes);
      }
      result.addRow(attRow.getId(), values);
    }
    return result;
  }

  private ResponseObject getCalendarAppointments(RequestInfo reqInfo) {
    long calendarId = BeeUtils.toLong(reqInfo.getParameter(PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, "parameter not found");
    }

    Filter calFilter = ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId));

    BeeRowSet calAttTypes = qs.getViewData(VIEW_CAL_ATTENDEE_TYPES, calFilter);
    BeeRowSet calAttendees = qs.getViewData(VIEW_CALENDAR_ATTENDEES, calFilter);

    BeeRowSet calAppTypes = qs.getViewData(VIEW_CAL_APPOINTMENT_TYPES, calFilter);
    BeeRowSet calPersons = qs.getViewData(VIEW_CALENDAR_PERSONS, calFilter);

    CompoundFilter attFilter = Filter.or();
    if (!calAttTypes.isEmpty()) {
      attFilter.add(Filter.in(COL_ATTENDEE_TYPE,
          DataUtils.getDistinct(calAttTypes, COL_ATTENDEE_TYPE)));
    }
    if (!calAttendees.isEmpty()) {
      attFilter.add(Filter.idIn(DataUtils.getDistinct(calAttendees, COL_ATTENDEE)));
    }

    BeeRowSet attendees = qs.getViewData(VIEW_ATTENDEES, attFilter);

    CompoundFilter appFilter = Filter.and();
    appFilter.add(ComparisonFilter.isNotEqual(COL_STATUS,
        new IntegerValue(AppointmentStatus.CANCELED.ordinal())));

    if (!calAppTypes.isEmpty()) {
      appFilter.add(Filter.in(COL_APPOINTMENT_TYPE,
          DataUtils.getDistinct(calAppTypes, COL_APPOINTMENT_TYPE)));
    }
    if (!calPersons.isEmpty()) {
      appFilter.add(Filter.in(COL_ORGANIZER,
          DataUtils.getDistinct(calPersons, COL_COMPANY_PERSON)));
    }

    Set<Long> attIds = Sets.newHashSet();
    if (!attFilter.isEmpty()) {
      for (BeeRow row : attendees.getRows()) {
        attIds.add(row.getId());
      }
    }

    BeeRowSet appointments = getAppointments(appFilter, null, attIds);

    if (!attendees.isEmpty()) {
      appointments.setTableProperty(VIEW_ATTENDEES, DataUtils.buildList(attendees));
    }

    LogUtils.infoNow(logger, SVC_GET_CALENDAR_APPOINTMENTS, appointments.getNumberOfRows(),
        appointments.getViewName(), attendees.getNumberOfRows(), attendees.getViewName());

    return ResponseObject.response(appointments);
  }

  private ResponseObject getOverlappingAppointments(RequestInfo reqInfo) {
    String svc = SVC_GET_OVERLAPPING_APPOINTMENTS;

    String appId = reqInfo.getParameter(PARAM_APPOINTMENT_ID);

    String start = reqInfo.getParameter(PARAM_APPOINTMENT_START);
    if (!BeeUtils.isLong(start)) {
      return ResponseObject.error(svc, PARAM_APPOINTMENT_START, "parameter not found");
    }
    String end = reqInfo.getParameter(PARAM_APPOINTMENT_END);
    if (!BeeUtils.isLong(end)) {
      return ResponseObject.error(svc, PARAM_APPOINTMENT_END, "parameter not found");
    }

    String attIds = reqInfo.getParameter(PARAM_ATTENDEES);
    if (BeeUtils.isEmpty(attIds)) {
      return ResponseObject.error(svc, PARAM_ATTENDEES, "parameter not found");
    }

    CompoundFilter filter = Filter.and();
    filter.add(ComparisonFilter.isNotEqual(COL_STATUS,
        new IntegerValue(AppointmentStatus.CANCELED.ordinal())));

    if (BeeUtils.isLong(appId)) {
      filter.add(ComparisonFilter.compareId(Operator.NE, BeeUtils.toLong(appId)));
    }

    filter.add(ComparisonFilter.isMore(COL_END_DATE_TIME, new LongValue(BeeUtils.toLong(start))));
    filter.add(ComparisonFilter.isLess(COL_START_DATE_TIME, new LongValue(BeeUtils.toLong(end))));

    BeeRowSet appointments = qs.getViewData(VIEW_APPOINTMENTS, filter);
    if (appointments.isEmpty()) {
      return ResponseObject.response(appointments);
    }

    Filter in = Filter.in(COL_APPOINTMENT, DataUtils.getRowIds(appointments));

    BeeRowSet appAtts = qs.getViewData(VIEW_APPOINTMENT_ATTENDEES, in);
    BeeRowSet appProps = qs.getViewData(VIEW_APPOINTMENT_PROPS, in);
    BeeRowSet appRemind = qs.getViewData(VIEW_APPOINTMENT_REMINDERS, in);

    List<Long> resources = DataUtils.parseList(attIds);

    List<BeeRow> children;
    int attIndex = appAtts.getColumnIndex(COL_ATTENDEE);
    int propIndex = appProps.getColumnIndex(COL_PROPERTY);
    int remindIndex = appRemind.getColumnIndex(COL_REMINDER_TYPE);

    Iterator<BeeRow> iterator = appointments.getRows().iterator();
    while (iterator.hasNext()) {
      BeeRow row = iterator.next();
      String id = BeeUtils.toString(row.getId());

      children = DataUtils.filterRows(appAtts, COL_APPOINTMENT, id);

      boolean ok = false;
      for (BeeRow r : children) {
        if (resources.contains(r.getLong(attIndex))) {
          ok = true;
          break;
        }
      }
      if (!ok) {
        iterator.remove();
        continue;
      }

      row.setProperty(VIEW_APPOINTMENT_ATTENDEES,
          DataUtils.buildList(DataUtils.getDistinct(children, attIndex)));

      children = DataUtils.filterRows(appProps, COL_APPOINTMENT, id);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_PROPS,
            DataUtils.buildList(DataUtils.getDistinct(children, propIndex)));
      }

      children = DataUtils.filterRows(appRemind, COL_APPOINTMENT, id);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_REMINDERS,
            DataUtils.buildList(DataUtils.getDistinct(children, remindIndex)));
      }
    }

    LogUtils.infoNow(logger, svc, appointments.getNumberOfRows(), appointments.getViewName());
    return ResponseObject.response(appointments);
  }

  private ResponseObject getUserCalendar(RequestInfo reqInfo) {
    if (!checkTable(TBL_USER_CALENDARS)) {
      return ResponseObject.error("table not active:", TBL_USER_CALENDARS);
    }

    long calendarId = BeeUtils.toLong(reqInfo.getParameter(PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, "parameter not found");
    }

    long userId = usr.getCurrentUserId();

    Filter filter = Filter.and(ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId)),
        ComparisonFilter.isEqual(COL_USER, new LongValue(userId)));

    BeeRowSet ucRowSet = qs.getViewData(VIEW_USER_CALENDARS, filter);
    if (!ucRowSet.isEmpty()) {
      return ResponseObject.response(ucRowSet);
    }

    BeeRowSet calRowSet = qs.getViewData(VIEW_CALENDARS, ComparisonFilter.compareId(calendarId));
    if (calRowSet.isEmpty()) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, calendarId,
          "calendar not found");
    }

    CalendarSettings settings = CalendarSettings.create(calRowSet.getRow(0),
        calRowSet.getColumns());

    SqlInsert sqlInsert = new SqlInsert(TBL_USER_CALENDARS)
        .addConstant(COL_CALENDAR, calendarId)
        .addConstant(COL_USER, userId)
        .addConstant(COL_PIXELS_PER_INTERVAL, settings.getPixelsPerInterval())
        .addConstant(COL_INTERVALS_PER_HOUR, settings.getIntervalsPerHour())
        .addConstant(COL_WORKING_HOUR_START, settings.getWorkingHourStart())
        .addConstant(COL_WORKING_HOUR_END, settings.getWorkingHourEnd())
        .addConstant(COL_SCROLL_TO_HOUR, settings.getScrollToHour())
        .addConstant(COL_DEFAULT_DISPLAYED_DAYS, settings.getDefaultDisplayedDays());

    if (settings.getTimeBlockClickNumber() != null) {
      sqlInsert.addConstant(COL_TIME_BLOCK_CLICK_NUMBER,
          settings.getTimeBlockClickNumber().ordinal());
    }

    for (View view : View.values()) {
      if (settings.isVisible(view)) {
        sqlInsert.addConstant(view.getColumnId(), true);
      }
    }

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return response;
    }

    BeeRowSet result = qs.getViewData(VIEW_USER_CALENDARS, filter);
    if (result.isEmpty()) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, calendarId,
          "user calendar not created");
    }
    return ResponseObject.response(result);
  }

  private void insertAppointmentAttendee(long appId, long attId) {
    qs.insertData(new SqlInsert(TBL_APPOINTMENT_ATTENDEES).addConstant(COL_APPOINTMENT, appId)
        .addConstant(COL_ATTENDEE, attId));
  }

  private void insertAppointmentProperty(long appId, long propId) {
    qs.insertData(new SqlInsert(TBL_APPOINTMENT_PROPS).addConstant(COL_APPOINTMENT, appId)
        .addConstant(COL_PROPERTY, propId));
  }

  private void insertAppointmentReminder(long appId, long rtId) {
    qs.insertData(new SqlInsert(TBL_APPOINTMENT_REMINDERS).addConstant(COL_APPOINTMENT, appId)
        .addConstant(COL_REMINDER_TYPE, rtId));
  }

  private ResponseObject saveActiveView(RequestInfo reqInfo) {
    Long rowId = BeeUtils.toLongOrNull(reqInfo.getParameter(PARAM_USER_CALENDAR_ID));
    if (!DataUtils.isId(rowId)) {
      return ResponseObject.error(SVC_SAVE_ACTIVE_VIEW, PARAM_USER_CALENDAR_ID,
          "parameter not found");
    }

    Integer activeView = BeeUtils.toIntOrNull(reqInfo.getParameter(PARAM_ACTIVE_VIEW));
    if (!BeeUtils.isOrdinal(View.class, activeView)) {
      return ResponseObject.error(SVC_SAVE_ACTIVE_VIEW, PARAM_ACTIVE_VIEW, "parameter not found");
    }

    SqlUpdate update = new SqlUpdate(TBL_USER_CALENDARS).addConstant(COL_ACTIVE_VIEW, activeView)
        .setWhere(SqlUtils.equal(TBL_USER_CALENDARS, sys.getIdName(TBL_USER_CALENDARS), rowId));

    return qs.updateDataWithResponse(update);
  }

  private Map<Integer, Integer> splitByHour(DateTime start, DateTime end) {
    Map<Integer, Integer> result = Maps.newHashMap();
    if (start == null || end == null) {
      return result;
    }

    DateTime dt = DateTime.copyOf(start);
    while (TimeUtils.isLess(dt, end)) {
      int hour = dt.getHour();
      DateTime next = TimeUtils.nextHour(dt, 0);
      
      int value = BeeUtils.unbox(result.get(hour));

      if (TimeUtils.isLess(next, end)) {
        result.put(hour, TimeUtils.minuteDiff(dt, next) + value);
        dt.setTime(next.getTime());
      } else {
        result.put(hour, TimeUtils.minuteDiff(dt, end) + value);
        break;
      }
    }
    return result;
  }
  
  private Map<YearMonth, Integer> splitByYearMonth(DateTime start, DateTime end) {
    Map<YearMonth, Integer> result = Maps.newHashMap();
    if (start == null || end == null) {
      return result;
    }

    if (TimeUtils.sameMonth(start, end)) {
      result.put(new YearMonth(start), TimeUtils.minuteDiff(start, end));
      return result;
    }

    DateTime dt = DateTime.copyOf(start);
    while (TimeUtils.isLess(dt, end)) {
      YearMonth ym = new YearMonth(dt);
      DateTime next = TimeUtils.startOfNextMonth(dt).getDateTime();

      if (TimeUtils.isLess(next, end)) {
        result.put(ym, TimeUtils.minuteDiff(dt, next));
        dt.setTime(next.getTime());
      } else {
        result.put(ym, TimeUtils.minuteDiff(dt, end));
        break;
      }
    }
    return result;
  }

  private ResponseObject updateAppointment(RequestInfo reqInfo) {
    BeeRowSet newRowSet = BeeRowSet.restore(reqInfo.getContent());
    if (newRowSet.isEmpty()) {
      return ResponseObject.error(SVC_UPDATE_APPOINTMENT, ": rowSet is empty");
    }

    long appId = newRowSet.getRow(0).getId();
    if (!DataUtils.isId(appId)) {
      return ResponseObject.error(SVC_UPDATE_APPOINTMENT, ": invalid row id", appId);
    }

    String propIds = newRowSet.getTableProperty(COL_PROPERTY);
    String attIds = newRowSet.getTableProperty(COL_ATTENDEE);
    String rtIds = newRowSet.getTableProperty(COL_REMINDER_TYPE);

    String viewName = VIEW_APPOINTMENTS;
    BeeRowSet oldRowSet = qs.getViewData(viewName, ComparisonFilter.compareId(appId));
    if (oldRowSet == null || oldRowSet.isEmpty()) {
      return ResponseObject.error(SVC_UPDATE_APPOINTMENT, ": old row not found", appId);
    }

    BeeRowSet updated = DataUtils.getUpdated(viewName, oldRowSet.getColumns(), oldRowSet.getRow(0),
        newRowSet.getRow(0));

    ResponseObject response;
    if (updated == null) {
      response = ResponseObject.response(oldRowSet.getRow(0));
    } else {
      response = deb.commitRow(updated, true);
      if (response.hasErrors()) {
        return response;
      }
    }

    Filter appFilter = ComparisonFilter.isEqual(COL_APPOINTMENT, new LongValue(appId));

    List<Long> oldProperties =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_PROPS, appFilter), COL_PROPERTY);
    List<Long> oldAttendees =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_ATTENDEES, appFilter), COL_ATTENDEE);
    List<Long> oldReminders =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_REMINDERS, appFilter),
            COL_REMINDER_TYPE);

    List<Long> newProperties = DataUtils.parseList(propIds);
    List<Long> newAttendees = DataUtils.parseList(attIds);
    List<Long> newReminders = DataUtils.parseList(rtIds);

    updateChildren(TBL_APPOINTMENT_PROPS, COL_APPOINTMENT, appId,
        COL_PROPERTY, oldProperties, newProperties);
    updateChildren(TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT, appId,
        COL_ATTENDEE, oldAttendees, newAttendees);
    updateChildren(TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT, appId,
        COL_REMINDER_TYPE, oldReminders, newReminders);

    return response;
  }

  private void updateChildren(String tblName, String parentRelation, long parentId,
      String columnId, List<Long> oldValues, List<Long> newValues) {
    List<Long> insert = Lists.newArrayList(newValues);
    insert.removeAll(oldValues);

    List<Long> delete = Lists.newArrayList(oldValues);
    delete.removeAll(newValues);

    for (Long value : insert) {
      qs.insertData(new SqlInsert(tblName).addConstant(parentRelation, parentId)
          .addConstant(columnId, value));
    }
    for (Long value : delete) {
      IsCondition condition = SqlUtils.and(SqlUtils.equal(tblName, parentRelation, parentId),
          SqlUtils.equal(tblName, columnId, value));
      qs.updateData(new SqlDelete(tblName).setWhere(condition));
    }
  }
}
