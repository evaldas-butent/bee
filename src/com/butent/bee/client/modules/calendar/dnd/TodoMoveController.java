package com.butent.bee.client.modules.calendar.dnd;

import com.google.common.base.Predicate;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.DndSource;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.modules.calendar.CalendarPanel;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;

public class TodoMoveController {

  private static final String CONTENT_TYPE = "CalendarTodo";

  public TodoMoveController(final DndSource todoContainer, CalendarPanel calendarPanel) {

    todoContainer.addDragStartHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        Element cell = EventUtils.getEventTargetElement(event);
        String rowIndex = DomUtils.getDataRow(cell);

        if (CellGrid.isBodyRow(rowIndex)) {
          EventUtils.allowMove(event);
          DndHelper.fillContent(CONTENT_TYPE, null, null, rowIndex);
        }
      }
    });

    todoContainer.addDragEndHandler(new DragEndHandler() {
      @Override
      public void onDragEnd(DragEndEvent event) {
        DndHelper.reset();
      }
    });

    DndHelper.makeTarget(calendarPanel, Collections.singleton(CONTENT_TYPE), null,
        new Predicate<Object>() {
          @Override
          public boolean apply(Object input) {
            return true;
          }
        },
        new BiConsumer<DropEvent, Object>() {
          @Override
          public void accept(DropEvent t, Object u) {
            EventUtils.logEvent(t.getNativeEvent(), true, "drop");

            GridView grid = ViewHelper.getChildGrid(todoContainer.asWidget(), GRID_CALENDAR_TODO);
            Integer rowIndex = (u instanceof String) ? BeeUtils.toIntOrNull((String) u) : null;

            if (grid != null && BeeUtils.isNonNegative(rowIndex)) {
              IsRow row = BeeUtils.getQuietly(grid.getRowData(), rowIndex);

              if (row != null) {
                onDrop(row);
              }
            }
          }
        });
  }

  private static void onDrop(IsRow srcRow) {
    LogUtils.getRootLogger().debug(srcRow.getId(), srcRow.getString(0));
  }
}
