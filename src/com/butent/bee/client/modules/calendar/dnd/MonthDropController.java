package com.butent.bee.client.modules.calendar.dnd;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.drop.AbsolutePositionDropController;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class MonthDropController extends AbsolutePositionDropController {

  private int rowCount;
  private int columnCount;
  
  private int headerHeight;

  public MonthDropController(Absolute dropTarget) {
    super(dropTarget);
  }

  @Override
  public void onDrop(DragContext context) {
    super.onDrop(context);

    Draggable draggable = draggableList.get(0);

    AppointmentWidget widget = CalendarUtils.getDragAppointmentWidget(context);
    if (widget != null) {
      widget.setDropRowIndex(getRow(context, draggable));
      widget.setDropColumnIndex(getColumn(context, draggable));

      widget.setDropMinutes(getMinutes(context, draggable));
    }
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }
  
  public void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  private int getColumn(DragContext context, Draggable draggable) {
    int x = getX(context, draggable);
    return BeeUtils.clamp(x / (dropTargetClientWidth / columnCount), 0, columnCount - 1);
  }

  private int getMinutes(DragContext context, Draggable draggable) {
    int x = getX(context, draggable);
    int colWidth = dropTargetClientWidth / columnCount;
    
    double minutes = BeeUtils.rescale(x % colWidth, 0, colWidth, 0, TimeUtils.MINUTES_PER_DAY); 
    return (int) Math.round(minutes); 
  }

  private int getRow(DragContext context, Draggable draggable) {
    int y = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;
    int row = Math.max(y - headerHeight, 0) / ((dropTargetClientHeight - headerHeight) / rowCount);
    return BeeUtils.clamp(row, 0, rowCount - 1);
  }
  
  private int getX(DragContext context, Draggable draggable) {
    return Math.max(context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX 
        + draggable.offsetWidth / 2, 0);
  }
}
