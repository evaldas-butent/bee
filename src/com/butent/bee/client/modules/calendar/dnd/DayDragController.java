package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;

import com.butent.bee.client.calendar.CalendarView;
import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

public class DayDragController extends PickupDragController {

  private JustDate date;
  
  public DayDragController(AbsolutePanel boundaryPanel) {
    super(boundaryPanel, false);
    
    setBehaviorDragProxy(true);
    setBehaviorDragStartSensitivity(1);
    setBehaviorConstrainedToBoundaryPanel(true);
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

  public void setDate(JustDate date) {
    this.date = date;
  }
  
  private void fireUpdate(CalendarView calendarView, AppointmentWidget appointmentWidget) {
    Appointment appointment = appointmentWidget.getAppointment();
    long duration = appointment.getEnd().getTime() - appointment.getStart().getTime();
    
    long startTime = date.getDateTime().getTime();
    
    int newColumnIndex = appointmentWidget.getDropColumnIndex();
    if (newColumnIndex > 0 && CalendarView.Type.DAY.equals(calendarView.getType())) {
      startTime += TimeUtils.MILLIS_PER_DAY * newColumnIndex;
    }
    
    int minutes = appointmentWidget.getDropMinutes();
    if (minutes > 0) {
      startTime += TimeUtils.MILLIS_PER_MINUTE * minutes;
    }
   
    DateTime newStart = new DateTime(startTime);
    DateTime newEnd = new DateTime(startTime + duration);
    
    calendarView.updateAppointment(appointment, newStart, newEnd,
        appointmentWidget.getColumnIndex(), newColumnIndex);
  }
}
