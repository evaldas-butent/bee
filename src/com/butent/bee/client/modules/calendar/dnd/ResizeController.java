package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.dnd.AbstractDragController;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

public class ResizeController extends AbstractDragController {
  
  private CalendarSettings settings;

  public ResizeController(AbsolutePanel boundaryPanel) {
    super(boundaryPanel);
  }
  
  public void addDefaultHandler(final CalendarWidget calendarWidget) {
    addDragHandler(new DragHandler() {
      public void onDragEnd(DragEndEvent event) {
        Appointment appointment = CalendarUtils.getDragAppointment(event.getContext());
        calendarWidget.setCommittedAppointment(appointment);
        calendarWidget.fireUpdateEvent(appointment);
      }

      public void onDragStart(DragStartEvent event) {
        Appointment appointment = CalendarUtils.getDragAppointment(event.getContext());
        calendarWidget.setRollbackAppointment(appointment.clone());
      }

      public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
      }

      public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
      }
    });
  }
  
  @Override
  public void dragEnd() {
    AppointmentWidget widget = (AppointmentWidget) getWidget();

    int top = StyleUtils.getTop(widget);
    int height = widget.getOffsetHeight();

    int snapSize = getSettings().getPixelsPerInterval();
    
    int y = BeeUtils.snap(top, snapSize) + BeeUtils.snap(height, snapSize);
    int minutes = CalendarUtils.getCoordinateMinutesSinceDayStarted(y, getSettings());

    Appointment appointment = widget.getAppointment();
    DateTime end = DateTime.copyOf(appointment.getStart());

    end.setHour(0);
    end.setMinute(minutes);
    appointment.setEnd(end);

    super.dragEnd();
  }

  @Override
  public void dragMove() {
    Widget widget = getWidget();

    int delta = context.draggable.getAbsoluteTop() - context.desiredDraggableY;
    int oldHeight = widget.getOffsetHeight();
    
    int snapSize = getSettings().getPixelsPerInterval();

    int newHeight = Math.max(oldHeight - delta, snapSize);
    int snapHeight = BeeUtils.snap(newHeight, snapSize);
    
    StyleUtils.setHeight(widget, snapHeight);
  }

  public CalendarSettings getSettings() {
    return settings;
  }

  public void setSettings(CalendarSettings settings) {
    this.settings = settings;
  }

  private Widget getWidget() {
    return context.draggable.getParent();
  }
}
