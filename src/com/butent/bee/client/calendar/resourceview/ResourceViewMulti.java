package com.butent.bee.client.calendar.resourceview;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;

import java.util.List;

public class ResourceViewMulti extends Composite {

  private static final String TIMELINE_EMPTY_CELL_STYLE = "leftEmptyCell";
  private static final String SCROLLBAR_EMPTY_CELL_STYLE = "rightEmptyCell";
  private static final String CONTAINER_CELL_STYLE = "centerDayContainerCell";
  private static final String SPLITTER_STYLE = "splitter";

  private FlexTable header = new FlexTable();

  private final AbsolutePanel grid = new AbsolutePanel();
  private AbsolutePanel splitter = new AbsolutePanel();

  private final SimplePanel gridOverlay = new SimplePanel();

  public ResourceViewMulti() {
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
    header.getCellFormatter().setStyleName(0, 1, CONTAINER_CELL_STYLE);
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

  public void setAttendees(List<Long> attendees) {
    grid.clear();
    if (attendees.isEmpty()) {
      return;
    }
    
    double width = 100d / attendees.size();
    for (int i = 0; i < attendees.size(); i++) {
      SimplePanel panel = new SimplePanel();
      panel.setStyleName("day-separator");
      StyleUtils.setLeft(panel, width * i, Unit.PCT);

      grid.add(panel);
    }
    
    StyleUtils.fullWidth(gridOverlay);
    StyleUtils.fullHeight(gridOverlay);

    StyleUtils.makeAbsolute(gridOverlay);
    StyleUtils.setLeft(gridOverlay, 0);
    StyleUtils.setTop(gridOverlay, 0);

    grid.add(gridOverlay);
  }
}
