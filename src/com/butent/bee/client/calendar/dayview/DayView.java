package com.butent.bee.client.calendar.dayview;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.CalendarSettings.Click;
import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.drop.DayViewDropController;
import com.butent.bee.client.calendar.drop.DayViewPickupDragController;
import com.butent.bee.client.calendar.drop.DayViewResizeController;
import com.butent.bee.client.calendar.util.AppointmentUtil;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;

public class DayView extends CalendarView {

  private DayViewHeader dayViewHeader = null;
  private DayViewMultiDayBody multiViewBody = null;
  private DayViewBody dayViewBody = null;

  private DayViewLayoutStrategy layoutStrategy = null;

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
  private final List<AppointmentWidget> selectedAppointmentWidgets = Lists.newArrayList();

  private final DayViewStyleManager styleManager = new DayViewStyleManager();

  private PickupDragController dragController = null;
  private DayViewDropController dropController = null;

  private DayViewResizeController resizeController = null;
  private DayViewResizeController proxyResizeController = null;

  public DayView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    if (dayViewBody == null) {
      dayViewHeader = new DayViewHeader();
      multiViewBody = new DayViewMultiDayBody();
      dayViewBody = new DayViewBody(this);

      layoutStrategy = new DayViewLayoutStrategy(this);
    }

    calendarWidget.getRootPanel().add(dayViewHeader);
    calendarWidget.getRootPanel().add(multiViewBody);
    calendarWidget.getRootPanel().add(dayViewBody);

    if (getSettings() != null) {
      scrollToHour(getSettings().getScrollToHour());
    }

    createDragController();
    createDropController();
    createResizeController();
  }

  public void doLayout() {
    JustDate date = calendarWidget.getDate();

    dayViewHeader.setDays(date, calendarWidget.getDays());
    dayViewHeader.setYear(date);

    multiViewBody.setDays(calendarWidget.getDays());

    dayViewBody.setDays(calendarWidget.getDays());
    dayViewBody.getTimeline().prepare();

    dropController.setColumns(calendarWidget.getDays());
    dropController.setIntervalsPerHour(calendarWidget.getSettings().getIntervalsPerHour());
    dropController.setDate(JustDate.copyOf(date));
    dropController.setSnapSize(calendarWidget.getSettings().getPixelsPerInterval());
    dropController.setMaxProxyHeight(getMaxProxyHeight());

    resizeController.setIntervalsPerHour(calendarWidget.getSettings().getIntervalsPerHour());
    resizeController.setSnapSize(calendarWidget.getSettings().getPixelsPerInterval());

    proxyResizeController.setSnapSize(calendarWidget.getSettings().getPixelsPerInterval());
    proxyResizeController.setIntervalsPerHour(calendarWidget.getSettings().getIntervalsPerHour());

    appointmentWidgets.clear();
    selectedAppointmentWidgets.clear();

    JustDate tmpDate = JustDate.copyOf(date);

    for (int i = 0; i < calendarWidget.getDays(); i++) {
      List<Appointment> filteredList =
          AppointmentUtil.filterListByDate(calendarWidget.getAppointments(), tmpDate);
      List<AppointmentAdapter> appointmentAdapters =
          layoutStrategy.doLayout(filteredList, i, calendarWidget.getDays());

      addAppointmentsToGrid(appointmentAdapters, false);
      TimeUtils.moveOneDayForward(tmpDate);
    }

    List<Appointment> filteredList =
        AppointmentUtil.filterListByDateRange(calendarWidget.getAppointments(),
            calendarWidget.getDate(), calendarWidget.getDays());

    List<AppointmentAdapter> adapterList = Lists.newArrayList();
    int desiredHeight = layoutStrategy.doMultiDayLayout(filteredList,
        adapterList, calendarWidget.getDate(), calendarWidget.getDays());

    StyleUtils.setHeight(multiViewBody.getGrid(), desiredHeight);
    addAppointmentsToGrid(adapterList, true);
  }

  public void doSizing() {
    if (calendarWidget.getOffsetHeight() > 0) {
      StyleUtils.setHeight(dayViewBody,
          calendarWidget.getOffsetHeight() - 2 - dayViewHeader.getOffsetHeight()
              - multiViewBody.getOffsetHeight());
    }
  }

  @Override
  public String getStyleName() {
    return "bee-cal";
  }

  @Override
  public void onAppointmentSelected(Appointment appt) {
    List<AppointmentWidget> clickedAppointmentAdapters = findAppointmentWidget(appt);

    if (!clickedAppointmentAdapters.isEmpty()) {
      for (AppointmentWidget adapter : selectedAppointmentWidgets) {
        styleManager.applyStyle(adapter, false);
      }

      for (AppointmentWidget adapter : clickedAppointmentAdapters) {
        styleManager.applyStyle(adapter, true);
      }

      selectedAppointmentWidgets.clear();
      selectedAppointmentWidgets.addAll(clickedAppointmentAdapters);

      double height = clickedAppointmentAdapters.get(0).getHeight();
      if (dayViewBody.getScrollPanel().getOffsetHeight() > height) {
        DOM.scrollIntoView(clickedAppointmentAdapters.get(0).getElement());
      }
    }
  }

  public void onDeleteKeyPressed() {
    if (calendarWidget.getSelectedAppointment() != null) {
      calendarWidget.fireDeleteEvent(calendarWidget.getSelectedAppointment());
    }
  }

  public void onDoubleClick(Element element, Event event) {
    List<AppointmentWidget> list = findAppointmentWidgetsByElement(element);

    if (!list.isEmpty()) {
      Appointment appt = list.get(0).getAppointment();
      calendarWidget.fireOpenEvent(appt);

    } else if (getSettings().getTimeBlockClickNumber() == Click.Double
        && element == dayViewBody.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onDownArrowKeyPressed() {
    calendarWidget.selectNextAppointment();
  }

  public void onLeftArrowKeyPressed() {
    calendarWidget.selectPreviousAppointment();
  }

  public void onMouseOver(Element element, Event event) {
    Appointment appointment = findAppointmentByElement(element);
    calendarWidget.fireMouseOverEvent(appointment, element);
  }

  public void onRightArrowKeyPressed() {
    calendarWidget.selectNextAppointment();
  }

  public void onSingleClick(Element element, Event event) {
    if (dayViewBody.getScrollPanel().getElement().equals(element)) {
      return;
    }

    Appointment appt = findAppointmentByElement(element);

    if (appt != null) {
      selectAppointment(appt);
    } else if ((getSettings().getTimeBlockClickNumber() == Click.Single
        || getSettings().getEnableDragDropCreation())
        && element == dayViewBody.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onUpArrowKeyPressed() {
    calendarWidget.selectPreviousAppointment();
  }

  @Override
  public void scrollToHour(int hour) {
    dayViewBody.getScrollPanel().setVerticalScrollPosition(hour *
        getSettings().getIntervalsPerHour() * getSettings().getPixelsPerInterval());
  }

  private void addAppointmentsToGrid(List<AppointmentAdapter> appointmentList,
      boolean addToMultiView) {
    for (AppointmentAdapter appt : appointmentList) {
      AppointmentWidget panel = new AppointmentWidget();

      panel.setLeft(appt.getLeft());
      panel.setWidth(appt.getWidth());

      panel.setTop(appt.getTop());
      panel.setHeight(appt.getHeight());

      panel.setAppointment(appt.getAppointment());
      panel.setTitle(appt.getAppointment().getTitle());

      boolean selected = calendarWidget.isTheSelectedAppointment(panel.getAppointment());
      if (selected) {
        selectedAppointmentWidgets.add(panel);
      }
      styleManager.applyStyle(panel, selected);
      appointmentWidgets.add(panel);

      if (addToMultiView) {
        panel.setMultiDay(true);
        multiViewBody.getGrid().add(panel);
      } else {
        panel.setDescription(appt.getAppointment().getDescription());
        dayViewBody.getGrid().getGrid().add(panel);

        if (calendarWidget.getSettings().isEnableDragDrop() && !appt.getAppointment().isReadOnly()) {
          resizeController.makeDraggable(panel.getResizeHandle());
          dragController.makeDraggable(panel, panel.getMoveHandle());
        }
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new DayViewPickupDragController(dayViewBody.getGrid().getGrid(), false);

      dragController.setBehaviorDragProxy(true);
      dragController.setBehaviorDragStartSensitivity(1);
      dragController.setBehaviorConstrainedToBoundaryPanel(true);
      dragController.setConstrainWidgetToBoundaryPanel(true);
      dragController.setBehaviorMultipleSelection(false);

      dragController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          calendarWidget.setCommittedAppointment(appt);
          calendarWidget.fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          calendarWidget.setRollbackAppointment(appt.clone());
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
      dropController = new DayViewDropController(dayViewBody.getGrid().getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new DayViewResizeController(dayViewBody.getGrid().getGrid());

      resizeController.addDragHandler(new DragHandler() {
        public void onDragEnd(DragEndEvent event) {
          Appointment appt =
              ((AppointmentWidget) event.getContext().draggable.getParent()).getAppointment();
          calendarWidget.setCommittedAppointment(appt);
          calendarWidget.fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          calendarWidget.setRollbackAppointment(((AppointmentWidget) event.getContext().draggable
              .getParent()).getAppointment().clone());
        }

        public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
        }

        public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
        }
      });
    }

    if (proxyResizeController == null) {
      proxyResizeController = new DayViewResizeController(dayViewBody.getGrid().getGrid());

      proxyResizeController.addDragHandler(new DragHandler() {
        long startTime = 0L;
        int initialX = 0;
        int initialY = 0;

        DateTime startDate;

        public void onDragEnd(DragEndEvent event) {
          long clickTime = System.currentTimeMillis() - startTime;
          int y = event.getContext().mouseY;
          if (clickTime <= 500 && initialY == y) {
            calendarWidget.fireTimeBlockClickEvent(startDate);
          } else {
            Appointment appt =
                ((AppointmentWidget) event.getContext().draggable.getParent()).getAppointment();
            calendarWidget.fireCreateEvent(appt);
          }
        }

        public void onDragStart(DragStartEvent event) {
          startTime = System.currentTimeMillis();
          initialX = event.getContext().mouseX;
          initialY = event.getContext().mouseY;
          startDate = getCoordinatesDate(initialX, initialY);
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
    int left = dayViewBody.getGrid().getGridOverlay().getAbsoluteLeft();
    int top = dayViewBody.getScrollPanel().getAbsoluteTop();
    int width = dayViewBody.getGrid().getGridOverlay().getOffsetWidth();
    int scrollOffset = dayViewBody.getScrollPanel().getVerticalScrollPosition();

    double relativeY = y - top + scrollOffset;
    double relativeX = x - left;

    double interval = Math.floor(relativeY / getSettings().getPixelsPerInterval());
    double day = Math.floor(relativeX / ((double) width / calendarWidget.getDays()));

    DateTime newStartDate = calendarWidget.getDate().getDateTime();
    newStartDate.setHour(0);
    newStartDate.setMinute(0);
    newStartDate.setSecond(0);
    newStartDate.setMinute((int) interval * (60 / getSettings().getIntervalsPerHour()));
    newStartDate.setDom(newStartDate.getDom() + (int) day);

    return newStartDate;
  }

  private int getMaxProxyHeight() {
    int maxProxyHeight = 2 * (dayViewBody.getScrollPanel().getOffsetHeight() / 3);
    return maxProxyHeight;
  }

  private void timeBlockClick(int x, int y) {
    int left = dayViewBody.getGrid().getGridOverlay().getAbsoluteLeft();
    int top = dayViewBody.getScrollPanel().getAbsoluteTop();
    int width = dayViewBody.getGrid().getGridOverlay().getOffsetWidth();
    int scrollOffset = dayViewBody.getScrollPanel().getVerticalScrollPosition();

    double relativeY = y - top + scrollOffset;
    double relativeX = x - left;

    double day = Math.floor(relativeX / ((double) width / calendarWidget.getDays()));

    DateTime newStartDate = getCoordinatesDate(x, y);

    if (getSettings().getTimeBlockClickNumber() != Click.Drag) {
      calendarWidget.fireTimeBlockClickEvent(newStartDate);
    } else {
      int snapSize = calendarWidget.getSettings().getPixelsPerInterval();
      width = width / calendarWidget.getDays();

      int height = snapSize;
      
      left = (int) day * width;
      top = (int) Math.floor(relativeY / snapSize) * snapSize;

      Appointment app = new Appointment();
      app.setStart(newStartDate);
      app.setEnd(newStartDate);
      
      AppointmentWidget proxy = new AppointmentWidget();
      proxy.setAppointment(app);
      proxy.setStart(newStartDate);
      proxy.setPixelSize(width, height);

      dayViewBody.getGrid().getGrid().add(proxy, left, top);
      styleManager.applyStyle(proxy, false);
      proxyResizeController.makeDraggable(proxy.getResizeHandle());

      NativeEvent evt = Document.get().createMouseDownEvent(1, 0, 0, x, y, false,
          false, false, false, NativeEvent.BUTTON_LEFT);
      proxy.getResizeHandle().getElement().dispatchEvent(evt);
    }
  }
}
