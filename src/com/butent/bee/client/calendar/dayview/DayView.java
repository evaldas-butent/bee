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

  private DayViewHeader dayViewHeader = null;
  private MultiDayPanel multiDayPanel = null;
  private AppointmentPanel appointmentPanel = null;

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
  private final List<AppointmentWidget> selectedAppointmentWidgets = Lists.newArrayList();

  private final CalendarStyleManager styleManager = new CalendarStyleManager();

  private PickupDragController dragController = null;
  private DayViewDropController dropController = null;

  private DayViewResizeController resizeController = null;

  public DayView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    if (appointmentPanel == null) {
      dayViewHeader = new DayViewHeader();
      multiDayPanel = new MultiDayPanel();
      appointmentPanel = new AppointmentPanel(getSettings());
    }

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

    appointmentPanel.getTimeline().prepare(getSettings());
    appointmentPanel.getGrid().build(days, getSettings());

    dropController.setColumns(days);
    dropController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    dropController.setDate(JustDate.copyOf(date));
    dropController.setSnapSize(getSettings().getPixelsPerInterval());
    dropController.setMaxProxyHeight(getMaxProxyHeight());

    resizeController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    resizeController.setSnapSize(getSettings().getPixelsPerInterval());

    appointmentWidgets.clear();
    selectedAppointmentWidgets.clear();

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

  @Override
  public void onAppointmentSelected(Appointment appt) {
    List<AppointmentWidget> clickedAppointmentAdapters = findAppointmentWidget(appt);

    if (!clickedAppointmentAdapters.isEmpty()) {
      for (AppointmentWidget adapter : selectedAppointmentWidgets) {
        styleManager.applySelected(adapter, false);
      }

      for (AppointmentWidget adapter : clickedAppointmentAdapters) {
        styleManager.applySelected(adapter, true);
      }

      selectedAppointmentWidgets.clear();
      selectedAppointmentWidgets.addAll(clickedAppointmentAdapters);
    }
  }

  public void onDoubleClick(Element element, Event event) {
    List<AppointmentWidget> list = findAppointmentWidgetsByElement(element);

    if (!list.isEmpty()) {
      Appointment appt = list.get(0).getAppointment();
      getCalendarWidget().fireOpenEvent(appt);

    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Double
        && element == appointmentPanel.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onSingleClick(Element element, Event event) {
    if (appointmentPanel.getScrollArea().getElement().equals(element)) {
      return;
    }

    Appointment appt = findAppointmentByElement(element);

    if (appt != null) {
      selectAppointment(appt);
    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Single
        && element == appointmentPanel.getGrid().getGridOverlay().getElement()) {
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

  private void addAppointmentsToGrid(List<AppointmentAdapter> adapters, boolean addToMultiView) {
    for (AppointmentAdapter adapter : adapters) {
      AppointmentWidget panel = new AppointmentWidget(adapter.getAppointment(), addToMultiView);

      panel.setLeft(adapter.getLeft());
      panel.setWidth(adapter.getWidth());

      panel.setTop(adapter.getTop());
      panel.setHeight(adapter.getHeight());

      panel.render();

      boolean selected = getCalendarWidget().isTheSelectedAppointment(panel.getAppointment());
      if (selected) {
        selectedAppointmentWidgets.add(panel);
      }
      styleManager.applyStyle(panel, selected);
      appointmentWidgets.add(panel);

      if (addToMultiView) {
        multiDayPanel.getGrid().add(panel);
      } else {
        appointmentPanel.getGrid().getGrid().add(panel);

        if (getSettings().isDragDropEnabled()) {
          resizeController.makeDraggable(panel.getResizeHandle());
          dragController.makeDraggable(panel, panel.getMoveHandle());
        }
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new DayViewPickupDragController(appointmentPanel.getGrid().getGrid(), false);

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
      dropController = new DayViewDropController(appointmentPanel.getGrid().getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new DayViewResizeController(appointmentPanel.getGrid().getGrid());

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

  private Appointment findAppointmentByElement(Element element) {
    Appointment appointmentAtElement = null;
    for (AppointmentWidget widget : appointmentWidgets) {
      if (DOM.isOrHasChild(widget.getElement(), element)) {
        appointmentAtElement = widget.getAppointment();
        break;
      }
    }
    return appointmentAtElement;
  }

  private List<AppointmentWidget> findAppointmentWidget(Appointment appt) {
    List<AppointmentWidget> appointmentAdapters = Lists.newArrayList();
    if (appt != null) {
      for (AppointmentWidget widget : appointmentWidgets) {
        if (widget.getAppointment().equals(appt)) {
          appointmentAdapters.add(widget);
        }
      }
    }
    return appointmentAdapters;
  }

  private List<AppointmentWidget> findAppointmentWidgetsByElement(Element element) {
    return findAppointmentWidget(findAppointmentByElement(element));
  }

  private DateTime getCoordinatesDate(int x, int y) {
    int left = appointmentPanel.getGrid().getGridOverlay().getAbsoluteLeft();
    int top = appointmentPanel.getScrollArea().getAbsoluteTop();
    int width = appointmentPanel.getGrid().getGridOverlay().getOffsetWidth();
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
