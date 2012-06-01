package com.butent.bee.client.calendar.resourceview;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.drop.ResourceViewDropController;
import com.butent.bee.client.calendar.drop.ResourceViewPickupDragController;
import com.butent.bee.client.calendar.drop.ResourceViewResizeController;
import com.butent.bee.client.calendar.util.AppointmentAdapter;
import com.butent.bee.client.calendar.util.AppointmentUtil;
import com.butent.bee.client.calendar.util.AppointmentWidget;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.Attendee;
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

public class ResourceView extends CalendarView {

  private ResourceViewHeader viewHeader = null;
  private ResourceViewMulti viewMulti = null;
  private ResourceViewBody viewBody = null;

  private ResourceViewLayoutStrategy layoutStrategy = null;

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
  private final List<AppointmentWidget> selectedAppointmentWidgets = Lists.newArrayList();

  private final ResourceViewStyleManager styleManager = new ResourceViewStyleManager();

  private PickupDragController dragController = null;
  private ResourceViewDropController dropController = null;

  private ResourceViewResizeController resizeController = null;
  
  private final int days = 1;

  public ResourceView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    if (viewBody == null) {
      viewHeader = new ResourceViewHeader();
      viewMulti = new ResourceViewMulti();
      viewBody = new ResourceViewBody(this);

      layoutStrategy = new ResourceViewLayoutStrategy(this);
    }

    calendarWidget.getRootPanel().add(viewHeader);
    calendarWidget.getRootPanel().add(viewMulti);
    calendarWidget.getRootPanel().add(viewBody);

    if (getSettings() != null) {
      scrollToHour(getSettings().getScrollToHour());
    }

    createDragController();
    createDropController();
    createResizeController();
  }

  public void doLayout() {
    JustDate date = calendarWidget.getDate();
    List<Attendee> attendees = calendarWidget.getAttendees();
    int cc = attendees.size();
    
    viewHeader.setAttendees(attendees);
    viewHeader.setDate(date);

    viewMulti.setAttendees(attendees);

    viewBody.setColumns(cc);
    viewBody.getTimeline().prepare();

    dropController.setColumns(cc);
    dropController.setIntervalsPerHour(calendarWidget.getSettings().getIntervalsPerHour());
    dropController.setDate(JustDate.copyOf(date));
    dropController.setSnapSize(calendarWidget.getSettings().getPixelsPerInterval());
    dropController.setMaxProxyHeight(getMaxProxyHeight());

    resizeController.setIntervalsPerHour(calendarWidget.getSettings().getIntervalsPerHour());
    resizeController.setSnapSize(calendarWidget.getSettings().getPixelsPerInterval());

    appointmentWidgets.clear();
    selectedAppointmentWidgets.clear();

    JustDate tmpDate = JustDate.copyOf(date);
    for (int i = 0; i < cc; i++) {
      List<Appointment> filteredList =
          AppointmentUtil.filterListByDateAndAttendee(calendarWidget.getAppointments(), tmpDate,
              attendees.get(i).getId());
      List<AppointmentAdapter> appointmentAdapters = layoutStrategy.doLayout(filteredList, i, cc);

      addAppointmentsToGrid(appointmentAdapters, false);
    }

    List<Appointment> filteredList =
        AppointmentUtil.filterListByDateRange(calendarWidget.getAppointments(),
            calendarWidget.getDate(), days);

    List<AppointmentAdapter> adapterList = Lists.newArrayList();
    int desiredHeight = layoutStrategy.doMultiDayLayout(filteredList,
        adapterList, calendarWidget.getDate(), days);

    StyleUtils.setHeight(viewMulti.getGrid(), desiredHeight);
    addAppointmentsToGrid(adapterList, true);
  }

  public void doSizing() {
    if (calendarWidget.getOffsetHeight() > 0) {
      StyleUtils.setHeight(viewBody,
          calendarWidget.getOffsetHeight() - 2 - viewHeader.getOffsetHeight()
              - viewMulti.getOffsetHeight());
    }
  }

  @Override
  public String getStyleName() {
    return "bee-cal";
  }

  @Override
  public Type getType() {
    return Type.RESOURCE;
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
      if (viewBody.getScrollPanel().getOffsetHeight() > height) {
        DOM.scrollIntoView(clickedAppointmentAdapters.get(0).getElement());
      }
    }
  }

  public void onDoubleClick(Element element, Event event) {
    List<AppointmentWidget> list = findAppointmentWidgetsByElement(element);

    if (!list.isEmpty()) {
      Appointment appt = list.get(0).getAppointment();
      calendarWidget.fireOpenEvent(appt);

    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Double
        && element == viewBody.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onMouseOver(Element element, Event event) {
    Appointment appointment = findAppointmentByElement(element);
    calendarWidget.fireMouseOverEvent(appointment, element);
  }

  public void onSingleClick(Element element, Event event) {
    if (viewBody.getScrollPanel().getElement().equals(element)) {
      return;
    }

    Appointment appt = findAppointmentByElement(element);

    if (appt != null) {
      selectAppointment(appt);
    } else if (getSettings().getTimeBlockClickNumber() == TimeBlockClick.Single
        && element == viewBody.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  @Override
  public void scrollToHour(int hour) {
    viewBody.getScrollPanel().setVerticalScrollPosition(hour *
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
      
      boolean selected = calendarWidget.isTheSelectedAppointment(panel.getAppointment());
      if (selected) {
        selectedAppointmentWidgets.add(panel);
      }
      styleManager.applyStyle(panel, selected);
      appointmentWidgets.add(panel);

      if (addToMultiView) {
        panel.setMultiDay(true);
        viewMulti.getGrid().add(panel);
      } else {
        panel.setDescription(appt.getAppointment());
        viewBody.getGrid().getGrid().add(panel);

        if (calendarWidget.getSettings().isDragDropEnabled()) {
          resizeController.makeDraggable(panel.getResizeHandle());
          dragController.makeDraggable(panel, panel.getMoveHandle());
        }
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new ResourceViewPickupDragController(viewBody.getGrid().getGrid(), false);

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
          ((ResourceViewPickupDragController) dragController).setMaxProxyHeight(getMaxProxyHeight());
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
      dropController = new ResourceViewDropController(viewBody.getGrid().getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new ResourceViewResizeController(viewBody.getGrid().getGrid());

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
    int left = viewBody.getGrid().getGridOverlay().getAbsoluteLeft();
    int top = viewBody.getScrollPanel().getAbsoluteTop();
    int width = viewBody.getGrid().getGridOverlay().getOffsetWidth();
    int scrollOffset = viewBody.getScrollPanel().getVerticalScrollPosition();

    double relativeY = y - top + scrollOffset;
    double relativeX = x - left;

    double interval = Math.floor(relativeY / getSettings().getPixelsPerInterval());
    double day = Math.floor(relativeX / ((double) width / days));

    DateTime newStartDate = calendarWidget.getDate().getDateTime();
    newStartDate.setHour(0);
    newStartDate.setMinute(0);
    newStartDate.setSecond(0);
    newStartDate.setMinute((int) interval * (60 / getSettings().getIntervalsPerHour()));
    newStartDate.setDom(newStartDate.getDom() + (int) day);

    return newStartDate;
  }

  private int getMaxProxyHeight() {
    int maxProxyHeight = 2 * (viewBody.getScrollPanel().getOffsetHeight() / 3);
    return maxProxyHeight;
  }

  private void timeBlockClick(int x, int y) {
    DateTime newStartDate = getCoordinatesDate(x, y);
    calendarWidget.fireTimeBlockClickEvent(newStartDate);
  }
}
