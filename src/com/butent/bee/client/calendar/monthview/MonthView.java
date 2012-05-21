package com.butent.bee.client.calendar.monthview;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;
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

  private final List<AppointmentWidget> appointmentsWidgets = Lists.newArrayList();
  private final List<AppointmentWidget> selectedAppointmentWidgets = Lists.newArrayList();

  private final AbsolutePanel appointmentCanvas = new AbsolutePanel();

  private final Map<Element, Integer> moreLabels = Maps.newHashMap();

  private final FlexTable monthCalendarGrid = new FlexTable();
  private final FlexCellFormatter cellFormatter = monthCalendarGrid.getFlexCellFormatter();

  private PickupDragController dragController = null;
  private MonthViewDropController monthViewDropController = null;

  private final MonthViewStyleManager styleManager = new MonthViewStyleManager();

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

    calendarWidget.addToRootPanel(monthCalendarGrid);

    monthCalendarGrid.setCellPadding(0);
    monthCalendarGrid.setCellSpacing(0);
    monthCalendarGrid.setBorderWidth(0);

    monthCalendarGrid.setStyleName(GRID_STYLE);

    calendarWidget.addToRootPanel(appointmentCanvas);
    StyleUtils.makeAbsolute(appointmentCanvas);
    appointmentCanvas.setStyleName(CANVAS_STYLE);

    selectedAppointmentWidgets.clear();

    if (dragController == null) {
      dragController = new MonthViewPickupDragController(appointmentCanvas, true);
      dragController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          calendarWidget.setCommittedAppointment(appt);
          calendarWidget.fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          calendarWidget.setRollbackAppointment(appt.clone());
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

    appointmentsWidgets.clear();
    moreLabels.clear();
    
    selectedAppointmentWidgets.clear();
    while (monthCalendarGrid.getRowCount() > 0) {
      monthCalendarGrid.removeRow(0);
    }

    buildCalendarGrid();

    calculateCellHeight();
    calculateCellAppointments();

    monthViewDropController.setDaysPerWeek(DAYS_IN_A_WEEK);
    monthViewDropController.setWeeksPerMonth(monthViewRequiredRows);
    monthViewDropController.setFirstDateDisplayed(firstDateDisplayed);

    Collections.sort(calendarWidget.getAppointments(), APPOINTMENT_COMPARATOR);
    MonthLayoutDescription monthLayoutDescription = new MonthLayoutDescription(firstDateDisplayed,
        monthViewRequiredRows, calendarWidget.getAppointments(), calculatedCellAppointments - 1);

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

  public String getStyleName() {
    return MONTH_VIEW;
  }

  @Override
  public void onAppointmentSelected(Appointment appt) {
    List<AppointmentWidget> clickedAppointmentWidgets = findAppointmentWidgets(appt);

    if (!clickedAppointmentWidgets.isEmpty()) {
      for (AppointmentWidget widget : selectedAppointmentWidgets) {
        styleManager.applyStyle(widget, false);
      }

      for (AppointmentWidget widget : clickedAppointmentWidgets) {
        styleManager.applyStyle(widget, true);
      }

      selectedAppointmentWidgets.clear();
      selectedAppointmentWidgets.addAll(clickedAppointmentWidgets);
    }
  }

  public void onDeleteKeyPressed() {
    if (calendarWidget.getSelectedAppointment() != null) {
      calendarWidget.fireDeleteEvent(calendarWidget.getSelectedAppointment());
    }
  }

  public void onDoubleClick(Element clickedElement, Event event) {
    if (clickedElement.equals(appointmentCanvas.getElement())) {
      if (calendarWidget.getSettings().getTimeBlockClickNumber() == TimeBlockClick.Double) {
        dayClicked(event);
      }
    } else {
      List<AppointmentWidget> list = findAppointmentWidgetsByElement(clickedElement);
      if (!list.isEmpty()) {
        calendarWidget.fireOpenEvent(list.get(0).getAppointment());
      }
    }
  }

  public void onMouseOver(Element element, Event event) {
    Appointment appointment = findAppointmentByElement(element);
    calendarWidget.fireMouseOverEvent(appointment, element);
  }

  @Override
  public void onSingleClick(Element clickedElement, Event event) {
    if (clickedElement.equals(appointmentCanvas.getElement())) {
      if (calendarWidget.getSettings().getTimeBlockClickNumber() == TimeBlockClick.Single) {
        dayClicked(event);
      }
    } else {
      Appointment appointment = findAppointmentByElement(clickedElement);
      if (appointment != null) {
        selectAppointment(appointment);
      } else {
        if (moreLabels.containsKey(clickedElement)) {
          calendarWidget.fireDateRequestEvent(cellDate(moreLabels.get(clickedElement)),
              clickedElement);
        }
      }
    }
  }

  public void scrollToHour(int hour) {
  }

  private void buildCalendarGrid() {
    for (int i = 0; i < DAYS_IN_A_WEEK; i++) {
      monthCalendarGrid.setText(0, i, CalendarFormat.INSTANCE.getDayOfWeekAbbreviatedNames()[i]);
      cellFormatter.setVerticalAlignment(0, i, HasVerticalAlignment.ALIGN_TOP);
      cellFormatter.setStyleName(0, i, WEEKDAY_LABEL_STYLE);
    }

    int month = calendarWidget.getDate().getMonth();
    firstDateDisplayed = firstDateShownInAMonthView(calendarWidget.getDate());

    JustDate today = TimeUtils.today();
    JustDate date = JustDate.copyOf(firstDateDisplayed);

    monthViewRequiredRows = monthViewRequiredRows(calendarWidget.getDate());

    for (int i = 1; i <= monthViewRequiredRows; i++) {
      for (int j = 0; j < DAYS_IN_A_WEEK; j++) {
        configureDayInGrid(i, j, BeeUtils.toString(date.getDom()), date.equals(today),
            date.getMonth() != month);
        TimeUtils.moveOneDayForward(date);
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
    cellFormatter.setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_TOP);
    cellFormatter.setStyleName(row, col, cellStyle.toString());
  }

  private void dayClicked(Event event) {
    int y = event.getClientY() - DOM.getAbsoluteTop(appointmentCanvas.getElement());
    int x = event.getClientX() - DOM.getAbsoluteLeft(appointmentCanvas.getElement());

    int row = y / (appointmentCanvas.getOffsetHeight() / monthViewRequiredRows);
    int col = x / (appointmentCanvas.getOffsetWidth() / DAYS_IN_A_WEEK);

    calendarWidget.fireTimeBlockClickEvent(cellDate(row * DAYS_IN_A_WEEK + col));
  }

  private Appointment findAppointmentByElement(Element element) {
    Appointment appointmentAtElement = null;
    for (AppointmentWidget widget : appointmentsWidgets) {
      if (DOM.isOrHasChild(widget.getElement(), element)) {
        appointmentAtElement = widget.getAppointment();
        break;
      }
    }
    return appointmentAtElement;
  }

  private List<AppointmentWidget> findAppointmentWidgets(Appointment appt) {
    List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
    if (appt != null) {
      for (AppointmentWidget widget : appointmentsWidgets) {
        if (widget.getAppointment().equals(appt)) {
          appointmentWidgets.add(widget);
        }
      }
    }
    return appointmentWidgets;
  }

  private List<AppointmentWidget> findAppointmentWidgetsByElement(Element element) {
    return findAppointmentWidgets(findAppointmentByElement(element));
  }

  private JustDate firstDateShownInAMonthView(HasDateValue dayInMonth) {
    JustDate date = TimeUtils.startOfMonth(dayInMonth);
    return TimeUtils.startOfWeek(date, (date.getDow() > 1) ? 0 : -1);
  }

  private void layOnAppointment(Appointment appointment, int colStart, int colEnd, int row,
      int cellPosition) {
    AppointmentWidget panel = new AppointmentWidget(appointment);

    placeItemInGrid(panel, colStart, colEnd, row, cellPosition);

    boolean selected = calendarWidget.isTheSelectedAppointment(appointment);
    styleManager.applyStyle(panel, selected);

    if (calendarWidget.getSettings().isDragDropEnabled()) {
      dragController.makeDraggable(panel);
    }

    if (selected) {
      selectedAppointmentWidgets.add(panel);
    }

    appointmentsWidgets.add(panel);
    appointmentCanvas.add(panel);
  }

  private void layOnNMoreLabel(int moreCount, int dayOfWeek, int weekOfMonth) {
    Label more = new Label(CalendarFormat.MESSAGES.more(moreCount));
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