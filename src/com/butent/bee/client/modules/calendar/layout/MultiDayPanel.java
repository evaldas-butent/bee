package com.butent.bee.client.modules.calendar.layout;

import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.widget.Html;

public class MultiDayPanel extends Composite {

  private final Horizontal panel = new Horizontal();

  private final Flow grid = new Flow();

  public MultiDayPanel() {
    panel.addStyleName(CalendarStyleManager.MULTI_DAY_PANEL);
    initWidget(panel);
    
    Html leftFiller = new Html();
    panel.add(leftFiller);
    panel.addStyleToCell(leftFiller, CalendarStyleManager.TIMELINE_EMPTY_CELL);
    
    grid.addStyleName(CalendarStyleManager.MULTI_DAY_GRID);
    panel.add(grid);
    panel.addStyleToCell(grid, CalendarStyleManager.MULTI_DAY_GRID_CELL);

    Html rightFiller = new Html();
    panel.add(rightFiller);
    panel.addStyleToCell(rightFiller, CalendarStyleManager.SCROLLBAR_EMPTY_CELL);
    panel.setCellWidth(rightFiller, DomUtils.getScrollBarWidth());
  }

  public Flow getGrid() {
    return grid;
  }

  public void setColumnCount(int columnCount) {
    grid.clear();
    CalendarLayoutManager.addColumnSeparators(grid, columnCount);
  }
}
