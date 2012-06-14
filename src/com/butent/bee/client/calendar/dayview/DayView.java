package com.butent.bee.client.calendar.dayview;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.CalendarView;
import com.butent.bee.client.modules.calendar.CalendarWidget;
import com.butent.bee.client.modules.calendar.dnd.DayDropController;
import com.butent.bee.client.modules.calendar.dnd.DayDragController;
import com.butent.bee.client.modules.calendar.dnd.ResizeController;
import com.butent.bee.client.modules.calendar.layout.AppointmentAdapter;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

import java.util.List;

public class DayView extends CalendarView {

  private final DayViewHeader dayViewHeader = new DayViewHeader();
  private final MultiDayPanel multiDayPanel = new MultiDayPanel();
  private final AppointmentPanel appointmentPanel = new AppointmentPanel();

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();

  private DayDragController dragController = null;
  private DayDropController dropController = null;

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

  @Override
  public void doLayout() {
    JustDate date = getDate();
    int days = getDisplayedDays();

    dayViewHeader.setDays(date, days);
    dayViewHeader.setYear(date);

    multiDayPanel.setColumnCount(days);
    
    int todayColumn = CalendarUtils.getTodayColumn(date, days);
    appointmentPanel.build(days, getSettings(), todayColumn, todayColumn);

    dragController.setDate(JustDate.copyOf(date));

    dropController.setColumns(days);
    dropController.setSettings(getSettings());

    resizeController.setSettings(getSettings());

    appointmentWidgets.clear();

    JustDate tmpDate = JustDate.copyOf(date);

    for (int i = 0; i < days; i++) {
      List<Appointment> simple = CalendarUtils.filterSimple(getAppointments(), tmpDate);

      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, days, getSettings());
        addAppointmentsToGrid(adapters, false, i);
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

      addAppointmentsToGrid(adapters, true, BeeConst.UNDEF);
    } else {
      StyleUtils.clearHeight(multiDayPanel.getGrid());
    }
  }

  @Override
  public void doScroll() {
    appointmentPanel.doScroll(getSettings());
  }

  @Override
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
  public void onClock() {
    appointmentPanel.onClock(getSettings());
  }
  
  private void addAppointmentsToGrid(List<AppointmentAdapter> adapters, boolean multi,
      int columnIndex) {

    for (AppointmentAdapter adapter : adapters) {
      AppointmentWidget widget = new AppointmentWidget(adapter.getAppointment(), multi,
          columnIndex);

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

        resizeController.makeDraggable(widget.getResizeHandle());
        dragController.makeDraggable(widget, widget.getMoveHandle());
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new DayDragController(appointmentPanel.getGrid());
      dragController.addDefaultHandler(this);
    }
  }

  private void createDropController() {
    if (dropController == null) {
      dropController = new DayDropController(appointmentPanel.getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new ResizeController(appointmentPanel.getGrid());
      resizeController.addDefaultHandler(this);
    }
  }

  private void timeBlockClick(Event event) {
    DateTime dateTime = appointmentPanel.getCoordinatesDate(event.getClientX(), event.getClientY(),
        getSettings(), getDate(), getDisplayedDays());
    createAppointment(dateTime, null);
  }
}
