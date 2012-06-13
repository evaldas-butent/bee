package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.AbsolutePanel;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.drop.AbsolutePositionDropController;
import com.butent.bee.client.dom.StyleUtils;
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

    int left = draggableList.get(0).desiredX;
    int top = draggableList.get(0).desiredY;

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
      
      int w = draggable.offsetWidth;

      x = BeeUtils.clamp(x, 0, dropTargetClientWidth - w);
      y = BeeUtils.clamp(y, 0, dropTargetClientHeight - draggable.offsetHeight);
      
      x = Math.min((x + w / 2) / snapX * snapX, snapX * (columns - 1));
      y = y / snapY * snapY;

      draggable.desiredX = x;
      draggable.desiredY = y;
      
      x += 2;
      y++;
      
      if (dropTarget.getWidgetIndex(draggable.positioner) >= 0) {
        StyleUtils.setLeft(draggable.positioner, x);
        StyleUtils.setTop(draggable.positioner, y);
      } else {
        dropTarget.add(draggable.positioner, x, y);
      }
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
