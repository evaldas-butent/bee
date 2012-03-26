package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.dom.DomUtils;

public class DayViewMultiDayBody extends Composite {

  private static final String TIMELINE_EMPTY_CELL_STYLE = "leftEmptyCell";
  private static final String SCROLLBAR_EMPTY_CELL_STYLE = "rightEmptyCell";
  private static final String DAY_CONTAINER_CELL_STYLE = "centerDayContainerCell";
  private static final String SPLITTER_STYLE = "splitter";

  protected AbsolutePanel grid = new AbsolutePanel();
  protected SimplePanel gridOverlay = new SimplePanel();

  private FlexTable header = new FlexTable();
  private AbsolutePanel splitter = new AbsolutePanel();

  public DayViewMultiDayBody() {

    initWidget(header);

    this.header.setStyleName("multiDayBody");
    this.setWidth("100%");

    header.insertRow(0);
    header.insertRow(0);

    header.insertCell(0, 0);
    header.insertCell(0, 0);
    header.insertCell(0, 0);

    header.setWidget(0, 1, grid);

    header.getCellFormatter().setStyleName(0, 0, TIMELINE_EMPTY_CELL_STYLE);
    header.getCellFormatter().setStyleName(0, 1, DAY_CONTAINER_CELL_STYLE);
    header.getCellFormatter().setStyleName(0, 2, SCROLLBAR_EMPTY_CELL_STYLE);
    header.getCellFormatter().setWidth(0, 2, DomUtils.getScrollBarWidth() + "px");

    grid.setHeight("30px");

    header.getFlexCellFormatter().setColSpan(1, 0, 3);
    header.setCellPadding(0);
    header.setBorderWidth(0);
    header.setCellSpacing(0);

    splitter.setStylePrimaryName(SPLITTER_STYLE);
    header.setWidget(1, 0, splitter);
  }

  public void setDays(int days) {
    grid.clear();
    double dayWidth = 100f / days;
    double dayLeft;

    for (int day = 0; day < days; day++) {
      dayLeft = dayWidth * day;

      SimplePanel dayPanel = new SimplePanel();
      dayPanel.setStyleName("day-separator");
      grid.add(dayPanel);
      DOM.setStyleAttribute(dayPanel.getElement(), "left", dayLeft + "%");
    }

    gridOverlay.setHeight("100%");
    gridOverlay.setWidth("100%");
    DOM.setStyleAttribute(gridOverlay.getElement(), "position", "absolute");
    DOM.setStyleAttribute(gridOverlay.getElement(), "left", "0px");
    DOM.setStyleAttribute(gridOverlay.getElement(), "top", "0px");
    grid.add(gridOverlay);
  }
}
