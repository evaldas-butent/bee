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
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

public class ResourceView extends CalendarView {

  private ResourceViewHeader viewHeader = null;
  private MultiDayPanel viewMulti = null;
  private AppointmentPanel viewBody = null;

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();
  private final List<AppointmentWidget> selectedAppointmentWidgets = Lists.newArrayList();

  private final CalendarStyleManager styleManager = new CalendarStyleManager();

  private PickupDragController dragController = null;
  private ResourceViewDropController dropController = null;

  private ResourceViewResizeController resizeController = null;

  public ResourceView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    if (viewBody == null) {
      viewHeader = new ResourceViewHeader();
      viewMulti = new MultiDayPanel();
      viewBody = new AppointmentPanel(getSettings());
    }

    addWidget(viewHeader);
    addWidget(viewMulti);
    addWidget(viewBody);

    createDragController();
    createDropController();
    createResizeController();
  }

  public void doLayout() {
    JustDate date = getDate();
    List<Long> attendees = getCalendarWidget().getAttendees();
    int cc = attendees.size();

    viewHeader.setAttendees(attendees);
    viewHeader.setDate(date);

    viewMulti.setColumnCount(cc);

    viewBody.getTimeline().prepare(getSettings());
    viewBody.getGrid().build(cc, getSettings());

    dropController.setColumns(cc);
    dropController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    dropController.setDate(JustDate.copyOf(date));
    dropController.setMaxProxyHeight(getMaxProxyHeight());

    resizeController.setIntervalsPerHour(getSettings().getIntervalsPerHour());
    resizeController.setSnapSize(getSettings().getPixelsPerInterval());

    appointmentWidgets.clear();
    selectedAppointmentWidgets.clear();
    
    int multiHeight = BeeConst.UNDEF;

    for (int i = 0; i < cc; i++) {
      Long id = attendees.get(i);

      List<Appointment> simple = AppointmentUtils.filterSimple(getAppointments(), date, id);
      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, cc, getSettings());
        addAppointmentsToGrid(adapters, false);
      }

      List<Appointment> multi = AppointmentUtils.filterMulti(getAppointments(), date, 1, id);
      if (!multi.isEmpty()) {
        List<AppointmentAdapter> adapters = Lists.newArrayList();
        for (Appointment appointment : multi) {
          adapters.add(new AppointmentAdapter(appointment));
        }
        
        multiHeight = Math.max(multiHeight,
            CalendarLayoutManager.doMultiLayout(adapters, date, i, cc));
        addAppointmentsToGrid(adapters, true);
      }
    }
    
    if (multiHeight > 0) {
      StyleUtils.setHeight(viewMulti.getGrid(), multiHeight);
    }
  }

  public void doSizing() {
    if (getCalendarWidget().getOffsetHeight() > 0) {
      StyleUtils.setHeight(viewBody, getCalendarWidget().getOffsetHeight()
          - 2 - viewHeader.getOffsetHeight() - viewMulti.getOffsetHeight());
    }
  }

  @Override
  public String getStyleName() {
    return CalendarStyleManager.RESOURCE_VIEW;
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
        && element == viewBody.getGrid().getGridOverlay().getElement()) {
      int x = DOM.eventGetClientX(event);
      int y = DOM.eventGetClientY(event);
      timeBlockClick(x, y);
    }
  }

  public void onSingleClick(Element element, Event event) {
    if (viewBody.getScrollArea().getElement().equals(element)) {
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
    if (hour > 0) {
      viewBody.getScrollArea().getElement().setScrollTop(hour *
          getSettings().getIntervalsPerHour() * getSettings().getPixelsPerInterval());
    }
  }

  private void addAppointmentsToGrid(List<AppointmentAdapter> appointmentList,
      boolean addToMultiView) {
    for (AppointmentAdapter appt : appointmentList) {
      AppointmentWidget panel = new AppointmentWidget(appt.getAppointment(), addToMultiView);

      panel.setLeft(appt.getLeft());
      panel.setWidth(appt.getWidth());

      panel.setTop(appt.getTop());
      panel.setHeight(appt.getHeight());

      panel.render();

      boolean selected = getCalendarWidget().isTheSelectedAppointment(panel.getAppointment());
      if (selected) {
        selectedAppointmentWidgets.add(panel);
      }
      styleManager.applyStyle(panel, selected);
      appointmentWidgets.add(panel);

      if (addToMultiView) {
        viewMulti.getGrid().add(panel);
      } else {
        viewBody.getGrid().getGrid().add(panel);

        if (getSettings().isDragDropEnabled()) {
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
          getCalendarWidget().setCommittedAppointment(appt);
          getCalendarWidget().fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
          getCalendarWidget().setRollbackAppointment(appt.clone());
          ((ResourceViewPickupDragController) dragController)
              .setMaxProxyHeight(getMaxProxyHeight());
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
    int left = viewBody.getGrid().getGridOverlay().getAbsoluteLeft();
    int top = viewBody.getScrollArea().getAbsoluteTop();
    int width = viewBody.getGrid().getGridOverlay().getOffsetWidth();
    int scrollOffset = viewBody.getScrollArea().getElement().getScrollTop();

    double relativeY = y - top + scrollOffset;
    double relativeX = x - left;

    double interval = Math.floor(relativeY / getSettings().getPixelsPerInterval());
    double day = Math.floor(relativeX / width);

    DateTime newStartDate = getDate().getDateTime();
    newStartDate.setHour(0);
    newStartDate.setMinute(0);
    newStartDate.setSecond(0);
    newStartDate.setMinute((int) interval * (60 / getSettings().getIntervalsPerHour()));
    newStartDate.setDom(newStartDate.getDom() + (int) day);

    return newStartDate;
  }

  private int getMaxProxyHeight() {
    int maxProxyHeight = 2 * (viewBody.getScrollArea().getOffsetHeight() / 3);
    return maxProxyHeight;
  }

  private void timeBlockClick(int x, int y) {
    DateTime newStartDate = getCoordinatesDate(x, y);
    getCalendarWidget().fireTimeBlockClickEvent(newStartDate);
  }
}
