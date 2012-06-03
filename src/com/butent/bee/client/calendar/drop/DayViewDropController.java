package com.butent.bee.client.calendar.drop;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.drop.AbsolutePositionDropController;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

public class DayViewDropController extends AbsolutePositionDropController {

  private int intervalsPerHour;
  private int snapSize;

  private int columns;
  private int rows;
  
  private JustDate date;

  private int gridX;
  private int gridY;

  private int maxProxyHeight = -1;

  public DayViewDropController(AbsolutePanel dropTarget) {
    super(dropTarget);
  }

  @Override
  public void onDrop(DragContext context) {
    super.onDrop(context);

    int top = draggableList.get(0).desiredY;
    int left = draggableList.get(0).desiredX;
    Widget widget = context.draggable;

    left = Math.max(0, Math.min(left, dropTarget.getOffsetWidth() - widget.getOffsetWidth()));
    top = Math.max(0, Math.min(top, dropTarget.getOffsetHeight() - widget.getOffsetHeight()));
    left = Math.round((float) left / gridX) * gridX;
    top = Math.round((float) top / gridY) * gridY;

    int intervalStart = (int) Math.floor(top / gridY);
    int intervalSpan = Math.round(widget.getOffsetHeight() / snapSize);

    int day = (int) Math.floor(left / gridX);
    day = Math.max(0, day);
    day = Math.min(day, columns - 1);

    Appointment appt = ((AppointmentWidget) widget).getAppointment();
    DateTime start = date.getDateTime();
    DateTime end = date.getDateTime();
    
    if (day != 0) {
      start.setDom(start.getDom() + day);
      end.setDom(end.getDom() + day);
    }

    start.setHour(0);
    start.setMinute(0);
    start.setSecond(0);
    start.setMinute((intervalStart) * (60 / intervalsPerHour));
    
    end.setHour(0);
    end.setMinute(0);
    end.setSecond(0);
    end.setMinute((intervalStart + intervalSpan) * (60 / intervalsPerHour));
    
    appt.setStart(start);
    appt.setEnd(end);
  }

  @Override
  public void onEnter(DragContext context) {
    super.onEnter(context);

    for (Draggable draggable : draggableList) {
      int width = draggable.positioner.getOffsetWidth();
      int height = draggable.positioner.getOffsetHeight();
      if (maxProxyHeight > 0 && height > maxProxyHeight) {
        height = maxProxyHeight - 5;
      }

      draggable.positioner.setPixelSize(width, height);
    }
  }

  @Override
  public void onMove(DragContext context) {
    super.onMove(context);

    gridX = (int) Math.floor(dropTarget.getOffsetWidth() / columns);
    gridY = (int) Math.floor(dropTarget.getOffsetHeight() / rows);

    for (Draggable draggable : draggableList) {
      draggable.desiredX = context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX;
      draggable.desiredY = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;

      draggable.desiredX =
          Math.max(0, Math.min(draggable.desiredX, dropTargetClientWidth - draggable.offsetWidth));
      draggable.desiredY =
          Math.max(0, Math.min(draggable.desiredY, dropTargetClientHeight - draggable.offsetHeight));
      draggable.desiredX = (int) Math.floor((double) draggable.desiredX / gridX) * gridX;
      draggable.desiredY = (int) Math.round((double) draggable.desiredY / gridY) * gridY;

      dropTarget.add(draggable.positioner, draggable.desiredX, draggable.desiredY);
    }
  }

  public void setColumns(int columns) {
    this.columns = columns;
  }

  public void setDate(JustDate date) {
    this.date = date;
  }

  public void setIntervalsPerHour(int intervalsPerHour) {
    this.intervalsPerHour = intervalsPerHour;
    this.rows = intervalsPerHour * 24;
  }

  public void setMaxProxyHeight(int maxProxyHeight) {
    this.maxProxyHeight = maxProxyHeight;
  }

  public void setSnapSize(int snapSize) {
    this.snapSize = snapSize;
  }
}
