package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;

import com.butent.bee.client.dnd.DragEndEvent;
import com.butent.bee.client.dnd.DragHandler;
import com.butent.bee.client.dnd.DragStartEvent;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.VetoDragException;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.view.MonthView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class MonthDragController extends PickupDragController {

  private final boolean sameTime;

  public MonthDragController(AbsolutePanel boundaryPanel, boolean sameTime) {
    super(boundaryPanel, false);
    this.sameTime = sameTime;

    setBehaviorDragProxy(true);
    setBehaviorDragStartSensitivity(1);
    setBehaviorConstrainedToBoundaryPanel(true);
  }

  public void addDefaultHandler(final MonthView monthView) {
    addDragHandler(new DragHandler() {
      public void onDragEnd(DragEndEvent event) {
        AppointmentWidget widget = CalendarUtils.getDragAppointmentWidget(event.getContext());
        if (widget != null) {
          fireUpdate(monthView, widget);
        }
      }

      public void onDragStart(DragStartEvent event) {
      }

      public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
      }

      public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
      }
    });
  }

  private void fireUpdate(MonthView monthView, AppointmentWidget appointmentWidget) {
    int row = appointmentWidget.getDropRowIndex();
    int col = appointmentWidget.getDropColumnIndex();

    Appointment appointment = appointmentWidget.getAppointment();
    DateTime oldStart = appointment.getStart();
    DateTime oldEnd = appointment.getEnd();

    JustDate newStartDate = TimeUtils.nextDay(monthView.getFirstDate(), row * 7 + col);
    DateTime newStart;
    DateTime newEnd;

    if (sameTime) {
      if (TimeUtils.sameDate(oldStart, newStartDate)) {
        newStart = oldStart;
        newEnd = oldEnd;
      } else {
        newStart = TimeUtils.combine(newStartDate, oldStart);
        newEnd = TimeUtils.combine(newStartDate, oldEnd);
      }

    } else {
      long startTime = newStartDate.getDateTime().getTime();

      int minutes = appointmentWidget.getDropMinutes();
      if (minutes > 0) {
        int snapSize = TimeUtils.MINUTES_PER_HOUR / monthView.getSettings().getIntervalsPerHour();
        minutes = BeeUtils.snap(minutes, snapSize);
        if (minutes >= TimeUtils.MINUTES_PER_DAY) {
          minutes = TimeUtils.MINUTES_PER_DAY - snapSize;
        }

        startTime += TimeUtils.MILLIS_PER_MINUTE * minutes;
      }

      long duration = oldEnd.getTime() - oldStart.getTime();
      newStart = new DateTime(startTime);
      newEnd = new DateTime(startTime + duration);
    }

    monthView.updateAppointment(appointment, newStart, newEnd,
        BeeConst.UNDEF, BeeConst.UNDEF, true);
  }
}
