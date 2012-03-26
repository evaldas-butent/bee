package com.butent.bee.client.calendar.drop;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.PickupDragController;
import com.butent.bee.client.dnd.util.DragClientBundle;
import com.butent.bee.client.dnd.util.WidgetArea;

public class DayViewPickupDragController extends PickupDragController {

  private int maxProxyHeight = -1;

  public DayViewPickupDragController(AbsolutePanel boundaryPanel,
      boolean allowDroppingOnBoundaryPanel) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
  }

  @Override
  public void dragMove() {
    try {
      super.dragMove();
    } catch (NullPointerException ex) {
    }
  }

  public void setMaxProxyHeight(int maxProxyHeight) {
    this.maxProxyHeight = maxProxyHeight;
  }

  @Override
  protected Widget newDragProxy(DragContext dragContext) {
    AbsolutePanel container = new AbsolutePanel();
    container.getElement().getStyle().setProperty("overflow", "visible");

    WidgetArea draggableArea = new WidgetArea(dragContext.draggable, null);
    for (Widget widget : dragContext.selectedWidgets) {
      WidgetArea widgetArea = new WidgetArea(widget, null);
      Widget proxy = new SimplePanel();
      int height = widget.getOffsetHeight();
      if (maxProxyHeight > 0 && height > maxProxyHeight) {
        height = maxProxyHeight - 5;
      }

      proxy.setPixelSize(widget.getOffsetWidth(), height);
      proxy.addStyleName(DragClientBundle.INSTANCE.css().proxy());
      container.add(proxy,
          widgetArea.getLeft() - draggableArea.getLeft(),
          widgetArea.getTop() - draggableArea.getTop());
    }

    return container;
  }
}
