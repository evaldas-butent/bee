package com.butent.bee.client.modules.calendar.view;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.CalendarView;
import com.butent.bee.client.modules.calendar.CalendarWidget;
import com.butent.bee.client.modules.calendar.dnd.DayMoveController;
import com.butent.bee.client.modules.calendar.dnd.ResizeController;
import com.butent.bee.client.modules.calendar.layout.AppointmentAdapter;
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Orientation;

import java.util.List;
import java.util.Map;

public class ResourceView extends CalendarView {

  private final ResourceViewHeader viewHeader = new ResourceViewHeader();
  private final MultiDayPanel viewMulti = new MultiDayPanel();
  private final AppointmentPanel viewBody = new AppointmentPanel();

  private DayMoveController moveController = null;
  private ResizeController resizeController = null;

  public ResourceView() {
    super();
  }

  @Override
  public void attach(CalendarWidget widget) {
    super.attach(widget);

    addWidget(viewHeader);
    addWidget(viewMulti);
    addWidget(viewBody);

    createMoveController();
    createResizeController();
  }

  @Override
  public void doLayout(long calendarId) {
    JustDate date = getDate();
    List<Long> attendees = getCalendarWidget().getAttendees();
    int cc = attendees.size();

    viewHeader.setAttendees(calendarId, attendees);
    viewHeader.setDate(date);
    
    if (TimeUtils.isToday(date)) {
      viewMulti.setColumns(cc, 0, cc - 1);
      viewBody.build(cc, getSettings(), 0, cc - 1);
    } else {
      viewMulti.setColumns(cc, BeeConst.UNDEF, BeeConst.UNDEF);
      viewBody.build(cc, getSettings());
    }

    moveController.setDate(JustDate.copyOf(date));
    moveController.setColumnCount(cc);
    moveController.setSettings(getSettings());

    resizeController.setSettings(getSettings());

    getAppointmentWidgets().clear();
    
    int multiHeight = BeeConst.UNDEF;
    
    Map<Long, String> attendeeColors = CalendarKeeper.getAttendeeColors(calendarId);

    for (int i = 0; i < cc; i++) {
      Long id = attendees.get(i);
      String bg = attendeeColors.get(id);

      List<Appointment> simple = CalendarUtils.filterSimple(getAppointments(), date, id);
      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, cc, getSettings());
        addAppointmentsToGrid(calendarId, adapters, false, i, bg);
      }

      List<Appointment> multi = CalendarUtils.filterMulti(getAppointments(), date, 1, id);
      if (!multi.isEmpty()) {
        List<AppointmentAdapter> adapters = Lists.newArrayList();
        for (Appointment appointment : multi) {
          adapters.add(new AppointmentAdapter(appointment));
        }
        
        multiHeight = Math.max(multiHeight,
            CalendarLayoutManager.doMultiLayout(adapters, date, i, cc));
        addAppointmentsToGrid(calendarId, adapters, true, i, bg);
      }
    }
    
    if (multiHeight > 0) {
      StyleUtils.setHeight(viewMulti.getGrid(), multiHeight);
    } else {
      StyleUtils.clearHeight(viewMulti.getGrid());
    }
  }
  
  @Override
  public void doScroll() {
    viewBody.doScroll(getSettings(), getAppointmentWidgets());
  }

  @Override
  public void doSizing() {
    if (getCalendarWidget().getOffsetHeight() > 0) {
      StyleUtils.setHeight(viewBody, getCalendarWidget().getOffsetHeight()
          - 2 - viewHeader.getOffsetHeight() - viewMulti.getOffsetHeight());
    }
  }

  @Override
  public Widget getScrollArea() {
    return viewBody.getScrollArea();
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
  public boolean onClick(long calendarId, Element element, Event event) {
    AppointmentWidget widget = CalendarUtils.findWidget(getAppointmentWidgets(), element);

    if (widget != null) {
      if (widget.canClick(element)) {
        openAppointment(widget.getAppointment());
        return true;
      } else {
        return false;
      }

    } else if (viewBody.isGrid(element)) {
      timeBlockClick(event);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void onClock() {
    viewBody.onClock(getSettings());
  }
  
  private void addAppointmentsToGrid(long calendarId, List<AppointmentAdapter> adapters,
      boolean multi, int columnIndex, String bg) {

    Orientation footerOrientation = multi ? null : Orientation.VERTICAL;
    
    for (AppointmentAdapter adapter : adapters) {
      AppointmentWidget widget = new AppointmentWidget(adapter.getAppointment(), multi,
          columnIndex, adapter.getHeight(), footerOrientation);

      widget.setLeft(adapter.getLeft());
      widget.setWidth(adapter.getWidth());

      widget.setTop(adapter.getTop());
      widget.setHeight(adapter.getHeight());

      widget.render(calendarId, bg);

      getAppointmentWidgets().add(widget);

      if (multi) {
        viewMulti.getGrid().add(widget);
      } else {
        viewBody.getGrid().add(widget);

        widget.getMoveHandle().addMoveHandler(moveController);
        widget.getResizeHandle().addMoveHandler(resizeController);
      }
    }
  }

  private void createMoveController() {
    if (moveController == null) {
      moveController = new DayMoveController(this, viewBody.getScrollArea().getElement());
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new ResizeController(this, viewBody.getScrollArea());
    }
  }

  private void timeBlockClick(Event event) {
    int x = event.getClientX();
    int y = event.getClientY();

    List<Long> attendees = getCalendarWidget().getAttendees();
    int columnIndex = viewBody.getColumnIndex(x, attendees.size());
    
    DateTime dateTime = viewBody.getCoordinatesDate(x, y, getSettings(), getDate(), 1);

    createAppointment(dateTime, attendees.get(columnIndex));
  }
}
