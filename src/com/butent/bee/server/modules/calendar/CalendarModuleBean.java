package com.butent.bee.server.modules.calendar;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CALENDAR_METHOD);

    if (BeeUtils.same(svc, SVC_GET_USER_CALENDAR)) {
      response = getUserCalendar(reqInfo);
    } else if (BeeUtils.same(svc, SVC_CREATE_APPOINTMENT)) {
      response = createAppointment(reqInfo);
    } else if (BeeUtils.same(svc, SVC_GET_CALENDAR_APPOINTMENTS)) {
      response = getCalendarAppointments(reqInfo);

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

  private ResponseObject getCalendarAppointments(RequestInfo reqInfo) {
    long calendarId = BeeUtils.toLong(reqInfo.getParameter(PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.error(SVC_GET_USER_CALENDAR, PARAM_CALENDAR_ID, "parameter not found");
    }

    Filter calFilter = ComparisonFilter.isEqual(COL_CALENDAR, new LongValue(calendarId));

    BeeRowSet calAttTypes = sys.getViewData(VIEW_CAL_ATTENDEE_TYPES, calFilter);
    BeeRowSet calAttendees = sys.getViewData(VIEW_CALENDAR_ATTENDEES, calFilter);

    BeeRowSet calAppTypes = sys.getViewData(VIEW_CAL_APPOINTMENT_TYPES, calFilter);
    BeeRowSet calPersons = sys.getViewData(VIEW_CALENDAR_PERSONS, calFilter);

    CompoundFilter attFilter = Filter.or();
    if (!calAttTypes.isEmpty()) {
      attFilter.add(Filter.in(COL_ATTENDEE_TYPE,
          DataUtils.getDistinct(calAttTypes, COL_ATTENDEE_TYPE)));
    }
    if (!calAttendees.isEmpty()) {
      attFilter.add(Filter.idIn(DataUtils.getDistinct(calAttendees, COL_ATTENDEE)));
    }

    BeeRowSet attendees = sys.getViewData(VIEW_ATTENDEES, attFilter);

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

    BeeRowSet appAtts = sys.getViewData(VIEW_APPOINTMENT_ATTENDEES);
    BeeRowSet appProps = sys.getViewData(VIEW_APPOINTMENT_PROPS);

    BeeRowSet appointments = sys.getViewData(VIEW_APPOINTMENTS, appFilter);

    Set<Long> attIds = Sets.newHashSet();
    boolean filterByAttendee = !attFilter.isEmpty();
    if (filterByAttendee) {
      for (BeeRow row : attendees.getRows()) {
        attIds.add(row.getId());
      }
    }

    List<BeeRow> children;
    Iterator<BeeRow> iterator = appointments.getRows().iterator();

    while (iterator.hasNext()) {
      BeeRow row = iterator.next();
      String appId = BeeUtils.toString(row.getId());

      children = DataUtils.filterRows(appAtts, COL_APPOINTMENT, appId);
      int index = appAtts.getColumnIndex(COL_ATTENDEE);

      if (filterByAttendee) {
        boolean ok = false;
        for (BeeRow r : children) {
          if (attIds.contains(r.getLong(index))) {
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
            DataUtils.buildList(DataUtils.getDistinct(children, index)));
      }

      children = DataUtils.filterRows(appProps, COL_APPOINTMENT, appId);
      if (!children.isEmpty()) {
        index = appProps.getColumnIndex(COL_PROPERTY);
        row.setProperty(VIEW_APPOINTMENT_PROPS,
            DataUtils.buildList(DataUtils.getDistinct(children, index)));
      }
    }

    if (!attendees.isEmpty()) {
      appointments.setTableProperty(VIEW_ATTENDEES, DataUtils.buildList(attendees));
    }

    LogUtils.infoNow(logger, SVC_GET_CALENDAR_APPOINTMENTS, appointments.getNumberOfRows(),
        appointments.getViewName(), attendees.getNumberOfRows(), attendees.getViewName());

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

    BeeRowSet ucRowSet = sys.getViewData(VIEW_USER_CALENDARS, filter);
    if (!ucRowSet.isEmpty()) {
      return ResponseObject.response(ucRowSet);
    }

    BeeRowSet calRowSet = sys.getViewData(VIEW_CALENDARS, ComparisonFilter.compareId(calendarId));
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
        .addConstant(COL_DEFAULT_DISPLAYED_DAYS, settings.getDefaultDisplayedDays())
        .addConstant(COL_ENABLE_DRAG_DROP, settings.isDragDropEnabled())
        .addConstant(COL_OFFSET_HOUR_LABELS, settings.offsetHourLabels());

    if (settings.getTimeBlockClickNumber() != null) {
      sqlInsert.addConstant(COL_TIME_BLOCK_CLICK_NUMBER,
          settings.getTimeBlockClickNumber().ordinal());
    }

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return response;
    }

    BeeRowSet result = sys.getViewData(VIEW_USER_CALENDARS, filter);
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
}
