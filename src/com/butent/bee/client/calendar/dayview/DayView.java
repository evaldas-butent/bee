package com.butent.bee.client.calendar.dayview;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.drop.DayViewDropController;
import com.butent.bee.client.calendar.drop.DayViewPickupDragController;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.dnd.ResizeController;
import com.butent.bee.client.modules.calendar.layout.AppointmentAdapter;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
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

  private ResizeController resizeController = null;

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

    resizeController.setSettings(getSettings());

    appointmentWidgets.clear();

    JustDate tmpDate = JustDate.copyOf(date);

    for (int i = 0; i < days; i++) {
      List<Appointment> simple = CalendarUtils.filterSimple(getAppointments(), tmpDate);

      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, days, getSettings());
        addAppointmentsToGrid(adapters, false);
      }

      TimeUtils.moveOneDayForward(tmpDate);
    }

    List<Appointment> multi = CalendarUtils.filterMulti(getAppointments(), date, days);
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

  @Override
  public boolean onClick(Element element, Event event) {
    AppointmentWidget widget = CalendarUtils.findWidget(appointmentWidgets, element);

    if (widget != null) {
      if (widget.canClick(element)) {
        openAppointment(widget.getAppointment());
        return true;
      } else {
        return false;
      }

    } else if (appointmentPanel.isGrid(element)) {
      timeBlockClick(event);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void scrollToHour(int hour) {
    if (hour > 0) {
      appointmentPanel.scrollToHour(hour, getSettings());
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
          Appointment appt = CalendarUtils.getDragAppointment(event.getContext());
          getCalendarWidget().setCommittedAppointment(appt);
          getCalendarWidget().fireUpdateEvent(appt);
        }

        public void onDragStart(DragStartEvent event) {
          Appointment appt = CalendarUtils.getDragAppointment(event.getContext());
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
      resizeController = new ResizeController(appointmentPanel.getGrid());
      resizeController.addDefaultHandler(getCalendarWidget());
    }
  }

  private int getMaxProxyHeight() {
    int maxProxyHeight = 2 * (appointmentPanel.getScrollArea().getOffsetHeight() / 3);
    return maxProxyHeight;
  }

  private void timeBlockClick(Event event) {
    DateTime dateTime = appointmentPanel.getCoordinatesDate(event.getClientX(), event.getClientY(),
        getSettings(), getDate(), getDays());
    createAppointment(dateTime, null);
  }
}
