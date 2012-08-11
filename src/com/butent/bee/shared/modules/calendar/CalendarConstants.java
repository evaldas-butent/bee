package com.butent.bee.shared.modules.calendar;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public class CalendarConstants {

  public enum AppointmentStatus implements HasCaption {
    TENTATIVE("Planuojamas"),
    CONFIRMED("Patvirtintas"),
    DELAYED("Atidėtas"),
    CANCELED("Atšauktas"),
    RUNNING("Vykdomas"),
    COMPLETED("Baigtas");

    private final String caption;

    private AppointmentStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum ReminderMethod implements HasCaption {
    EMAIL, SMS;

    @Override
    public String getCaption() {
      return this.name().toLowerCase();
    }
  }

  public enum Report implements HasCaption {
    BUSY_MONTHS("Užimtumas mėnesiais"),
    BUSY_HOURS("Užimtumas valandomis"),
    CANCEL_MONTHS("Atšaukimas mėnesiais"),
    CANCEL_HOURS("Atšaukimas valandomis");

    private final String caption;

    private Report(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum ResponseStatus implements HasCaption {
    NEEDS_ACTION, DECLINED, TENTATIVE, ACCEPTED;

    @Override
    public String getCaption() {
      return BeeUtils.proper(this.name(), BeeConst.CHAR_UNDER);
    }
  }

  public enum Transparency implements HasCaption {
    OPAQUE("Nepersidengiantis"), TRANSPARENT("Persidengiantis");

    public static boolean isOpaque(Integer value) {
      return (value == null) ? false : value == OPAQUE.ordinal();
    }

    private final String caption;

    private Transparency(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum Visibility implements HasCaption {
    DEFAULT, PUBLIC, PRIVATE, CONFIDENTIAL;

    @Override
    public String getCaption() {
      return this.name().toLowerCase();
    }
  }

  public enum TimeBlockClick implements HasCaption {
    SINGLE, DOUBLE;

    @Override
    public String getCaption() {
      return Integer.toString(this.ordinal() + 1);
    }
  }

  public enum View implements HasCaption {
    DAY("DayView", "Diena"),
    DAYS("DaysView", "Dienos"),
    WORK_WEEK("WorkWeekView", "Darbo savaitė"),
    WEEK("WeekView", "Savaitė"),
    MONTH("MonthView", "Mėnuo"),
    RESOURCES("ResourceView", "Resursai");

    private final String columnId;
    private final String caption;

    private View(String columnId, String caption) {
      this.columnId = columnId;
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public String getCaption(int days) {
      if (DAYS.equals(this)) {
        return BeeUtils.toString(days) + ((days < 10) ? " dienos" : " dien.");
      } else {
        return caption;
      }
    }

    public String getColumnId() {
      return columnId;
    }
  }

  public static final String CALENDAR_MODULE = "Calendar";
  public static final String CALENDAR_METHOD = CALENDAR_MODULE + "Method";

  public static final String SVC_GET_USER_CALENDAR = "get_user_calendar";
  public static final String SVC_CREATE_APPOINTMENT = "create_appointment";
  public static final String SVC_UPDATE_APPOINTMENT = "update_appointment";
  public static final String SVC_GET_CALENDAR_APPOINTMENTS = "get_calendar_appointments";
  public static final String SVC_SAVE_ACTIVE_VIEW = "save_active_view";
  public static final String SVC_GET_OVERLAPPING_APPOINTMENTS = "get_overlapping_appointments";
  public static final String SVC_GET_REPORT_OPTIONS = "get_report_options";
  public static final String SVC_DO_REPORT = "do_report";

  public static final String PARAM_CALENDAR_ID = "calendar_id";
  public static final String PARAM_USER_CALENDAR_ID = "user_calendar_id";
  public static final String PARAM_ACTIVE_VIEW = "active_view";
  public static final String PARAM_APPOINTMENT_ID = "appointment_id";
  public static final String PARAM_APPOINTMENT_START = "appointment_start";
  public static final String PARAM_APPOINTMENT_END = "appointment_end";
  public static final String PARAM_ATTENDEES = "attendees";
  public static final String PARAM_REPORT = "report";

  public static final String TBL_USER_CALENDARS = "UserCalendars";
  public static final String TBL_CONFIGURATION = "Configuration";

  public static final String TBL_APPOINTMENTS = "Appointments";
  public static final String TBL_APPOINTMENT_PROPS = "AppointmentProps";
  public static final String TBL_APPOINTMENT_ATTENDEES = "AppointmentAttendees";
  public static final String TBL_APPOINTMENT_REMINDERS = "AppointmentReminders";

  public static final String TBL_ATTENDEES = "Attendees";
  public static final String TBL_REMINDER_TYPES = "ReminderTypes";

  public static final String TBL_REPORT_OPTIONS = "ReportOptions";

  public static final String VIEW_CALENDARS = "Calendars";
  public static final String VIEW_USER_CALENDARS = "UserCalendars";
  public static final String VIEW_CONFIGURATION = "Configuration";

  public static final String VIEW_PROPERTY_GROUPS = "PropertyGroups";
  public static final String VIEW_EXTENDED_PROPERTIES = "ExtendedProperties";

  public static final String VIEW_ATTENDEES = "Attendees";
  public static final String VIEW_ATTENDEE_PROPS = "AttendeeProps";
  public static final String VIEW_ATTENDEE_TYPES = "AttendeeTypes";

  public static final String VIEW_APPOINTMENTS = "Appointments";
  public static final String VIEW_APPOINTMENT_TYPES = "AppointmentTypes";

  public static final String VIEW_APPOINTMENT_PROPS = "AppointmentProps";
  public static final String VIEW_APPOINTMENT_ATTENDEES = "AppointmentAttendees";
  public static final String VIEW_APPOINTMENT_REMINDERS = "AppointmentReminders";

  public static final String VIEW_APPOINTMENT_STYLES = "AppointmentStyles";
  public static final String VIEW_THEMES = "Themes";
  public static final String VIEW_THEME_COLORS = "ThemeColors";

  public static final String VIEW_REMINDER_TYPES = "ReminderTypes";

  public static final String VIEW_CAL_APPOINTMENT_TYPES = "CalAppointmentTypes";
  public static final String VIEW_CAL_ATTENDEE_TYPES = "CalAttendeeTypes";
  public static final String VIEW_CALENDAR_ATTENDEES = "CalendarAttendees";
  public static final String VIEW_CALENDAR_PERSONS = "CalendarPersons";

  public static final String VIEW_REPORT_OPTIONS = "ReportOptions";

  public static final String GRID_CALENDARS = "Calendars";
  public static final String GRID_APPOINTMENTS = "Appointments";

  public static final String FORM_CALENDAR_SETTINGS = "CalendarSettings";

  public static final String FORM_NEW_APPOINTMENT = "ServiceAppointment";
  public static final String FORM_EDIT_APPOINTMENT = "ServiceAppointment";

  public static final String COL_USER = "User";
  public static final String COL_CALENDAR = "Calendar";
  public static final String COL_CALENDAR_NAME = "CalendarName";
  public static final String COL_NAME = "Name";

  public static final String COL_DEFAULT_DISPLAYED_DAYS = "DefaultDisplayedDays";

  public static final String COL_PIXELS_PER_INTERVAL = "PixelsPerInterval";
  public static final String COL_INTERVALS_PER_HOUR = "IntervalsPerHour";

  public static final String COL_WORKING_HOUR_START = "WorkingHourStart";
  public static final String COL_WORKING_HOUR_END = "WorkingHourEnd";
  public static final String COL_SCROLL_TO_HOUR = "ScrollToHour";

  public static final String COL_TIME_BLOCK_CLICK_NUMBER = "TimeBlockClickNumber";

  public static final String COL_FAVORITE = "Favorite";

  public static final String COL_COMPANY = "Company";
  public static final String COL_COMPANY_NAME = "CompanyName";
  public static final String COL_COMPANY_PHONE = "CompanyPhone";
  public static final String COL_COMPANY_EMAIL = "CompanyEmail";

  public static final String COL_APPOINTMENT = "Appointment";
  public static final String COL_APPOINTMENT_TYPE = "AppointmentType";
  public static final String COL_TYPE_NAME = "TypeName";

  public static final String COL_ATTENDEE = "Attendee";
  public static final String COL_ATTENDEE_NAME = "AttendeeName";
  public static final String COL_ATTENDEE_TYPE = "AttendeeType";

  public static final String COL_TIME_ZONE = "TimeZone";
  public static final String COL_THEME = "Theme";

  public static final String COL_PROPERTY = "Property";
  public static final String COL_PROPERTY_GROUP = "PropertyGroup";
  public static final String COL_PROPERTY_NAME = "PropertyName";
  public static final String COL_GROUP_NAME = "GroupName";
  public static final String COL_DEFAULT_PROPERTY = "DefaultProperty";

  public static final String COL_HOURS = "Hours";
  public static final String COL_MINUTES = "Minutes";
  public static final String COL_SCHEDULED = "Scheduled";

  public static final String COL_REMINDER_METHOD = "Method";
  public static final String COL_MESSAGE = "Message";
  public static final String COL_TEMPLATE = "Template";
  public static final String COL_SENT = "Sent";
  public static final String COL_ERROR = "Error";
  public static final String COL_RECIPIENT = "Recipient";

  public static final String COL_START_DATE = "StartDate";
  public static final String COL_START_DATE_TIME = "StartDateTime";

  public static final String COL_END_DATE = "EndDate";
  public static final String COL_END_DATE_TIME = "EndDateTime";

  public static final String COL_EFFECTIVE_START = "EffectiveStart";
  public static final String COL_EFFECTIVE_END = "EffectiveEnd";

  public static final String COL_VEHICLE = "Vehicle";
  public static final String COL_VEHICLE_OWNER = "VehicleOwner";
  public static final String COL_VEHICLE_NUMBER = "VehicleNumber";
  public static final String COL_VEHICLE_PARENT_MODEL = "VehicleParentModel";
  public static final String COL_VEHICLE_MODEL = "VehicleModel";

  public static final String COL_COLOR = "Color";
  public static final String COL_DEFAULT_COLOR = "DefaultColor";
  public static final String COL_BACKGROUND = "Background";
  public static final String COL_FOREGROUND = "Foreground";

  public static final String COL_SUMMARY = "Summary";
  public static final String COL_DESCRIPTION = "Description";

  public static final String COL_REMINDER_TYPE = "ReminderType";

  public static final String COL_STATUS = "Status";

  public static final String COL_ORGANIZER = "Organizer";
  public static final String COL_ORGANIZER_FIRST_NAME = "OrganizerFirstName";
  public static final String COL_ORGANIZER_LAST_NAME = "OrganizerLastName";

  public static final String COL_OWNER_FIRST_NAME = "OwnerFirstName";
  public static final String COL_OWNER_LAST_NAME = "OwnerLastName";

  public static final String COL_COMPANY_PERSON = "CompanyPerson";

  public static final String COL_SIMPLE_HEADER = "SimpleHeader";
  public static final String COL_SIMPLE_BODY = "SimpleBody";
  public static final String COL_MULTI_HEADER = "MultiHeader";
  public static final String COL_MULTI_BODY = "MultiBody";

  public static final String COL_APPOINTMENT_COMPACT = "AppointmentCompact";
  public static final String COL_APPOINTMENT_TITLE = "AppointmentTitle";

  public static final String COL_STYLE = "Style";

  public static final String COL_SIMPLE = "Simple";
  public static final String COL_MULTI = "Multi";
  public static final String COL_COMPACT = "Compact";

  public static final String COL_HEADER = "Header";
  public static final String COL_BODY = "Body";
  public static final String COL_FOOTER = "Footer";

  public static final String COL_ACTIVE_VIEW = "ActiveView";

  public static final String COL_TRANSPARENCY = "Transparency";
  public static final String COL_TYPE_TRANSPARENCY = "TypeTransparency";

  public static final String COL_REPORT = "Report";
  public static final String COL_CAPTION = "Caption";
  public static final String COL_LOWER_DATE = "LowerDate";
  public static final String COL_UPPER_DATE = "UpperDate";
  public static final String COL_LOWER_HOUR = "LowerHour";
  public static final String COL_UPPER_HOUR = "UpperHour";
  public static final String COL_ATTENDEE_TYPES = "AttendeeTypes";
  public static final String COL_ATTENDEES = "Attendees";

  public static final String NAME_START = "Start";
  public static final String NAME_END = "End";

  public static final String PRM_REMINDER_SUBJECT = "ReminderSubject";

  private CalendarConstants() {
  }
}
