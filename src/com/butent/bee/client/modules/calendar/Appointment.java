package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.primitives.Longs;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Appointment implements Comparable<Appointment> {

  private static final int BACKGROUND_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      CommonsConstants.COL_BACKGROUND);
  private static final int COLOR_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      CommonsConstants.COL_COLOR);
  private static final int COMPANY_NAME_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      ALS_COMPANY_NAME);
  private static final int CREATOR_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS, COL_CREATOR);
  private static final int DESCRIPTION_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_DESCRIPTION);
  private static final int END_DATE_TIME_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_END_DATE_TIME);
  private static final int FOREGROUND_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      CommonsConstants.COL_FOREGROUND);
  private static final int START_DATE_TIME_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_START_DATE_TIME);
  private static final int STYLE_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS, COL_STYLE);
  private static final int SUMMARY_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS, COL_SUMMARY);
  private static final int APPOINTMENT_TYPE_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_APPOINTMENT_TYPE);
  private static final int VEHICLE_MODEL_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_VEHICLE_MODEL);
  private static final int VEHICLE_NUMBER_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_VEHICLE_NUMBER);
  private static final int VEHICLE_PARENT_MODEL_INDEX = Data.getColumnIndex(VIEW_APPOINTMENTS,
      COL_VEHICLE_PARENT_MODEL);

  private final BeeRow row;

  private final List<Long> attendees = Lists.newArrayList();
  private final List<Long> owners = Lists.newArrayList();
  private final List<Long> properties = Lists.newArrayList();
  private final List<Long> reminders = Lists.newArrayList();

  private final Long separatedAttendee;

  public Appointment(BeeRow row) {
    this(row, null);
  }

  public Appointment(BeeRow row, Long separatedAttendee) {
    this.row = row;
    this.separatedAttendee = separatedAttendee;

    String attList = row.getProperty(TBL_APPOINTMENT_ATTENDEES);
    if (!BeeUtils.isEmpty(attList)) {
      attendees.addAll(DataUtils.parseIdList(attList));
    }

    String ownerList = row.getProperty(TBL_APPOINTMENT_OWNERS);
    if (!BeeUtils.isEmpty(ownerList)) {
      owners.addAll(DataUtils.parseIdList(ownerList));
    }

    String propList = row.getProperty(TBL_APPOINTMENT_PROPS);
    if (!BeeUtils.isEmpty(propList)) {
      properties.addAll(DataUtils.parseIdList(propList));
    }

    String remindList = row.getProperty(TBL_APPOINTMENT_REMINDERS);
    if (!BeeUtils.isEmpty(remindList)) {
      reminders.addAll(DataUtils.parseIdList(remindList));
    }
  }

  @Override
  public int compareTo(Appointment appointment) {
    int compare = Longs.compare(getStartMillis(), appointment.getStartMillis());
    if (compare == BeeConst.COMPARE_EQUAL) {
      compare = Longs.compare(appointment.getEndMillis(), getEndMillis());
    }
    return compare;
  }

  public List<Long> getAttendees() {
    return attendees;
  }

  public String getBackground() {
    return row.getString(BACKGROUND_INDEX);
  }

  public Long getColor() {
    return row.getLong(COLOR_INDEX);
  }

  public String getCompanyName() {
    return row.getString(COMPANY_NAME_INDEX);
  }

  public Long getCreator() {
    return row.getLong(CREATOR_INDEX);
  }

  public String getDescription() {
    return row.getString(DESCRIPTION_INDEX);
  }

  public long getDuration() {
    return getEndMillis() - getStartMillis();
  }

  public DateTime getEnd() {
    return row.getDateTime(END_DATE_TIME_INDEX);
  }

  public long getEndMillis() {
    return BeeUtils.unbox(row.getLong(END_DATE_TIME_INDEX));
  }

  public String getForeground() {
    return row.getString(FOREGROUND_INDEX);
  }

  public long getId() {
    return row.getId();
  }

  public List<Long> getOwners() {
    return owners;
  }

  public List<Long> getProperties() {
    return properties;
  }

  public Range<DateTime> getRange() {
    return Range.closedOpen(getStart(), getEnd());
  }

  public List<Long> getReminders() {
    return reminders;
  }

  public BeeRow getRow() {
    return row;
  }

  public Long getSeparatedAttendee() {
    return separatedAttendee;
  }

  public DateTime getStart() {
    return row.getDateTime(START_DATE_TIME_INDEX);
  }

  public long getStartMillis() {
    return BeeUtils.unbox(row.getLong(START_DATE_TIME_INDEX));
  }

  public Long getStyle() {
    return row.getLong(STYLE_INDEX);
  }

  public String getSummary() {
    return row.getString(SUMMARY_INDEX);
  }

  public Long getType() {
    return row.getLong(APPOINTMENT_TYPE_INDEX);
  }

  public String getVehicleModel() {
    return row.getString(VEHICLE_MODEL_INDEX);
  }

  public String getVehicleNumber() {
    return row.getString(VEHICLE_NUMBER_INDEX);
  }

  public String getVehicleParentModel() {
    return row.getString(VEHICLE_PARENT_MODEL_INDEX);
  }

  public boolean isMultiDay() {
    DateTime start = getStart();
    return start != null && TimeUtils.isMore(getEnd(), TimeUtils.startOfDay(start, 1));
  }

  public void setEnd(DateTime end) {
    row.setValue(END_DATE_TIME_INDEX, end);
  }

  public void setStart(DateTime start) {
    row.setValue(START_DATE_TIME_INDEX, start);
  }

  public void updateAttendees(List<Long> ids) {
    attendees.clear();
    if (!BeeUtils.isEmpty(ids)) {
      attendees.addAll(ids);
    }
    row.setProperty(TBL_APPOINTMENT_ATTENDEES, DataUtils.buildIdList(ids));
  }
}
