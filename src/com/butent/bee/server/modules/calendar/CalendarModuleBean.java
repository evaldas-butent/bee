package com.butent.bee.server.modules.calendar;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewDeleteEvent;
import com.butent.bee.server.data.ViewEvent.ViewInsertEvent;
import com.butent.bee.server.data.ViewEvent.ViewModifyEvent;
import com.butent.bee.server.data.ViewEvent.ViewUpdateEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.calendar.CalendarConstants.AppointmentStatus;
import com.butent.bee.shared.modules.calendar.CalendarConstants.Report;
import com.butent.bee.shared.modules.calendar.CalendarConstants.View;
import com.butent.bee.shared.modules.calendar.CalendarConstants.Visibility;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants.ReminderMethod;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.mail.MessagingException;

@Singleton
@Lock(LockType.READ)
public class CalendarModuleBean implements BeeModule {

  private static final Filter VALID_APPOINTMENT = Filter.and(Filter.notEmpty(COL_START_DATE_TIME),
      ComparisonFilter.compareWithColumn(COL_START_DATE_TIME, Operator.LT, COL_END_DATE_TIME));

  private static BeeLogger logger = LogUtils.getLogger(CalendarModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;
  @EJB
  MailModuleBean mail;
  @EJB
  ParamHolderBean prm;
  @Resource
  TimerService timerService;

  private final Multimap<Long, Timer> notificationTimers = HashMultimap.create();

  @Lock(LockType.WRITE)
  public void createNotificationTimers(Pair<String, Long> idInfo) {
    IsCondition wh = null;
    String reminderIdName = sys.getIdName(TBL_APPOINTMENT_REMINDERS);

    Collection<Timer> timers = null;

    if (idInfo == null) {
      timers = Lists.newArrayList(notificationTimers.values());
      notificationTimers.clear();

    } else {
      String idName = idInfo.getA();
      Long id = idInfo.getB();

      if (BeeUtils.same(idName, TBL_APPOINTMENTS)) {
        timers = notificationTimers.removeAll(id);
        wh = SqlUtils.equals(TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT, id);

      } else if (BeeUtils.same(idName, TBL_APPOINTMENT_REMINDERS)) {
        Long appointmentId = null;
        Timer timer = null;

        for (Entry<Long, Timer> entry : notificationTimers.entries()) {
          timer = entry.getValue();

          try {
            if (Objects.equal(timer.getInfo(), id)) {
              appointmentId = entry.getKey();
              break;
            }
          } catch (NoSuchObjectLocalException e) {
          }
        }
        if (appointmentId != null) {
          timers = Lists.newArrayList(timer);
          notificationTimers.remove(appointmentId, timer);
        }
        wh = SqlUtils.equals(TBL_APPOINTMENT_REMINDERS, reminderIdName, id);
      }
    }
    if (!BeeUtils.isEmpty(timers)) {
      for (Timer timer : timers) {
        try {
          logger.debug("Canceled timer:", timer.getInfo());
          timer.cancel();
        } catch (NoSuchObjectLocalException e) {
        }
      }
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_APPOINTMENTS, COL_START_DATE_TIME)
        .addFields(TBL_APPOINTMENT_REMINDERS, reminderIdName, COL_APPOINTMENT,
            COL_HOURS, COL_MINUTES, COL_SCHEDULED)
        .addField(CommonsConstants.TBL_REMINDER_TYPES, COL_HOURS, "defHours")
        .addField(CommonsConstants.TBL_REMINDER_TYPES, COL_MINUTES, "defMinutes")
        .addFrom(TBL_APPOINTMENTS)
        .addFromInner(TBL_APPOINTMENT_REMINDERS,
            sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT))
        .addFromInner(CommonsConstants.TBL_REMINDER_TYPES,
            sys.joinTables(CommonsConstants.TBL_REMINDER_TYPES, TBL_APPOINTMENT_REMINDERS,
                COL_REMINDER_TYPE))
        .setWhere(SqlUtils.and(wh,
            SqlUtils.more(TBL_APPOINTMENTS, COL_START_DATE_TIME, System.currentTimeMillis()),
            SqlUtils.notEqual(TBL_APPOINTMENTS, COL_STATUS,
                AppointmentStatus.CANCELED.ordinal()))));

    for (SimpleRow row : data) {
      Long start = row.getLong(COL_SCHEDULED);

      if (start == null) {
        long offset = BeeUtils.unbox(row.getInt(COL_HOURS)) * TimeUtils.MILLIS_PER_HOUR
            + BeeUtils.unbox(row.getInt(COL_MINUTES)) * TimeUtils.MILLIS_PER_MINUTE;

        if (offset == 0) {
          offset = BeeUtils.unbox(row.getInt("defHours")) * TimeUtils.MILLIS_PER_HOUR
              + BeeUtils.unbox(row.getInt("defMinutes")) * TimeUtils.MILLIS_PER_MINUTE;
        }
        if (offset != 0) {
          start = BeeUtils.unbox(row.getLong(COL_START_DATE_TIME)) - offset;
        }
      }
      if (start != null) {
        DateTime time = TimeUtils.toDateTimeOrNull(start);
        int from = prm.getTime(CALENDAR_MODULE, PRM_REMINDER_TIME_FROM);
        int until = prm.getTime(CALENDAR_MODULE, PRM_REMINDER_TIME_UNTIL);

        if (from < until) {
          int current = TimeUtils.minutesSinceDayStarted(time) * TimeUtils.MILLIS_PER_MINUTE;

          if (!BeeUtils.betweenInclusive(current, from, until)) {
            time = new DateTime(((current < from) ? TimeUtils.previousDay(time) : time.getDate()));
            time.setTime(time.getTime() + until);
          }
        }
        if (time.getTime() > System.currentTimeMillis()) {
          notificationTimers.put(row.getLong(COL_APPOINTMENT),
              timerService.createSingleActionTimer(time.getJava(),
                  new TimerConfig(row.getLong(reminderIdName), false)));

          logger.debug("Created timer:", time, row.getValue(reminderIdName));
        }
      }
    }
  }

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE, MailConstants.MAIL_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> results = Lists.newArrayList();

    Filter filter = Filter.or(
        Filter.anyContains(Sets.newHashSet(COL_SUMMARY, COL_DESCRIPTION,
            COL_ORGANIZER_FIRST_NAME, COL_ORGANIZER_LAST_NAME,
            COL_COMPANY_NAME, COL_COMPANY_EMAIL,
            COL_VEHICLE_NUMBER, COL_VEHICLE_PARENT_MODEL, COL_VEHICLE_MODEL), query),
        DataUtils.anyItemContains(COL_STATUS, AppointmentStatus.class, query));

    BeeRowSet appointments = getAppointments(filter, new Order(COL_START_DATE_TIME, false));
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
    } else if (BeeUtils.same(svc, SVC_GET_REPORT_OPTIONS)) {
      response = getReportOptions(reqInfo);
    } else if (BeeUtils.same(svc, SVC_DO_REPORT)) {
      response = doReport(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Calendar service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return Lists.newArrayList(
        new BeeParameter(CALENDAR_MODULE, PRM_REMINDER_TIME_FROM, ParameterType.TIME,
            "Kalendoriaus anksčiausias priminimų laikas", false, "8:00"),
        new BeeParameter(CALENDAR_MODULE, PRM_REMINDER_TIME_UNTIL, ParameterType.TIME,
            "Kalendoriaus vėliausias priminimų laikas", false, "18:00"));
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
    createNotificationTimers(null);

    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void updateTimers(ViewModifyEvent event) {
        if (event.isAfter()) {
          if (BeeUtils.same(event.getViewName(), CommonsConstants.TBL_REMINDER_TYPES)) {
            if (event instanceof ViewDeleteEvent
                || event instanceof ViewUpdateEvent
                && (DataUtils.contains(((ViewUpdateEvent) event).getColumns(), COL_HOURS)
                || DataUtils.contains(((ViewUpdateEvent) event).getColumns(), COL_MINUTES))) {

              createNotificationTimers(null);
            }
          } else if (BeeUtils.same(event.getViewName(), TBL_APPOINTMENTS)) {
            if (event instanceof ViewDeleteEvent) {
              for (long id : ((ViewDeleteEvent) event).getIds()) {
                createNotificationTimers(Pair.of(TBL_APPOINTMENTS, id));
              }
            } else if (event instanceof ViewUpdateEvent) {
              ViewUpdateEvent ev = ((ViewUpdateEvent) event);

              if (DataUtils.contains(ev.getColumns(), COL_STATUS)
                  || DataUtils.contains(ev.getColumns(), COL_START_DATE_TIME)) {

                createNotificationTimers(Pair.of(TBL_APPOINTMENTS, ev.getRow().getId()));
              }
            }
          } else if (BeeUtils.same(event.getViewName(), TBL_APPOINTMENT_REMINDERS)) {
            if (event instanceof ViewDeleteEvent) {
              for (long id : ((ViewDeleteEvent) event).getIds()) {
                createNotificationTimers(Pair.of(TBL_APPOINTMENT_REMINDERS, id));
              }
            } else if (event instanceof ViewUpdateEvent) {
              ViewUpdateEvent ev = ((ViewUpdateEvent) event);

              if (DataUtils.contains(ev.getColumns(), COL_REMINDER_TYPE)
                  || DataUtils.contains(ev.getColumns(), COL_HOURS)
                  || DataUtils.contains(ev.getColumns(), COL_MINUTES)
                  || DataUtils.contains(ev.getColumns(), COL_SCHEDULED)) {

                createNotificationTimers(Pair.of(TBL_APPOINTMENT_REMINDERS, ev.getRow().getId()));
              }
            } else if (event instanceof ViewInsertEvent) {
              createNotificationTimers(Pair.of(TBL_APPOINTMENT_REMINDERS,
                  ((ViewInsertEvent) event).getRow().getId()));
            }
          }
        }
      }
    });
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
      for (long propId : DataUtils.parseIdList(propIds)) {
        insertAppointmentProperty(appId, propId);
      }
    }

    if (!BeeUtils.isEmpty(attIds)) {
      for (long attId : DataUtils.parseIdList(attIds)) {
        insertAppointmentAttendee(appId, attId);
      }
    }

    if (!BeeUtils.isEmpty(rtIds)) {
      for (long rtId : DataUtils.parseIdList(rtIds)) {
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

    BeeRowSet rowSet = BeeRowSet.restore(reqInfo.getContent());
    if (rowSet.isEmpty()) {
      return ResponseObject.error(SVC_DO_REPORT, ": options rowSet is empty");
    }

    BeeRow row = rowSet.getRow(0);

    JustDate lowerDate = DataUtils.getDate(rowSet, row, COL_LOWER_DATE);
    JustDate upperDate = DataUtils.getDate(rowSet, row, COL_UPPER_DATE);

    Integer lowerHour = DataUtils.getInteger(rowSet, row, COL_LOWER_HOUR);
    Integer upperHour = DataUtils.getInteger(rowSet, row, COL_UPPER_HOUR);

    String atpList = DataUtils.getString(rowSet, row, COL_ATTENDEE_TYPES);
    String attList = DataUtils.getString(rowSet, row, COL_ATTENDEES);

    SqlUpdate update = new SqlUpdate(TBL_REPORT_OPTIONS)
        .addConstant(sys.getVersionName(TBL_REPORT_OPTIONS), System.currentTimeMillis())
        .addConstant(COL_CAPTION, DataUtils.getString(rowSet, row, COL_CAPTION))
        .addConstant(COL_LOWER_DATE, lowerDate)
        .addConstant(COL_UPPER_DATE, upperDate)
        .addConstant(COL_LOWER_HOUR, lowerHour)
        .addConstant(COL_UPPER_HOUR, upperHour)
        .addConstant(COL_ATTENDEE_TYPES, atpList)
        .addConstant(COL_ATTENDEES, attList)
        .setWhere(sys.idEquals(TBL_REPORT_OPTIONS, row.getId()));

    ResponseObject response = qs.updateDataWithResponse(update);
    if (response.hasErrors()) {
      return response;
    }

    List<Long> attendeeTypes = DataUtils.parseIdList(atpList);
    List<Long> attendees = DataUtils.parseIdList(attList);

    Report report = Report.values()[paramRep];
    BeeRowSet result;

    switch (report) {
      case BUSY_HOURS:
        result = getAttendeesByHour(lowerDate, upperDate, lowerHour, upperHour,
            attendeeTypes, attendees, false);
        break;
      case BUSY_MONTHS:
        result = getAttendeesByMonth(lowerDate, upperDate, attendeeTypes, attendees, false);
        break;
      case CANCEL_HOURS:
        result = getAttendeesByHour(lowerDate, upperDate, lowerHour, upperHour,
            attendeeTypes, attendees, true);
        break;
      case CANCEL_MONTHS:
        result = getAttendeesByMonth(lowerDate, upperDate, attendeeTypes, attendees, true);
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

  private void formatTimeColumns(BeeRowSet rowSet, int colFrom, int colTo) {
    for (BeeRow row : rowSet.getRows()) {
      for (int c = colFrom; c <= colTo; c++) {
        Integer value = row.getInteger(c);
        if (value != null) {
          row.setValue(c, formatMinutes(value));
        }
      }
    }
  }

  private BeeRowSet getAppointments(Filter filter, Order order) {
    long userId = usr.getCurrentUserId();

    Filter visible = Filter.or().add(
        ComparisonFilter.isEqual(COL_CREATOR, new LongValue(userId)),
        Filter.isEmpty(COL_CREATOR),
        Filter.isEmpty(COL_VISIBILITY),
        ComparisonFilter.isNotEqual(COL_VISIBILITY,
            new IntegerValue(Visibility.PRIVATE.ordinal())));

    BeeRowSet appointments = qs.getViewData(VIEW_APPOINTMENTS, Filter.and(filter, visible), order);
    if (appointments == null || appointments.isEmpty()) {
      return appointments;
    }

    BeeRowSet appAtts = qs.getViewData(VIEW_APPOINTMENT_ATTENDEES);
    BeeRowSet appProps = qs.getViewData(VIEW_APPOINTMENT_PROPS);
    BeeRowSet appRemind = qs.getViewData(VIEW_APPOINTMENT_REMINDERS);

    int aaIndex = appAtts.getColumnIndex(COL_ATTENDEE);
    int apIndex = appProps.getColumnIndex(COL_PROPERTY);
    int arIndex = appRemind.getColumnIndex(COL_REMINDER_TYPE);

    List<BeeRow> children;
    Iterator<BeeRow> iterator = appointments.getRows().iterator();

    while (iterator.hasNext()) {
      BeeRow row = iterator.next();
      String appId = BeeUtils.toString(row.getId());

      children = DataUtils.filterRows(appAtts, COL_APPOINTMENT, appId);

      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_ATTENDEES,
            DataUtils.buildIdList(DataUtils.getDistinct(children, aaIndex)));
      }

      children = DataUtils.filterRows(appProps, COL_APPOINTMENT, appId);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_PROPS,
            DataUtils.buildIdList(DataUtils.getDistinct(children, apIndex)));
      }

      children = DataUtils.filterRows(appRemind, COL_APPOINTMENT, appId);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_REMINDERS,
            DataUtils.buildIdList(DataUtils.getDistinct(children, arIndex)));
      }
    }

    return appointments;
  }

  private Filter getAttendeeFilter(List<Long> attendeeTypes, List<Long> attendees) {
    if (!BeeUtils.isEmpty(attendeeTypes) || !BeeUtils.isEmpty(attendees)) {
      return Filter.or(Filter.any(COL_ATTENDEE_TYPE, attendeeTypes),
          Filter.idIn(attendees));
    } else if (!BeeUtils.isEmpty(attendeeTypes)) {
      return Filter.any(COL_ATTENDEE_TYPE, attendeeTypes);
    } else if (!BeeUtils.isEmpty(attendees)) {
      return Filter.idIn(attendees);
    } else {
      return null;
    }
  }

  private SimpleRowSet getAttendeePeriods(JustDate lowerDate, JustDate upperDate,
      List<Long> attendeeTypes, List<Long> attendees, boolean canceled) {

    SqlSelect ss = new SqlSelect()
        .addFields(TBL_APPOINTMENTS, COL_START_DATE_TIME, COL_END_DATE_TIME)
        .addFields(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE)
        .addFrom(TBL_APPOINTMENTS)
        .addFromInner(TBL_APPOINTMENT_ATTENDEES,
            sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT));

    int statusCanceled = AppointmentStatus.CANCELED.ordinal();
    IsCondition statusCondition = canceled
        ? SqlUtils.equals(TBL_APPOINTMENTS, COL_STATUS, statusCanceled)
        : SqlUtils.notEqual(TBL_APPOINTMENTS, COL_STATUS, statusCanceled);

    HasConditions where = SqlUtils.and(statusCondition);

    if (!BeeUtils.isEmpty(attendeeTypes) || !BeeUtils.isEmpty(attendees)) {
      HasConditions attCondition = SqlUtils.or();

      if (!BeeUtils.isEmpty(attendeeTypes)) {
        ss.addFromInner(TBL_ATTENDEES,
            sys.joinTables(TBL_ATTENDEES, TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE));

        for (Long atpId : attendeeTypes) {
          attCondition.add(SqlUtils.equals(TBL_ATTENDEES, COL_ATTENDEE_TYPE, atpId));
        }
      }

      if (!BeeUtils.isEmpty(attendees)) {
        for (Long attId : attendees) {
          attCondition.add(SqlUtils.equals(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE, attId));
        }
      }

      where.add(attCondition);
    }

    if (lowerDate != null) {
      where.add(SqlUtils.more(TBL_APPOINTMENTS, COL_END_DATE_TIME, lowerDate));
    }
    if (upperDate != null) {
      where.add(SqlUtils.less(TBL_APPOINTMENTS, COL_START_DATE_TIME, upperDate));
    }

    ss.setWhere(where);

    return qs.getData(ss);
  }

  private BeeRowSet getAttendeesByHour(JustDate lowerDate, JustDate upperDate,
      Integer lowerHour, Integer upperHour, List<Long> attendeeTypes, List<Long> attendees,
      boolean canceled) {

    SimpleRowSet data =
        getAttendeePeriods(lowerDate, upperDate, attendeeTypes, attendees, canceled);
    if (data == null || data.getNumberOfRows() <= 0) {
      return null;
    }

    int startIndex = data.getColumnIndex(COL_START_DATE_TIME);
    int endIndex = data.getColumnIndex(COL_END_DATE_TIME);
    int attIndex = data.getColumnIndex(COL_ATTENDEE);

    Table<Long, Integer, Integer> table = HashBasedTable.create();

    Range<DateTime> dateRange = getDateRange(lowerDate, upperDate);
    Range<Integer> hourRange = getHourRange(lowerHour, upperHour);

    for (int r = 0; r < data.getNumberOfRows(); r++) {
      DateTime start = data.getDateTime(r, startIndex);
      DateTime end = data.getDateTime(r, endIndex);

      Long attendee = data.getLong(r, attIndex);
      Map<Integer, Integer> map = splitByHour(start, end, dateRange, hourRange);

      for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
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

    BeeRowSet attRowSet = qs.getViewData(VIEW_ATTENDEES,
        getAttendeeFilter(attendeeTypes, attendees));
    if (attRowSet.isEmpty()) {
      return null;
    }

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, COL_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_NAME));
    for (Integer hour : hours) {
      result.addColumn(ValueType.TEXT, hour.toString());
    }

    if (hours.size() > 1) {
      result.addColumn(ValueType.TEXT, "Viso");
    }
    int columnCount = result.getNumberOfColumns();

    for (BeeRow attRow : attRowSet.getRows()) {
      Map<Integer, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[result.getNumberOfColumns()];
      values[0] = DataUtils.getString(attRowSet, attRow, COL_TYPE_NAME);
      values[1] = DataUtils.getString(attRowSet, attRow, COL_NAME);

      for (int c = 0; c < hours.size(); c++) {
        Integer minutes = tableRow.get(hours.get(c));
        values[c + 2] = (minutes == null) ? null : minutes.toString();
      }
      result.addRow(attRow.getId(), values);
    }

    if (hours.size() > 1) {
      totalColumns(result, 2, columnCount - 2, columnCount - 1);
    }
    if (result.getNumberOfRows() > 1) {
      totalRows(result, 2, columnCount - 1, 0, "Iš viso:", 0);
    }
    formatTimeColumns(result, 2, columnCount - 1);

    return result;
  }

  private BeeRowSet getAttendeesByMonth(JustDate lowerDate, JustDate upperDate,
      List<Long> attendeeTypes, List<Long> attendees, boolean canceled) {

    SimpleRowSet data =
        getAttendeePeriods(lowerDate, upperDate, attendeeTypes, attendees, canceled);
    if (data == null || data.getNumberOfRows() <= 0) {
      return null;
    }

    int startIndex = data.getColumnIndex(COL_START_DATE_TIME);
    int endIndex = data.getColumnIndex(COL_END_DATE_TIME);
    int attIndex = data.getColumnIndex(COL_ATTENDEE);

    Table<Long, YearMonth, Integer> table = HashBasedTable.create();

    Range<DateTime> range = getDateRange(lowerDate, upperDate);

    for (int r = 0; r < data.getNumberOfRows(); r++) {
      DateTime start = data.getDateTime(r, startIndex);
      DateTime end = data.getDateTime(r, endIndex);

      Long attendee = data.getLong(r, attIndex);

      for (Map.Entry<YearMonth, Integer> entry : splitByYearMonth(start, end, range).entrySet()) {
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

    BeeRowSet attRowSet = qs.getViewData(VIEW_ATTENDEES,
        getAttendeeFilter(attendeeTypes, attendees));
    if (attRowSet.isEmpty()) {
      return null;
    }

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, COL_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_NAME));
    for (YearMonth ym : months) {
      result.addColumn(ValueType.TEXT, ym.toString());
    }

    if (months.size() > 1) {
      result.addColumn(ValueType.TEXT, "Viso");
    }
    int columnCount = result.getNumberOfColumns();

    for (BeeRow attRow : attRowSet.getRows()) {
      Map<YearMonth, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[columnCount];
      values[0] = DataUtils.getString(attRowSet, attRow, COL_TYPE_NAME);
      values[1] = DataUtils.getString(attRowSet, attRow, COL_NAME);

      for (int c = 0; c < months.size(); c++) {
        Integer minutes = tableRow.get(months.get(c));
        values[c + 2] = (minutes == null) ? null : minutes.toString();
      }
      result.addRow(attRow.getId(), values);
    }

    if (months.size() > 1) {
      totalColumns(result, 2, columnCount - 2, columnCount - 1);
    }
    if (result.getNumberOfRows() > 1) {
      totalRows(result, 2, columnCount - 1, 0, "Iš viso:", 0);
    }
    formatTimeColumns(result, 2, columnCount - 1);

    return result;
  }

  private ResponseObject getCalendarAppointments(RequestInfo reqInfo) {
    long calendarId = BeeUtils.toLong(reqInfo.getParameter(PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, "parameter not found");
    }

    Filter calFilter = ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId));

    BeeRowSet calAppTypes = qs.getViewData(VIEW_CAL_APPOINTMENT_TYPES, calFilter);
    BeeRowSet calPersons = qs.getViewData(VIEW_CALENDAR_PERSONS, calFilter);

    CompoundFilter appFilter = Filter.and();
    appFilter.add(VALID_APPOINTMENT);
    appFilter.add(ComparisonFilter.isNotEqual(COL_STATUS,
        new IntegerValue(AppointmentStatus.CANCELED.ordinal())));

    if (!calAppTypes.isEmpty()) {
      appFilter.add(Filter.any(COL_APPOINTMENT_TYPE,
          DataUtils.getDistinct(calAppTypes, COL_APPOINTMENT_TYPE)));
    }
    if (!calPersons.isEmpty()) {
      appFilter.add(Filter.any(COL_ORGANIZER,
          DataUtils.getDistinct(calPersons, COL_COMPANY_PERSON)));
    }

    BeeRowSet appointments = getAppointments(appFilter, null);

    logger.info(SVC_GET_CALENDAR_APPOINTMENTS, appointments.getNumberOfRows(),
        appointments.getViewName());

    return ResponseObject.response(appointments);
  }

  private BeeRowSet getCalendarAttendees(long userId, long calendarId) {
    Filter calFilter = ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId));

    BeeRowSet calAttTypes = qs.getViewData(VIEW_CAL_ATTENDEE_TYPES, calFilter);
    BeeRowSet calAttendees = qs.getViewData(VIEW_CALENDAR_ATTENDEES, calFilter);

    CompoundFilter attFilter = Filter.or();
    if (!calAttTypes.isEmpty()) {
      attFilter.add(Filter.any(COL_ATTENDEE_TYPE,
          DataUtils.getDistinct(calAttTypes, COL_ATTENDEE_TYPE)));
    }
    if (!calAttendees.isEmpty()) {
      attFilter.add(Filter.idIn(DataUtils.getDistinct(calAttendees, COL_ATTENDEE)));
    }

    if (attFilter.isEmpty()) {
      String tblUsers = UserServiceBean.TBL_USERS;
      Long cp = qs.getLong(new SqlSelect()
          .addFields(tblUsers, UserServiceBean.FLD_COMPANY_PERSON)
          .addFrom(tblUsers)
          .setWhere(sys.idEquals(tblUsers, userId)));

      if (cp != null) {
        Filter cpFilter = ComparisonFilter.isEqual(COL_COMPANY_PERSON, new LongValue(cp));
        BeeRowSet attendees = qs.getViewData(VIEW_ATTENDEES, cpFilter);
        if (!attendees.isEmpty()) {
          return attendees;
        }
      }
    }

    return qs.getViewData(VIEW_ATTENDEES, attFilter);
  }

  private Range<DateTime> getDateRange(JustDate lower, JustDate upper) {
    if (lower != null && upper != null) {
      return Ranges.closedOpen(lower.getDateTime(), upper.getDateTime());
    } else if (lower != null) {
      return Ranges.atLeast(lower.getDateTime());
    } else if (upper != null) {
      return Ranges.lessThan(upper.getDateTime());
    } else {
      return Ranges.all();
    }
  }

  private Range<Integer> getHourRange(Integer lower, Integer upper) {
    if (BeeUtils.isPositive(lower) && BeeUtils.isPositive(upper)) {
      return Ranges.closedOpen(lower, upper);
    } else if (BeeUtils.isPositive(lower)) {
      return Ranges.atLeast(lower);
    } else if (BeeUtils.isPositive(upper)) {
      return Ranges.lessThan(upper);
    } else {
      return Ranges.all();
    }
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

    Filter in = Filter.any(COL_APPOINTMENT, DataUtils.getRowIds(appointments));

    BeeRowSet appAtts = qs.getViewData(VIEW_APPOINTMENT_ATTENDEES, in);
    BeeRowSet appProps = qs.getViewData(VIEW_APPOINTMENT_PROPS, in);
    BeeRowSet appRemind = qs.getViewData(VIEW_APPOINTMENT_REMINDERS, in);

    List<Long> resources = DataUtils.parseIdList(attIds);

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
          DataUtils.buildIdList(DataUtils.getDistinct(children, attIndex)));

      children = DataUtils.filterRows(appProps, COL_APPOINTMENT, id);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_PROPS,
            DataUtils.buildIdList(DataUtils.getDistinct(children, propIndex)));
      }

      children = DataUtils.filterRows(appRemind, COL_APPOINTMENT, id);
      if (!children.isEmpty()) {
        row.setProperty(VIEW_APPOINTMENT_REMINDERS,
            DataUtils.buildIdList(DataUtils.getDistinct(children, remindIndex)));
      }
    }

    logger.info(svc, appointments.getNumberOfRows(), appointments.getViewName());
    return ResponseObject.response(appointments);
  }

  private ResponseObject getReportOptions(RequestInfo reqInfo) {
    Integer report = BeeUtils.toIntOrNull(reqInfo.getParameter(PARAM_REPORT));
    if (!BeeUtils.isOrdinal(Report.class, report)) {
      return ResponseObject.error(SVC_GET_REPORT_OPTIONS, PARAM_REPORT, "parameter not found");
    }

    long userId = usr.getCurrentUserId();

    Filter filter = Filter.and(ComparisonFilter.isEqual(COL_USER, new LongValue(userId)),
        ComparisonFilter.isEqual(COL_REPORT, new IntegerValue(report)));

    BeeRowSet rowSet = qs.getViewData(VIEW_REPORT_OPTIONS, filter);
    if (rowSet.getNumberOfRows() == 1) {
      return ResponseObject.response(rowSet.getRow(0));
    }

    SqlInsert sqlInsert = new SqlInsert(TBL_REPORT_OPTIONS).addConstant(COL_USER, userId)
        .addConstant(COL_REPORT, report);

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return response;
    }

    BeeRowSet result = qs.getViewData(VIEW_REPORT_OPTIONS, filter);
    if (result.getNumberOfRows() != 1) {
      return ResponseObject.error(SVC_GET_REPORT_OPTIONS, COL_USER, userId, COL_REPORT, report,
          "report options not created");
    }
    return ResponseObject.response(result.getRow(0));
  }

  private BeeRowSet getUserCalAttendees(long ucId, long userId, long calendarId, boolean isNew) {
    Filter filter = ComparisonFilter.isEqual(COL_USER_CALENDAR, new LongValue(ucId));

    if (!isNew) {
      BeeRowSet ucAttendees = qs.getViewData(VIEW_USER_CAL_ATTENDEES, filter);
      if (!ucAttendees.isEmpty()) {
        return ucAttendees;
      }
    }

    BeeRowSet calendarAttendees = getCalendarAttendees(userId, calendarId);
    if (calendarAttendees.isEmpty()) {
      logger.warning("calendar attendees not found, calendar id:", calendarId);
      return null;
    }

    int ord = 0;
    for (BeeRow row : calendarAttendees.getRows()) {
      SqlInsert sqlInsert = new SqlInsert(TBL_USER_CAL_ATTENDEES)
          .addConstant(COL_USER_CALENDAR, ucId)
          .addConstant(COL_ATTENDEE, row.getId())
          .addConstant(COL_ENABLED, true)
          .addConstant(COL_ORDINAL, ord++);

      qs.insertDataWithResponse(sqlInsert);
    }

    return qs.getViewData(VIEW_USER_CAL_ATTENDEES, filter);
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
      BeeRow row = ucRowSet.getRow(0);
      BeeRowSet ucAttendees = getUserCalAttendees(row.getId(), userId, calendarId, false);
      if (!DataUtils.isEmpty(ucAttendees)) {
        row.setProperty(PROP_USER_CAL_ATTENDEES, ucAttendees.serialize());
      }

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

    BeeRow row = result.getRow(0);
    BeeRowSet ucAttendees = getUserCalAttendees(row.getId(), userId, calendarId, true);
    if (!DataUtils.isEmpty(ucAttendees)) {
      row.setProperty(PROP_USER_CAL_ATTENDEES, ucAttendees.serialize());
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

  @Timeout
  private void notifyEvent(Timer timer) {
    long reminderId = (Long) timer.getInfo();
    logger.debug("Fired timer:", reminderId);

    IsCondition wh = sys.idEquals(TBL_APPOINTMENT_REMINDERS, reminderId);

    String personContacts = SqlUtils.uniqueName();
    String personEmail = SqlUtils.uniqueName();

    SimpleRow data = qs.getRow(new SqlSelect()
        .addFields(TBL_APPOINTMENTS, COL_START_DATE_TIME)
        .addFields(CommonsConstants.TBL_CONTACTS, CommonsConstants.COL_EMAIL)
        .addFields(TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT, COL_MESSAGE)
        .addFields(CommonsConstants.TBL_REMINDER_TYPES, CommonsConstants.COL_REMINDER_METHOD,
            CommonsConstants.COL_REMINDER_TEMPLATE_CAPTION, CommonsConstants.COL_REMINDER_TEMPLATE)
        .addField(personContacts, CommonsConstants.COL_EMAIL, personEmail)
        .addFrom(TBL_APPOINTMENTS)
        .addFromInner(TBL_APPOINTMENT_REMINDERS,
            sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT))
        .addFromInner(CommonsConstants.TBL_REMINDER_TYPES,
            sys.joinTables(CommonsConstants.TBL_REMINDER_TYPES, TBL_APPOINTMENT_REMINDERS,
                COL_REMINDER_TYPE))
        .addFromLeft(CommonsConstants.VIEW_COMPANIES,
            sys.joinTables(CommonsConstants.VIEW_COMPANIES, TBL_APPOINTMENTS, COL_COMPANY))
        .addFromLeft(CommonsConstants.TBL_CONTACTS, sys.joinTables(CommonsConstants.TBL_CONTACTS,
            CommonsConstants.VIEW_COMPANIES, CommonsConstants.COL_CONTACT))
        .addFromLeft(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, TBL_APPOINTMENT_REMINDERS,
                COL_RECIPIENT))
        .addFromLeft(CommonsConstants.TBL_CONTACTS, personContacts,
            SqlUtils.join(personContacts, sys.getIdName(CommonsConstants.TBL_CONTACTS),
                CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.COL_CONTACT))
        .setWhere(SqlUtils.and(wh,
            SqlUtils.more(TBL_APPOINTMENTS, COL_START_DATE_TIME,
                System.currentTimeMillis() - TimeUtils.MILLIS_PER_MINUTE),
            SqlUtils.notEqual(TBL_APPOINTMENTS, COL_STATUS,
                AppointmentStatus.CANCELED.ordinal()))));

    if (data != null) {
      notificationTimers.remove(data.getLong(COL_APPOINTMENT), timer);
      String error = null;
      String subject = data.getValue(CommonsConstants.COL_REMINDER_TEMPLATE_CAPTION);
      String template = BeeUtils.notEmpty(data.getValue(COL_MESSAGE),
          data.getValue(CommonsConstants.COL_REMINDER_TEMPLATE));

      if (BeeUtils.isEmpty(subject)) {
        error = "No reminder caption specified";
      }
      if (BeeUtils.isEmpty(error) && BeeUtils.isEmpty(template)) {
        error = "No reminder message specified";
      }
      if (BeeUtils.isEmpty(error)) {
        ReminderMethod method = NameUtils.getEnumByIndex(ReminderMethod.class,
            data.getInt(CommonsConstants.COL_REMINDER_METHOD));

        if (method == ReminderMethod.EMAIL) {
          Long sender = prm.getLong(MailConstants.MAIL_MODULE, "DefaultAccount");
          Long email = BeeUtils.toLongOrNull(BeeUtils.notEmpty(data.getValue(personEmail),
              data.getValue(CommonsConstants.COL_EMAIL)));

          if (!DataUtils.isId(sender)) {
            error = "No default sender specified (parameter DefaultAccount)";
          } else if (!DataUtils.isId(email)) {
            error = "No recipient email address specified";
          } else {
            try {
              mail.sendMail(mail.getAccount(sender), Sets.newHashSet(email), null, null, subject,
                  template.replace("{time}", data.getDateTime(COL_START_DATE_TIME).toString()),
                  null);
            } catch (MessagingException e) {
              error = e.toString();
            }
          }
        } else {
          error = "Unsupported reminder method: " + method;
        }
      }
      if (!BeeUtils.isEmpty(error)) {
        logger.severe(error);
      }
      qs.updateData(new SqlUpdate(TBL_APPOINTMENT_REMINDERS)
          .addConstant(COL_SENT, System.currentTimeMillis())
          .addConstant(COL_ERROR, error)
          .setWhere(wh));
    }
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
        .setWhere(sys.idEquals(TBL_USER_CALENDARS, rowId));

    return qs.updateDataWithResponse(update);
  }

  private Map<Integer, Integer> splitByHour(DateTime start, DateTime end,
      Range<DateTime> dateRange, Range<Integer> hourRange) {
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
        if (dateRange.contains(dt) && hourRange.contains(hour)) {
          result.put(hour, TimeUtils.minuteDiff(dt, next) + value);
        }
        dt.setTime(next.getTime());

      } else {
        if (dateRange.contains(dt) && hourRange.contains(hour)) {
          result.put(hour, TimeUtils.minuteDiff(dt, end) + value);
        }
        break;
      }
    }
    return result;
  }

  private Map<YearMonth, Integer> splitByYearMonth(DateTime start, DateTime end,
      Range<DateTime> range) {
    Map<YearMonth, Integer> result = Maps.newHashMap();
    if (start == null || end == null) {
      return result;
    }

    if (TimeUtils.sameMonth(start, end) && range.contains(start) && range.contains(end)) {
      result.put(new YearMonth(start), TimeUtils.minuteDiff(start, end));
      return result;
    }

    DateTime dt = DateTime.copyOf(start);
    while (TimeUtils.isLess(dt, end)) {
      YearMonth ym = new YearMonth(dt);
      DateTime next = TimeUtils.nextDay(dt).getDateTime();

      int value = BeeUtils.unbox(result.get(ym));

      if (TimeUtils.isLess(next, end)) {
        if (range.contains(dt)) {
          result.put(ym, TimeUtils.minuteDiff(dt, next) + value);
        }
        dt.setTime(next.getTime());
      } else {
        if (range.contains(dt)) {
          result.put(ym, TimeUtils.minuteDiff(dt, end) + value);
        }
        break;
      }
    }
    return result;
  }

  private void totalColumns(BeeRowSet rowSet, int colFrom, int colTo, int colTotal) {
    for (BeeRow row : rowSet.getRows()) {
      int sum = 0;
      for (int c = colFrom; c <= colTo; c++) {
        Integer value = row.getInteger(c);
        if (value != null) {
          sum += value;
        }
      }
      row.setValue(colTotal, sum);
    }
  }

  private void totalRows(BeeRowSet rowSet, int colFrom, int colTo, long rowId,
      String caption, int colCaption) {

    Integer[] totals = new Integer[colTo - colFrom + 1];
    for (int i = 0; i < totals.length; i++) {
      totals[i] = 0;
    }

    for (BeeRow row : rowSet.getRows()) {
      for (int c = colFrom; c <= colTo; c++) {
        Integer value = row.getInteger(c);
        if (value != null) {
          totals[c - colFrom] += value;
        }
      }
    }

    String[] arr = new String[rowSet.getNumberOfColumns()];
    if (!BeeUtils.isEmpty(caption)) {
      arr[colCaption] = caption;
    }
    for (int c = colFrom; c <= colTo; c++) {
      arr[c] = BeeUtils.toString(totals[c - colFrom]);
    }

    rowSet.addRow(rowId, arr);
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

    List<Long> newProperties = DataUtils.parseIdList(propIds);
    List<Long> newAttendees = DataUtils.parseIdList(attIds);
    List<Long> newReminders = DataUtils.parseIdList(rtIds);

    updateChildren(TBL_APPOINTMENT_PROPS, COL_APPOINTMENT, appId,
        COL_PROPERTY, oldProperties, newProperties);
    updateChildren(TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT, appId,
        COL_ATTENDEE, oldAttendees, newAttendees);

    if (!oldReminders.isEmpty() || !newReminders.isEmpty()) {
      updateChildren(TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT, appId,
          COL_REMINDER_TYPE, oldReminders, newReminders);

      createNotificationTimers(Pair.of(TBL_APPOINTMENTS, appId));
    }
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
      IsCondition condition = SqlUtils.equals(tblName, parentRelation, parentId, columnId, value);
      qs.updateData(new SqlDelete(tblName).setWhere(condition));
    }
  }
}
