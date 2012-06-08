package com.butent.bee.client.calendar.dayview;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.drop.DayViewDropController;
import com.butent.bee.client.calendar.drop.DayViewPickupDragController;
import com.butent.bee.client.calendar.drop.DayViewResizeController;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.layout.AppointmentAdapter;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

import java.util.List;

public class DayView extends CalendarView {

  private final DayViewHeader dayViewHeader = new DayViewHeader();
  private final MultiDayPanel multiDayPanel = new MultiDayPanel();
  private final AppointmentPanel appointmentPanel = new AppointmentPanel();

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();

  private PickupDragController dragController = null;
  private DayViewDropController dropController = null;

  private DayViewResizeController resizeController = null;

  public DayView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    addWidget(dayViewHeader);
    addWidget(multiDayPanel);
    addWidget(appointmentPanel);

    createDragController();
    createDropController();
    createResizeController();
  }

  public void doLayout() {
    JustDate date = getDate();
    int days = getDays();

    dayViewHeader.setDays(date, days);
    dayViewHeader.setYear(date);

    multiDayPanel.setColumnCount(days);

    appointmentPanel.build(days, getSettings());

    dropController.setColumns(days);
    dropController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    dropController.setDate(JustDate.copyOf(date));
    dropController.setSnapSize(getSettings().getPixelsPerInterval());
    dropController.setMaxProxyHeight(getMaxProxyHeight());

    resizeController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    resizeController.setSnapSize(getSettings().getPixelsPerInterval());

    appointmentWidgets.clear();

    JustDate tmpDate = JustDate.copyOf(date);

    for (int i = 0; i < days; i++) {
      List<Appointment> simple = AppointmentUtils.filterSimple(getAppointments(), tmpDate);

      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, days, getSettings());
        addAppointmentsToGrid(adapters, false);
      }

      TimeUtils.moveOneDayForward(tmpDate);
    }

    List<Appointment> multi = AppointmentUtils.filterMulti(getAppointments(), date, days);
    if (!multi.isEmpty()) {
      List<AppointmentAdapter> adapters = Lists.newArrayList();
      for (Appointment appointment : multi) {
        adapters.add(new AppointmentAdapter(appointment));
      }

      int desiredHeight = CalendarLayoutManager.doMultiLayout(adapters, date, days);
      StyleUtils.setHeight(multiDayPanel.getGrid(), desiredHeight);

      addAppointmentsToGrid(adapters, true);
    } else {
      StyleUtils.clearHeight(multiDayPanel.getGrid());
    }
  }

  public void doSizing() {
    if (getCalendarWidget().getOffsetHeight() > 0) {
      StyleUtils.setHeight(appointmentPanel, getCalendarWidget().getOffsetHeight()
          - 2 - dayViewHeader.getOffsetHeight() - multiDayPanel.getOffsetHeight());
    }
  }

  @Override
  public String getStyleName() {
    return CalendarStyleManager.DAY_VIEW;
  }

  @Override
  public Type getType() {
    return Type.DAY;
  }

  public void onDoubleClick(Element element, Event event) {
    List<AppointmentWidget> widgets = 
        AppointmentUtils.findAppointmentWidgets(appointmentWidgets, element);

    if (!widgets.isEmpty()) {
      getCalendarWidget().fireOpenEvent(widgets.get(0).getAppointment());

    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Double
        && element == appointmentPanel.getGrid().getOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onSingleClick(Element element, Event event) {
    if (appointmentPanel.getScrollArea().getElement().equals(element)) {
      return;
    }

    Appointment appt = AppointmentUtils.findAppointment(appointmentWidgets, element);

    if (appt != null) {
    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Single
        && element == appointmentPanel.getGrid().getOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  @Override
  public void scrollToHour(int hour) {
    if (hour > 0) {
      appointmentPanel.getScrollArea().getElement().setScrollTop(hour *
          getSettings().getIntervalsPerHour() * getSettings().getPixelsPerInterval());
    }
  }

  private void addAppointmentsToGrid(List<AppointmentAdapter> adapters, boolean multi) {
    for (AppointmentAdapter adapter : adapters) {
      AppointmentWidget widget = new AppointmentWidget(adapter.getAppointment(), multi);

      widget.setLeft(adapter.getLeft());
      widget.setWidth(adapter.getWidth());

      widget.setTop(adapter.getTop());
      widget.setHeight(adapter.getHeight());

      widget.render();

      appointmentWidgets.add(widget);

      if (multi) {
        multiDayPanel.getGrid().add(widget);
      } else {
        appointmentPanel.getGrid().add(widget);

        if (getSettings().isDragDropEnabled()) {
          resizeController.makeDraggable(widget.getResizeHandle());
          dragController.makeDraggable(widget, widget.getMoveHandle());
        }
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new DayViewPickupDragController(appointmentPanel.getGrid(), false);

      dragController.setBehaviorDragProxy(true);
      dragController.setBehaviorDragStartSensitivity(1);
      dragController.setBehaviorConstrainedToBoundaryPanel(true);
      dragController.setConstrainWidgetToBoundaryPanel(true);
      dragController.setBehaviorMultipleSelection(false);

      dragController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          getCalendarWidget().setCommittedAppointment(appt);
          getCalendarWidget().fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          getCalendarWidget().setRollbackAppointment(appt.clone());
          ((DayViewPickupDragController) dragController).setMaxProxyHeight(getMaxProxyHeight());
        }

        public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
        }

        public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
        }
      });
    }
  }

  private void createDropController() {
    if (dropController == null) {
      dropController = new DayViewDropController(appointmentPanel.getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new DayViewResizeController(appointmentPanel.getGrid());

      resizeController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt =
              ((AppointmentWidget) event.getContext().draggable.getParent()).getAppointment();
          getCalendarWidget().setCommittedAppointment(appt);
          getCalendarWidget().fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          getCalendarWidget().setRollbackAppointment(
              ((AppointmentWidget) event.getContext().draggable
                  .getParent()).getAppointment().clone());
        }

        public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
        }

        public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
        }
      });
    }
  }

  private DateTime getCoordinatesDate(int x, int y) {
    int left = appointmentPanel.getGrid().getOverlay().getAbsoluteLeft();
    int top = appointmentPanel.getScrollArea().getAbsoluteTop();
    int width = appointmentPanel.getGrid().getOverlay().getOffsetWidth();
    int scrollOffset = appointmentPanel.getScrollArea().getElement().getScrollTop();

    double relativeY = y - top + scrollOffset;
    double relativeX = x - left;

    double interval = Math.floor(relativeY / getSettings().getPixelsPerInterval());
    double day = Math.floor(relativeX / ((double) width / getDays()));

    DateTime newStartDate = getDate().getDateTime();
    newStartDate.setHour(0);
    newStartDate.setMinute(0);
    newStartDate.setSecond(0);
    newStartDate.setMinute((int) interval * (60 / getSettings().getIntervalsPerHour()));
    newStartDate.setDom(newStartDate.getDom() + (int) day);

    return newStartDate;
  }

  private int getMaxProxyHeight() {
    int maxProxyHeight = 2 * (appointmentPanel.getScrollArea().getOffsetHeight() / 3);
    return maxProxyHeight;
  }

  private void timeBlockClick(int x, int y) {
    DateTime newStartDate = getCoordinatesDate(x, y);
    getCalendarWidget().fireTimeBlockClickEvent(newStartDate);
  }
}
