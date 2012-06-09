package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.drop.AbsolutePositionDropController;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.utils.BeeUtils;

public class DayDropController extends AbsolutePositionDropController {

  private CalendarSettings settings;

  private int columns;

  public DayDropController(AbsolutePanel dropTarget) {
    super(dropTarget);
  }

  @Override
  public void onDrop(DragContext context) {
    super.onDrop(context);

    int top = draggableList.get(0).desiredY;
    int left = draggableList.get(0).desiredX;

    AppointmentWidget widget = (AppointmentWidget) context.draggable;
    left = Math.max(0, Math.min(left, dropTarget.getOffsetWidth() - widget.getOffsetWidth()));
    top = Math.max(0, Math.min(top, dropTarget.getOffsetHeight() - widget.getOffsetHeight()));

    int snapX = CalendarUtils.getColumnWidth(dropTarget, columns);
    int snapY = settings.getPixelsPerInterval();
    
    left = BeeUtils.snap(left, snapX);
    top = top / snapY * snapY;

    int columnIndex = BeeUtils.clamp(left / snapX, 0, columns - 1);
    int minutes = CalendarUtils.getCoordinateMinutesSinceDayStarted(top, settings);
    
    widget.setDropColumnIndex(columnIndex);
    widget.setDropMinutes(minutes);
  }

  @Override
  public void onMove(DragContext context) {
    int snapX = CalendarUtils.getColumnWidth(dropTarget, columns);
    int snapY = settings.getPixelsPerInterval();

    for (Draggable draggable : draggableList) {
      int x = context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX;
      int y = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;

      x = BeeUtils.clamp(x, 0, dropTargetClientWidth - draggable.offsetWidth);
      y = BeeUtils.clamp(y, 0, dropTargetClientHeight - draggable.offsetHeight);
      
      x = Math.min(BeeUtils.snap(x, snapX), snapX * (columns - 1));
      y = y / snapY * snapY;

      draggable.desiredX = x;
      draggable.desiredY = y;

      dropTarget.add(draggable.positioner, x + 2, y + 1);
    }

    if (context.dragController.getBehaviorScrollIntoView()) {
      draggableList.get(draggableList.size() - 1).positioner.getElement().scrollIntoView();
    }
    calcDropTargetOffset();
  }

  public void setColumns(int columns) {
    this.columns = columns;
  }

  public void setSettings(CalendarSettings settings) {
    this.settings = settings;
  }
}
