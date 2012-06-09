package com.butent.bee.client.calendar.resourceview;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.dnd.DayDragController;
import com.butent.bee.client.modules.calendar.dnd.DayDropController;
import com.butent.bee.client.modules.calendar.dnd.ResizeController;
import com.butent.bee.client.modules.calendar.layout.AppointmentAdapter;
import com.butent.bee.client.modules.calendar.layout.AppointmentPanel;
import com.butent.bee.client.modules.calendar.layout.CalendarLayoutManager;
import com.butent.bee.client.modules.calendar.layout.MultiDayPanel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

public class ResourceView extends CalendarView {

  private final ResourceViewHeader viewHeader = new ResourceViewHeader();
  private final MultiDayPanel viewMulti = new MultiDayPanel();
  private final AppointmentPanel viewBody = new AppointmentPanel();

  private final List<AppointmentWidget> appointmentWidgets = Lists.newArrayList();

  private DayDragController dragController = null;
  private DayDropController dropController = null;
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

    viewBody.build(cc, getSettings());

    dragController.setDate(JustDate.copyOf(date));

    dropController.setColumns(cc);
    dropController.setSettings(getSettings());

    resizeController.setSettings(getSettings());

    appointmentWidgets.clear();
    
    int multiHeight = BeeConst.UNDEF;

    for (int i = 0; i < cc; i++) {
      Long id = attendees.get(i);

      List<Appointment> simple = CalendarUtils.filterSimple(getAppointments(), date, id);
      if (!simple.isEmpty()) {
        List<AppointmentAdapter> adapters =
            CalendarLayoutManager.doLayout(simple, i, cc, getSettings());
        addAppointmentsToGrid(adapters, false, i);
      }

      List<Appointment> multi = CalendarUtils.filterMulti(getAppointments(), date, 1, id);
      if (!multi.isEmpty()) {
        List<AppointmentAdapter> adapters = Lists.newArrayList();
        for (Appointment appointment : multi) {
          adapters.add(new AppointmentAdapter(appointment));
        }
        
        multiHeight = Math.max(multiHeight,
            CalendarLayoutManager.doMultiLayout(adapters, date, i, cc));
        addAppointmentsToGrid(adapters, true, i);
      }
    }
    
    if (multiHeight > 0) {
      StyleUtils.setHeight(viewMulti.getGrid(), multiHeight);
    } else {
      StyleUtils.clearHeight(viewMulti.getGrid());
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
  public boolean onClick(Element element, Event event) {
    AppointmentWidget widget = CalendarUtils.findWidget(appointmentWidgets, element);

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
  public void scrollToHour(int hour) {
    if (hour > 0) {
      viewBody.scrollToHour(hour, getSettings());
    }
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
        viewMulti.getGrid().add(widget);
      } else {
        viewBody.getGrid().add(widget);

        if (getSettings().isDragDropEnabled()) {
          resizeController.makeDraggable(widget.getResizeHandle());
          dragController.makeDraggable(widget, widget.getMoveHandle());
        }
      }
    }
  }

  private void createDragController() {
    if (dragController == null) {
      dragController = new DayDragController(viewBody.getGrid());
      dragController.addDefaultHandler(this);
    }
  }

  private void createDropController() {
    if (dropController == null) {
      dropController = new DayDropController(viewBody.getGrid());
      dragController.registerDropController(dropController);
    }
  }

  private void createResizeController() {
    if (resizeController == null) {
      resizeController = new ResizeController(viewBody.getGrid());
      resizeController.addDefaultHandler(this);
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
