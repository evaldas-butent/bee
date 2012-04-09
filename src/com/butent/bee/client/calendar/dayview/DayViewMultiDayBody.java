package com.butent.bee.client.calendar.dayview;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;

public class DayViewMultiDayBody extends Composite {

  private static final String TIMELINE_EMPTY_CELL_STYLE = "leftEmptyCell";
  private static final String SCROLLBAR_EMPTY_CELL_STYLE = "rightEmptyCell";
  private static final String DAY_CONTAINER_CELL_STYLE = "centerDayContainerCell";
  private static final String SPLITTER_STYLE = "splitter";

  private FlexTable header = new FlexTable();

  private final AbsolutePanel grid = new AbsolutePanel();
  private AbsolutePanel splitter = new AbsolutePanel();

  private final SimplePanel gridOverlay = new SimplePanel();

  public DayViewMultiDayBody() {
    initWidget(header);

    header.setStyleName("multiDayBody");
    StyleUtils.fullWidth(header);

    header.insertRow(0);
    header.insertRow(0);

    header.insertCell(0, 0);
    header.insertCell(0, 0);
    header.insertCell(0, 0);

    StyleUtils.setHeight(grid, 30);
    header.setWidget(0, 1, grid);

    header.getCellFormatter().setStyleName(0, 0, TIMELINE_EMPTY_CELL_STYLE);
    header.getCellFormatter().setStyleName(0, 1, DAY_CONTAINER_CELL_STYLE);
    header.getCellFormatter().setStyleName(0, 2, SCROLLBAR_EMPTY_CELL_STYLE);

    header.getCellFormatter().setWidth(0, 2, DomUtils.getScrollBarWidth() + "px");

    header.getFlexCellFormatter().setColSpan(1, 0, 3);

    header.setCellPadding(0);
    header.setCellSpacing(0);
    header.setBorderWidth(0);

    splitter.setStylePrimaryName(SPLITTER_STYLE);
    header.setWidget(1, 0, splitter);
  }

  public AbsolutePanel getGrid() {
    return grid;
  }

  public void setDays(int days) {
    grid.clear();
    
    int dayWidth = 100 / days;
    for (int i = 0; i < days; i++) {
      SimplePanel dayPanel = new SimplePanel();
      dayPanel.setStyleName("day-separator");
      StyleUtils.setLeft(dayPanel, dayWidth * i, Unit.PCT);

      grid.add(dayPanel);
    }
    
    StyleUtils.fullWidth(gridOverlay);
    StyleUtils.fullHeight(gridOverlay);

    StyleUtils.makeAbsolute(gridOverlay);
    StyleUtils.setLeft(gridOverlay, 0);
    StyleUtils.setTop(gridOverlay, 0);

    grid.add(gridOverlay);
  }
}
