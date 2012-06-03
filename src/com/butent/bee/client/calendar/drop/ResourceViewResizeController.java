package com.butent.bee.client.calendar.drop;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dnd.AbstractDragController;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.shared.time.DateTime;

public class ResourceViewResizeController extends AbstractDragController {

  int snapSize;
  int intervalsPerHour;

  public ResourceViewResizeController(AbsolutePanel boundaryPanel) {
    super(boundaryPanel);
  }

  public void dragEnd() {
    AppointmentWidget apptWidget = (AppointmentWidget) context.draggable.getParent();
    int apptHeight = apptWidget.getOffsetHeight();
    Appointment appt = apptWidget.getAppointment();

    DateTime end = DateTime.copyOf(appt.getStart());

    double top = DOM.getIntStyleAttribute(apptWidget.getElement(), "top");

    int intervalStart = (int) Math.round(top / snapSize);
    int intervalSpan = Math.round(apptHeight / snapSize);

    end.setHour(0);
    end.setMinute((intervalStart + intervalSpan) * (60 / intervalsPerHour));

    appt.setEnd(end);

    super.dragEnd();
  }

  public void dragMove() {
    Widget appointment = context.draggable.getParent();

    int delta = context.draggable.getAbsoluteTop() - context.desiredDraggableY;
    int contentHeight = appointment.getOffsetHeight();

    int newHeight = Math.max(contentHeight - delta, snapSize);

    int snapHeight = (int) Math.round((double) newHeight / snapSize) * snapSize;

    appointment.setHeight(snapHeight + "px");
  }

  public void setIntervalsPerHour(int intervalsPerHour) {
    this.intervalsPerHour = intervalsPerHour;
  }

  public void setSnapSize(int snapSize) {
    this.snapSize = snapSize;
  }
}
