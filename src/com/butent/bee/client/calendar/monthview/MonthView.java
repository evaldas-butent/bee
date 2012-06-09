package com.butent.bee.client.calendar.monthview;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.CalendarFormat;
import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.drop.MonthViewDropController;
import com.butent.bee.client.calendar.drop.MonthViewPickupDragController;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.HasDateValue;
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

          if (compare == 0) {
            compare = a1.getStart().compareTo(a2.getStart());
          }
          if (compare == 0) {
            compare = a2.getEnd().compareTo(a1.getEnd());
          }
          return compare;
        }
      };

  private static final int DAYS_IN_A_WEEK = 7;

  private static final String MONTH_VIEW = "bee-cal-MonthView";
  private static final String CANVAS_STYLE = "canvas";
  private static final String GRID_STYLE = "grid";
  private static final String CELL_STYLE = "dayCell";
  private static final String MORE_LABEL_STYLE = "moreAppointments";
  private static final String CELL_HEADER_STYLE = "dayCellLabel";
  private static final String WEEKDAY_LABEL_STYLE = "weekDayLabel";

  private static int appointmentHeight() {
    return 20;
  }

  private static int appointmentPaddingTop() {
    return 4;
  }

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();

  private final AbsolutePanel appointmentCanvas = new AbsolutePanel();

  private final Map<Element, Integer> moreLabels = Maps.newHashMap();

  private final FlexTable monthCalendarGrid = new FlexTable();
  private final FlexCellFormatter cellFormatter = monthCalendarGrid.getFlexCellFormatter();

  private PickupDragController dragController = null;
  private MonthViewDropController monthViewDropController = null;

  private JustDate firstDateDisplayed = null;

  private int monthViewRequiredRows = 5;

  private int calculatedWeekDayHeaderHeight;

  private int calculatedDayHeaderHeight;

  private int calculatedCellAppointments;

  private int calculatedCellOffsetHeight;

  private int calculatedCellHeight;

  public MonthView() {
    super();
  }

  public void attach(CalendarWidget widget) {
    super.attach(widget);

    addWidget(monthCalendarGrid);

    monthCalendarGrid.setCellPadding(0);
    monthCalendarGrid.setCellSpacing(0);
    monthCalendarGrid.setBorderWidth(0);

    monthCalendarGrid.setStyleName(GRID_STYLE);

    addWidget(appointmentCanvas);
    StyleUtils.makeAbsolute(appointmentCanvas);
    appointmentCanvas.setStyleName(CANVAS_STYLE);

    if (dragController == null) {
      dragController = new MonthViewPickupDragController(appointmentCanvas, true);
      dragController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          updateAppointment(appt, null, null, -1, -1);
        }

        public void onDragStart(DragStartEvent event) {
        }

        public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
        }

        public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
        }
      });

      dragController.setBehaviorDragStartSensitivity(5);
      dragController.setBehaviorDragProxy(true);
    }

    if (monthViewDropController == null) {
      monthViewDropController = new MonthViewDropController(appointmentCanvas, monthCalendarGrid);
      dragController.registerDropController(monthViewDropController);
    }
  }

  @Override
  public void doLayout() {
    appointmentCanvas.clear();
    monthCalendarGrid.clear();

    appointmentWidgets.clear();
    moreLabels.clear();

    while (monthCalendarGrid.getRowCount() > 0) {
      monthCalendarGrid.removeRow(0);
    }

    buildCalendarGrid();

    calculateCellHeight();
    calculateCellAppointments();

    monthViewDropController.setDaysPerWeek(DAYS_IN_A_WEEK);
    monthViewDropController.setWeeksPerMonth(monthViewRequiredRows);
    monthViewDropController.setFirstDateDisplayed(firstDateDisplayed);

    Collections.sort(getAppointments(), APPOINTMENT_COMPARATOR);
    MonthLayoutDescription monthLayoutDescription = new MonthLayoutDescription(firstDateDisplayed,
        monthViewRequiredRows, getAppointments(), calculatedCellAppointments - 1);

    WeekLayoutDescription[] weeks = monthLayoutDescription.getWeekDescriptions();
    for (int weekOfMonth = 0; weekOfMonth < weeks.length
        && weekOfMonth < monthViewRequiredRows; weekOfMonth++) {
      WeekLayoutDescription weekDescription = weeks[weekOfMonth];
      if (weekDescription != null) {
        layOnTopOfTheWeekHangingAppointments(weekDescription, weekOfMonth);
        layOnWeekDaysAppointments(weekDescription, weekOfMonth);
      }
    }
  }

  @Override
  public void doSizing() {
  }

  public String getStyleName() {
    return MONTH_VIEW;
  }

  @Override
  public Type getType() {
    return Type.MONTH;
  }

  @Override
  public boolean onClick(Element element, Event event) {
    if (element.equals(appointmentCanvas.getElement())) {
      dayClicked(event);
      return true;

    } else if (moreLabels.containsKey(element)) {
      getCalendarWidget().fireDateRequestEvent(cellDate(moreLabels.get(element)), element);
      return true;

    } else {
      AppointmentWidget widget = CalendarUtils.findWidget(appointmentWidgets, element);
      if (widget != null && widget.canClick(element)) {
        openAppointment(widget.getAppointment());
        return true;
      }
    }
    return false;
  }

  public void scrollToHour(int hour) {
  }

  private void buildCalendarGrid() {
    for (int i = 0; i < DAYS_IN_A_WEEK; i++) {
      monthCalendarGrid.setText(0, i, CalendarFormat.getDayOfWeekNames()[i]);
      cellFormatter.setStyleName(0, i, WEEKDAY_LABEL_STYLE);
    }

    JustDate date = getDate();
    int month = date.getMonth();
    firstDateDisplayed = firstDateShownInAMonthView(date);

    monthViewRequiredRows = monthViewRequiredRows(date);

    JustDate today = TimeUtils.today();
    JustDate tmpDate = JustDate.copyOf(firstDateDisplayed);
    for (int i = 1; i <= monthViewRequiredRows; i++) {
      for (int j = 0; j < DAYS_IN_A_WEEK; j++) {
        configureDayInGrid(i, j, BeeUtils.toString(tmpDate.getDom()), tmpDate.equals(today),
            tmpDate.getMonth() != month);
        TimeUtils.moveOneDayForward(tmpDate);
      }
    }
  }

  private void calculateCellAppointments() {
    int paddingTop = appointmentPaddingTop();
    int height = appointmentHeight();

    calculatedCellAppointments = (calculatedCellHeight - paddingTop) / (height + paddingTop) - 1;
  }

  private void calculateCellHeight() {
    int gridHeight = monthCalendarGrid.getOffsetHeight();
    int weekdayRowHeight = monthCalendarGrid.getRowFormatter().getElement(0).getOffsetHeight();
    int dayHeaderHeight = cellFormatter.getElement(1, 0).getFirstChildElement().getOffsetHeight();

    calculatedCellOffsetHeight = (gridHeight - weekdayRowHeight) / monthViewRequiredRows;
    calculatedCellHeight = calculatedCellOffsetHeight - dayHeaderHeight;
    calculatedWeekDayHeaderHeight = weekdayRowHeight;
    calculatedDayHeaderHeight = dayHeaderHeight;
  }

  private JustDate cellDate(int cell) {
    return TimeUtils.nextDay(firstDateDisplayed, cell);
  }

  private void configureDayInGrid(int row, int col, String text, boolean isToday,
      boolean notInCurrentMonth) {
    Label label = new Label(text);

    StringBuilder headerStyle = new StringBuilder(CELL_HEADER_STYLE);
    StringBuilder cellStyle = new StringBuilder(CELL_STYLE);

    if (isToday) {
      headerStyle.append("-today");
      cellStyle.append("-today");
    }
    if (notInCurrentMonth) {
      headerStyle.append("-disabled");
    }

    label.setStyleName(headerStyle.toString());

    switch (col) {
      case 0:
        cellStyle.append(" firstColumn");
        break;
      case DAYS_IN_A_WEEK - 1:
        cellStyle.append(" lastColumn");
        break;
    }

    monthCalendarGrid.setWidget(row, col, label);
    cellFormatter.setStyleName(row, col, cellStyle.toString());
  }

  private void dayClicked(Event event) {
    int y = event.getClientY() - DOM.getAbsoluteTop(appointmentCanvas.getElement());
    int x = event.getClientX() - DOM.getAbsoluteLeft(appointmentCanvas.getElement());

    int row = y / (appointmentCanvas.getOffsetHeight() / monthViewRequiredRows);
    int col = x / (appointmentCanvas.getOffsetWidth() / DAYS_IN_A_WEEK);

    createAppointment(cellDate(row * DAYS_IN_A_WEEK + col).getDateTime(), null);
  }

  private JustDate firstDateShownInAMonthView(HasDateValue dayInMonth) {
    JustDate date = TimeUtils.startOfMonth(dayInMonth);
    return TimeUtils.startOfWeek(date, (date.getDow() > 1) ? 0 : -1);
  }

  private void layOnAppointment(Appointment appointment, int colStart, int colEnd, int row,
      int cellPosition) {
    AppointmentWidget panel = new AppointmentWidget(appointment, false, BeeConst.UNDEF);
    panel.renderCompact();

    placeItemInGrid(panel, colStart, colEnd, row, cellPosition);

    if (getSettings().isDragDropEnabled()) {
      dragController.makeDraggable(panel);
    }

    appointmentWidgets.add(panel);
    appointmentCanvas.add(panel);
  }

  private void layOnNMoreLabel(int moreCount, int dayOfWeek, int weekOfMonth) {
    Label more = new Label("+ " + moreCount);
    more.setStyleName(MORE_LABEL_STYLE);
    placeItemInGrid(more, dayOfWeek, dayOfWeek, weekOfMonth, calculatedCellAppointments);
    appointmentCanvas.add(more);
    moreLabels.put(more.getElement(), (dayOfWeek) + (weekOfMonth * 7));
  }

  private void layOnTopOfTheWeekHangingAppointments(WeekLayoutDescription weekDescription,
      int weekOfMonth) {
    AppointmentStackingManager weekTopElements = weekDescription.getTopAppointmentsManager();
    for (int layer = 0; layer < calculatedCellAppointments; layer++) {
      List<AppointmentLayoutDescription> descriptionsInLayer =
          weekTopElements.getDescriptionsInLayer(layer);
      if (descriptionsInLayer == null) {
        break;
      }

      for (AppointmentLayoutDescription weekTopElement : descriptionsInLayer) {
        layOnAppointment(weekTopElement.getAppointment(), weekTopElement.getWeekStartDay(),
            weekTopElement.getWeekEndDay(), weekOfMonth, layer);
      }
    }
  }

  private void layOnWeekDaysAppointments(WeekLayoutDescription week, int weekOfMonth) {
    AppointmentStackingManager topAppointmentManager = week.getTopAppointmentsManager();

    for (int dayOfWeek = 0; dayOfWeek < DAYS_IN_A_WEEK; dayOfWeek++) {
      DayLayoutDescription dayAppointments = week.getDayLayoutDescription(dayOfWeek);
      int appointmentLayer = topAppointmentManager.lowestLayerIndex(dayOfWeek);

      if (dayAppointments != null) {
        int count = dayAppointments.getAppointments().size();
        for (int i = 0; i < count; i++) {
          Appointment appointment = dayAppointments.getAppointments().get(i);
          appointmentLayer =
              topAppointmentManager.nextLowestLayerIndex(dayOfWeek, appointmentLayer);
          if (appointmentLayer > calculatedCellAppointments - 1) {
            int remaining =
                count + topAppointmentManager.multidayAppointmentsOverLimitOn(dayOfWeek) - i;
            if (remaining == 1) {
              layOnAppointment(appointment, dayOfWeek, dayOfWeek, weekOfMonth, appointmentLayer);
            } else {
              layOnNMoreLabel(remaining, dayOfWeek, weekOfMonth);
            }
            break;
          }
          layOnAppointment(appointment, dayOfWeek, dayOfWeek, weekOfMonth, appointmentLayer);
          appointmentLayer++;
        }
      } else if (topAppointmentManager.multidayAppointmentsOverLimitOn(dayOfWeek) > 0) {
        layOnNMoreLabel(topAppointmentManager.multidayAppointmentsOverLimitOn(dayOfWeek),
            dayOfWeek, weekOfMonth);
      }
    }
  }

  private int monthViewRequiredRows(HasDateValue dayInMonth) {
    int requiredRows = 5;

    JustDate firstOfTheMonth = TimeUtils.startOfMonth(dayInMonth);
    JustDate firstDayInCalendar = firstDateShownInAMonthView(dayInMonth);

    if (firstDayInCalendar.getMonth() != firstOfTheMonth.getMonth()) {
      JustDate lastDayOfPreviousMonth = TimeUtils.previousDay(firstOfTheMonth);
      int prevMonthOverlap = TimeUtils.dayDiff(firstDayInCalendar, lastDayOfPreviousMonth) + 1;

      JustDate firstOfNextMonth = TimeUtils.startOfNextMonth(firstOfTheMonth);
      int daysInMonth = TimeUtils.dayDiff(firstOfTheMonth, firstOfNextMonth);

      if (prevMonthOverlap + daysInMonth > 35) {
        requiredRows = 6;
      }
    }
    return requiredRows;
  }

  private void placeItemInGrid(Widget widget, int colStart, int colEnd, int row, int cellPosition) {
    int paddingTop = appointmentPaddingTop();
    int height = appointmentHeight();

    double left = (double) colStart / (double) DAYS_IN_A_WEEK * 100d + .5d;
    double width = ((double) (colEnd - colStart + 1) / (double) DAYS_IN_A_WEEK) * 100d - 1d;

    int top = calculatedWeekDayHeaderHeight + (row * calculatedCellOffsetHeight)
        + calculatedDayHeaderHeight + paddingTop + (cellPosition * (height + paddingTop));

    StyleUtils.makeAbsolute(widget);
    StyleUtils.setTop(widget, top);

    widget.getElement().getStyle().setLeft(left, Unit.PCT);
    widget.getElement().getStyle().setWidth(width, Unit.PCT);
  }
}