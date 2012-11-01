package com.butent.bee.client.modules.calendar.view;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarFormat;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarView;
import com.butent.bee.client.modules.calendar.CalendarWidget;
import com.butent.bee.client.modules.calendar.dnd.MonthDropController;
import com.butent.bee.client.modules.calendar.dnd.MonthDragController;
import com.butent.bee.client.modules.calendar.layout.AppointmentLayoutDescription;
import com.butent.bee.client.modules.calendar.layout.AppointmentStackingManager;
import com.butent.bee.client.modules.calendar.layout.DayLayoutDescription;
import com.butent.bee.client.modules.calendar.layout.MonthLayoutDescription;
import com.butent.bee.client.modules.calendar.layout.WeekLayoutDescription;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MonthView extends CalendarView {

  public static final Comparator<Appointment> APPOINTMENT_COMPARATOR =
      new Comparator<Appointment>() {
        public int compare(Appointment a1, Appointment a2) {
          int compare = Boolean.valueOf(a2.isMultiDay()).compareTo(a1.isMultiDay());
          if (compare == BeeConst.COMPARE_EQUAL) {
            compare = a1.compareTo(a2);
          }
          return compare;
        }
      };

  private static final int DAYS_IN_A_WEEK = 7;

  private static final int APPOINTMENT_HEIGHT = 17;
  private static final int APPOINTMENT_MARGIN_TOP = 3;

  private static final double APPOINTMENT_MARGIN_LEFT = 0.3;
  private static final double APPOINTMENT_MARGIN_RIGHT = 0.3;

  private static final int PERCENT_SCALE = 3;

  private final HtmlTable grid = new HtmlTable();
  private final Absolute canvas = new Absolute();

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
  private final Map<String, List<Appointment>> moreLabels = Maps.newHashMap();

  private MonthDragController dragController = null;
  private MonthDropController dropController = null;

  private JustDate firstDate;
  private int requiredRows;

  private int cellOffsetHeight;
  private int cellHeight;

  private int weekDayHeaderHeight;
  private int dayHeaderHeight;

  private int maxCellAppointments;

  public MonthView() {
    super();

    grid.addStyleName(CalendarStyleManager.MONTH_GRID);
    canvas.addStyleName(CalendarStyleManager.MONTH_CANVAS);
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    addWidget(grid);
    addWidget(canvas);

    if (dragController == null) {
      dragController = new MonthDragController(canvas, true);
      dragController.addDefaultHandler(this);
    }

    if (dropController == null) {
      dropController = new MonthDropController(canvas);
      dragController.registerDropController(dropController);
    }
  }

  @Override
  public void doLayout() {
    grid.clear();
    grid.removeAllRows();

    canvas.clear();

    appointmentWidgets.clear();
    moreLabels.clear();

    buildGrid();

    calculateHeights();

    this.maxCellAppointments = cellHeight / (APPOINTMENT_HEIGHT + APPOINTMENT_MARGIN_TOP) - 1;

    dropController.setRowCount(requiredRows);
    dropController.setColumnCount(DAYS_IN_A_WEEK);
    dropController.setHeaderHeight(weekDayHeaderHeight);

    Collections.sort(getAppointments(), APPOINTMENT_COMPARATOR);
    MonthLayoutDescription monthLayoutDescription = new MonthLayoutDescription(firstDate,
        requiredRows, getAppointments(), maxCellAppointments - 1);

    WeekLayoutDescription[] weeks = monthLayoutDescription.getWeekDescriptions();
    for (int i = 0; i < requiredRows; i++) {
      WeekLayoutDescription weekDescription = weeks[i];
      if (weekDescription != null) {
        layOnTopAppointments(weekDescription, i);
        layOnWeekDaysAppointments(weekDescription, i);
      }
    }
  }

  @Override
  public void doScroll() {
  }
  
  @Override
  public void doSizing() {
  }

  @Override
  public List<AppointmentWidget> getAppointmentWidgets() {
    return appointmentWidgets;
  }

  public JustDate getFirstDate() {
    return firstDate;
  }

  @Override
  public Widget getScrollArea() {
    return null;
  }

  @Override
  public String getStyleName() {
    return CalendarStyleManager.MONTH_VIEW;
  }

  @Override
  public Type getType() {
    return Type.MONTH;
  }

  @Override
  public boolean onClick(Element element, Event event) {
    if (element.equals(canvas.getElement())) {
      dayClicked(event);
      return true;

    } else if (moreLabels.containsKey(element.getId())) {
      showAppointments(moreLabels.get(element.getId()));
      return true;

    } else {
      AppointmentWidget widget = CalendarUtils.findWidget(appointmentWidgets, element);
      if (widget != null 
          && (widget.isMulti() || !widget.getCompactBar().getElement().isOrHasChild(element))) {
        openAppointment(widget.getAppointment());
        return true;
      }
    }
    return false;
  }

  @Override
  public void onClock() {
  }

  private void buildCell(int row, int col, String text, boolean isToday, boolean currentMonth) {
    BeeLabel label = new BeeLabel(text);
    label.addStyleName(CalendarStyleManager.MONTH_CELL_LABEL);

    grid.setWidget(row, col, label);
    grid.getCellFormatter().addStyleName(row, col, CalendarStyleManager.MONTH_CELL);

    if (isToday) {
      label.addStyleName(CalendarStyleManager.TODAY);
      grid.getCellFormatter().addStyleName(row, col, CalendarStyleManager.TODAY);
    }
    if (!currentMonth) {
      label.addStyleName(CalendarStyleManager.DISABLED);
      grid.getCellFormatter().addStyleName(row, col, CalendarStyleManager.DISABLED);
    }

    switch (col) {
      case 0:
        grid.getCellFormatter().addStyleName(row, col, CalendarStyleManager.FIRST_COLUMN);
        break;
      case DAYS_IN_A_WEEK - 1:
        grid.getCellFormatter().addStyleName(row, col, CalendarStyleManager.LAST_COLUMN);
        break;
    }
  }

  private void buildGrid() {
    for (int i = 0; i < DAYS_IN_A_WEEK; i++) {
      grid.setText(0, i, CalendarFormat.getDayOfWeekNames()[i]);
      grid.getCellFormatter().setStyleName(0, i, CalendarStyleManager.WEEKDAY_LABEL);
    }

    JustDate date = getDate();
    int month = date.getMonth();

    this.firstDate = calculateFirstDate(date);
    this.requiredRows = calculateRequiredRows(date);

    JustDate today = TimeUtils.today();
    JustDate tmpDate = JustDate.copyOf(firstDate);

    for (int i = 1; i <= requiredRows; i++) {
      for (int j = 0; j < DAYS_IN_A_WEEK; j++) {
        buildCell(i, j, BeeUtils.toString(tmpDate.getDom()), tmpDate.equals(today),
            tmpDate.getMonth() == month);
        TimeUtils.moveOneDayForward(tmpDate);
      }
    }
  }

  private JustDate calculateFirstDate(JustDate dayInMonth) {
    JustDate date = TimeUtils.startOfMonth(dayInMonth);
    return TimeUtils.startOfWeek(date, (date.getDow() > 1) ? 0 : -1);
  }

  private void calculateHeights() {
    int gridHeight = grid.getOffsetHeight();

    this.weekDayHeaderHeight = grid.getRowFormatter().getElement(0).getOffsetHeight();
    this.dayHeaderHeight =
        grid.getCellFormatter().getElement(1, 0).getFirstChildElement().getOffsetHeight();

    this.cellOffsetHeight = (gridHeight - weekDayHeaderHeight) / requiredRows;
    this.cellHeight = cellOffsetHeight - dayHeaderHeight;
  }

  private int calculateRequiredRows(JustDate dayInMonth) {
    int rows = 5;

    JustDate firstOfTheMonth = TimeUtils.startOfMonth(dayInMonth);
    JustDate firstDayInCalendar = calculateFirstDate(dayInMonth);

    if (firstDayInCalendar.getMonth() != firstOfTheMonth.getMonth()) {
      JustDate lastDayOfPreviousMonth = TimeUtils.previousDay(firstOfTheMonth);
      int prevMonthOverlap = TimeUtils.dayDiff(firstDayInCalendar, lastDayOfPreviousMonth) + 1;

      JustDate firstOfNextMonth = TimeUtils.startOfNextMonth(firstOfTheMonth);
      int daysInMonth = TimeUtils.dayDiff(firstOfTheMonth, firstOfNextMonth);

      if (prevMonthOverlap + daysInMonth > 35) {
        rows = 6;
      }
    }
    return rows;
  }

  private JustDate cellDate(int row, int col) {
    return TimeUtils.nextDay(firstDate, row * DAYS_IN_A_WEEK + col);
  }

  private void dayClicked(Event event) {
    int x = event.getClientX() - canvas.getElement().getAbsoluteLeft();
    int y = event.getClientY() - canvas.getElement().getAbsoluteTop();

    int colWidth = canvas.getOffsetWidth() / DAYS_IN_A_WEEK;
    int col = x / colWidth;
    int row = y / ((canvas.getOffsetHeight() - weekDayHeaderHeight) / requiredRows);
    
    DateTime start = cellDate(row, col).getDateTime();

    double h = BeeUtils.rescale(x % colWidth, 0, colWidth, 0, 24);
    int hour = BeeUtils.clamp((int) Math.round(h), 0, 23);
    if (hour > 0) {
      start.setHour(hour);
    }

    createAppointment(start, null);
  }

  private void layOnAppointment(Appointment appointment, boolean multi, int colStart, int colEnd,
      int row, int cellPosition) {

    AppointmentWidget widget = new AppointmentWidget(appointment, multi, BeeConst.UNDEF);
    if (multi) {
      widget.render();
    } else {
      widget.renderCompact();
    }

    placeItemInGrid(widget, appointment, multi, colStart, colEnd, row, cellPosition);

    if (!multi) {
      dragController.makeDraggable(widget, widget.getCompactBar());
    }

    appointmentWidgets.add(widget);
    canvas.add(widget);
  }

  private void layOnMoreLabel(List<Appointment> appointments, int dayOfWeek, int weekOfMonth) {
    BeeLabel more = new BeeLabel("+ " + appointments.size());
    more.setStyleName(CalendarStyleManager.MORE_LABEL);

    placeItemInGrid(more, null, false, dayOfWeek, dayOfWeek, weekOfMonth, maxCellAppointments);

    canvas.add(more);
    moreLabels.put(more.getId(), appointments);
  }

  private void layOnTopAppointments(WeekLayoutDescription weekDescription, int weekOfMonth) {
    AppointmentStackingManager manager = weekDescription.getTopAppointmentsManager();
    for (int layer = 0; layer < maxCellAppointments; layer++) {
      List<AppointmentLayoutDescription> descriptions = manager.getDescriptionsInLayer(layer);
      if (descriptions == null) {
        break;
      }

      for (AppointmentLayoutDescription description : descriptions) {
        layOnAppointment(description.getAppointment(), true, description.getWeekStartDay(),
            description.getWeekEndDay(), weekOfMonth, layer);
      }
    }
  }

  private void layOnWeekDaysAppointments(WeekLayoutDescription week, int weekOfMonth) {
    AppointmentStackingManager manager = week.getTopAppointmentsManager();

    for (int dayOfWeek = 0; dayOfWeek < DAYS_IN_A_WEEK; dayOfWeek++) {
      DayLayoutDescription dayAppointments = week.getDayLayoutDescription(dayOfWeek);
      int layer = manager.lowestLayerIndex(dayOfWeek);

      if (dayAppointments != null) {
        int count = dayAppointments.getAppointments().size();

        for (int i = 0; i < count; i++) {
          Appointment appointment = dayAppointments.getAppointments().get(i);
          layer = manager.nextLowestLayerIndex(dayOfWeek, layer);

          if (layer > maxCellAppointments - 1) {
            int remaining = count + manager.countOverLimit(dayOfWeek) - i;

            if (remaining == 1) {
              layOnAppointment(appointment, false, dayOfWeek, dayOfWeek, weekOfMonth, layer);
            } else {
              List<Appointment> overLimit = manager.getOverLimit(dayOfWeek);
              overLimit.addAll(Lists.newArrayList(dayAppointments.getAppointments()
                  .subList(i, count)));

              layOnMoreLabel(overLimit, dayOfWeek, weekOfMonth);
            }
            break;
          }

          layOnAppointment(appointment, false, dayOfWeek, dayOfWeek, weekOfMonth, layer);
          layer++;
        }

      } else if (manager.countOverLimit(dayOfWeek) > 0) {
        layOnMoreLabel(manager.getOverLimit(dayOfWeek), dayOfWeek, weekOfMonth);
      }
    }
  }

  private void placeItemInGrid(Widget widget, Appointment appointment, boolean multi,
      int colStart, int colEnd, int row, int cellPosition) {

    double colWidth = 100d / DAYS_IN_A_WEEK;

    double left = colStart * colWidth;
    double width = (colEnd - colStart + 1) * colWidth;

    double marginLeft = APPOINTMENT_MARGIN_LEFT;
    double marginRight = APPOINTMENT_MARGIN_RIGHT;

    if (appointment != null) {
      DateTime start = appointment.getStart();
      DateTime end = appointment.getEnd();

      int startMinutes = TimeUtils.minutesSinceDayStarted(start);
      int endMinutes = TimeUtils.minutesSinceDayStarted(end);

      if (multi) {
        if (TimeUtils.sameDate(start, cellDate(row, colStart)) && startMinutes > 0) {
          marginLeft = startMinutes * colWidth / TimeUtils.MINUTES_PER_DAY;
        }
        if (TimeUtils.sameDate(end, cellDate(row, colEnd)) && endMinutes > 0) {
          marginRight =
              (TimeUtils.MINUTES_PER_DAY - endMinutes) * colWidth / TimeUtils.MINUTES_PER_DAY;
        }

      } else if (widget instanceof AppointmentWidget) {
        Widget bar = ((AppointmentWidget) widget).getCompactBar();

        double x = startMinutes * 100d / TimeUtils.MINUTES_PER_DAY;
        bar.getElement().getStyle().setLeft(BeeUtils.round(x, PERCENT_SCALE), Unit.PCT);
        
        if (TimeUtils.sameDate(start, end)) {
          x = (TimeUtils.MINUTES_PER_DAY - endMinutes) * 100d / TimeUtils.MINUTES_PER_DAY;
        } else {
          x = 0;
        }
        bar.getElement().getStyle().setRight(BeeUtils.round(x, PERCENT_SCALE), Unit.PCT);
      }
    }

    left += marginLeft;
    width -= (marginLeft + marginRight);

    int y = weekDayHeaderHeight + row * cellOffsetHeight + dayHeaderHeight + APPOINTMENT_MARGIN_TOP
        + cellPosition * (APPOINTMENT_HEIGHT + APPOINTMENT_MARGIN_TOP);
    double top = 100d * y / grid.getOffsetHeight();

    widget.getElement().getStyle().setLeft(BeeUtils.round(left, PERCENT_SCALE), Unit.PCT);
    widget.getElement().getStyle().setWidth(BeeUtils.round(width, PERCENT_SCALE), Unit.PCT);

    widget.getElement().getStyle().setTop(BeeUtils.round(top, PERCENT_SCALE), Unit.PCT);
  }
  
  private void showAppointments(List<Appointment> appointments) {
    if (BeeUtils.isEmpty(appointments)) {
      return;
    }
    
    final Flow panel = new Flow();
    panel.addStyleName(CalendarStyleManager.MORE_PANEL);
    
    BeeLabel caption = new BeeLabel(Global.CONSTANTS.selectAppointment());
    caption.addStyleName(CalendarStyleManager.MORE_CAPTION);
    panel.add(caption);

    for (Appointment appointment : appointments) {
      boolean multi = appointment.isMultiDay();
      AppointmentWidget widget = new AppointmentWidget(appointment, multi, BeeConst.UNDEF);
      widget.render();
      
      panel.add(widget);
    }
    
    final Popup popup = new Popup(true, true, CalendarStyleManager.MORE_POPUP);

    Binder.addMouseDownHandler(panel, new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
          Appointment appointment = null;

          Element element = EventUtils.getEventTargetElement(event);
          for (int i = 0; i < panel.getWidgetCount(); i++) {
            if (panel.getWidget(i).getElement().isOrHasChild(element)) {
              appointment = ((AppointmentWidget) panel.getWidget(i)).getAppointment();
              popup.hide();
              break;
            }
          }
          
          if (appointment != null) {
            event.stopPropagation();
            openAppointment(appointment);
          }
        }
      }
    });
    
    popup.setAnimationEnabled(true);

    popup.setWidget(panel);
    popup.center();
  }
}