package com.butent.bee.server.modules.calendar;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewDeleteEvent;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewUpdateEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
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
import com.butent.bee.shared.data.IsRow;
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
import com.butent.bee.shared.modules.calendar.CalendarConstants.AppointmentStatus;
import com.butent.bee.shared.modules.calendar.CalendarConstants.CalendarVisibility;
import com.butent.bee.shared.modules.calendar.CalendarConstants.Report;
import com.butent.bee.shared.modules.calendar.CalendarConstants.ViewType;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.calendar.CalendarTask;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants.ReminderMethod;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.TaskType;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.HeadlineProducer;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

@Singleton
@Lock(LockType.READ)
public class CalendarModuleBean implements BeeModule {

  private static final Filter VALID_APPOINTMENT = Filter.and(Filter.notNull(COL_START_DATE_TIME),
      ComparisonFilter.compareWithColumn(COL_START_DATE_TIME, Operator.LT, COL_END_DATE_TIME));

  private static BeeLogger logger = LogUtils.getLogger(CalendarModuleBean.class);

  private static String formatMinutes(int minutes) {
    return BeeUtils.toString(minutes / TimeUtils.MINUTES_PER_HOUR) + DateTime.TIME_FIELD_SEPARATOR
        + TimeUtils.padTwo(minutes % TimeUtils.MINUTES_PER_HOUR);
  }

  private static void formatTimeColumns(BeeRowSet rowSet, int colFrom, int colTo) {
    for (BeeRow row : rowSet.getRows()) {
      for (int c = colFrom; c <= colTo; c++) {
        Integer value = row.getInteger(c);
        if (value != null) {
          row.setValue(c, formatMinutes(value));
        }
      }
    }
  }

  private static Filter getAttendeeFilter(List<Long> attendeeTypes, List<Long> attendees) {
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

  private static Range<DateTime> getDateRange(JustDate lower, JustDate upper) {
    if (lower != null && upper != null) {
      return Range.closedOpen(lower.getDateTime(), upper.getDateTime());
    } else if (lower != null) {
      return Range.atLeast(lower.getDateTime());
    } else if (upper != null) {
      return Range.lessThan(upper.getDateTime());
    } else {
      return Range.all();
    }
  }

  private static Range<Integer> getHourRange(Integer lower, Integer upper) {
    if (BeeUtils.isPositive(lower) && BeeUtils.isPositive(upper)) {
      return Range.closedOpen(lower, upper);
    } else if (BeeUtils.isPositive(lower)) {
      return Range.atLeast(lower);
    } else if (BeeUtils.isPositive(upper)) {
      return Range.lessThan(upper);
    } else {
      return Range.all();
    }
  }

  private static Map<Integer, Integer> splitByHour(DateTime start, DateTime end,
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

  private static Map<YearMonth, Integer> splitByYearMonth(DateTime start, DateTime end,
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

  private static void totalColumns(BeeRowSet rowSet, int colFrom, int colTo, int colTotal) {
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

  private static void totalRows(BeeRowSet rowSet, int colFrom, int colTo, long rowId,
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

  @EJB
  NewsBean news;

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
            logger.warning(e);
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
          logger.warning(e);
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
        long from = BeeUtils.unbox(prm.getTime(PRM_REMINDER_TIME_FROM));
        long until = BeeUtils.unbox(prm.getTime(PRM_REMINDER_TIME_UNTIL));

        if (from < until) {
          int current = TimeUtils.minutesSinceDayStarted(time) * TimeUtils.MILLIS_PER_MINUTE;

          if (current < from || current > until) {
            time = new DateTime((current < from) ? TimeUtils.previousDay(time) : time.getDate());
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
        Filter.anyContains(Sets.newHashSet(COL_SUMMARY, COL_DESCRIPTION, COL_APPOINTMENT_LOCATION,
            ALS_COMPANY_NAME, COL_VEHICLE_NUMBER), query),
        Filter.anyItemContains(COL_STATUS, AppointmentStatus.class, query));

    List<BeeRow> appointments = getAppointments(filter, new Order(COL_START_DATE_TIME, false));
    if (!BeeUtils.isEmpty(appointments)) {
      for (BeeRow row : appointments) {
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
    } else if (BeeUtils.same(svc, SVC_GET_CALENDAR_ITEMS)) {
      response = getCalendarItems(reqInfo);
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
        BeeParameter.createTimeOfDay(CALENDAR_MODULE, PRM_REMINDER_TIME_FROM, false,
            TimeUtils.parseTime("8:00")),
        BeeParameter.createTimeOfDay(CALENDAR_MODULE, PRM_REMINDER_TIME_UNTIL, false,
            TimeUtils.parseTime("18:00")));
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

    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void updateTimers(ViewModifyEvent event) {
        if (event.isAfter()) {
          if (BeeUtils.same(event.getTargetName(), CommonsConstants.TBL_REMINDER_TYPES)) {
            if (event instanceof ViewDeleteEvent
                || event instanceof ViewUpdateEvent
                && (DataUtils.contains(((ViewUpdateEvent) event).getColumns(), COL_HOURS)
                || DataUtils.contains(((ViewUpdateEvent) event).getColumns(), COL_MINUTES))) {

              createNotificationTimers(null);
            }
          } else if (BeeUtils.same(event.getTargetName(), TBL_APPOINTMENTS)) {
            if (event instanceof ViewDeleteEvent) {
              for (long id : ((ViewDeleteEvent) event).getIds()) {
                createNotificationTimers(Pair.of(TBL_APPOINTMENTS, id));
              }
            } else if (event instanceof ViewUpdateEvent) {
              ViewUpdateEvent ev = (ViewUpdateEvent) event;

              if (DataUtils.contains(ev.getColumns(), COL_STATUS)
                  || DataUtils.contains(ev.getColumns(), COL_START_DATE_TIME)) {

                createNotificationTimers(Pair.of(TBL_APPOINTMENTS, ev.getRow().getId()));
              }
            }
          } else if (BeeUtils.same(event.getTargetName(), TBL_APPOINTMENT_REMINDERS)) {
            if (event instanceof ViewDeleteEvent) {
              for (long id : ((ViewDeleteEvent) event).getIds()) {
                createNotificationTimers(Pair.of(TBL_APPOINTMENT_REMINDERS, id));
              }
            } else if (event instanceof ViewUpdateEvent) {
              ViewUpdateEvent ev = (ViewUpdateEvent) event;

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

    news.registerUsageQueryProvider(Feed.APPOINTMENTS_MY, new UsageQueryProvider() {
      @Override
      public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
          DateTime startDate) {

        Long companyPerson = usr.getCompanyPerson(userId);
        if (companyPerson == null) {
          return null;
        }

        String idColumn = sys.getIdName(TBL_APPOINTMENTS);
        String usageTable = NewsConstants.getUsageTable(TBL_APPOINTMENTS);

        return new SqlSelect()
            .addFields(TBL_APPOINTMENTS, idColumn)
            .addMax(usageTable, NewsConstants.COL_USAGE_ACCESS)
            .addFrom(TBL_APPOINTMENTS)
            .addFromInner(usageTable, news.joinUsage(TBL_APPOINTMENTS))
            .addFromLeft(TBL_APPOINTMENT_OWNERS,
                sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_OWNERS, COL_APPOINTMENT))
            .addFromLeft(TBL_APPOINTMENT_ATTENDEES,
                sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT))
            .addFromLeft(TBL_ATTENDEES,
                sys.joinTables(TBL_ATTENDEES, TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE))
            .setWhere(
                SqlUtils.and(SqlUtils.equals(usageTable, NewsConstants.COL_USAGE_USER, userId),
                    SqlUtils.or(
                        SqlUtils.equals(TBL_APPOINTMENT_OWNERS, COL_APPOINTMENT_OWNER, userId),
                        SqlUtils.equals(TBL_ATTENDEES, COL_COMPANY_PERSON, companyPerson))))
            .addGroup(TBL_APPOINTMENTS, idColumn);
      }

      @Override
      public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
          DateTime startDate) {
        Long companyPerson = usr.getCompanyPerson(userId);
        if (companyPerson == null) {
          return null;
        }

        String idColumn = sys.getIdName(TBL_APPOINTMENTS);
        String usageTable = NewsConstants.getUsageTable(TBL_APPOINTMENTS);

        SqlSelect subquery = new SqlSelect()
            .addFields(TBL_APPOINTMENTS, idColumn)
            .addFields(usageTable, NewsConstants.COL_USAGE_UPDATE)
            .addFrom(TBL_APPOINTMENTS)
            .addFromInner(usageTable, news.joinUsage(TBL_APPOINTMENTS))
            .addFromLeft(TBL_APPOINTMENT_OWNERS,
                sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_OWNERS, COL_APPOINTMENT))
            .addFromLeft(TBL_APPOINTMENT_ATTENDEES,
                sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT))
            .addFromLeft(TBL_ATTENDEES,
                sys.joinTables(TBL_ATTENDEES, TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE))
            .setWhere(SqlUtils.and(NewsHelper.getUpdatesCondition(usageTable, userId, startDate),
                SqlUtils.or(
                    SqlUtils.equals(TBL_APPOINTMENT_OWNERS, COL_APPOINTMENT_OWNER, userId),
                    SqlUtils.equals(TBL_ATTENDEES, COL_COMPANY_PERSON, companyPerson))));

        usageTable = NewsConstants.getUsageTable(TBL_APPOINTMENT_ATTENDEES);

        subquery.addUnion(new SqlSelect()
            .addFields(TBL_APPOINTMENTS, idColumn)
            .addFields(usageTable, NewsConstants.COL_USAGE_UPDATE)
            .addFrom(TBL_APPOINTMENTS)
            .addFromInner(TBL_APPOINTMENT_ATTENDEES,
                sys.joinTables(TBL_APPOINTMENTS, TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT))
            .addFromInner(TBL_ATTENDEES,
                sys.joinTables(TBL_ATTENDEES, TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE))
            .addFromInner(usageTable, news.joinUsage(TBL_APPOINTMENT_ATTENDEES))
            .setWhere(SqlUtils.and(NewsHelper.getUpdatesCondition(usageTable, userId, startDate),
                SqlUtils.equals(TBL_ATTENDEES, COL_COMPANY_PERSON, companyPerson))));

        String alias = SqlUtils.uniqueName();

        return new SqlSelect()
            .addFields(alias, idColumn)
            .addMax(alias, NewsConstants.COL_USAGE_UPDATE)
            .addFrom(subquery, alias)
            .addGroup(alias, idColumn);
      }
    });

    HeadlineProducer headlineProducer = new HeadlineProducer() {
      @Override
      public Headline produce(Feed feed, long userId, BeeRowSet rowSet, IsRow row, boolean isNew) {
        String caption = DataUtils.getString(rowSet, row, COL_SUMMARY);
        if (BeeUtils.isEmpty(caption)) {
          caption = BeeUtils.bracket(row.getId());
        }

        List<String> subtitles = Lists.newArrayList();

        String period = TimeUtils.renderPeriod(
            DataUtils.getDateTime(rowSet, row, COL_START_DATE_TIME),
            DataUtils.getDateTime(rowSet, row, COL_END_DATE_TIME));
        if (!BeeUtils.isEmpty(period)) {
          subtitles.add(period);
        }

        AppointmentStatus status = EnumUtils.getEnumByIndex(AppointmentStatus.class,
            DataUtils.getInteger(rowSet, row, COL_STATUS));
        if (status != null) {
          subtitles.add(status.getCaption(usr.getLocalizableConstants(userId)));
        }

        return Headline.create(row.getId(), caption, subtitles, isNew);
      }
    };

    news.registerHeadlineProducer(Feed.APPOINTMENTS_MY, headlineProducer);
    news.registerHeadlineProducer(Feed.APPOINTMENTS_ALL, headlineProducer);
  }

  private boolean checkTable(String name) {
    return sys.isTable(name) && sys.getTable(name).isActive();
  }

  private ResponseObject createAppointment(RequestInfo reqInfo) {
    BeeRowSet rowSet = BeeRowSet.restore(reqInfo.getContent());
    if (rowSet.isEmpty()) {
      return ResponseObject.error(SVC_CREATE_APPOINTMENT, ": rowSet is empty");
    }

    String propIds = rowSet.getTableProperty(TBL_APPOINTMENT_PROPS);
    String attIds = rowSet.getTableProperty(COL_ATTENDEE);
    String rtIds = rowSet.getTableProperty(COL_REMINDER_TYPE);

    ResponseObject response = deb.commitRow(rowSet);
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
      return ResponseObject.parameterNotFound(SVC_DO_REPORT, PARAM_REPORT);
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

  private List<BeeRow> getAppointments(Filter filter, Order order) {
    long userId = usr.getCurrentUserId();

    Filter visible = Filter.or().add(
        ComparisonFilter.isEqual(COL_CREATOR, new LongValue(userId)),
        Filter.isNull(COL_CREATOR),
        Filter.isNull(COL_VISIBILITY),
        ComparisonFilter.isNotEqual(COL_VISIBILITY,
            new IntegerValue(CalendarVisibility.PRIVATE.ordinal())));

    BeeRowSet appointments = qs.getViewData(VIEW_APPOINTMENTS, Filter.and(filter, visible), order);
    if (DataUtils.isEmpty(appointments)) {
      logger.debug("no appointments found where", filter);
      logger.debug("visible where", visible);
      return Lists.newArrayList();
    }

    BeeRowSet appAtts = qs.getViewData(VIEW_APPOINTMENT_ATTENDEES);
    BeeRowSet appProps = qs.getViewData(VIEW_APPOINTMENT_PROPS);
    BeeRowSet appRemind = qs.getViewData(VIEW_APPOINTMENT_REMINDERS);

    int aaIndex = appAtts.getColumnIndex(COL_ATTENDEE);
    int apIndex = appProps.getColumnIndex(COL_APPOINTMENT_PROPERTY);
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

    return appointments.getRows().getList();
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

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, ALS_ATTENDEE_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_ATTENDEE_NAME));
    for (Integer hour : hours) {
      result.addColumn(ValueType.TEXT, hour.toString());
    }

    if (hours.size() > 1) {
      result.addColumn(ValueType.TEXT, usr.getLocalizableConstants().calTotal());
    }
    int columnCount = result.getNumberOfColumns();

    for (BeeRow attRow : attRowSet.getRows()) {
      Map<Integer, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[result.getNumberOfColumns()];
      values[0] = DataUtils.getString(attRowSet, attRow, ALS_ATTENDEE_TYPE_NAME);
      values[1] = DataUtils.getString(attRowSet, attRow, COL_ATTENDEE_NAME);

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
      totalRows(result, 2, columnCount - 1, 0, usr.getLocalizableConstants().totalOf() + ":", 0);
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

    BeeRowSet result = new BeeRowSet(new BeeColumn(ValueType.TEXT, ALS_ATTENDEE_TYPE_NAME),
        new BeeColumn(ValueType.TEXT, COL_ATTENDEE_NAME));
    for (YearMonth ym : months) {
      result.addColumn(ValueType.TEXT, ym.toString());
    }

    if (months.size() > 1) {
      result.addColumn(ValueType.TEXT, usr.getLocalizableConstants().calTotal());
    }
    int columnCount = result.getNumberOfColumns();

    for (BeeRow attRow : attRowSet.getRows()) {
      Map<YearMonth, Integer> tableRow = table.row(attRow.getId());
      if (tableRow == null || tableRow.isEmpty()) {
        continue;
      }

      String[] values = new String[columnCount];
      values[0] = DataUtils.getString(attRowSet, attRow, ALS_ATTENDEE_TYPE_NAME);
      values[1] = DataUtils.getString(attRowSet, attRow, COL_ATTENDEE_NAME);

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
      totalRows(result, 2, columnCount - 1, 0, usr.getLocalizableConstants().totalOf() + ":", 0);
    }
    formatTimeColumns(result, 2, columnCount - 1);

    return result;
  }

  private ResponseObject getCalendarItems(RequestInfo reqInfo) {
    long calendarId = BeeUtils.toLong(reqInfo.getParameter(PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), PARAM_CALENDAR_ID);
    }

    Filter calFilter = ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId));
    BeeRowSet calAppTypes = qs.getViewData(VIEW_CAL_APPOINTMENT_TYPES, calFilter);

    CompoundFilter appFilter = Filter.and();
    appFilter.add(VALID_APPOINTMENT);
    appFilter.add(ComparisonFilter.isNotEqual(COL_STATUS,
        new IntegerValue(AppointmentStatus.CANCELED.ordinal())));

    Long startTime = BeeUtils.toLongOrNull(reqInfo.getParameter(PARAM_START_TIME));
    Long endTime = BeeUtils.toLongOrNull(reqInfo.getParameter(PARAM_END_TIME));

    if (startTime != null) {
      appFilter.add(ComparisonFilter.isMore(COL_END_DATE_TIME, new LongValue(startTime)));
    }
    if (endTime != null) {
      appFilter.add(ComparisonFilter.isLess(COL_START_DATE_TIME, new LongValue(endTime)));
    }

    if (!calAppTypes.isEmpty()) {
      appFilter.add(Filter.any(COL_APPOINTMENT_TYPE,
          DataUtils.getDistinct(calAppTypes, COL_APPOINTMENT_TYPE)));
    }

    List<BeeRow> appointments = getAppointments(appFilter, null);
    List<CalendarTask> tasks = getCalendarTasks(calendarId, startTime, endTime);

    logger.info(reqInfo.getService(), calendarId,
        (startTime == null) ? BeeConst.STRING_EMPTY : new DateTime(startTime).toCompactString(),
        (endTime == null) ? BeeConst.STRING_EMPTY : new DateTime(endTime).toCompactString(),
        BeeConst.STRING_EQ, appointments.size(), BeeConst.STRING_PLUS, tasks.size());
    
    
    Map<String, Object> result = Maps.newHashMap();
    if (!appointments.isEmpty()) {
      result.put(ItemType.APPOINTMENT.name(), appointments);
    }
    if (!tasks.isEmpty()) {
      result.put(ItemType.TASK.name(), tasks);
    }

    if (result.isEmpty()) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result);
    }
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
      String tblUsers = CommonsConstants.TBL_USERS;
      Long cp = qs.getLong(new SqlSelect()
          .addFields(tblUsers, CommonsConstants.COL_COMPANY_PERSON)
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

  private List<CalendarTask> getCalendarTasks(long calendarId, Long startMillis, Long endMillis) {
    List<CalendarTask> tasks = Lists.newArrayList();

    BeeRowSet calRowSet = qs.getViewData(VIEW_CALENDARS, Filter.compareId(calendarId));
    if (DataUtils.isEmpty(calRowSet)) {
      logger.warning("calendar", calendarId, "not found");
      return tasks;
    }

    BeeRow calendar = calRowSet.getRow(0);
    Long calendarOwner = DataUtils.getLong(calRowSet, calendar, COL_CALENDAR_OWNER);

    boolean assigned;
    boolean delegated;
    boolean observed;

    if (DataUtils.isId(calendarOwner)) {
      assigned = BeeUtils.isTrue(DataUtils.getBoolean(calRowSet, calendar, COL_ASSIGNED_TASKS));
      delegated = BeeUtils.isTrue(DataUtils.getBoolean(calRowSet, calendar, COL_DELEGATED_TASKS));
      observed = BeeUtils.isTrue(DataUtils.getBoolean(calRowSet, calendar, COL_OBSERVED_TASKS));
    } else {
      assigned = false;
      delegated = false;
      observed = false;
    }

    Set<Long> executors = Sets.newHashSet();
    Map<Long, ColorAndStyle> executorAppearance = Maps.newHashMap();

    SimpleRowSet exGroupData = qs.getData(new SqlSelect()
        .setDistinctMode(true)
        .addFields(CommonsConstants.TBL_USER_GROUPS, CommonsConstants.COL_UG_USER)
        .addFields(TBL_CAL_EXECUTOR_GROUPS, COL_BACKGROUND, COL_FOREGROUND, COL_STYLE)
        .addFrom(TBL_CAL_EXECUTOR_GROUPS)
        .addFromInner(CommonsConstants.TBL_USER_GROUPS,
            SqlUtils.join(TBL_CAL_EXECUTOR_GROUPS, COL_EXECUTOR_GROUP,
                CommonsConstants.TBL_USER_GROUPS, CommonsConstants.COL_UG_GROUP))
        .setWhere(SqlUtils.equals(TBL_CAL_EXECUTOR_GROUPS, COL_CALENDAR, calendarId)));

    if (!DataUtils.isEmpty(exGroupData)) {
      for (SimpleRow exRow : exGroupData) {
        Long user = exRow.getLong(CommonsConstants.COL_UG_USER);
        executors.add(user);

        ColorAndStyle cs = ColorAndStyle.maybeCreate(exRow.getValue(COL_BACKGROUND),
            exRow.getValue(COL_FOREGROUND), exRow.getLong(COL_STYLE));
        if (cs != null) {
          if (executorAppearance.containsKey(user)) {
            cs.merge(executorAppearance.get(user), false);
          }
          executorAppearance.put(user, cs);
        }
      }
    }

    SimpleRowSet executorData = qs.getData(new SqlSelect()
        .addFields(TBL_CALENDAR_EXECUTORS, COL_EXECUTOR_USER,
            COL_BACKGROUND, COL_FOREGROUND, COL_STYLE)
        .addFrom(TBL_CALENDAR_EXECUTORS)
        .setWhere(SqlUtils.equals(TBL_CALENDAR_EXECUTORS, COL_CALENDAR, calendarId)));

    if (!DataUtils.isEmpty(executorData)) {
      for (SimpleRow exRow : executorData) {
        Long user = exRow.getLong(COL_EXECUTOR_USER);
        executors.add(user);

        ColorAndStyle cs = ColorAndStyle.maybeCreate(exRow.getValue(COL_BACKGROUND),
            exRow.getValue(COL_FOREGROUND), exRow.getLong(COL_STYLE));
        if (cs != null) {
          if (executorAppearance.containsKey(user)) {
            cs.merge(executorAppearance.get(user), false);
          }
          executorAppearance.put(user, cs);
        }
      }
    }

    if (!assigned && !delegated && !observed && executors.isEmpty()) {
      return tasks;
    }

    EnumMap<TaskType, ColorAndStyle> typeAppearance = Maps.newEnumMap(TaskType.class);

    String source = CrmConstants.TBL_TASKS;
    String idName = sys.getIdName(source);

    HasConditions where = SqlUtils.and();
    where.add(SqlUtils.notEqual(source, CrmConstants.COL_STATUS, TaskStatus.CANCELED.ordinal()));

    if (startMillis != null) {
      where.add(SqlUtils.more(source, CrmConstants.COL_FINISH_TIME, startMillis));
    }
    if (endMillis != null) {
      where.add(SqlUtils.less(source, CrmConstants.COL_START_TIME, endMillis));
    }

    HasConditions match = SqlUtils.or();
    if (assigned) {
      match.add(SqlUtils.equals(source, CrmConstants.COL_EXECUTOR, calendarOwner));
      if (executors.contains(calendarOwner)) {
        executors.remove(calendarOwner);
      }

      ColorAndStyle cs = ColorAndStyle.maybeCreate(
          DataUtils.getString(calRowSet, calendar, COL_ASSIGNED_TASKS_BACKGROUND),
          DataUtils.getString(calRowSet, calendar, COL_ASSIGNED_TASKS_FOREGROUND),
          DataUtils.getLong(calRowSet, calendar, COL_ASSIGNED_TASKS_STYLE));
      if (cs != null) {
        typeAppearance.put(TaskType.ASSIGNED, cs);
      }
    }

    if (delegated) {
      if (executors.contains(calendarOwner)) {
        match.add(SqlUtils.equals(source, CrmConstants.COL_OWNER, calendarOwner));
      } else {
        match.add(SqlUtils.and(
            SqlUtils.equals(source, CrmConstants.COL_OWNER, calendarOwner),
            SqlUtils.notEqual(source, CrmConstants.COL_EXECUTOR, calendarOwner)));
      }

      ColorAndStyle cs = ColorAndStyle.maybeCreate(
          DataUtils.getString(calRowSet, calendar, COL_DELEGATED_TASKS_BACKGROUND),
          DataUtils.getString(calRowSet, calendar, COL_DELEGATED_TASKS_FOREGROUND),
          DataUtils.getLong(calRowSet, calendar, COL_DELEGATED_TASKS_STYLE));
      if (cs != null) {
        typeAppearance.put(TaskType.DELEGATED, cs);
      }
    }

    if (observed) {
      match.add(SqlUtils.and(
          SqlUtils.notEqual(source, CrmConstants.COL_OWNER, calendarOwner),
          SqlUtils.notEqual(source, CrmConstants.COL_EXECUTOR, calendarOwner),
          SqlUtils.in(source, idName, CrmConstants.TBL_TASK_USERS, CrmConstants.COL_TASK,
              SqlUtils.equals(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, calendarOwner))));

      ColorAndStyle cs = ColorAndStyle.maybeCreate(
          DataUtils.getString(calRowSet, calendar, COL_OBSERVED_TASKS_BACKGROUND),
          DataUtils.getString(calRowSet, calendar, COL_OBSERVED_TASKS_FOREGROUND),
          DataUtils.getLong(calRowSet, calendar, COL_OBSERVED_TASKS_STYLE));
      if (cs != null) {
        typeAppearance.put(TaskType.OBSERVED, cs);
      }
    }

    if (!executors.isEmpty()) {
      match.add(SqlUtils.inList(source, CrmConstants.COL_EXECUTOR, executors));
    }

    where.add(match);

    SqlSelect query = new SqlSelect()
        .addFields(source, idName, CrmConstants.COL_START_TIME, CrmConstants.COL_FINISH_TIME,
            CrmConstants.COL_SUMMARY, CrmConstants.COL_DESCRIPTION,
            CrmConstants.COL_PRIORITY, CrmConstants.COL_STATUS,
            CrmConstants.COL_OWNER, CrmConstants.COL_EXECUTOR)
        .addField(CommonsConstants.TBL_COMPANIES,
            CommonsConstants.COL_COMPANY_NAME, CommonsConstants.ALS_COMPANY_NAME)
        .addFrom(source)
        .addFromInner(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, source, CrmConstants.COL_COMPANY))
        .setWhere(where)
        .addOrder(source, CrmConstants.COL_START_TIME, CrmConstants.COL_FINISH_TIME, idName);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return tasks;
    }

    for (SimpleRow row : data) {
      Long id = row.getLong(idName);
      if (!DataUtils.isId(id)) {
        continue;
      }

      Long owner = row.getLong(CrmConstants.COL_OWNER);
      Long executor = row.getLong(CrmConstants.COL_EXECUTOR);

      Set<Long> observers = Sets.newHashSet();

      Long[] taskUsers = qs.getLongColumn(new SqlSelect()
          .addFields(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER)
          .addFrom(CrmConstants.TBL_TASK_USERS)
          .setWhere(SqlUtils.and(
              SqlUtils.equals(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_TASK, id),
              SqlUtils.notEqual(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, owner),
              SqlUtils.notEqual(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, executor))));

      if (taskUsers != null && taskUsers.length > 0) {
        for (Long user : taskUsers) {
          observers.add(user);
        }
      }

      TaskType type;
      if (!DataUtils.isId(calendarOwner)) {
        type = TaskType.GENERAL;
      } else if (Objects.equal(executor, calendarOwner)) {
        type = TaskType.ASSIGNED;
      } else if (Objects.equal(owner, calendarOwner)) {
        type = TaskType.DELEGATED;
      } else if (observers.contains(calendarOwner)) {
        type = TaskType.OBSERVED;
      } else {
        type = TaskType.GENERAL;
      }

      CalendarTask task = new CalendarTask(type, id, row);

      if (!observers.isEmpty()) {
        task.setObservers(observers);
      }
      
      ColorAndStyle cs;
      if (executor != null && executorAppearance.containsKey(executor)) {
        cs = executorAppearance.get(executor);
      } else {
        cs = null;
      }
      
      if (typeAppearance.containsKey(type)) {
        if (cs == null) {
          cs = typeAppearance.get(type);
        } else {
          cs = cs.copy();
          cs.merge(typeAppearance.get(type), false);
        }
      }
      
      if (cs != null) {
        task.setBackground(cs.getBackground());
        task.setForeground(cs.getForeground());
        task.setStyle(cs.getStyle());
      }

      tasks.add(task);
    }

    return tasks;
  }

  private ResponseObject getOverlappingAppointments(RequestInfo reqInfo) {
    String svc = SVC_GET_OVERLAPPING_APPOINTMENTS;

    String appId = reqInfo.getParameter(PARAM_APPOINTMENT_ID);

    String start = reqInfo.getParameter(PARAM_APPOINTMENT_START);
    if (!BeeUtils.isLong(start)) {
      return ResponseObject.parameterNotFound(svc, PARAM_APPOINTMENT_START);
    }
    String end = reqInfo.getParameter(PARAM_APPOINTMENT_END);
    if (!BeeUtils.isLong(end)) {
      return ResponseObject.parameterNotFound(svc, PARAM_APPOINTMENT_END);
    }

    String attIds = reqInfo.getParameter(PARAM_ATTENDEES);
    if (BeeUtils.isEmpty(attIds)) {
      return ResponseObject.parameterNotFound(svc, PARAM_ATTENDEES);
    }

    CompoundFilter filter = Filter.and();
    filter.add(ComparisonFilter.isNotEqual(COL_STATUS,
        new IntegerValue(AppointmentStatus.CANCELED.ordinal())));

    if (BeeUtils.isLong(appId)) {
      filter.add(Filter.compareId(Operator.NE, BeeUtils.toLong(appId)));
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
    int propIndex = appProps.getColumnIndex(COL_APPOINTMENT_PROPERTY);
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
      return ResponseObject.parameterNotFound(SVC_GET_REPORT_OPTIONS, PARAM_REPORT);
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
      return ResponseObject.parameterNotFound(reqInfo.getService(), PARAM_CALENDAR_ID);
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

    BeeRowSet calRowSet = qs.getViewData(VIEW_CALENDARS, Filter.compareId(calendarId));
    if (calRowSet.isEmpty()) {
      return ResponseObject.error(reqInfo.getService(), PARAM_CALENDAR_ID, calendarId,
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

    for (ViewType view : ViewType.values()) {
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
        .addConstant(COL_APPOINTMENT_PROPERTY, propId));
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
        ReminderMethod method = EnumUtils.getEnumByIndex(ReminderMethod.class,
            data.getInt(CommonsConstants.COL_REMINDER_METHOD));

        if (method == ReminderMethod.EMAIL) {
          Long sender = prm.getRelation(MailConstants.PRM_DEFAULT_ACCOUNT);
          Long email = BeeUtils.toLongOrNull(BeeUtils.notEmpty(data.getValue(personEmail),
              data.getValue(CommonsConstants.COL_EMAIL)));

          if (!DataUtils.isId(sender)) {
            error = "No default sender specified (parameter DefaultAccount)";
          } else if (!DataUtils.isId(email)) {
            error = "No recipient email address specified";
          } else {
            ResponseObject response = mail.sendMail(sender, email, subject,
                template.replace("{time}", data.getDateTime(COL_START_DATE_TIME).toString()));

            if (response.hasErrors()) {
              error = ArrayUtils.toString(response.getErrors());
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
      return ResponseObject.parameterNotFound(SVC_SAVE_ACTIVE_VIEW, PARAM_USER_CALENDAR_ID);
    }

    Integer activeView = BeeUtils.toIntOrNull(reqInfo.getParameter(PARAM_ACTIVE_VIEW));
    if (!BeeUtils.isOrdinal(ViewType.class, activeView)) {
      return ResponseObject.parameterNotFound(SVC_SAVE_ACTIVE_VIEW, PARAM_ACTIVE_VIEW);
    }

    SqlUpdate update = new SqlUpdate(TBL_USER_CALENDARS).addConstant(COL_ACTIVE_VIEW, activeView)
        .setWhere(sys.idEquals(TBL_USER_CALENDARS, rowId));

    return qs.updateDataWithResponse(update);
  }

  private ResponseObject updateAppointment(RequestInfo reqInfo) {
    BeeRowSet newRowSet = BeeRowSet.restore(reqInfo.getContent());
    if (newRowSet.isEmpty()) {
      return ResponseObject.error(reqInfo.getService(), ": rowSet is empty");
    }

    long appId = newRowSet.getRow(0).getId();
    if (!DataUtils.isId(appId)) {
      return ResponseObject.error(reqInfo.getService(), ": invalid row id", appId);
    }

    String propIds = newRowSet.getTableProperty(TBL_APPOINTMENT_PROPS);
    String attIds = newRowSet.getTableProperty(COL_ATTENDEE);
    String rtIds = newRowSet.getTableProperty(COL_REMINDER_TYPE);

    String viewName = VIEW_APPOINTMENTS;
    BeeRowSet oldRowSet = qs.getViewData(viewName, Filter.compareId(appId));
    if (oldRowSet == null || oldRowSet.isEmpty()) {
      return ResponseObject.error(reqInfo.getService(), ": old row not found", appId);
    }

    BeeRowSet updated = DataUtils.getUpdated(viewName, oldRowSet.getColumns(), oldRowSet.getRow(0),
        newRowSet.getRow(0), null);

    ResponseObject response;
    if (updated == null) {
      response = ResponseObject.response(oldRowSet.getRow(0));
    } else {
      response = deb.commitRow(updated);
      if (response.hasErrors()) {
        return response;
      }
    }

    Filter appFilter = ComparisonFilter.isEqual(COL_APPOINTMENT, new LongValue(appId));

    List<Long> oldProperties =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_PROPS, appFilter),
            COL_APPOINTMENT_PROPERTY);
    List<Long> oldAttendees =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_ATTENDEES, appFilter), COL_ATTENDEE);
    List<Long> oldReminders =
        DataUtils.getDistinct(qs.getViewData(VIEW_APPOINTMENT_REMINDERS, appFilter),
            COL_REMINDER_TYPE);

    List<Long> newProperties = DataUtils.parseIdList(propIds);
    List<Long> newAttendees = DataUtils.parseIdList(attIds);
    List<Long> newReminders = DataUtils.parseIdList(rtIds);

    boolean childrenChanged = updateChildren(TBL_APPOINTMENT_PROPS, COL_APPOINTMENT, appId,
        COL_APPOINTMENT_PROPERTY, oldProperties, newProperties);
    childrenChanged |= updateChildren(TBL_APPOINTMENT_ATTENDEES, COL_APPOINTMENT, appId,
        COL_ATTENDEE, oldAttendees, newAttendees);

    if (!oldReminders.isEmpty() || !newReminders.isEmpty()) {
      childrenChanged |= updateChildren(TBL_APPOINTMENT_REMINDERS, COL_APPOINTMENT, appId,
          COL_REMINDER_TYPE, oldReminders, newReminders);

      createNotificationTimers(Pair.of(TBL_APPOINTMENTS, appId));
    }

    if (childrenChanged) {
      news.maybeRecordUpdate(VIEW_APPOINTMENTS, appId);
    }

    return response;
  }

  private boolean updateChildren(String tblName, String parentRelation, long parentId,
      String columnId, List<Long> oldValues, List<Long> newValues) {

    List<Long> insert = Lists.newArrayList(newValues);
    insert.removeAll(oldValues);

    List<Long> delete = Lists.newArrayList(oldValues);
    delete.removeAll(newValues);

    if (insert.isEmpty() && delete.isEmpty()) {
      return false;

    } else {
      for (Long value : insert) {
        qs.insertData(new SqlInsert(tblName).addConstant(parentRelation, parentId)
            .addConstant(columnId, value));
      }
      for (Long value : delete) {
        IsCondition condition = SqlUtils.equals(tblName, parentRelation, parentId, columnId, value);
        qs.updateData(new SqlDelete(tblName).setWhere(condition));
      }

      return true;
    }
  }
}
