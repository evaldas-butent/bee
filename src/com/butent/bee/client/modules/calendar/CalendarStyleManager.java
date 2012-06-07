package com.butent.bee.client.modules.calendar;

public class CalendarStyleManager {

  public static final String DAY_VIEW = "bee-cal";
  public static final String RESOURCE_VIEW = DAY_VIEW;

  public static final String CALENDAR_HEADER = "calendar-header";
  public static final String YEAR_CELL = "year-cell";
  public static final String DAY_CELL_CONTAINER = "day-cell-container";

  public static final String DAY_CELL = "day-cell";
  public static final String DAY_CELL_TODAY = "day-cell-today";
  public static final String DAY_CELL_WEEKEND = "day-cell-weekend";

  public static final String MULTI_DAY_PANEL = "multiDayPanel";
  public static final String MULTI_DAY_GRID_CELL = "centerDayContainerCell";
  public static final String MULTI_DAY_GRID = "multiDayGrid";

  public static final String TIMELINE_EMPTY_CELL = "leftEmptyCell";
  public static final String SCROLLBAR_EMPTY_CELL = "rightEmptyCell";
  
  public static final String SCROLL_AREA = "scroll-area";
  public static final String APPOINTMENT_PANEL = "appointment-panel";
  public static final String TIME_STRIP = "time-strip";
  public static final String APPOINTMENT_GRID = "appointment-grid";

  public static final String HOUR_PANEL = "hour-panel";
  public static final String HOUR_LAYOUT = "hour-layout";
  public static final String HOUR_LABEL = "hour-label";

  public static final String MAJOR_TIME_INTERVAL = "major-time-interval";
  public static final String MINOR_TIME_INTERVAL = "minor-time-interval";

  public static final String WORKING_HOURS = "working-hours";
  public static final String NON_WORKING = "non-working";

  public static final String RESOURCE_DATE_CELL = "date-cell";
  public static final String RESOURCE_CAPTION_CONTAINER = "caption-container";
  public static final String RESOURCE_CAPTION_CELL = "caption-cell";
  
  public static final String COLUMN_SEPARATOR = "column-separator";

  private static final String APPOINTMENT = "appointment";
  private static final String APPOINTMENT_MULTIDAY = "appointment-multiday";

  private static final String SELECTED = "-selected";

  public CalendarStyleManager() {
    super();
  }

  public void applySelected(AppointmentWidget widget, boolean selected) {
    Appointment appointment = widget.getAppointment();

    String styleName = (appointment.isMultiDay() ? APPOINTMENT_MULTIDAY : APPOINTMENT) + SELECTED;
    widget.setStyleName(styleName, selected);
  }
  
  public void applyStyle(AppointmentWidget widget, boolean selected) {
    Appointment appointment = widget.getAppointment();

    String styleName = appointment.isMultiDay() ? APPOINTMENT_MULTIDAY : APPOINTMENT;
    widget.addStyleName(styleName);

    if (selected) {
      widget.addStyleName(styleName + SELECTED);
    }
  }
}
