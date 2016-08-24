package com.butent.bee.client.modules.calendar;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.dnd.TodoMoveController;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.tasks.TaskConstants;


import java.util.EnumSet;

public class Calendar extends CalendarPanel {

  private static final String STYLE_TODO_PREFIX = STYLE_PREFIX + "todo-";
  private static final String STYLE_TODO_HIDDEN = STYLE_TODO_PREFIX + "hidden";
  private static final String STYLE_TODO_CONTAINER = STYLE_TODO_PREFIX + "container";

  private final Flow todoContainer;
  private TodoMoveController todoMoveController;


  public Calendar(long calendarId, String caption, CalendarSettings settings,
      BeeRowSet ucAttendees) {

    super(calendarId, caption, settings, ucAttendees);

    if (BeeKeeper.getUser().isDataVisible(TaskConstants.VIEW_TODO_LIST)) {
      Button todoListCommand = new Button(Localized.dictionary().crmTodoList());
      todoListCommand.addClickHandler(event -> showTodoList());
      getHeader().addCommandItem(todoListCommand);
    }

    this.todoContainer = new Flow(STYLE_TODO_CONTAINER);
    addEast(todoContainer, 0, 2);

    addStyleName(STYLE_TODO_HIDDEN);

  }

  @Override
  public Flow getTodoContainer() {
    return todoContainer;
  }

  private void showTodoList() {
    if (todoContainer.isEmpty()) {
      if (getOffsetWidth() < 100) {
        BeeKeeper.getScreen().notifyWarning("NO");
        return;
      }

      GridInterceptor interceptor = GridFactory.getGridInterceptor(GRID_CALENDAR_TODO);
      String supplierKey = GridFactory.getSupplierKey(GRID_CALENDAR_TODO, interceptor);

      GridFactory.createGrid(GRID_CALENDAR_TODO, supplierKey, interceptor,
          EnumSet.of(UiOption.EMBEDDED), null, presenter -> {
            if (!todoContainer.isEmpty()) {
              todoContainer.clear();
            }

            int size = Math.min(getOffsetWidth() / 3, 320);
            setWidgetSize(todoContainer, size);
            todoContainer.add(presenter.getMainView());

            removeStyleName(STYLE_TODO_HIDDEN);
          });

      if (todoMoveController == null) {
        todoMoveController = new TodoMoveController(todoContainer, this);
      }

    } else {
      todoContainer.clear();

      addStyleName(STYLE_TODO_HIDDEN);
      setWidgetSize(todoContainer, 0);
    }
  }
}