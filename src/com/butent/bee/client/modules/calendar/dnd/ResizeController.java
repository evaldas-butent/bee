package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.CalendarView;
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
  
  public void addDefaultHandler(final CalendarView calendarView) {
    addDragHandler(new DragHandler() {
      public void onDragEnd(DragEndEvent event) {
        AppointmentWidget appointmentWidget = 
            CalendarUtils.getDragAppointmentWidget(event.getContext());
        fireUpdate(calendarView, appointmentWidget);
      }

      public void onDragStart(DragStartEvent event) {
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
    
    widget.setDropColumnIndex(widget.getColumnIndex());
    widget.setDropMinutes(minutes);

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

  private void fireUpdate(CalendarView calendarView, AppointmentWidget appointmentWidget) {
    Appointment appointment = appointmentWidget.getAppointment();

    DateTime newEnd = DateTime.copyOf(appointment.getStart());

    newEnd.setHour(0);
    newEnd.setMinute(appointmentWidget.getDropMinutes());
    
    calendarView.updateAppointment(appointment, appointment.getStart(), newEnd,
        appointmentWidget.getColumnIndex(), appointmentWidget.getDropColumnIndex(), false);
  }
  
  private Widget getWidget() {
    return context.draggable.getParent();
  }
}
